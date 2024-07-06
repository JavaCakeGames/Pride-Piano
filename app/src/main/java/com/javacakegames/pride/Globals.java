package com.javacakegames.pride;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Vibrator;

import java.util.Random;
import java.util.Timer;

public class Globals {

  public static final Timer TIMER = new Timer();
  public static final Random RNG = new Random();
  public static final float TWO_THIRDS = .666666666666666666666666666666666667f;
  public static final boolean IS_ARC =
    Build.DEVICE != null && Build.DEVICE.matches(".+_cheets|cheets_.+");

  public static AudioManager audioMan;
  public static Vibrator vibrator;

  public static void init(Context context) {
    audioMan = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
  }

}
