package com.javacakegames.pride;

public class Sound {

  public float startVolume, currVolume;
  public float volumeStep;
  public int streamId;
  public boolean fading;
  public boolean free;

  public Sound(float startVolume, int streamId) {
    init(startVolume, streamId);
  }

  public void init(float startVolume, int streamId) {
    this.startVolume = this.currVolume = startVolume;
    this.volumeStep = startVolume * 0.04f;
    this.streamId = streamId;
    this.fading = false;
    this.free = false;
  }

}
