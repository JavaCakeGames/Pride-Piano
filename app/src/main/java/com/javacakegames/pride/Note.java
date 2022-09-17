package com.javacakegames.pride;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;

public class Note {

  private final boolean black;
  private final int colour, pressedColour;
  private final float pitch;
  private final int index;
  private final int volume = 1;
  private final GameView parent;

  private boolean pressed, previousPressed;
  private float width;
  private int playID;

  public Note(int index, GameView parent, boolean black, int colour, float pitch) {
    this.black = black;
    this.index = index;
    this.parent = parent;
    this.colour = colour;
    this.pitch = pitch;

    float[] hsv = new float[3];
    Color.colorToHSV(colour, hsv);
    hsv[1] *= 0.5f;
    if (hsv[2] == 0) hsv[2] += 0.2f; // black
    else if (hsv[2] == 1) hsv[2] -= 0.2f; // white
    this.pressedColour = Color.HSVToColor(hsv);
  }

  public void update() {
    if (pressed != previousPressed) {
      if (pressed) {
        playID = parent.play(pitch);
        // https://source.android.com/devices/input/haptics/haptics-ux-design
        Vibrator vibrator = (Vibrator) parent.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 8) {
          parent.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        } else {
          vibrator.vibrate(20);
        }
      } else { // released
        parent.stop(playID);
      }
      parent.drawCanvas(); // todo optimise to not do full redraw
    }
    previousPressed = pressed;
  }

  // Override me
  public boolean process(float screenX, float screenY, boolean down) {
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
    }
    canvas.drawRect(left, 0, right, height, paint);
  }

  public void resize(int screenWidth, int screenHeight) {
    float divisor = black ? 14f : 7f;
    divisor = 7f;
    this.width = screenWidth / divisor;
  }

  void setPressed(boolean pressed) {
    this.pressed = pressed;
  }

  View getParent() {
    return parent;
  }

  float getWidth() {
    return width;
  }

}
