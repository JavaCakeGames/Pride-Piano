package com.javacakegames.pride.activities;

import android.app.ActivityManager;
import android.graphics.Color;
import android.os.Build;

import com.javacakegames.pride.notes.WhiteNote;

import java.util.Calendar;

public class PlainActivity extends PrideActivity {

  @Override
  boolean isPlain() {
    return true;
  }

  @Override
  int getRecentsColour() {
    return Calendar.getInstance().get(Calendar.DAY_OF_YEAR & 1) == 1
      ? Color.BLACK : Color.WHITE;
  }

}
