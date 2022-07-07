package com.javacakegames.pride;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

  private GameView gameView;

  //@SuppressLint("InlinedApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= 19) {
      getWindow().getDecorView().setSystemUiVisibility(
        SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION |
        SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE
      ); // IMMERSIVE on 19+
    }
    getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
    // https://stackoverflow.com/a/41792212
    // Immediately goes away instead of animation like
    // with requestWindowFeature(Window.FEATURE_NO_TITLE);
    View titleView = findViewById(android.R.id.title);
    if (titleView != null) {
      ((View)titleView.getParent()).setVisibility(View.GONE);
    }

    if (Build.VERSION.SDK_INT >= 28) { // Support camera cutouts
      getWindow().getAttributes().layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    }

    this.gameView = new GameView(this);
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
    //gameView.setVisibility(View.VISIBLE);
    /*if (!firstResume) {
      gameView.surfaceCreated(null);
    } else {
      firstResume = false;
    }*/
  }
}
