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
import android.widget.TextView;

/** Native shell: accessible controls, local preferences and lifecycle. */
public final class MainActivity extends Activity implements GameView.Listener {
    private final int INK=0xff0b1418, MINT=0xffbef7d1, WHITE=0xffeff6ef;
    private GameView game;
    private SharedPreferences preferences;
    private LinearLayout footer;
    private Button primary, secondary, sound, pause;
    private TextView caption;
    private boolean soundOn;
    private ToneGenerator tones;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(INK); getWindow().setNavigationBarColor(INK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        preferences=getSharedPreferences("boucle",MODE_PRIVATE);
        soundOn=preferences.getBoolean("sound",true);
        FrameLayout root=new FrameLayout(this); root.setBackgroundColor(INK);
        root.setOnApplyWindowInsetsListener((view,insets)-> {
            if(android.os.Build.VERSION.SDK_INT>=30) {
                android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                root.setPadding(safe.left,safe.top,safe.right,safe.bottom);
            } else root.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        FrameLayout stage=new FrameLayout(this); root.addView(stage,new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER));
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{int width=Math.min(r-l-root.getPaddingLeft()-root.getPaddingRight(),dp(480));FrameLayout.LayoutParams params=(FrameLayout.LayoutParams)stage.getLayoutParams();if(width>0 && params.width!=width){params.width=width;stage.setLayoutParams(params);}});
        game=new GameView(this,preferences.getInt("best",0),this);
        stage.addView(game,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(22),0,dp(16),0);
        TextView title=new TextView(this); title.setText("◯  BOUCLE"); title.setTextColor(WHITE); title.setTextSize(19);
        title.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL)); title.setLetterSpacing(.09f); title.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(title,new LinearLayout.LayoutParams(0,-1,1));
        Button help=smallButton("?"); help.setContentDescription("Comment jouer"); help.setOnClickListener(v->showHelp());
        top.addView(help,new LinearLayout.LayoutParams(dp(44),dp(44)));
        sound=smallButton(soundOn?"♪":"♪ ×"); updateSoundLabel();
        sound.setOnClickListener(v-> {
            soundOn=!soundOn; preferences.edit().putBoolean("sound",soundOn).apply(); updateSoundLabel();
            if(soundOn)playTone(false); else if(tones!=null)tones.stopTone();
        });
        top.addView(sound,new LinearLayout.LayoutParams(dp(48),dp(44)));
        pause=smallButton("Ⅱ"); pause.setContentDescription("Mettre en pause"); pause.setOnClickListener(v->game.pauseGame());
        top.addView(pause,new LinearLayout.LayoutParams(dp(44),dp(44)));
        stage.addView(top,new FrameLayout.LayoutParams(-1,dp(64),Gravity.TOP));
        footer=new LinearLayout(this); footer.setOrientation(LinearLayout.VERTICAL); footer.setGravity(Gravity.CENTER); footer.setPadding(dp(24),0,dp(24),dp(12));
        primary=button("Jouer  →",true); secondary=button("Accueil",false);
        primary.setOnClickListener(v-> {if(game.state==GameView.PAUSED)game.resumeGame();else game.startGame();});
        secondary.setOnClickListener(v->game.goHome());
        caption=new TextView(this); caption.setTextSize(11); caption.setTextColor(0xff91a6a5); caption.setGravity(Gravity.CENTER); caption.setLetterSpacing(.06f);
        footer.addView(primary,new LinearLayout.LayoutParams(-1,dp(56)));
        LinearLayout.LayoutParams sub=new LinearLayout.LayoutParams(-1,dp(44));sub.topMargin=dp(4);footer.addView(secondary,sub);
        footer.addView(caption,new LinearLayout.LayoutParams(-1,dp(32)));
        stage.addView(footer,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
        setContentView(root);onStateChanged(GameView.HOME);
        if(android.os.Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,()->handleBack());
    }
    private void updateSoundLabel(){sound.setText(soundOn?"♪":"♪ ×");sound.setContentDescription(soundOn?"Désactiver les sons":"Activer les sons");}
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private Button smallButton(String text){Button b=button(text,false);b.setTextSize(19);b.setPadding(0,0,0,0);return b;}
    private Button button(String text,boolean filled){
        Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(16);
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));b.setTextColor(filled?INK:WHITE);
        b.setMinHeight(0);b.setMinimumHeight(0);b.setMinWidth(0);b.setMinimumWidth(0);b.setElevation(0);
        GradientDrawable background=new GradientDrawable();background.setCornerRadius(dp(18));background.setColor(filled?MINT:Color.TRANSPARENT);
        b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(filled?0xff86d6b0:0xff28413e),background,null));return b;
    }
    private void showHelp(){
        game.pauseGame();
        new AlertDialog.Builder(this,android.app.AlertDialog.THEME_DEVICE_DEFAULT_DARK).setTitle("Le geste fait le score")
            .setMessage("1. Pose un doigt et dessine autour des points lumineux.\n\n2. Reviens au point de départ pour fermer la boucle. Plus tu captures de points ensemble, plus tu marques !\n\n3. Évite les éclats corail : toucher un éclat avec le trait ou l’enfermer coûte une vie.\n\nTu as 60 secondes et 3 vies. Les captures réussies augmentent ton multiplicateur jusqu’à ×5. Une boucle vide le remet à ×1.\n\nLa partie se met en pause quand tu quittes l’appli. Ton record reste sur ce téléphone.")
            .setPositiveButton("C’est compris",null).show();
    }
    @Override public void onStateChanged(int state){
        if(footer==null)return;
        boolean playing=state==GameView.PLAYING;footer.setVisibility(playing?View.GONE:View.VISIBLE);pause.setVisibility(playing?View.VISIBLE:View.GONE);
        primary.setText(state==GameView.PAUSED?"Reprendre  →":state==GameView.OVER?"Rejouer  →":"Jouer  →");
        secondary.setVisibility(state==GameView.HOME?View.GONE:View.VISIBLE);
        caption.setText(state==GameView.HOME?"60 SECONDES  ·  3 VIES  ·  HORS LIGNE":"UN DOIGT. UNE BOUCLE. À TOI DE JOUER.");
        if(state==GameView.OVER){preferences.edit().putInt("best",game.best).apply();game.announceForAccessibility("Partie terminée. Score "+game.engine.score+". Record "+game.best);}
    }
    @Override public void onCapture(boolean hit){playTone(hit);}
    private void playTone(boolean hit){
        if(!soundOn)return;
        try{if(tones==null)tones=new ToneGenerator(AudioManager.STREAM_MUSIC,23);tones.startTone(hit?ToneGenerator.TONE_PROP_NACK:ToneGenerator.TONE_PROP_BEEP,hit?110:65);}catch(RuntimeException ignored){}
    }
    @Override protected void onResume(){super.onResume();if(game!=null)game.setActive(true);}
    @Override protected void onPause(){if(game!=null){game.pauseGame();game.setActive(false);}if(tones!=null)tones.stopTone();super.onPause();}
    @Override protected void onDestroy(){if(tones!=null)tones.release();super.onDestroy();}
    private void handleBack(){if(game.state==GameView.PLAYING)game.pauseGame();else if(game.state==GameView.PAUSED||game.state==GameView.OVER)game.goHome();else finish();}
    @Override public void onBackPressed(){handleBack();}
}