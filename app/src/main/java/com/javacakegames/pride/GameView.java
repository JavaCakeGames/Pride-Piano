package com.javacakegames.pride;

import static android.view.KeyEvent.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.javacakegames.pride.notes.BlackNote;
import com.javacakegames.pride.notes.Note;
import com.javacakegames.pride.notes.WhiteNote;

@SuppressLint("ViewConstructor")
public class GameView extends View {

  private final Paint paint = new Paint(0); // No antialias
  private final SoundManager soundManager;
  private final Note[] notes = new Note[13];

  public GameView(Context context, boolean plain) {
    super(context);

    this.setBackgroundColor(Color.GRAY);

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
    if (plainBlack != null && darkMode) plainBlack = 0xff000000;

    for (int i = 0; i < 7; i++) {
      notes[i] = new WhiteNote(i, this, plainWhite);
    }
    for (int i = 0; i < 6; i++) {
      notes[i + 7] = new BlackNote(i, this, plainBlack);
    }

    if (Build.VERSION.SDK_INT >= 26) setDefaultFocusHighlightEnabled(false);
    setFocusableInTouchMode(true);
    requestFocus();

  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    for (Note note : notes) note.resize(w);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    for (Note note : notes) note.draw(paint, canvas);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (Build.VERSION.SDK_INT >= 8) {
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
          for(int pointerIdx = 0; pointerIdx < pointerCount; pointerIdx++) {
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

    return updateNotes();
  }

  @Override
  public boolean onKeyPreIme(int keyCode, KeyEvent event) {
    boolean handled = false;
    if (event.getAction() == ACTION_DOWN && event.getRepeatCount() == 0) {
      handled = processKeystroke(keyCode, true);
    } else if (event.getAction() == ACTION_UP) {
      handled = processKeystroke(keyCode, false);
    }
    return handled || super.onKeyPreIme(keyCode, event);
  }

  public SoundManager getSoundMan() {
    return soundManager;
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

  public void appPaused() {
    for (Note note : notes) note.setPressed(false, -2);
  }

  /**
   * Process a keystroke from physical (or I guess on-screen) keyboard
   * @param keyCode The key code as defined by KeyEvent
   * @param pressed True if pressed, false if released
   * @return True if key event handled
   */
  public boolean processKeystroke(int keyCode, boolean pressed) {
    switch (keyCode) {
      case KEYCODE_Z: notes[0].setPressed(pressed, 256); break;
      case KEYCODE_X: notes[1].setPressed(pressed, 256); break;
      case KEYCODE_C: notes[2].setPressed(pressed, 256); break;
      case KEYCODE_V: notes[3].setPressed(pressed, 256); break;
      case KEYCODE_B: notes[4].setPressed(pressed, 256); break;
      case KEYCODE_N: notes[5].setPressed(pressed, 256); break;
      case KEYCODE_M: notes[6].setPressed(pressed, 256); break;
      case KEYCODE_S: notes[7].setPressed(pressed, 256); break;
      case KEYCODE_D: notes[8].setPressed(pressed, 256); break;
      case KEYCODE_G: notes[10].setPressed(pressed, 256); break;
      case KEYCODE_H: notes[11].setPressed(pressed, 256); break;
      case KEYCODE_J: notes[12].setPressed(pressed, 256); break;
      case KEYCODE_SPACE:
      case KEYCODE_MENU:
        for (Note note : notes) note.setPressed(pressed, 257);
        break;
      default: return false;
    }

    return updateNotes();
  }

  private boolean updateNotes() {
    for (Note note : notes) note.update();
    return true;
  }

}
