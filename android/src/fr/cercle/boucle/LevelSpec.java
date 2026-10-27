package fr.cercle.boucle;

/** Fifty authored stages: capture count, grouped capture and successful streak. */
public final class LevelSpec {
    public static final int COUNT=50;
    public static final String[] CHAPTERS={"Premiers gestes","Prendre de l’élan","Le trait danse","Maîtriser la boucle","Le grand cercle"};
    public final int number,seconds,dotCount,hazardCount,target,minGroup,streak;
    public final float speed;
    public final String name;
    public final long seed;
    private LevelSpec(int n,String name,int seconds,int dots,int hazards,int speed,int target,int group,int streak){
        this.number=n;this.name=name;this.seconds=seconds;dotCount=dots;hazardCount=hazards;this.speed=speed;this.target=target;minGroup=group;this.streak=streak;seed=0xB0C1E50L+n*104729L;
    }
    // seconds, dots, hazards, speed, total captures, largest group, successful streak
    private static final int[][] DATA={
        {45,9,2,70,20,3,3},{45,10,2,70,22,3,3},{45,11,2,70,23,3,3},{45,11,2,70,25,3,4},{45,12,2,70,26,4,3},
        {45,11,3,70,27,3,4},{45,12,3,70,29,3,3},{45,13,3,70,30,4,3},{45,12,3,70,31,3,5},{48,13,3,70,33,4,4},
        {45,12,3,74,34,4,4},{45,13,3,78,35,4,4},{45,13,3,82,37,4,5},{48,14,4,74,38,4,4},{45,13,4,78,39,4,4},
        {48,14,4,83,41,4,4},{45,14,4,87,42,5,4},{48,13,4,91,44,4,5},{48,15,4,95,46,5,4},{52,15,4,99,49,5,5},
        {48,14,4,104,47,4,4},{48,15,4,108,49,5,5},{45,14,4,112,50,5,6},{48,15,5,101,52,4,4},{48,15,5,107,53,5,5},
        {52,16,5,112,54,5,5},{48,15,5,117,56,5,5},{52,16,5,122,57,6,5},{48,16,5,127,60,5,6},{52,17,5,133,63,6,5},
        {48,15,5,138,61,5,5},{52,16,5,143,63,6,6},{48,16,6,130,64,6,6},{52,17,6,135,65,5,5},{52,16,6,140,68,6,6},
        {52,17,6,146,71,6,6},{56,17,6,151,73,6,5},{52,17,6,156,73,7,6},{56,17,6,161,76,6,7},{56,17,6,166,81,6,6},
        {52,17,6,172,74,6,5},{56,17,6,177,76,7,7},{52,17,7,163,79,6,7},{56,17,7,168,81,6,6},{56,17,7,173,84,7,7},
        {56,17,7,178,87,6,7},{60,17,7,183,90,7,6},{56,17,7,189,92,6,7},{60,17,7,195,95,7,6},{60,17,7,203,102,7,7}
    };
    private static final String[] NAMES={
        "Le premier tour","Petite récolte","Un geste assuré","Trouver le rythme","Une belle brassée","Premiers éclats","La bonne ouverture","Prendre large","Sans hésiter","La première couronne",
        "L’élan","Une poignée de lumière","La cadence","Le troisième éclat","Au bon moment","Le fil se tend","La bonne poignée","Garder le cap","Une grande brassée","La deuxième couronne",
        "Le pas de côté","Le grand geste","Sans fausse note","La danse des éclats","Une fenêtre s’ouvre","Le tempo","Le geste précis","La grande boucle","Un fil régulier","La troisième couronne",
        "L’assurance","Le bon détour","Le cinquième éclat","Le passage étroit","Cueillir au vol","Le souffle long","La boucle juste","Sept lumières","Une série parfaite","La quatrième couronne",
        "Le dernier chapitre","La grande récolte","La ronde des six","Au cœur du mouvement","L’instant à saisir","Tenir le fil","La main sûre","Le dernier détour","Tout près du sommet","Le grand cercle"
    };
    public static LevelSpec get(int number){
        if(number<1||number>COUNT)throw new IllegalArgumentException("Niveau hors campagne : "+number);
        int[] d=DATA[number-1];return new LevelSpec(number,NAMES[number-1],d[0],d[1],d[2],d[3],d[4],d[5],d[6]);
    }
    public int chapter(){return(number-1)/10;}
    public String chapterName(){return CHAPTERS[chapter()];}
    public String objective(){return "Capturer "+target+" points en "+seconds+" s";}
    public String extras(){
        String a=minGroup>0?minGroup+" points dans une boucle":"";
        String b=streak>0?streak+" captures réussies de suite":"";
        return a.isEmpty()?b:b.isEmpty()?a:a+" · série de "+streak;
    }
}