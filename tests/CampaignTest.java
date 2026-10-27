package fr.cercle.boucle;
import java.util.*;
public final class CampaignTest {
    private static int checks;
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static List<GameEngine.Point> box(float x1,float y1,float x2,float y2){return Arrays.asList(new GameEngine.Point(x1,y1),new GameEngine.Point(x2,y1),new GameEngine.Point(x2,y2),new GameEngine.Point(x1,y2));}
    public static void main(String[] args){
        int[] oldTargets={8,10,12,14,16,16,18,20,20,24,22,24,26,26,28,30,30,32,34,36,32,34,34,36,38,40,40,42,44,46,44,46,46,48,50,52,54,54,56,60,54,56,58,60,62,64,66,68,70,75};
        int[] oldTimes={60,60,60,60,60,60,60,60,60,65,60,60,60,65,60,65,60,65,65,70,65,65,60,65,65,70,65,70,65,70,65,70,65,70,70,70,75,70,75,75,70,75,70,75,75,75,80,75,80,80};
        Set<String> names=new HashSet<String>(),rules=new HashSet<String>();CampaignProgress progression=new CampaignProgress(1,"");
        for(int n=1;n<=50;n++){
            LevelSpec l=LevelSpec.get(n);check((float)l.target/l.seconds>(float)oldTargets[n-1]/oldTimes[n-1]*1.4f,"higher capture pressure than1.1.0");check(l.hazardCount>=2&&l.minGroup>=3&&l.streak>=3,"no trivial campaign stages");check(l.number==n,"stage number");check(names.add(l.name),"unique name");check(l.chapter()==(n-1)/10,"chapter");check(l.minGroup<=l.dotCount&&l.target>=l.minGroup,"attainable goal bounds");
            check(rules.add(l.seconds+":"+l.dotCount+":"+l.hazardCount+":"+l.speed+":"+l.target+":"+l.minGroup+":"+l.streak),"unique stage rules");
            GameEngine g=new GameEngine(4);g.startLevel(n);check(g.dots.size()==l.dotCount&&g.hazards.size()==l.hazardCount,"authored populations");check(g.remaining==l.seconds&&g.lives==3&&!g.won&&!g.over,"fresh stage");
            float x=g.dots.get(0).x,y=g.dots.get(0).y;g.startLevel(n);check(g.dots.get(0).x==x&&g.dots.get(0).y==y,"repeatable initial layout");
            float cx=0,cy=0;int cluster=Math.max(4,l.minGroup);for(int i=0;i<cluster;i++){cx+=g.dots.get(i).x;cy+=g.dots.get(i).y;}cx/=cluster;cy/=cluster;
            GameEngine.CaptureResult opening=g.capture(box(cx-135,cy-135,cx+135,cy+135));check(!opening.hit&&!opening.invalid&&opening.count>=Math.max(4,l.minGroup),"readable safe opening "+n);
            int attempts=0;
            while(!g.over&&attempts++<120){
                GameEngine.Dot chosen=null;for(GameEngine.Dot d:g.dots){boolean safe=true;for(GameEngine.Hazard h:g.hazards)if(Math.hypot(d.x-h.x,d.y-h.y)<=h.radius+36)safe=false;if(safe){chosen=d;break;}}
                check(chosen!=null,"capture opportunity "+n);GameEngine.CaptureResult r=g.capture(box(chosen.x-24,chosen.y-24,chosen.x+24,chosen.y+24));check(!r.hit&&!r.invalid&&r.count>0,"safe capture "+n);g.update(.05f);
            }
            check(g.won&&g.over,"stage completable "+n);check(g.collected>=l.target&&g.largestGroup>=l.minGroup&&g.bestStreak>=l.streak,"all goals checked "+n);check(g.earnedStars()==3,"three stars for clean quick play");
            int score=g.score;check(g.capture(box(65,880,345,1210)).invalid&&g.score==score,"completed stage frozen");progression.recordWin(n,g.earnedStars());check(progression.unlocked==Math.min(50,n+1),"sequential unlock");
            GameEngine loss=new GameEngine(4);loss.startLevel(n);for(int i=0;i<l.seconds*21;i++)loss.update(.05f);check(loss.over&&!loss.won&&loss.earnedStars()==0,"timeout is failure");
        }
        check(progression.completed()==50&&progression.totalStars()==150&&progression.unlocked==50,"full campaign boundary");progression.recordWin(1,1);check(progression.stars(1)==3,"replay cannot reduce stars");
        CampaignProgress restored=new CampaignProgress(progression.unlocked,progression.save());check(restored.completed()==50&&restored.totalStars()==150,"save round trip");
        CampaignProgress old=new CampaignProgress(0,"");check(old.unlocked==1&&old.totalStars()==0,"legacy installation");
        boolean rejected=false;try{old.recordWin(2,3);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"locked stage cannot win");
        rejected=false;try{LevelSpec.get(51);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"no stage 51");
        LevelSpec first=LevelSpec.get(1);check(first.target==20&&first.seconds==45&&first.hazardCount==2&&first.speed>=70,"challenging first stage");
        GameEngine stars=new GameEngine(1);stars.startLevel(1);stars.hazards.clear();stars.lives=1;stars.remaining=1;stars.combo=2;stars.dots.clear();for(int i=0;i<20;i++)stars.dots.add(new GameEngine.Dot(400+i,600));stars.capture(box(1,1,999,1249));check(stars.won&&stars.earnedStars()==1,"one star minimum");
        GameEngine arcade=new GameEngine(8);arcade.startLevel(50);arcade.reset(8);check(arcade.level==null&&!arcade.won&&arcade.remaining==60&&arcade.dots.size()==14,"arcade preserved");
        System.out.println("Campaign: "+checks+" checks passed, all 50 stages completed by deterministic simulation.");
    }
}