package com.javacakegames.pride.notes;

import com.javacakegames.pride.GameView;

public class BlackNote extends Note {

  private static final int[] blackColours = {
    0xff000000, 0xff945516, 0x00000000,
    0xffffffff, 0xff7bcce5, 0xfff4aec8
  };
  private static final float[] blackPitches = {
    0.747134188548398f, 0.834133166610544f, 0f,
    0.984042315523848f, 1.09870595966501f, 1.2267649207462f
  };

  public BlackNote(int index, GameView parent, Integer plainColour) {
    super(
      index, parent, true,
      plainColour != null ? plainColour : blackColours[index],
      blackPitches[index], plainColour != null
    );
  }

  @Override
  public boolean process(float screenX, float screenY, boolean down,
                         int pointerId, boolean silent) {
    if (screenY < getParent().getHeight() * 0.666666667f) {
      int note = (int) Math.floor(
        (screenX - getWidth() / 2) / getParent().getWidth() * 7
      );
      return processNote(note, down, pointerId);
    }
    return false;
  }

}
