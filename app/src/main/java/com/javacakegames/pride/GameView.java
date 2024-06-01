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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Timer;
import java.util.TimerTask;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

  private final AudioManager audioManager;
  private final int maxSystemVolume;
  private float noteVolume; // 0-1

  private final Paint paint = new Paint(0); // No antialias
  private final SoundPool soundPool;

  private final Note[] notes = new Note[13];

  final int soundID;

  private boolean canvasDirty = true;

  public GameView(Context context, boolean plain) {
    super(context);

    this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    this.maxSystemVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

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

    boolean darkMode = false;
    // todo looks bad in dark mode so commented out
    /*if (Build.VERSION.SDK_INT > 8) {
      int uiMode = getResources().getConfiguration().uiMode;
      if ((uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
        darkMode = true;
      }
    }*/

    Integer plainWhite = plain ? 0xffffffff : null;
    if (plainWhite != null && darkMode) plainWhite = 0xff000000;
    Integer plainBlack = plain ? 0x00000000 : null;
    if (plainBlack != null && darkMode) plainBlack = 0x00ffffff;

    for (int i = 0; i < 7; i++) {
      notes[i] = new WhiteNote(i, this, plainWhite);
    }
    for (int i = 0; i < 6; i++) {
      notes[i + 7] = new BlackNote(i, this, plainBlack);
    }


    getHolder().addCallback(this);

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
      postOnAnimation(new Runnable() {
        @Override
        public void run() {
          drawCanvas();
          postOnAnimation(this);
        }
      });
    } else {
      TimerTask renderTask;
      renderTask = new TimerTask() {
        @Override
        public void run() {
          drawCanvas();
        }
      };
      timer.schedule(renderTask, 0, 16);
    }

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

      for (Note note : notes)
        note.draw(paint, canvas);
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    // While multitouch was added in API 5, I don't know how to do it without
    // the stuff introduced in API 8, so < 8 gets no multitouch support here.
    if (Build.VERSION.SDK_INT >= 8) {
      int index;
      switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
          index = event.getActionIndex();
          processTouch(event.getX(index), event.getY(index), event.getPointerId(index), true);
          break;
        case MotionEvent.ACTION_MOVE:
          // https://stackoverflow.com/a/10954685

          int pointerCount = event.getPointerCount();
          for(int pointerIndex = 0; pointerIndex < pointerCount; pointerIndex++)
          {
            int pointerId = event.getPointerId(pointerIndex);
            processTouch(event.getX(pointerIndex), event.getY(pointerIndex), pointerId, true);
          }
          break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
          index = event.getActionIndex();
          processTouch(event.getX(index), event.getY(index), event.getPointerId(index), false);
          break;
      }
    } else {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
          processTouch(event.getX(), event.getY(), 0, true);
          break;
        case MotionEvent.ACTION_UP:
          processTouch(event.getX(), event.getY(), 0, false);
          break;
      }
    }

    for (Note note : notes) note.update();

    return true;
  }

  int play(float pitch) { // todo move to own thread
    if (!canvasDirty) { // First occurrence this frame
      noteVolume = Math.min(2/3f, (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / maxSystemVolume);
    }
    return soundPool.play(soundID, noteVolume, noteVolume, 5, 0, pitch);
  }

  private final Timer timer = new Timer();

  void stop(int id) {
    final float[] volume = {1};
    TimerTask timerTask = new TimerTask() {
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

    if (!canvasDirty) return;

    Canvas canvas = null;
    try {
      canvas = getHolder().lockCanvas();
      synchronized (getHolder()) {
        draw(canvas);
      }
    } finally {
      if (canvas != null) {
        getHolder().unlockCanvasAndPost(canvas);
        canvasDirty = false;
      }
    }
  }

  public void dirtyCanvas() {
    canvasDirty = true;
  }

  private void processTouch(float x, float y, int index, boolean down) {
    boolean notePlayed;
    for (int i = notes.length - 1; i >= 0; i--) {
      notePlayed = notes[i].process(x, y, down, index, false);
      if (notePlayed) {
        // Set all other notes to not pressed by this pointer index
        // Comparing adjacent notes isn't good enough with fast finger movements
        for (int j = notes.length - 1; j >= 0; j--) {
          if (j != i) notes[j].setPressed(false, index);
        }
        return;
      }
    }

  }

}
