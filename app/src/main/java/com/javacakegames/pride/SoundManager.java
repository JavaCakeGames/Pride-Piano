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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {

  private final CopyOnWriteArrayList<Sound> sounds;
  private final List<Sound> soundsPool;
  private final SoundPool soundPool;

  private final int soundId;
  private final int maxSystemVolume;

  private float startVolume;
  private int streamCounter;

  private final ExecutorService executorService;

  public SoundManager(Context context) {

    int MAX_STREAMS = 24; // 32 is OS limit

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

    executorService = Executors.newCachedThreadPool();

  }

  public int play(float pitch) {

    Sound soundToAdd = null;
    for (Sound sound : soundsPool) {
      if (sound.free) {
        sound.init(startVolume, streamCounter);
        streamCounter++;
        soundToAdd = sound;
        break;
      }
    }
    if (soundToAdd == null) {
      soundToAdd = new Sound(startVolume, streamCounter);
      streamCounter++;
      soundsPool.add(soundToAdd);
    }
    sounds.add(soundToAdd);

    Sound finalSoundToAdd = soundToAdd;
    executorService.submit(() -> {
      finalSoundToAdd.osId = soundPool.play(soundId, startVolume, startVolume, 0, 0, pitch);
    });

    return soundToAdd.myId;
  }

  public void stop(int id) {
    executorService.submit(() -> {
      for (Sound sound : sounds) {
        if (sound.myId == id) {
          sound.fading = true;
        }
      }
    });
  }

  private class SystemVolumeTask extends TimerTask {

    @Override
    public void run() {
      if (Globals.IS_ARC) startVolume = .33333333333333333333f;
      else {
        if (Build.VERSION.SDK_INT >= 5 && !audioMan.isWiredHeadsetOn()) {
          startVolume = Math.min(Globals.TWO_THIRDS, // Louder for internal speaker
            (float) audioMan.getStreamVolume(STREAM_MUSIC) / maxSystemVolume);
        } else {
          startVolume = 1;
        }
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
            soundPool.stop(sound.osId);
            sounds.remove(sound);
            sound.free = true;
          } else {
            soundPool.setVolume(sound.osId, vol, vol);
          }
        }
      }

    }

  }


}
