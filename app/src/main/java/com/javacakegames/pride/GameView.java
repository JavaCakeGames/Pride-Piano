package com.javacakegames.pride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

  private final Paint paint = new Paint();
  private final SoundPool soundPool;

  private final boolean[]
    whiteNotesPressed = new boolean[7],
    previousWhiteNotesPressed = new boolean[7],
    blackNotesPressed = new boolean[6],
    previousBlackNotesPressed = new boolean[6];

  private final Note[] notes = new Note[13];

  private float noteWidth;

  final int soundID;

  public GameView(Context context) {
    super(context);

    paint.setDither(false);

    final int maxStreams = 12;
    if (Build.VERSION.SDK_INT >= 21) {
      soundPool = new SoundPool.Builder()
        .setMaxStreams(maxStreams)
        .setAudioAttributes(
          new AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_GAME)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
        ).build();
    } else {
      soundPool = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
    }
    soundID = soundPool.load(getContext(), R.raw.gb4, 1);

    for (int i = 0; i < 7; i++) {
      notes[i] = new WhiteNote(i, this);
    }
    for (int i = 0; i < 6; i++) {
      notes[i + 7] = new BlackNote(i, this);
    }

    getHolder().addCallback(this);

  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    for (Note note : notes) note.resize(width, height);
    drawCanvas();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
  }

  @Override
  public void draw(Canvas canvas) {
    if (canvas != null) {
      super.draw(canvas);
      canvas.drawColor(Color.GRAY);

      paint.setAntiAlias(false);
      paint.setStyle(Paint.Style.FILL);
      for (Note note : notes)
        note.draw(paint, canvas);

      noteWidth /= 2;
      /*for (int i = 0; i < 6; i++) {
        if (blackNoteColours[i] != 0x00000000) {
          paint.setColor(blackNoteColours[i]);
          float left = i * (noteWidth * 2) + (noteWidth * 1.5f);
          float right = left + noteWidth;
          canvas.drawRect(left, 0, right, getHeight() * 0.666666667f, paint);
        }
      }*/

      paint.setAntiAlias(true);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(5);
      paint.setColor(0xff66338b);
      canvas.drawCircle(50, 50, 25, paint);

    }
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    Arrays.fill(whiteNotesPressed, Boolean.FALSE);
    Arrays.fill(blackNotesPressed, Boolean.FALSE);

    // While multitouch was added in API 5, I don't know how to do it without
    // the stuff introduced in API 8, so < 8 gets no multitouch support here.
    if (Build.VERSION.SDK_INT >= 8) {
      switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
        case MotionEvent.ACTION_MOVE:
          int index = event.getActionIndex();
          processTouch(event.getX(index), event.getY(index), true);
          break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
          index = event.getActionIndex();
          processTouch(event.getX(index), event.getY(index), false);
          break;
      }
    } else {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
          processTouch(event.getX(), event.getY(), true);
          break;
        case MotionEvent.ACTION_UP:
          processTouch(event.getX(), event.getY(), false);
          break;
      }
    }

    for (Note note : notes) note.update();

    return true;
  }

  int play(float pitch) { // todo move to own thread
    return soundPool.play(soundID, 1, 1, 5, 0, pitch);
  }

  private final Timer timer = new Timer();
  private TimerTask timerTask;
  void stop(int id) {
    final float[] volume = {1};
    timerTask = new TimerTask() {
      @Override
      public void run() {
        volume[0] -= 0.04f;
        soundPool.setVolume(id, Math.max(0, volume[0]), Math.max(0, volume[0]));
        if (volume[0] <= 0) {
          soundPool.stop(id);
          this.cancel();
        }
      }
    };
    timer.schedule(timerTask, 0, 50);
  }

  void drawCanvas() {
    Canvas canvas = null;
    try {
      canvas = getHolder().lockCanvas();
      synchronized (getHolder()) {
        draw(canvas);
      }
    } finally {
      if (canvas != null) {
        getHolder().unlockCanvasAndPost(canvas);
      }
    }
  }

  private void processTouch(float x, float y, boolean down) {

    boolean notePlayed;
    for (int i = notes.length - 1; i >= 0; i--) {
      notePlayed = notes[i].process(x, y, down, 0, false);
      if (notePlayed) break;
    }

    /*performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE);

    VibrationEffect vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
    VibrationEffect vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
    vibrator.cancel();
    vibrator.vibrate(vibrationEffect);*/

  }

}
