package fr.cercle.boucle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Deterministic game rules and geometry, independent of Android. */
public final class GameEngine {
    public static final float WIDTH = 1000f, HEIGHT = 1250f;
    public static final float MAX_DT = 0.05f, DURATION = 60f, DAMAGE_COOLDOWN = 0.7f;
    public static final float MIN_LOOP_AREA = 1800f;
    public static final int DOT_COUNT = 14, MAX_MULTIPLIER = 5;
    private static final float EPS = 0.001f, DOT_RADIUS = 13f;
    private Random random;
    public final List<Dot> dots = new ArrayList<Dot>();
    public final List<Hazard> hazards = new ArrayList<Hazard>();
    public int score, combo, lives;
    public float remaining, damageCooldown;
    public boolean over, won;
    public LevelSpec level;
    public float duration=DURATION;
    public int collected, largestGroup, bestStreak;

    public static class Point {
        public float x, y;
        public Point(float x, float y) { this.x = x; this.y = y; }
    }
    public static final class Dot extends Point {
        public float radius;
        public Dot(float x, float y) { this(x, y, DOT_RADIUS); }
        public Dot(float x, float y, float radius) { super(x, y); this.radius = radius; }
    }
    public static final class Hazard extends Point {
        public float radius, vx, vy;
        public Hazard(float x, float y, float radius, float vx, float vy) {
            super(x, y); this.radius = radius; this.vx = vx; this.vy = vy;
        }
    }
    public static final class CaptureResult {
        public int count, points;
        public boolean hit, invalid;
        public float x, y;
    }

    public GameEngine(long seed) { reset(seed); }
    public void reset(long seed) {
        random = new Random(seed); dots.clear(); hazards.clear();
        score = 0; combo = 0; lives = 3; remaining = DURATION; duration=DURATION; damageCooldown = 0f; over = false; won=false; level=null; collected=0; largestGroup=0; bestStreak=0;
        addHazard(250f, 340f, 27f, 82f);
        addHazard(760f, 650f, 30f, 73f);
        addHazard(430f, 1020f, 25f, 91f);
        for (int i = 0; i < DOT_COUNT; i++) spawnDot();
    }
    /** Each retry uses the same authored rules and initial random layout. */
    public void startLevel(int number) {
        LevelSpec spec=LevelSpec.get(number);reset(spec.seed);level=spec;duration=spec.seconds;remaining=duration;
        dots.clear();hazards.clear();
        float[][] positions={{700,300},{650,750},{400,550},{800,1100},{250,250},{500,1000}};
        for(int i=0;i<spec.hazardCount;i++)addHazard(positions[i][0]+random.nextFloat()*30-15,positions[i][1]+random.nextFloat()*30-15,25+(number+i)%5,spec.speed*(.90f+i*.035f));
        // A readable opening cluster makes the grouped objective attainable.
        int cluster=Math.max(4,spec.minGroup);
        float[][] openings={{205,1040},{800,520},{205,650},{470,160}};float[] center=openings[(number-1)%openings.length];
        for(int i=0;i<cluster;i++){double a=i*Math.PI*2/cluster;dots.add(new Dot(center[0]+(float)Math.cos(a)*105,center[1]+(float)Math.sin(a)*105));}
        while(dots.size()<spec.dotCount)spawnDot();
    }
    public int earnedStars(){if(!won||level==null)return 0;return 1+(lives>=2?1:0)+(remaining>=duration*.35f?1:0);}
    private void checkObjective(){
        if(level!=null&&collected>=level.target&&largestGroup>=level.minGroup&&bestStreak>=level.streak){won=true;over=true;}
    }
    private void addHazard(float x, float y, float radius, float speed) {
        double angle = random.nextDouble() * Math.PI * 2;
        hazards.add(new Hazard(x, y, radius, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed));
    }
    /** Long frames are capped so obstacles cannot teleport across a stroke. */
    public void update(float dt) {
        if (over || !finite(dt) || dt <= 0f) return;
        float step = Math.min(MAX_DT, dt);
        damageCooldown = Math.max(0f, damageCooldown - step);
        remaining = Math.max(0f, remaining - step);
        float factor = 1f + (duration - remaining) / 150f;
        for (Hazard h : hazards) {
            h.x += h.vx * step * factor; h.y += h.vy * step * factor;
            float left = h.radius + 20f, right = WIDTH - h.radius - 20f;
            float top = h.radius + 20f, bottom = HEIGHT - h.radius - 20f;
            if (h.x < left) { h.x = left; h.vx = Math.abs(h.vx); }
            if (h.x > right) { h.x = right; h.vx = -Math.abs(h.vx); }
            if (h.y < top) { h.y = top; h.vy = Math.abs(h.vy); }
            if (h.y > bottom) { h.y = bottom; h.vy = -Math.abs(h.vy); }
        }
        if (remaining <= 0f) over = true;
    }
    /** Close a stroke; collision takes priority over an invalid polygon. */
    public CaptureResult capture(List<Point> stroke) {
        CaptureResult r = new CaptureResult();
        if (over) { r.invalid = true; return r; }
        List<Point> polygon = normalized(stroke);
        if (polygon == null || polygon.isEmpty()) { r.invalid = true; return r; }
        for (Point p : polygon) { r.x += p.x; r.y += p.y; }
        r.x /= polygon.size(); r.y /= polygon.size();
        if (touchesHazard(polygon) || closingEdgeTouchesHazard(polygon)) { damage(r); return r; }
        if (!validPolygon(polygon)) { r.invalid = true; return r; }
        for (Hazard h : hazards) {
            if (contains(polygon, h.x, h.y)) { r.x = h.x; r.y = h.y; damage(r); return r; }
        }
        float centerX = 0f, centerY = 0f;
        for (int i = dots.size() - 1; i >= 0; i--) {
            Dot d = dots.get(i);
            if (contains(polygon, d.x, d.y)) { centerX += d.x; centerY += d.y; dots.remove(i); r.count++; }
        }
        if (r.count == 0) { combo = 0; return r; }
        r.points = 10 * r.count * r.count * Math.min(combo + 1, MAX_MULTIPLIER);
        score += r.points; combo++; collected+=r.count; largestGroup=Math.max(largestGroup,r.count); bestStreak=Math.max(bestStreak,combo);
        r.x = centerX / r.count; r.y = centerY / r.count;
        for (int i = 0; i < r.count; i++) spawnDot();
        checkObjective();
        return r;
    }
    private void damage(CaptureResult r) {
        r.hit = true; combo = 0;
        if (damageCooldown > 0f) return;
        lives = Math.max(0, lives - 1); damageCooldown = DAMAGE_COOLDOWN;
        if (lives == 0) over = true;
    }
    /** Test an open stroke; capture also checks its last-to-first edge. */
    public boolean touchesHazard(List<Point> stroke) {
        if (stroke == null || stroke.isEmpty()) return false;
        for (Hazard h : hazards) {
            Point previous = null;
            for (Point p : stroke) {
                if (p == null || !finite(p.x) || !finite(p.y)) return false;
                if (distanceSquared(p.x, p.y, h.x, h.y) <= h.radius * h.radius + EPS) return true;
                if (previous != null && segmentDistanceSquared(previous, p, h.x, h.y) <= h.radius * h.radius + EPS) return true;
                previous = p;
            }
        }
        return false;
    }
    private boolean closingEdgeTouchesHazard(List<Point> p) {
        if (p.size() < 2) return false;
        for (Hazard h : hazards)
            if (segmentDistanceSquared(p.get(p.size() - 1), p.get(0), h.x, h.y) <= h.radius * h.radius + EPS) return true;
        return false;
    }
    private static List<Point> normalized(List<Point> stroke) {
        if (stroke == null) return null;
        List<Point> p = new ArrayList<Point>();
        for (Point point : stroke) {
            if (point == null || !finite(point.x) || !finite(point.y)) return null;
            if (p.isEmpty() || !same(p.get(p.size() - 1), point)) p.add(point);
        }
        if (p.size() > 1 && same(p.get(0), p.get(p.size() - 1))) p.remove(p.size() - 1);
        return p;
    }
    private static boolean validPolygon(List<Point> p) {
        int n = p.size(); if (n < 3) return false;
        double twiceArea = 0;
        for (int i = 0; i < n; i++) {
            Point a = p.get(i), b = p.get((i + 1) % n), c = p.get((i + 2) % n);
            twiceArea += (double) a.x * b.y - (double) b.x * a.y;
            // Adjacent edges may share a vertex, but may not double back and overlap.
            if (Math.abs(cross(a, b, c)) <= EPS && ((double) a.x - b.x) * (c.x - b.x) + ((double) a.y - b.y) * (c.y - b.y) > EPS) return false;
            for (int j = i + 1; j < n; j++) {
                if (j == i + 1 || (i == 0 && j == n - 1)) continue;
                if (segmentsIntersect(a, b, p.get(j), p.get((j + 1) % n))) return false;
            }
        }
        return Math.abs(twiceArea) * 0.5 >= MIN_LOOP_AREA;
    }
    private static boolean contains(List<Point> p, float x, float y) {
        boolean inside = false;
        for (int i = 0, j = p.size() - 1; i < p.size(); j = i++) {
            Point a = p.get(j), b = p.get(i);
            if (segmentDistanceSquared(a, b, x, y) <= EPS * EPS) return true;
            if ((a.y > y) != (b.y > y) && x < ((double) b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x) inside = !inside;
        }
        return inside;
    }
    private static boolean segmentsIntersect(Point a, Point b, Point c, Point d) {
        double ac = cross(a, b, c), ad = cross(a, b, d), ca = cross(c, d, a), cb = cross(c, d, b);
        if (((ac > EPS && ad < -EPS) || (ac < -EPS && ad > EPS)) && ((ca > EPS && cb < -EPS) || (ca < -EPS && cb > EPS))) return true;
        return (Math.abs(ac) <= EPS && onSegment(a, b, c)) || (Math.abs(ad) <= EPS && onSegment(a, b, d))
                || (Math.abs(ca) <= EPS && onSegment(c, d, a)) || (Math.abs(cb) <= EPS && onSegment(c, d, b));
    }
    private static boolean onSegment(Point a, Point b, Point p) {
        return p.x >= Math.min(a.x, b.x) - EPS && p.x <= Math.max(a.x, b.x) + EPS
                && p.y >= Math.min(a.y, b.y) - EPS && p.y <= Math.max(a.y, b.y) + EPS;
    }
    private static double cross(Point a, Point b, Point c) {
        return ((double) b.x - a.x) * (c.y - a.y) - ((double) b.y - a.y) * (c.x - a.x);
    }
    private static double segmentDistanceSquared(Point a, Point b, float x, float y) {
        double dx = (double) b.x - a.x, dy = (double) b.y - a.y, length = dx * dx + dy * dy;
        double t = length == 0 ? 0 : (((double) x - a.x) * dx + ((double) y - a.y) * dy) / length;
        t = Math.max(0, Math.min(1, t));
        double px = a.x + t * dx - x, py = a.y + t * dy - y;
        return px * px + py * py;
    }
    private static boolean same(Point a, Point b) { return distanceSquared(a.x, a.y, b.x, b.y) <= EPS * EPS; }
    private static float distanceSquared(float ax, float ay, float bx, float by) {
        float dx = ax - bx, dy = ay - by; return dx * dx + dy * dy;
    }
    private static boolean finite(float value) { return !Float.isNaN(value) && !Float.isInfinite(value); }
    private void spawnDot() {
        float bestX = 52f, bestY = 72f, bestClearance = -Float.MAX_VALUE;
        for (int i = 0; i < 600; i++) {
            float x = 52f + random.nextFloat() * (WIDTH - 104f), y = 72f + random.nextFloat() * (HEIGHT - 144f);
            float clearance = spawnClearance(x, y);
            if (clearance > bestClearance) { bestClearance = clearance; bestX = x; bestY = y; }
            if (clearance >= 0f) { dots.add(new Dot(x, y)); return; }
        }
        // Deterministic fallback for unusually crowded future layouts.
        for (float y = 72f; y <= HEIGHT - 72f; y += 44f) for (float x = 52f; x <= WIDTH - 52f; x += 44f) {
            float clearance = spawnClearance(x, y);
            if (clearance > bestClearance) { bestClearance = clearance; bestX = x; bestY = y; }
        }
        dots.add(new Dot(bestX, bestY));
    }
    private float spawnClearance(float x, float y) {
        float clearance = Float.MAX_VALUE;
        for (Dot d : dots) clearance = Math.min(clearance, (float) Math.sqrt(distanceSquared(x, y, d.x, d.y)) - 72f);
        for (Hazard h : hazards) clearance = Math.min(clearance, (float) Math.sqrt(distanceSquared(x, y, h.x, h.y)) - h.radius - DOT_RADIUS - 48f);
        return clearance;
    }
}
