package com.javacakegames.pride;

public class Sound {

  public float startVolume, currVolume;
  public float volumeStep;
  public int myId, osId;
  public boolean fading;
  public boolean free;

  public Sound(float startVolume) {
    init(startVolume);
  }

  public void init(float startVolume) {
    this.startVolume = this.currVolume = startVolume;
    this.volumeStep = startVolume * 0.04f;
    this.myId = Globals.RNG.nextInt();
    this.fading = false;
    this.free = false;
  }

}
