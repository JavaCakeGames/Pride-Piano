package com.javacakegames.pride;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.javacakegames.pride.Globals.audioMan;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class SoundManager {

  private final CopyOnWriteArrayList<Sound> sounds;
  private final List<Sound> soundsPool;
  private final SoundPool soundPool;

  private final int soundId;
  private int cacheSoundId;
  private final int maxSystemVolume;

  private float startVolume;

  public SoundManager(Context context) {

    // On most devices, 21 max streams. 32 is OS limit.
    int MAX_STREAMS = (Globals.supportedFingers << 1) + 1;

    sounds = new CopyOnWriteArrayList<>();
    soundsPool = new ArrayList<>(MAX_STREAMS);

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
        cacheSoundId = soundPool.play(soundId, 0, 0, 1, -1, 1)
      );
    } else {
      while (cacheSoundId == 0) {
        cacheSoundId = soundPool.play(soundId, 0, 0, 0, -1, 1);
      }
    }

  }

  public int play(float pitch) {
    int id = soundPool.play(soundId, startVolume, startVolume, 0, 0, pitch);
    Sound soundToAdd = null;
    for (Sound sound : soundsPool) {
      if (sound.free) {
        sound.init(startVolume, id);
        soundToAdd = sound;
        break;
      }
    }
    if (soundToAdd == null) {
      soundToAdd = new Sound(startVolume, id);
      soundsPool.add(soundToAdd);
    }
    sounds.add(soundToAdd);
    return id;
  }

  public void stop(int id) {
    for (Sound sound : sounds) {
      if (sound.streamId == id) {
        sound.fading = true;
      }
    }
  }

  public void appPaused() {
    soundPool.pause(cacheSoundId);
  }

  public void appResumed() {
    soundPool.resume(cacheSoundId);
  }

  private class SystemVolumeTask extends TimerTask {

    @Override
    public void run() {
      if (Build.VERSION.SDK_INT >= 5 && !audioMan.isWiredHeadsetOn()) {
        startVolume = Math.min(Globals.TWO_THIRDS, // Louder for internal speaker
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
            sound.free = true;
          } else {
            soundPool.setVolume(sound.streamId, vol, vol);
          }
        }
      }

    }

  }


}
