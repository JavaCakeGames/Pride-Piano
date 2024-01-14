package com.javacakegames.pride;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class PlainActivity extends Activity {

  private GameView gameView;

  //@SuppressLint("InlinedApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d("creaated", "plain");

    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    if (Build.VERSION.SDK_INT >= 19) {
      getWindow().getDecorView().setSystemUiVisibility(
        SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION |
        SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE
      ); // IMMERSIVE on 19+
    }

    if (Build.VERSION.SDK_INT >= 28) { // Support camera cutouts
      getWindow().getAttributes().layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    }

    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    if (currentVolume >= 15) { // todo detect volume changes, scale notes with system volume (clipping reduction)
      Toast.makeText(this, "Loud volume results in distortion.", Toast.LENGTH_SHORT).show();
    }

    this.gameView = new GameView(this, true);
    setContentView(gameView);
  }

  // Might be needed on screen lock?
  @Override
  protected void onPause() {
    super.onPause();
    //gameView.setVisibility(View.GONE);
    //gameView.surfaceDestroyed(null);
  }

  @Override
  protected void onResume() {
    super.onResume();
    gameView.dirtyCanvas();
    //gameView.setVisibility(View.VISIBLE);
    /*if (!firstResume) {
      gameView.surfaceCreated(null);
    } else {
      firstResume = false;
    }*/
  }
}
