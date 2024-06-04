package com.javacakegames.pride.notes;

import com.javacakegames.pride.GameView;

public class WhiteNote extends Note {

  private static final int[] WHITE_COLOURS = {
    0xfffdd817, 0xffe22016, 0xfff28917,
    0xffefe524, 0xff78b82a, 0xff2c58a4, 0xff6d2380
  };
  private static final float[] WHITE_PITCHES = {
    0.703571384f, 0.789732158f, 0.886444395f,
    0.939154914f, 1.054165747f, 1.183261152f, 1.328165733f
  };

  public WhiteNote(int index, GameView parent, Integer plainColour) {
    super(
      index, parent, false,
      plainColour != null ? plainColour : WHITE_COLOURS[index],
      WHITE_PITCHES[index], plainColour != null
    );
  }

  @Override
  public boolean process(float screenX, float screenY, boolean down,
                         int pointerId, boolean silent) {
    int note = (int) (screenX / getParent().getWidth() * 7);
    return processNote(note, down, pointerId);
  }

}
