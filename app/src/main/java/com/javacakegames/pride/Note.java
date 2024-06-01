package com.javacakegames.pride;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.Vibrator;
import android.view.View;

public class Note {

  private final boolean black;
  private final int colour, pressedColour;
  private final float pitch;
  private final int index;
  private final int volume = 1;
  private final GameView parent;
  private final boolean plain;

  private boolean pressed, previousPressed;
  private int pressedPointer;
  private float width;
  private int playID;

  public Note(int index, GameView parent, boolean black, int colour, float pitch, boolean plain) {
    this.black = black;
    this.index = index;
    this.parent = parent;
    this.colour = colour;
    this.pitch = pitch;
    this.pressedColour = calcPressedColour(colour);
    this.plain = plain;
  }

  public void update() {
    if (pressed != previousPressed) {
      if (pressed) {
        playID = parent.play(pitch); // todo handler.post(runnable);
        // https://github.com/libgdx/libgdx/pull/6243
        // https://source.android.com/devices/input/haptics/haptics-ux-design
        // Doesn't work
        // todo getting this each time might be costly?
        Vibrator vibrator = (Vibrator) parent.getContext().getSystemService(Context.VIBRATOR_SERVICE);
//        if (Build.VERSION.SDK_INT >= 8) {
//          parent.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
//        } else {
        AudioManager audioMan = (AudioManager) parent.getContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioMan.getRingerMode() != AudioManager.RINGER_MODE_SILENT) vibrator.vibrate(50);
      } else { // released
        parent.stop(playID);
      }
      parent.dirtyCanvas();
    }
    previousPressed = pressed;
  }


  /** Process finger down/up/move event. Overriden by BlackNote and WhiteNote.
   * @param screenX Finger X position
   * @param screenY Finger Y position
   * @param down Was the finger placed onto the screen? As opposed to raised.
   * @param index The finger's index. Used for tracking movement without raising.
   * @param silent True if we shouldn't make a racket (app in background).
   * @return True if the note has done anything with the event. Prevents propagation.
   */
  public boolean process(float screenX, float screenY, boolean down, int index, boolean silent) {
    return false;
  }

  boolean processNote(int note, boolean isDown) {
    if (note == this.index && pitch != 0) {
      setPressed(isDown, index);
      return true;
    }
    return false;
  }

  public void draw(Paint paint, Canvas canvas) {
    int colour = pressed ? this.pressedColour : this.colour;
    paint.setColor(colour);
    float left = index * width;
    float right = left + width;
    float height = parent.getHeight();
    if (black) {
      left += width * 0.75f;
      right += width * 0.25f;
      height *= 0.666666667f;
    } else if (plain) {
      left += width * 0.025f;
      right -= width * 0.025f;
    }
    canvas.drawRect(left, 0, right, height, paint);

    if (!black && index == 0 && !plain) {
      float halfWidth = width / 2;
      paint.setAntiAlias(true);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(halfWidth * 0.05f);
      paint.setColor(0xff66338b);
      canvas.drawCircle(halfWidth, height - halfWidth * 1.5f, halfWidth / 2, paint);
      paint.setAntiAlias(false);
      paint.setStyle(Paint.Style.FILL);
    }
  }

  public void resize(int screenWidth, int screenHeight) {
    float divisor = black ? 14f : 7f;
    divisor = 7f;
    this.width = screenWidth / 7f;
    // todo height?
  }

  void setPressed(boolean pressed, int index) {
    if (!pressed && index == this.pressedPointer) {
      this.pressed = false;
    } else if (pressed) {
      this.pressed = pressed;
      this.pressedPointer = index;
    }
  }

  View getParent() {
    return parent;
  }

  float getWidth() {
    return width;
  }

  private int calcPressedColour(int colour) {
    float[] hsv = new float[3];
    Color.colorToHSV(colour, hsv);
    hsv[1] *= 0.5f;
    if (hsv[2] == 0) hsv[2] += 0.2f; // black
    else if (hsv[2] == 1) hsv[2] -= 0.2f; // white
    return Color.HSVToColor(hsv);
  }

}
