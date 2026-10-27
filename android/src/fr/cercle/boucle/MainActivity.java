package fr.cercle.boucle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Native navigation, level picker, local preferences and lifecycle. */
public final class MainActivity extends Activity implements GameView.Listener {
    private final int INK=0xff0b1418,MINT=0xffbef7d1,WHITE=0xffeff6ef;
    private GameView game;
    private CampaignProgress progress;
    private SharedPreferences preferences;
    private LinearLayout footer;
    private Button primary,secondary,tertiary,sound,pause;
    private TextView caption,brand;
    private boolean soundOn;
    private ToneGenerator tones;

    @Override public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(INK);getWindow().setNavigationBarColor(INK);getWindow().getDecorView().setSystemUiVisibility(0);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        preferences=getSharedPreferences("boucle",MODE_PRIVATE);soundOn=preferences.getBoolean("sound",true);
        progress=new CampaignProgress(preferences.getInt("unlockedLevel",1),preferences.getString("campaignStars",""));
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(INK);
        root.setOnApplyWindowInsetsListener((view,insets)->{
            if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());root.setPadding(safe.left,safe.top,safe.right,safe.bottom);}
            else root.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;
        });
        FrameLayout stage=new FrameLayout(this);root.addView(stage,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{int width=Math.min(r-l-root.getPaddingLeft()-root.getPaddingRight(),dp(390));FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)stage.getLayoutParams();if(width>0&&params.width!=width){params.width=width;stage.setLayoutParams(params);}});
        game=new GameView(this,preferences.getInt("best",0),this);game.footerPixels=dp(184);game.setCampaignProgress(progress.unlocked,progress.totalStars(),progress.completed());
        game.selectedLevel=Math.max(1,Math.min(progress.unlocked,preferences.getInt("selectedLevel",progress.unlocked)));
        stage.addView(game,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(22),0,dp(16),0);
        brand=new TextView(this);brand.setTextColor(WHITE);brand.setTextSize(18);brand.setTypeface(Typeface.create("sans-serif-medium",0));brand.setLetterSpacing(.06f);brand.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
        Button help=smallButton("?");help.setContentDescription("Comment jouer");help.setOnClickListener(v->showHelp());top.addView(help,new LinearLayout.LayoutParams(dp(44),dp(44)));
        sound=smallButton(soundOn?"♪":"♪ ×");updateSoundLabel();sound.setOnClickListener(v->{soundOn=!soundOn;preferences.edit().putBoolean("sound",soundOn).apply();updateSoundLabel();if(soundOn)playTone(false);else if(tones!=null)tones.stopTone();});top.addView(sound,new LinearLayout.LayoutParams(dp(48),dp(44)));
        pause=smallButton("Ⅱ");pause.setContentDescription("Mettre en pause");pause.setOnClickListener(v->game.pauseGame());top.addView(pause,new LinearLayout.LayoutParams(dp(44),dp(44)));
        stage.addView(top,new FrameLayout.LayoutParams(-1,dp(64),Gravity.TOP));
        footer=new LinearLayout(this);footer.setOrientation(LinearLayout.VERTICAL);footer.setGravity(Gravity.CENTER);footer.setPadding(dp(24),0,dp(24),dp(12));
        primary=button("Jouer",true);secondary=button("Les 50 niveaux",false);tertiary=button("Mode libre",false);tertiary.setTextSize(13);tertiary.setTextColor(0xff9db8b1);
        primary.setOnClickListener(v->primaryAction());secondary.setOnClickListener(v->secondaryAction());tertiary.setOnClickListener(v->{if(game.state==GameView.HOME)game.startGame();else game.goHome();});
        footer.addView(primary,new LinearLayout.LayoutParams(-1,dp(56)));LinearLayout.LayoutParams sub=new LinearLayout.LayoutParams(-1,dp(44));sub.topMargin=dp(4);footer.addView(secondary,sub);footer.addView(tertiary,new LinearLayout.LayoutParams(-1,dp(44)));
        caption=new TextView(this);caption.setTextSize(10);caption.setTextColor(0xff91a6a5);caption.setGravity(Gravity.CENTER);caption.setLetterSpacing(.04f);footer.addView(caption,new LinearLayout.LayoutParams(-1,dp(24)));
        stage.addView(footer,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));setContentView(root);onStateChanged(GameView.HOME);
        if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->handleBack());
    }
    private void primaryAction(){
        if(game.state==GameView.PAUSED){game.resumeGame();return;}
        if(game.state==GameView.HOME){game.startLevel(game.selectedLevel);return;}
        if(game.engine.level!=null){int n=game.engine.level.number;if(game.engine.won&&n<50)n++;game.startLevel(n);}else game.startGame();
    }
    private void secondaryAction(){
        if(game.state==GameView.HOME||(game.state==GameView.OVER&&game.engine.level!=null)){showLevels();return;}
        if(game.state==GameView.PAUSED){if(game.engine.level!=null)game.startLevel(game.engine.level.number);else game.startGame();}else game.goHome();
    }
    private void updateSoundLabel(){sound.setText(soundOn?"♪":"♪ ×");sound.setContentDescription(soundOn?"Désactiver les sons":"Activer les sons");}
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private Button smallButton(String text){Button b=button(text,false);b.setTextSize(19);b.setPadding(0,0,0,0);return b;}
    private Button button(String text,boolean filled){
        Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(16);b.setTypeface(Typeface.create("sans-serif-medium",0));b.setTextColor(filled?INK:WHITE);b.setMinHeight(0);b.setMinimumHeight(0);b.setMinWidth(0);b.setMinimumWidth(0);b.setElevation(0);
        GradientDrawable bg=new GradientDrawable();bg.setCornerRadius(dp(18));bg.setColor(filled?MINT:Color.TRANSPARENT);b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(filled?0xff86d6b0:0xff28413e),bg,null));return b;
    }
    private String starLabel(int n){String s="";for(int i=0;i<3;i++)s+=i<n?"★":"☆";return s;}
    private void showLevels(){
        game.pauseGame();ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(dp(12),0,dp(12),dp(12));scroll.addView(list);
        AlertDialog dialog=new AlertDialog.Builder(this,android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("Les 50 niveaux").setView(scroll).setNegativeButton("Fermer",null).create();
        for(int chapter=0;chapter<5;chapter++){
            TextView heading=new TextView(this);heading.setText((chapter+1)+". "+LevelSpec.CHAPTERS[chapter]);heading.setTextColor(MINT);heading.setTextSize(14);heading.setTypeface(Typeface.create("sans-serif-medium",0));heading.setPadding(dp(5),dp(18),0,dp(8));list.addView(heading);
            for(int row=0;row<2;row++){LinearLayout line=new LinearLayout(this);for(int col=0;col<5;col++){
                final int number=chapter*10+row*5+col+1;boolean open=number<=progress.unlocked;Button b=button(number+"\n"+(open?starLabel(progress.stars(number)):"—"),number==game.selectedLevel);b.setTextSize(12);b.setPadding(0,0,0,0);b.setEnabled(open);if(!open)b.setTextColor(0xff536666);
                b.setContentDescription("Niveau "+number+", "+LevelSpec.get(number).name+(open?", "+progress.stars(number)+" étoiles":" verrouillé"));b.setOnClickListener(v->{game.selectLevel(number);dialog.dismiss();});
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(58),1);p.setMargins(dp(2),dp(2),dp(2),dp(2));line.addView(b,p);
            }list.addView(line,new LinearLayout.LayoutParams(-1,-2));}
        }
        dialog.show();dialog.getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-dp(24),dp(460)),Math.min(getResources().getDisplayMetrics().heightPixels-dp(80),dp(680)));
    }
    private void showHelp(){
        game.pauseGame();new AlertDialog.Builder(this,android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("Le geste fait le score")
            .setMessage("Trace autour des points lumineux, puis reviens au départ pour fermer la boucle. Évite les éclats corail : les toucher avec le trait ou les enfermer coûte une vie.\n\nCAMPAGNE : 50 niveaux en 5 chapitres. Réussis les objectifs affichés avant la fin du temps. « Groupe » indique le plus grand nombre de points capturés dans une seule boucle. « Série » indique le nombre de captures réussies de suite ; une boucle vide ou un éclat remet la série à zéro. Une fois l’objectif de série atteint, il reste validé.\n\nUne victoire donne une étoile ; garder au moins deux vies et finir avec au moins 35 % du temps donnent chacun une étoile supplémentaire. Les niveaux réussis restent rejouables.\n\nMODE LIBRE : 60 secondes pour battre ton record. Les combos montent jusqu’à ×5.\n\nProgression et record restent sur ce téléphone. Quitter l’appli met la partie en pause.")
            .setPositiveButton("C’est compris",null).show();
    }
    @Override public void onStateChanged(int state){
        if(footer==null)return;boolean campaign=game.engine.level!=null;
        if(state==GameView.OVER){
            if(campaign&&game.engine.won){progress.recordWin(game.engine.level.number,game.engine.earnedStars());preferences.edit().putInt("unlockedLevel",progress.unlocked).putString("campaignStars",progress.save()).putInt("selectedLevel",Math.min(50,game.engine.level.number+1)).apply();game.setCampaignProgress(progress.unlocked,progress.totalStars(),progress.completed());}
            else if(!campaign)preferences.edit().putInt("best",game.best).apply();
            game.announceForAccessibility(campaign?(game.engine.won?"Niveau réussi, "+game.engine.earnedStars()+" étoiles.":"Niveau à réessayer."):"Partie terminée. Score "+game.engine.score);
        }
        boolean playing=state==GameView.PLAYING;footer.setVisibility(playing?View.GONE:View.VISIBLE);pause.setVisibility(playing?View.VISIBLE:View.GONE);
        brand.setText(state!=GameView.HOME&&campaign?"◯  NIVEAU "+game.engine.level.number+" / 50":"◯  BOUCLE");brand.setTextSize(state!=GameView.HOME&&campaign?15:18);
        tertiary.setVisibility(state==GameView.OVER&&!campaign?View.GONE:View.VISIBLE);
        if(state==GameView.HOME){primary.setText("Jouer le niveau "+game.selectedLevel+"  →");secondary.setText("Les 50 niveaux");tertiary.setText("Mode libre  ·  record "+game.best);caption.setText(progress.completed()+" / 50 NIVEAUX RÉUSSIS  ·  "+progress.totalStars()+" / 150 ÉTOILES");preferences.edit().putInt("selectedLevel",game.selectedLevel).apply();}
        else if(state==GameView.PAUSED){primary.setText("Reprendre  →");secondary.setText("Recommencer");tertiary.setText("Accueil");caption.setText(campaign?game.engine.level.objective():"60 SECONDES  ·  3 VIES  ·  HORS LIGNE");}
        else if(state==GameView.OVER){primary.setText(campaign?(game.engine.won&&game.engine.level.number<50?"Niveau suivant  →":"Rejouer le niveau "+game.engine.level.number+"  →"):"Rejouer en mode libre  →");secondary.setText(campaign?"Choisir un niveau":"Accueil");tertiary.setText("Accueil");caption.setText(campaign&&game.engine.won&&game.engine.level.number==50?"BRAVO, LES 50 NIVEAUX SONT TERMINÉS !":"UN DOIGT. UNE BOUCLE. À TOI DE JOUER.");}
    }
    @Override public void onCapture(boolean hit){playTone(hit);}
    private void playTone(boolean hit){if(!soundOn)return;try{if(tones==null)tones=new ToneGenerator(AudioManager.STREAM_MUSIC,23);tones.startTone(hit?ToneGenerator.TONE_PROP_NACK:ToneGenerator.TONE_PROP_BEEP,hit?110:65);}catch(RuntimeException ignored){}}
    @Override protected void onResume(){super.onResume();if(game!=null)game.setActive(true);}
    @Override protected void onPause(){if(game!=null){game.pauseGame();game.setActive(false);}if(tones!=null)tones.stopTone();super.onPause();}
    @Override protected void onDestroy(){if(tones!=null)tones.release();super.onDestroy();}
    private void handleBack(){if(game.state==GameView.PLAYING)game.pauseGame();else if(game.state==GameView.PAUSED||game.state==GameView.OVER)game.goHome();else finish();}
    @Override public void onBackPressed(){handleBack();}
}