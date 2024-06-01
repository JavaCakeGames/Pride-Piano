package com.javacakegames.pride;

public class WhiteNote extends Note {

  private static final int[] whiteColours = {
    0xfffdd817, 0xffe22016, 0xfff28917,
    0xffefe524, 0xff78b82a, 0xff2c58a4, 0xff6d2380
  };
  private static final float[] whitePitches = {
    0.707106918374981f, 0.789433389319839f, 0.881370416241527f,
    0.931289107250668f, 1.03979104198895f, 1.16096702220402f, 1.29630068053494f
  };

  public WhiteNote(int index, GameView parent, Integer plainColour) {
    super(index, parent, false, plainColour != null ? plainColour : whiteColours[index], whitePitches[index], plainColour != null);
  }

  @Override
  public boolean process(float screenX, float screenY, boolean down, int index, boolean silent) {
    int note = (int) (screenX / getParent().getWidth() * 7);
    return processNote(note, down);
  }

}
