package com.javacakegames.pride.activities;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

import android.app.Activity;
import android.app.ActivityManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import com.javacakegames.pride.GameView;
import com.javacakegames.pride.Globals;
import com.javacakegames.pride.notes.WhiteNote;

import java.util.Calendar;

public class PrideActivity extends Activity {

  private GameView gameView;

  //@SuppressLint("InlinedApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Globals.init(this);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    if (Build.VERSION.SDK_INT >= 21) {
      this.setTaskDescription(
        new ActivityManager.TaskDescription(null, null, getRecentsColour())
      );
    }

    this.gameView = new GameView(this, isPlain());
    setContentView(gameView);
  }

  @Override
  protected void onPause() {
    super.onPause();
    gameView.appPaused();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (Build.VERSION.SDK_INT >= 19) {
      getWindow().getDecorView().setSystemUiVisibility(
        SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
          SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION |
          SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_IMMERSIVE
      ); // IMMERSIVE on 19+
    }
  }

  boolean isPlain() {
    return false;
  }

  int getRecentsColour() {
    return WhiteNote.WHITE_COLOURS
      [Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
  }

}
