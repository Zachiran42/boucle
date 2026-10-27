package fr.cercle.boucle;
import java.util.ArrayList;
import java.util.List;

/** Run with java fr.cercle.boucle.GameEngineTest; no Android or test library required. */
public final class GameEngineTest {
    private static int checks;
    public static void main(String[] args) {
        resetAndSpawn(); convexBoundary(); concave(); invalidLoops();
        hazardSegments(); damageAndCooldown(); scoring(); timeAndBounce();
        System.out.println("GameEngine: " + checks + " checks passed.");
    }
    private static GameEngine empty() {
        GameEngine g = new GameEngine(42L); g.dots.clear(); g.hazards.clear(); return g;
    }
    private static List<GameEngine.Point> polygon(float... xy) {
        List<GameEngine.Point> p = new ArrayList<GameEngine.Point>();
        for (int i = 0; i < xy.length; i += 2) p.add(new GameEngine.Point(xy[i], xy[i + 1]));
        return p;
    }
    private static void resetAndSpawn() {
        GameEngine a = new GameEngine(919L), b = new GameEngine(919L);
        check(a.dots.size() == 14 && a.hazards.size() == 3, "initial population");
        check(a.lives == 3 && a.remaining == 60f && !a.over, "initial state");
        for (int i = 0; i < a.dots.size(); i++) {
            GameEngine.Dot d = a.dots.get(i), twin = b.dots.get(i);
            close(d.x, twin.x, "seed x"); close(d.y, twin.y, "seed y");
            check(d.x >= 52 && d.x <= 948 && d.y >= 72 && d.y <= 1178, "spawn bounds");
            for (GameEngine.Hazard h : a.hazards) check(distance(d, h) >= h.radius + d.radius + 47.99f, "hazard clearance");
            for (int j = 0; j < i; j++) check(distance(d, a.dots.get(j)) >= 71.99f, "dot clearance");
        }
        a.score = 90; a.lives = 1; a.over = true; a.reset(919L);
        check(a.score == 0 && a.combo == 0 && a.lives == 3 && !a.over, "reset state");
        close(a.dots.get(0).x, b.dots.get(0).x, "reset seed");
    }
    private static void convexBoundary() {
        GameEngine g = empty();
        g.dots.add(new GameEngine.Dot(100, 100)); g.dots.add(new GameEngine.Dot(200, 150));
        g.dots.add(new GameEngine.Dot(200, 200)); g.dots.add(new GameEngine.Dot(201, 150));
        GameEngine.CaptureResult r = g.capture(polygon(50, 50, 200, 50, 200, 200, 50, 200, 50, 50));
        check(r.count == 3 && r.points == 90 && !r.hit && !r.invalid, "interior edge vertex included");
        check(g.dots.size() == 4, "replenish captured count");
        check(hasDot(g, 201, 150), "outside survives"); close(r.x, 500f / 3f, "capture center");
    }
    private static void concave() {
        GameEngine g = empty();
        g.dots.add(new GameEngine.Dot(70, 240)); g.dots.add(new GameEngine.Dot(240, 70));
        g.dots.add(new GameEngine.Dot(150, 150)); g.dots.add(new GameEngine.Dot(100, 180));
        GameEngine.CaptureResult r = g.capture(polygon(20, 20, 300, 20, 300, 100, 100, 100, 100, 300, 20, 300));
        check(!r.invalid && r.count == 3, "concave excludes notch includes boundary");
        check(hasDot(g, 150, 150), "concave notch survives");
        g = empty(); g.dots.add(new GameEngine.Dot(70, 240));
        check(g.capture(polygon(20, 300, 100, 300, 100, 100, 300, 100, 300, 20, 20, 20)).count == 1, "clockwise");
    }
    private static void invalidLoops() {
        GameEngine g = empty(); g.dots.add(new GameEngine.Dot(80, 80));
        check(g.capture(polygon(0, 0, 200, 200, 0, 200, 200, 0)).invalid, "bow tie");
        check(g.capture(polygon(0, 0, 250, 200, 0, 200, 200, 0)).invalid, "crossing nonzero signed area");
        check(g.capture(polygon(0, 0, 20, 0, 20, 20, 0, 20)).invalid, "tiny polygon");
        check(g.capture(polygon(0, 0, 150, 0, 150, 0, 0, 0)).invalid, "line");
        check(g.capture(polygon(0, 0, 200, 0, 80, 0, 80, 200, 0, 200)).invalid, "adjacent overlap");
        check(g.capture(polygon(0, 0, 100, 100, 0, 200, 200, 200, 100, 100, 200, 0)).invalid, "self touching");
        check(g.capture(polygon(0, 0, Float.NaN, 90, 200, 200)).invalid, "NaN");
        check(g.capture(null).invalid && g.capture(new ArrayList<GameEngine.Point>()).invalid, "missing polygon");
        check(g.score == 0 && g.lives == 3 && g.dots.size() == 1, "invalid has no side effects");
    }
    private static void hazardSegments() {
        GameEngine g = empty(); g.hazards.add(new GameEngine.Hazard(100, 100, 20, 0, 0));
        check(g.touchesHazard(polygon(10, 80, 190, 80)), "tangent segment");
        check(!g.touchesHazard(polygon(10, 79, 190, 79)), "outside radius");
        check(g.touchesHazard(polygon(100, 100)), "point inside hazard");
        check(!g.touchesHazard(polygon(200, 0, 200, 200, 0, 200)), "open stroke");
        GameEngine.CaptureResult r = g.capture(polygon(200, 0, 200, 200, 0, 200));
        check(r.hit && g.lives == 2 && r.points == 0, "implicit closing edge");
    }
    private static void damageAndCooldown() {
        GameEngine g = empty(); g.hazards.add(new GameEngine.Hazard(100, 100, 15, 0, 0));
        g.dots.add(new GameEngine.Dot(60, 60)); g.combo = 3;
        List<GameEngine.Point> square = polygon(20, 20, 180, 20, 180, 180, 20, 180);
        GameEngine.CaptureResult r = g.capture(square);
        check(r.hit && r.count == 0 && r.points == 0 && !r.invalid, "enclosed obstacle blocks");
        check(g.lives == 2 && g.combo == 0 && g.dots.size() == 1, "damage keeps dots resets combo");
        g.capture(square); check(g.lives == 2, "cooldown");
        advance(g, 0.75f); g.capture(square); check(g.lives == 1, "after cooldown");
        advance(g, 0.75f); g.capture(square); check(g.lives == 0 && g.over, "third hit ends game");
        float time = g.remaining; g.update(0.05f);
        check(g.capture(square).invalid && g.lives == 0, "finished ignores capture"); close(g.remaining, time, "finished timer");
    }
    private static void scoring() {
        GameEngine g = empty(); List<GameEngine.Point> square = polygon(20, 20, 180, 20, 180, 180, 20, 180);
        int expected = 0;
        for (int i = 1; i <= 8; i++) {
            g.dots.clear(); g.dots.add(new GameEngine.Dot(80, 80)); g.dots.add(new GameEngine.Dot(100, 100));
            int points = 40 * Math.min(i, 5); expected += points;
            GameEngine.CaptureResult r = g.capture(square);
            check(r.count == 2 && r.points == points && g.score == expected, "quadratic bounded score " + i);
            check(g.combo == i, "combo " + i);
        }
        g.dots.clear(); GameEngine.CaptureResult r = g.capture(square);
        check(!r.invalid && !r.hit && r.points == 0 && g.combo == 0, "empty resets combo");
        g.dots.add(new GameEngine.Dot(80, 80)); check(g.capture(square).points == 10, "score restarts");
    }
    private static void timeAndBounce() {
        GameEngine g = empty(); GameEngine.Hazard h = new GameEngine.Hazard(969, 100, 10, 100, 0); g.hazards.add(h);
        g.update(10f); close(g.remaining, 60f - GameEngine.MAX_DT, "dt cap");
        check(h.x == 970f && h.vx < 0, "right bounce");
        float time = g.remaining, x = h.x;
        g.update(-1f); g.update(Float.NaN); g.update(Float.POSITIVE_INFINITY);
        close(g.remaining, time, "invalid dt timer"); close(h.x, x, "invalid dt movement");
        g.remaining = 0.01f; g.update(0.02f); check(g.over && g.remaining == 0f, "timer ends game");
        check(g.capture(polygon(0, 0, 200, 0, 200, 200, 0, 200)).invalid, "expired capture ignored");
    }
    private static boolean hasDot(GameEngine g, float x, float y) {
        for (GameEngine.Dot d : g.dots) if (d.x == x && d.y == y) return true;
        return false;
    }
    private static void advance(GameEngine g, float seconds) {
        for (int i = 0; i < Math.ceil(seconds / 0.01f); i++) g.update(0.01f);
    }
    private static float distance(GameEngine.Point a, GameEngine.Point b) { return (float) Math.hypot(a.x - b.x, a.y - b.y); }
    private static void close(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < 0.002f, message + ": " + actual + " != " + expected);
    }
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
}
