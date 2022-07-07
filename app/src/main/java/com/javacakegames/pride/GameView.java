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
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

  private final Paint paint = new Paint();
  private GameThread gameThread;
  private final SoundPool soundPool;

  private final int[] whiteNoteColours = {
    0xfffdd817, 0xffe22016, 0xfff28917, 0xffefe524, 0xff78b82a, 0xff2c58a4, 0xff6d2380
  };
  private final int[] blackNoteColours = {
    0xffffffff, 0xfff4aec8, 0x00000000, 0xff7bcce5, 0xff945516, 0xff000000
  };
  private final float[] whiteNotePitches = {
    0.707106918374981f, 0.789433389319839f, 0.881370416241527f,
    0.931289107250668f, 1.03979104198895f, 1.16096702220402f, 1.29630068053494f
  };
  private final float[] blackNotePitches = {
    0.747134188548398f, 0.834133166610544f, 0.984042315523848f,
    1.09870595966501f, 1.2267649207462f
  };

  final int soundID;

  public GameView(Context context) {
    super(context);

    paint.setDither(false);

    final int maxStreams = 5;
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

    getHolder().addCallback(this);

  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    /*gameThread = new GameThread(this);
    gameThread.setRunning(true);
    gameThread.start();*/
    drawCanvas();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    //gameThread.setRunning(false);
    /*while (true) {
      try {
        gameThread.join();
        break;
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      }
    }*/
  }

  @Override
  public void draw(Canvas canvas) {
    if (canvas != null) {
      super.draw(canvas);
      canvas.drawColor(Color.GRAY);
      float noteWidth = getWidth() / 7f;

      paint.setAntiAlias(false);
      paint.setStyle(Paint.Style.FILL);
      for (int i = 0; i < 7; i++) {
        paint.setColor(whiteNoteColours[i]);
        float left = i * noteWidth;
        float right = left + noteWidth;
        canvas.drawRect(left, 0, right, getHeight(), paint);
      }

      noteWidth /= 2;
      for (int i = 0; i < 6; i++) {
        paint.setColor(blackNoteColours[i]);
        float left = i * (noteWidth * 2) + (noteWidth * 1.5f);
        float right = left + noteWidth;
        canvas.drawRect(left, 0, right, getHeight() * 0.666666667f, paint);
      }

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
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
      case MotionEvent.ACTION_MOVE:
        if (Build.VERSION.SDK_INT >= 5)
          for (int i = 0; i < event.getPointerCount(); i++)
            processTouch(event.getX(i), event.getY(i));
        else processTouch(event.getX(), event.getY());
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        break;
    }
    return true;
  }

  private void drawCanvas() {
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

  private void processTouch(float x, float y) {

    boolean white = false;
    if (y < getHeight() * 0.666666667f) {
      white = true;
    }

    if (!white) {
      float pitch = whiteNotePitches[(int) (x / getWidth() * 7)];
      Log.d("processTouch", String.valueOf(pitch));
      final int volume = 1;
      soundPool.play(soundID, volume, volume, 5, 0, pitch);
    }
  }

}
