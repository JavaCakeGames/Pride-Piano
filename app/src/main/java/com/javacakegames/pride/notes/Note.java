package com.javacakegames.pride.notes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.view.View;

import com.javacakegames.pride.GameView;
import com.javacakegames.pride.Globals;
import com.javacakegames.pride.SoundManager;

public class Note {

  private final boolean black;
  private final int colour, pressedColour;
  private final float pitch;
  private final int index;
  private final GameView parent;
  private final boolean plain;

  private boolean pressed, previousPressed;
  private int pressedPointer;
  private float width;
  private int playID;

  private static final int circleColour = 0xff66338b;
  private int circlePressedColour;

  private final SoundManager soundMan;

  public Note(int index, GameView parent, boolean black, int colour,
              float pitch, boolean plain) {
    this.black = black;
    this.index = index;
    this.parent = parent;
    this.colour = colour;
    this.pitch = pitch;
    this.pressedColour = calcPressedColour(colour);
    this.plain = plain;
    this.soundMan = parent.getSoundMan();
  }

  public void update() {
    if (pressed != previousPressed) {
      if (pressed) {
        playID = soundMan.play(pitch);
        // https://github.com/libgdx/libgdx/pull/6243
        // https://source.android.com/devices/input/haptics/haptics-ux-design
        // Doesn't work
//        if (Build.VERSION.SDK_INT >= 8) {
//          parent.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
//        } else {
        if (Globals.audioMan.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
          Globals.vibrator.vibrate(20);
      } else { // released
        soundMan.stop(playID);
      }
      parent.dirtyCanvas();
    }
    previousPressed = pressed;
  }

  /** Process finger down/up/move event. Overriden by BlackNote and WhiteNote.
   * @param screenX Finger X position
   * @param screenY Finger Y position
   * @param down Was the finger placed onto the screen? As opposed to raised.
   * @param pointerId The finger's index. Used for tracking movement without raising.
   * @param silent True if we shouldn't make a racket (app in background).
   * @return True if the note has done anything with the event. Prevents propagation.
   */
  public boolean process(float screenX, float screenY, boolean down, int pointerId, boolean silent) {
    return false;
  }

  boolean processNote(int note, boolean isDown, int pointerId) {
    if (note == this.index && pitch != 0) {
      setPressed(isDown, pointerId);
      return true;
    }
    return false;
  }

  public void draw(Paint paint, Canvas canvas) {
    if (pitch == 0) return;
    int colour = pressed ? this.pressedColour : this.colour;
    paint.setColor(colour);
    float left = index * width;
    float right = left + width;
    float height = parent.getHeight();
    if (black) {
      left += width * 0.7f;
      right += width * 0.3f;
      height *= Globals.TWO_THIRDS;
    } else if (plain) {
      if (index != 0) left += width * 0.025f;
      if (index != 6) right -= width * 0.025f;
    }
    canvas.drawRect(left, 0, right, height, paint);

    if (!black && index == 0 && !plain) {
      float halfWidth = width / 2;
      paint.setAntiAlias(true);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(halfWidth * 0.1f);
      if (pressed) {
        if (circlePressedColour == 0)
          circlePressedColour = calcPressedColour(circleColour);
        paint.setColor(circlePressedColour);
      } else {
        paint.setColor(circleColour);
      }
      canvas.drawCircle(halfWidth, height * .816666666666667f, halfWidth / 2, paint);
      paint.setAntiAlias(false);
      paint.setStyle(Paint.Style.FILL);
    }
  }

  public void resize(int screenWidth) {
    this.width = screenWidth / 7f;
  }

  public void setPressed(boolean pressed, int pointerId) {
    if (pointerId == -2 || (!pressed && pointerId == this.pressedPointer)) {
      this.pressed = false;
    } else if (pressed) {
      this.pressed = true;
      this.pressedPointer = pointerId;
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
    if (hsv[2] == 0) hsv[2] += .2f; // black
    else if (hsv[2] == 1 || hsv[2] == Globals.TWO_THIRDS) hsv[2] -= .1f; // white
    return Color.HSVToColor(hsv);
  }

}
