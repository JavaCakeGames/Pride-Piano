package com.javacakegames.pride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.javacakegames.pride.notes.BlackNote;
import com.javacakegames.pride.notes.Note;
import com.javacakegames.pride.notes.WhiteNote;

import java.util.TimerTask;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

  private final Paint paint = new Paint(0); // No antialias
  private final SoundManager soundManager;
  private final Note[] notes = new Note[13];

  private boolean canvasDirty = true;
  private int dirtyTimer;

  public GameView(Context context, boolean plain) {
    super(context);

    paint.setDither(false);
    soundManager = new SoundManager(context);

    boolean darkMode = false;
    if (Build.VERSION.SDK_INT >= 8) {
      int uiMode = getResources().getConfiguration().uiMode;
      if ((uiMode & Configuration.UI_MODE_NIGHT_MASK) ==
           Configuration.UI_MODE_NIGHT_YES) {
        darkMode = true;
      }
    }

    Integer plainWhite = plain ? 0xffffffff : null;
    if (plainWhite != null && darkMode) plainWhite = 0xffaaaaaa;
    Integer plainBlack = plain ? 0xff000000 : null;
    if (plainBlack != null && darkMode) plainBlack = 0x00000000;

    for (int i = 0; i < 7; i++) {
      notes[i] = new WhiteNote(i, this, plainWhite);
    }
    for (int i = 0; i < 6; i++) {
      notes[i + 7] = new BlackNote(i, this, plainBlack);
    }

    getHolder().addCallback(this);

    if (Build.VERSION.SDK_INT >= 16) {
      postOnAnimation(new Runnable() {
        @Override
        public void run() {
          if (dirtyTimer >= 0) {
            canvasDirty = true;
            dirtyTimer--;
          }
          drawCanvas();
          postOnAnimation(this);
        }
      });
    } else {
      TimerTask renderTask;
      renderTask = new TimerTask() {
        @Override
        public void run() {
          // Always dirty the canvas; reduces tearing in Gingerbread AVD
          canvasDirty = true;
          drawCanvas();
        }
      };
      Globals.TIMER.schedule(renderTask, 0, 16);
    }

  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format,
                             int width, int height) {
    for (Note note : notes) note.resize(width);
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

  @SuppressLint({"ClickableViewAccessibility", "NewApi"})
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if (Globals.supportedFingers >= 2) {
      int index;
      switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
          index = event.getActionIndex();
          processTouch(
            event.getX(index), event.getY(index),
            event.getPointerId(index), true
          );
          break;
        case MotionEvent.ACTION_MOVE:
          // https://stackoverflow.com/a/10954685

          int pointerCount = event.getPointerCount();
          for(int pointerIdx = 0; pointerIdx < pointerCount; pointerIdx++)
          {
            int pointerId = event.getPointerId(pointerIdx);
            processTouch(
              event.getX(pointerIdx), event.getY(pointerIdx),
              pointerId, true
            );
          }
          break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
          index = event.getActionIndex();
          processTouch(
            event.getX(index), event.getY(index),
            event.getPointerId(index), false
          );
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

  public SoundManager getSoundMan() {
    return soundManager;
  }

  private void drawCanvas() {

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

  public void dirtyCanvas(boolean prolonged) {
    canvasDirty = true;
    // 1 seems like enough, but set to 2 to be safe.
    // Maybe required due to double buffering?
    if (prolonged) dirtyTimer = 2;
  }

  public void dirtyCanvas() {
    canvasDirty = true;
  }

  private void processTouch(float x, float y, int pointerId, boolean down) {
    boolean notePlayed;
    for (int i = notes.length - 1; i >= 0; i--) {
      notePlayed = notes[i].process(x, y, down, pointerId, false);
      if (notePlayed) {
        // Set all other notes to not pressed by this pointer index
        // Comparing adjacent notes isn't good enough with fast finger movements
        for (int j = notes.length - 1; j >= 0; j--) {
          if (j != i) notes[j].setPressed(false, pointerId);
        }
        return;
      }
    }

  }

}
