package com.javacakegames.pride;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.javacakegames.pride.Globals.audioMan;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoundManager {

  private final ArrayList<Sound> sounds;
  private final List<Sound> soundsPool;
  private final SoundPool soundPool;

  private final int soundId;
  private final int maxSystemVolume;

  private float startVolume;
  private int streamCounter;

  private final Handler soundHandler;

  public SoundManager(Context context) {

    int MAX_STREAMS = 24; // 32 is OS limit

    sounds = new ArrayList<>();
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

    HandlerThread soundThread = new HandlerThread("");
    soundThread.start();
    soundHandler = new Handler(soundThread.getLooper());

    Runnable systemVolumeRunnable = new Runnable() {
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
        soundHandler.postDelayed(this, 1000);
      }
    };

    Runnable stopRunnable = new Runnable() {
      @Override
      public void run() {
        Iterator<Sound> iterator = sounds.iterator();
        while (iterator.hasNext()) {
          Sound sound = iterator.next();
          if (sound.fading) {
            sound.currVolume -= sound.volumeStep;
            float vol = Math.max(0, sound.currVolume);
            if (vol == 0) {
              soundPool.stop(sound.osId);
              iterator.remove();
              sound.free = true;
            } else {
              soundPool.setVolume(sound.osId, vol, vol);
            }
          }
        }
        soundHandler.postDelayed(this, 50);
      }
    };
    soundHandler.post(systemVolumeRunnable);
    soundHandler.post(stopRunnable);
  }

  public int play(float pitch) {
    Sound soundToAdd = null;
    for (Sound sound : soundsPool) {
      if (sound.free) {
        sound.init(startVolume, streamCounter++);
        soundToAdd = sound;
        break;
      }
    }
    if (soundToAdd == null) {
      soundToAdd = new Sound(startVolume, streamCounter++);
      soundsPool.add(soundToAdd);
    }

    Sound finalSoundToAdd = soundToAdd;
    soundHandler.post(() -> {
      sounds.add(finalSoundToAdd);
      finalSoundToAdd.osId =
        soundPool.play(soundId, startVolume, startVolume, 0, 0, pitch);
    });

    return soundToAdd.myId;
  }

  public void stop(int id) {
    soundHandler.post(() -> {
      for (Sound sound : sounds) {
        if (sound.myId == id) {
          sound.fading = true;
          break;
        }
      }
    });
  }

}