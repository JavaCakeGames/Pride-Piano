package com.javacakegames.pride;

import android.content.res.Configuration;
import android.os.Build;

public class BlackNote extends Note {

  private static final int[] blackColours = {
    0xffffffff, 0xfff4aec8, 0x00000000,
    0xff7bcce5, 0xff945516, 0xff000000
  };
  private static final float[] blackPitches = {
    0.747134188548398f, 0.834133166610544f, 0f,
    0.984042315523848f, 1.09870595966501f, 1.2267649207462f
  };

  public BlackNote(int index, GameView parent, Integer plainColour) {
    super(index, parent, true, plainColour != null ? (blackColours[index] & 0xff000000) | plainColour : blackColours[index], blackPitches[index], plainColour != null);
  }

  @Override
  public boolean process(float screenX, float screenY, boolean down, int index, boolean silent) {
    if (screenY < getParent().getHeight() * 0.666666667f) {
      int note = (int) Math.floor((screenX - getWidth() / 2) / getParent().getWidth() * 7);
      if (processNote(note, down)) return true;
      // todo why is this different?
    }
    setPressed(false, index);
    return false;
  }
}
