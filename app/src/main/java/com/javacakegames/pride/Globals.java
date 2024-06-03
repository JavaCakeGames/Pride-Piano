package com.javacakegames.pride;

import static android.content.pm.PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT;
import static android.content.pm.PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Vibrator;

import java.util.Timer;

public class Globals {

  public static final Timer TIMER = new Timer();
  public static int supportedFingers = 1;
  public static final float TWO_THIRDS = .666666666666666666666666666666666667f;

  public static AudioManager audioMan;
  public static Vibrator vibrator;

  public static void init(Context context) {

    if (Build.VERSION.SDK_INT >= 8) {
      PackageManager packMan = context.getPackageManager();
      if (packMan.hasSystemFeature(FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND)) {
        supportedFingers = 10; // Only possible on API 9+
        // While jazzhand means >= 5, most devices will be 10.
      } else
        if (packMan.hasSystemFeature(FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT)) {
        supportedFingers = 2; // Only on API 8+
      }
    }

    audioMan = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
  }

}
