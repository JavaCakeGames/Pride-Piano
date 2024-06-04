package com.javacakegames.pride.notes;

import com.javacakegames.pride.GameView;
import com.javacakegames.pride.Globals;

public class BlackNote extends Note {

  private static final int[] BLACK_COLOURS = {
    0xff000000, 0xff945516, 0x00000000,
    0xffffffff, 0xff7bcce5, 0xfff4aec8
  };

  // f#: 0.995

  private static final float[] BLACK_PITCHES = {
    0.745407733f, 0.836692028f, 0f,
    0.995f, 1.116849813f, 1.253621625f
  };

  public BlackNote(int index, GameView parent, Integer plainColour) {
    super(
      index, parent, true,
      plainColour != null ? plainColour : BLACK_COLOURS[index],
      BLACK_PITCHES[index], plainColour != null
    );
  }

  @Override
  public boolean process(float screenX, float screenY, boolean down,
                         int pointerId, boolean silent) {
    if (screenY < getParent().getHeight() * Globals.TWO_THIRDS) {
      int note = (int) Math.floor(
        (screenX - getWidth() / 2) / getParent().getWidth() * 7
      );
      return processNote(note, down, pointerId);
    }
    return false;
  }

}
