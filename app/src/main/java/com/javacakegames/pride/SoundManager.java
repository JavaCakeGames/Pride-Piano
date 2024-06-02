package com.javacakegames.pride;

import static android.media.AudioManager.STREAM_MUSIC;

import static com.javacakegames.pride.Globals.audioMan;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class SoundManager {

  private final CopyOnWriteArrayList<Sound> sounds;
  private final SoundPool soundPool;

  private final int soundId;
  private final int maxSystemVolume;

  private float startVolume;

  public SoundManager(Context context) {

    // On most devices, 21 max streams. 32 is OS limit.
    int MAX_STREAMS = (Globals.supportedFingers << 1) + 1;

    sounds = new CopyOnWriteArrayList<>();

    this.maxSystemVolume = audioMan.getStreamMaxVolume(STREAM_MUSIC);

    if (Build.VERSION.SDK_INT >= 21) {
      soundPool = new SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
          new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ).build();
    } else {
      soundPool = new SoundPool(MAX_STREAMS, STREAM_MUSIC, 0);
    }
    soundId = soundPool.load(context, R.raw.gb4, 1);

    Globals.TIMER.schedule(new SystemVolumeTask(), 0, 1000);
    Globals.TIMER.schedule(new StopTask(), 0, 50);

    // Keep a silent note repeating in background to avoid lag spikes
    if (Build.VERSION.SDK_INT >= 8) {
      soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) ->
        soundPool.play(soundId, 0, 0, 1, -1, 1)
      );
    } else {
      int id = 0;
      while (id == 0) {
        id = soundPool.play(soundId, 0, 0, 0, -1, 1);
      }
    }

  }

  public int play(float pitch) {
    int id = soundPool.play(soundId, startVolume, startVolume, 0, 0, pitch);
    sounds.add(new Sound(startVolume, id)); // todo use pool, not new
    return id;
  }

  public void stop(int id) {
    for (Sound sound : sounds) {
      if (sound.streamId == id) {
        sound.fading = true;
      }
    }
  }

  private class SystemVolumeTask extends TimerTask {

    @Override
    public void run() {
      if (Build.VERSION.SDK_INT >= 5 && !audioMan.isWiredHeadsetOn()) {
        startVolume = Math.min(2 / 3f, // Louder for internal speaker
          (float) audioMan.getStreamVolume(STREAM_MUSIC) / maxSystemVolume);
      } else {
        startVolume = 1;
      }
    }

  }

  private class StopTask extends TimerTask {

    @Override
    public void run() {

      for (Sound sound : sounds) {
        if (sound.fading) {
          sound.currVolume -= sound.volumeStep;
          float vol = Math.max(0, sound.currVolume);
          if (vol == 0) {
            soundPool.stop(sound.streamId);
            sounds.remove(sound);
          } else {
            soundPool.setVolume(sound.streamId, vol, vol);
          }
        }
      }

    }

  }


}
