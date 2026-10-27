package fr.cercle.boucle;

/** Local progression is independent of UI and preferences storage. */
public final class CampaignProgress {
    public int unlocked;
    private final int[] stars=new int[LevelSpec.COUNT];
    public CampaignProgress(int unlocked,String saved){
        this.unlocked=Math.max(1,Math.min(LevelSpec.COUNT,unlocked));
        if(saved!=null)for(int i=0;i<Math.min(saved.length(),stars.length);i++){char c=saved.charAt(i);stars[i]=c>='0'&&c<='3'?c-'0':0;}
        // A recorded completion is enough to restore the next unlocked stage.
        for(int i=0;i<stars.length;i++)if(stars[i]>0)this.unlocked=Math.max(this.unlocked,Math.min(LevelSpec.COUNT,i+2));
    }
    public int stars(int number){return number<1||number>stars.length?0:stars[number-1];}
    public int totalStars(){int total=0;for(int n:stars)total+=n;return total;}
    public int completed(){int total=0;for(int n:stars)if(n>0)total++;return total;}
    public void recordWin(int number,int earned){
        if(number<1||number>unlocked||number>LevelSpec.COUNT||earned<1||earned>3)throw new IllegalArgumentException("Progression invalide");
        stars[number-1]=Math.max(stars[number-1],earned);unlocked=Math.max(unlocked,Math.min(LevelSpec.COUNT,number+1));
    }
    public String save(){StringBuilder s=new StringBuilder();for(int n:stars)s.append(n);return s.toString();}
}