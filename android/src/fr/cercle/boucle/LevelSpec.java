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
        {60,10,0,0,8,0,0},{60,11,1,32,10,0,0},{60,12,1,36,12,2,0},{60,12,1,40,14,0,3},{60,13,1,44,16,3,0},
        {60,12,2,38,16,0,3},{60,13,2,42,18,2,0},{60,14,2,46,20,3,0},{60,13,2,50,20,0,4},{65,14,2,54,24,3,3},
        {60,13,2,57,22,0,3},{60,14,2,60,24,3,0},{60,14,2,63,26,0,4},{65,15,3,57,26,3,0},{60,14,3,60,28,0,3},
        {65,15,3,64,30,3,3},{60,15,3,67,30,4,0},{65,14,3,70,32,0,4},{65,16,3,73,34,4,0},{70,16,3,76,36,4,4},
        {65,15,3,80,32,3,3},{65,16,3,83,34,4,0},{60,15,3,86,34,0,5},{65,16,4,78,36,3,3},{65,16,4,82,38,4,0},
        {70,17,4,86,40,0,4},{65,16,4,90,40,4,4},{70,17,4,94,42,5,0},{65,17,4,98,44,0,5},{70,18,4,102,46,5,4},
        {65,16,4,106,44,4,4},{70,17,4,110,46,5,0},{65,17,5,100,46,0,5},{70,18,5,104,48,4,4},{70,17,5,108,50,5,0},
        {70,18,5,112,52,0,5},{75,18,5,116,54,5,4},{70,18,5,120,54,6,0},{75,18,5,124,56,0,6},{75,18,5,128,60,5,5},
        {70,18,5,132,54,5,4},{75,18,5,136,56,6,0},{70,18,6,125,58,0,6},{75,18,6,129,60,5,5},{75,18,6,133,62,6,0},
        {75,18,6,137,64,0,6},{80,18,6,141,66,6,5},{75,18,6,145,68,5,6},{80,18,6,150,70,6,5},{80,18,6,156,75,6,6}
    };
    private static final String[] NAMES={
        "Le premier tour","Petite récolte","Deux d’un geste","Trouver le rythme","Un joli trio","Premiers éclats","La bonne ouverture","Prendre large","Sans hésiter","La première couronne",
        "L’élan","Une poignée de lumière","La cadence","Le troisième éclat","Au bon moment","Le fil se tend","Quatre à la fois","Garder le cap","Une grande brassée","La deuxième couronne",
        "Le pas de côté","Le grand geste","Cinq sans fausse note","La danse des éclats","Une fenêtre s’ouvre","Le tempo","Le geste précis","Cinq d’un seul tour","Un fil régulier","La troisième couronne",
        "L’assurance","Le bon détour","Le cinquième éclat","Le passage étroit","Cueillir au vol","Le souffle long","La boucle juste","Six lumières","Une série parfaite","La quatrième couronne",
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