package fr.cercle.boucle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Rendering and pointer handling; the testable engine owns all game rules. */
public final class GameView extends View {
    public interface Listener {void onStateChanged(int state);void onCapture(boolean hit);}
    public static final int HOME=0,PLAYING=1,PAUSED=2,OVER=3;
    public int state=HOME,best;
    public final GameEngine engine=new GameEngine(System.nanoTime());
    private final Listener listener;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path=new Path();
    private final RectF board=new RectF();
    private final List<GameEngine.Point> trail=new ArrayList<>();
    private final List<Particle> particles=new ArrayList<>();
    private final Random random=new Random();
    private final Typeface regular=Typeface.create("sans-serif",0),medium=Typeface.create("sans-serif-medium",0),bold=Typeface.create("sans-serif",1);
    private static final int MINT=0xffbef7d1,WHITE=0xffeff6ef,MUTED=0xff8ca6a5,CORAL=0xffff8275;
    private float ui=1,height=780,scale=1,time,trailLength,noticeTime,flashTime,fillTime,floatingTime,floatX,floatY;
    private Path capturedPath;
    private String notice="",floating="";
    private boolean active=true,lockedStroke,newRecord;
    private long frameNanos;
    private int finger=-1;

    public GameView(Context context,int record,Listener listener){
        super(context);best=record;this.listener=listener;setFocusable(true);
        setContentDescription("Terrain de Boucle. Dessine autour des points et reviens au début du tracé. Évite les éclats corail.");
    }
    public void setActive(boolean value){active=value;frameNanos=0;if(value)postInvalidateOnAnimation();}
    public void startGame(){
        engine.reset(System.nanoTime());trail.clear();particles.clear();finger=-1;lockedStroke=false;
        noticeTime=0;floatingTime=0;fillTime=0;newRecord=false;state=PLAYING;frameNanos=0;listener.onStateChanged(state);invalidate();
    }
    public void pauseGame(){if(state!=PLAYING)return;trail.clear();finger=-1;lockedStroke=false;state=PAUSED;listener.onStateChanged(state);invalidate();}
    public void resumeGame(){if(state!=PAUSED)return;state=PLAYING;frameNanos=0;listener.onStateChanged(state);invalidate();}
    public void goHome(){state=HOME;trail.clear();finger=-1;listener.onStateChanged(state);invalidate();}
    private void finishGame(){state=OVER;trail.clear();finger=-1;newRecord=engine.score>best;best=Math.max(best,engine.score);listener.onStateChanged(state);}
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){ui=w/390f;height=h/ui;layoutBoard();}
    private void layoutBoard(){
        float top=(state==HOME?228:160)*ui,bottom=getHeight()-(state==HOME?140:86)*ui;
        float available=Math.max(100*ui,bottom-top);scale=Math.min((getWidth()-40*ui)/GameEngine.WIDTH,available/GameEngine.HEIGHT);
        float w=GameEngine.WIDTH*scale,h=GameEngine.HEIGHT*scale,x=(getWidth()-w)/2,y=top+(available-h)/2;board.set(x,y,x+w,y+h);
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);long now=System.nanoTime();float elapsed=frameNanos==0?0:Math.max(0,(now-frameNanos)/1_000_000_000f);float dt=Math.min(.05f,elapsed);frameNanos=now;time+=dt;
        if(state==PLAYING){float budget=elapsed;while(budget>0 && state==PLAYING && !engine.over){float step=Math.min(GameEngine.MAX_DT,budget);engine.update(step);budget-=step;if(!trail.isEmpty()&&engine.touchesHazard(trail))resolveStroke();}if(engine.over&&state==PLAYING)finishGame();}
        noticeTime=Math.max(0,noticeTime-dt);flashTime=Math.max(0,flashTime-dt);floatingTime=Math.max(0,floatingTime-dt);fillTime=Math.max(0,fillTime-dt);
        for(int i=particles.size()-1;i>=0;i--){Particle p=particles.get(i);p.life-=dt;p.x+=p.vx*dt;p.y+=p.vy*dt;p.vx*=.98f;p.vy*=.98f;if(p.life<=0)particles.remove(i);}
        layoutBoard();c.drawColor(0xff0b1418);if(state==HOME)drawHome(c);else if(state==PLAYING)drawHud(c);drawBoard(c,state==HOME);if(state==HOME)center(c,"Capture les points. Évite le corail.",195,board.bottom/ui+24,12,MUTED,regular);
        if(state==PLAYING)drawHint(c);if(state==PAUSED||state==OVER)drawEndCard(c);if(active)postInvalidateOnAnimation();
    }
    private void text(Canvas c,String s,float x,float y,float size,int color,Typeface face){
        paint.setStyle(Paint.Style.FILL);paint.setStrokeWidth(1);paint.setColor(color);paint.setTypeface(face);paint.setTextSize(size*ui);paint.setTextAlign(Paint.Align.LEFT);c.drawText(s,x*ui,y*ui,paint);
    }
    private void center(Canvas c,String s,float x,float y,float size,int color,Typeface face){
        paint.setStyle(Paint.Style.FILL);paint.setColor(color);paint.setTypeface(face);paint.setTextSize(size*ui);paint.setTextAlign(Paint.Align.CENTER);c.drawText(s,x*ui,y*ui,paint);paint.setTextAlign(Paint.Align.LEFT);
    }
    private void tracked(Canvas c,String s,float x,float y,int color){
        paint.setStyle(Paint.Style.FILL);paint.setTypeface(medium);paint.setTextSize(10*ui);paint.setColor(color);paint.setTextAlign(Paint.Align.LEFT);
        float pos=x*ui;for(int i=0;i<s.length();i++){String a=s.substring(i,i+1);c.drawText(a,pos,y*ui,paint);pos+=paint.measureText(a)+1.5f*ui;}
    }
    private void drawHome(Canvas c){
        tracked(c,"UN JEU PAR CERCLE",24,91,MINT);text(c,"Le geste fait",22,137,37,WHITE,bold);text(c,"le score.",22,178,37,WHITE,bold);
        text(c,"Dessine. Referme. Capture.",24,207,14,MUTED,regular);
        paint.setColor(0xff233a37);paint.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(238*ui,78*ui,366*ui,105*ui),13*ui,13*ui,paint);
        center(c,"RECORD  "+best,302,96,10,MINT,medium);
    }
    private void drawHud(Canvas c){
        tracked(c,"SCORE",24,86,MUTED);text(c,String.format(Locale.ROOT,"%04d",engine.score),22,125,34,WHITE,bold);
        tracked(c,"TEMPS",165,86,MUTED);text(c,String.format(Locale.ROOT,"%02d",(int)Math.ceil(engine.remaining))+" s",163,125,30,engine.remaining<=10?CORAL:WHITE,medium);
        tracked(c,"VIES",293,86,MUTED);for(int i=0;i<3;i++){paint.setColor(i<engine.lives?CORAL:0xff2c3b3f);c.drawCircle((302+i*21)*ui,113*ui,5.5f*ui,paint);}
        text(c,"×"+Math.min(engine.combo+1,5),24,148,12,MINT,medium);text(c,engine.combo>0?"Capture groupée = gros score":"La prochaine belle boucle est à toi",56,148,11,MUTED,regular);
    }
    private void drawBoard(Canvas c,boolean demo){
        int save=c.save();paint.setStyle(Paint.Style.FILL);paint.setColor(0xff112126);c.drawRoundRect(board,20*ui,20*ui,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(ui);paint.setColor(flashTime>0?CORAL:0xff28403f);c.drawRoundRect(board,20*ui,20*ui,paint);
        c.clipRect(board.left+2,board.top+2,board.right-2,board.bottom-2);c.translate(board.left,board.top);c.scale(scale,scale);
        paint.setStyle(Paint.Style.FILL);paint.setColor(0xff28423f);for(int x=60;x<1000;x+=88)for(int y=62;y<1250;y+=88)c.drawCircle(x,y,2,paint);
        if(demo)drawDemo(c);else{
            if(fillTime>0&&capturedPath!=null){paint.setColor(alpha(MINT,(int)(fillTime/.45f*40)));c.drawPath(capturedPath,paint);}
            for(GameEngine.Dot d:engine.dots)drawDot(c,d.x,d.y,d.radius,time+d.x*.01f);
            for(GameEngine.Hazard h:engine.hazards)drawHazard(c,h.x,h.y,h.radius,time);
            if(!trail.isEmpty())drawTrail(c,trail,true);
        }
        for(Particle p:particles){paint.setColor(alpha(p.color,(int)(Math.min(1,p.life/.35f)*255)));c.drawCircle(p.x,p.y,p.radius,paint);}
        if(floatingTime>0){paint.setColor(alpha(WHITE,(int)(Math.min(1,floatingTime/.3f)*255)));paint.setTextSize(68);paint.setTypeface(bold);paint.setTextAlign(Paint.Align.CENTER);c.drawText(floating,floatX,floatY-(1-floatingTime)*95,paint);paint.setTextAlign(Paint.Align.LEFT);}
        c.restoreToCount(save);
    }
    private void drawDemo(Canvas c){
        float[][] dots={{210,245},{420,190},{695,225},{260,490},{450,435},{650,485},{340,690},{575,730},{780,780},{180,890},{470,1000},{710,1080}};
        for(float[] p:dots)drawDot(c,p[0],p[1],13,time+p[0]);drawHazard(c,795+(float)Math.sin(time*.5)*35,430,29,time);drawHazard(c,200,1080+(float)Math.sin(time*.8)*35,29,-time);
        List<GameEngine.Point> demo=new ArrayList<>();int count=80;float progress=.72f+.28f*(float)Math.sin(time*.65f);
        for(int i=0;i<=Math.max(7,(int)(count*progress));i++){double a=-Math.PI/2+i*Math.PI*2/count;demo.add(new GameEngine.Point(414+(float)Math.cos(a)*267,470+(float)Math.sin(a)*240));}drawTrail(c,demo,false);
        
    }
    private void drawDot(Canvas c,float x,float y,float r,float phase){
        paint.setStyle(Paint.Style.FILL);paint.setColor(alpha(MINT,14));c.drawCircle(x,y,r*2.8f,paint);paint.setColor(alpha(MINT,35));c.drawCircle(x,y,r*1.7f+2*(float)Math.sin(phase*2),paint);paint.setColor(WHITE);c.drawCircle(x,y,r,paint);paint.setColor(MINT);c.drawCircle(x-2,y-3,r*.42f,paint);
    }
    private void drawHazard(Canvas c,float x,float y,float r,float phase){
        paint.setStyle(Paint.Style.FILL);paint.setColor(alpha(CORAL,19));c.drawCircle(x,y,r+19,paint);paint.setColor(CORAL);int save=c.save();c.rotate(phase*35,x,y);
        path.reset();path.moveTo(x,y-r);path.lineTo(x+r*.48f,y-r*.36f);path.lineTo(x+r,y);path.lineTo(x+r*.4f,y+r*.4f);path.lineTo(x,y+r);path.lineTo(x-r*.4f,y+r*.4f);path.lineTo(x-r,y);path.lineTo(x-r*.4f,y-r*.4f);path.close();c.drawPath(path,paint);paint.setColor(0xff401e24);c.drawCircle(x,y,5,paint);c.restoreToCount(save);
    }
    private Path makePath(List<GameEngine.Point> points,boolean close){Path p=new Path();if(points.isEmpty())return p;p.moveTo(points.get(0).x,points.get(0).y);for(int i=1;i<points.size();i++)p.lineTo(points.get(i).x,points.get(i).y);if(close)p.close();return p;}
    private void drawTrail(Canvas c,List<GameEngine.Point> points,boolean ring){
        Path line=makePath(points,false);paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setColor(alpha(MINT,28));paint.setStrokeWidth(24);c.drawPath(line,paint);paint.setColor(MINT);paint.setStrokeWidth(6);c.drawPath(line,paint);
        GameEngine.Point first=points.get(0),last=points.get(points.size()-1);if(ring){paint.setColor(alpha(MINT,160));paint.setStrokeWidth(3);c.drawCircle(first.x,first.y,35+4*(float)Math.sin(time*5),paint);}paint.setStyle(Paint.Style.FILL);paint.setColor(WHITE);c.drawCircle(last.x,last.y,11,paint);
    }
    private void drawHint(Canvas c){
        String hint=noticeTime>0?notice:trail.isEmpty()?"Dessine autour des points lumineux":"Reviens au départ pour fermer la boucle";
        center(c,hint,195,height-52,13,noticeTime>0?WHITE:MUTED,regular);center(c,"ÉVITE LES ÉCLATS CORAIL",195,height-29,9,0xff678b87,medium);
        float ratio=Math.max(0,engine.remaining/60f);paint.setColor(0xff243a3c);c.drawRoundRect(24*ui,(height-11)*ui,366*ui,(height-8)*ui,2*ui,2*ui,paint);paint.setColor(engine.remaining<=10?CORAL:MINT);c.drawRoundRect(24*ui,(height-11)*ui,(24+342*ratio)*ui,(height-8)*ui,2*ui,2*ui,paint);
    }
    private void drawEndCard(Canvas c){
        paint.setColor(0xe60b1418);c.drawRect(0,64*ui,getWidth(),getHeight()-145*ui,paint);float middle=Math.max(240,(height-150)/2);
        tracked(c,state==PAUSED?"PAUSE":"PARTIE TERMINÉE",24,middle-115,MINT);text(c,state==PAUSED?"On souffle ?":newRecord?"Nouvelle marque !":"Bien bouclé.",22,middle-66,33,WHITE,bold);
        text(c,state==PAUSED?"Le chrono t’attend.":engine.lives==0?"Les éclats ont eu le dernier mot.":"Chaque boucle compte.",24,middle-32,14,MUTED,regular);
        text(c,String.valueOf(engine.score),20,middle+50,72,WHITE,bold);tracked(c,"POINTS",26,middle+76,MUTED);
        paint.setColor(0xff253e38);c.drawRoundRect(24*ui,(middle+103)*ui,366*ui,(middle+151)*ui,14*ui,14*ui,paint);text(c,"TON RECORD",40,middle+132,11,MUTED,medium);
        paint.setTextAlign(Paint.Align.RIGHT);paint.setTextSize(20*ui);paint.setColor(MINT);paint.setTypeface(medium);c.drawText(String.valueOf(best),348*ui,(middle+134)*ui,paint);paint.setTextAlign(Paint.Align.LEFT);
    }
    private static int alpha(int color,int a){return(color&0xffffff)|(Math.max(0,Math.min(a,255))<<24);}
    @Override public boolean onTouchEvent(MotionEvent event){
        if(state!=PLAYING)return true;int action=event.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN){finger=event.getPointerId(0);lockedStroke=false;trail.clear();trailLength=0;if(board.contains(event.getX(),event.getY()))append(event.getX(),event.getY());else lockedStroke=true;return true;}
        if(action==MotionEvent.ACTION_CANCEL||action==MotionEvent.ACTION_POINTER_DOWN){trail.clear();lockedStroke=true;finger=-1;return true;}
        int index=event.findPointerIndex(finger);if(index<0||lockedStroke)return true;
        if(action==MotionEvent.ACTION_MOVE){for(int h=0;h<event.getHistorySize()&&!lockedStroke;h++)append(event.getHistoricalX(index,h),event.getHistoricalY(index,h));if(!lockedStroke)append(event.getX(index),event.getY(index));}
        else if(action==MotionEvent.ACTION_UP){append(event.getX(index),event.getY(index));if(!lockedStroke&&!trail.isEmpty()){if(trail.size()>=6&&distance(trail.get(0),trail.get(trail.size()-1))<90&&trailLength>230)resolveStroke();else{trail.clear();showNotice("Reviens au départ pour fermer la boucle");}}finger=-1;lockedStroke=false;performClick();}return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    private void append(float px,float py){
        if(lockedStroke)return;if(!board.contains(px,py)){trail.clear();lockedStroke=true;showNotice("Garde le tracé dans le terrain");return;}
        GameEngine.Point p=new GameEngine.Point((px-board.left)/scale,(py-board.top)/scale);
        if(!trail.isEmpty()){float d=distance(p,trail.get(trail.size()-1));if(d<7)return;trailLength+=d;}trail.add(p);
        if(engine.touchesHazard(trail)){resolveStroke();return;}if(trail.size()>=6&&trailLength>230&&distance(trail.get(0),p)<50){resolveStroke();return;}
        if(trail.size()>750){trail.clear();lockedStroke=true;showNotice("Essaie une boucle plus courte");}invalidate();
    }
    private void resolveStroke(){
        if(trail.isEmpty())return;GameEngine.CaptureResult r=engine.capture(trail);lockedStroke=true;
        if(r.hit){flashTime=.25f;showNotice("Un éclat ! Fais-lui de la place.");listener.onCapture(true);performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);burst(r.x,r.y,CORAL,15);}
        else if(r.invalid)showNotice("Une boucle simple, sans croiser le trait");else if(r.count==0)showNotice("Boucle vide. Vise les points lumineux !");
        else{capturedPath=makePath(trail,true);fillTime=.45f;floating="+"+r.points;floatX=r.x;floatY=r.y;floatingTime=1;burst(r.x,r.y,MINT,24);listener.onCapture(false);performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);showNotice(r.count+" points capturés  ·  ×"+Math.min(engine.combo+1,5)+" pour la suite");}
        trail.clear();if(engine.over)finishGame();invalidate();
    }
    private void showNotice(String s){notice=s;noticeTime=2;}
    private float distance(GameEngine.Point a,GameEngine.Point b){return(float)Math.hypot(a.x-b.x,a.y-b.y);}
    private void burst(float x,float y,int color,int count){for(int i=0;i<count;i++){Particle p=new Particle();p.x=x;p.y=y;p.color=color;p.radius=3+random.nextFloat()*6;double a=random.nextDouble()*Math.PI*2;float speed=100+random.nextFloat()*240;p.vx=(float)Math.cos(a)*speed;p.vy=(float)Math.sin(a)*speed;p.life=.5f+random.nextFloat()*.6f;particles.add(p);}}
    private static final class Particle{float x,y,vx,vy,radius,life;int color;}
}