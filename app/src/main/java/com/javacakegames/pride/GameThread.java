package com.javacakegames.pride;

import android.graphics.Canvas;
import android.util.Log;

public class GameThread extends Thread {

  private final GameView view;
  private boolean running = false;

  public GameThread(GameView view) {
    this.view = view;
  }

  public void setRunning(boolean run) {
    running = run;
  }

  @Override
  public void run() {
    while (running) {

      /*long beginTime = System.currentTimeMillis();

      Canvas canvas = null;
      try {
        canvas = android.os.Build.VERSION.SDK_INT >= 26
          ? view.getHolder().lockHardwareCanvas()
          : view.getHolder().lockCanvas();
        synchronized (view.getHolder()) {
          view.draw(canvas);
        }
      } finally {
        if (canvas != null) {
          view.getHolder().unlockCanvasAndPost(canvas);
        }
      }
      /*try {
        Thread.sleep(1); // Workaround - https://stackoverflow.com/q/31779254
      } catch (InterruptedException ignored) {}*/

      //long delta = System.currentTimeMillis() - beginTime;


    }
  }

}
