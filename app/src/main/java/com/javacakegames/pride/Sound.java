package com.javacakegames.pride;

public class Sound {

  public float startVolume, currVolume;
  public float volumeStep;
  public int streamId;
  public boolean fading;

  public Sound(float startVolume, int streamId) {
    this.startVolume = this.currVolume = startVolume;
    this.volumeStep = startVolume * 0.04f;
    this.streamId = streamId;
  }

}
