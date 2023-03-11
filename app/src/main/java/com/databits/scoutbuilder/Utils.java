package com.databits.scoutbuilder;

import android.content.Context;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

public class Utils {

  Context context;

  public Utils(Context context) {
    this.context = context;
  }

  Preference preference = PowerPreference.getDefaultFile();

  public String getTitle(boolean location, int viewId, Bundle bundle) {
    if (!preference.getBoolean("edit_mode")) {
      return bundle.getString("title");
    } else {
      if (location) {
        return preference.getString("top_" + viewId + "_title_value");
      } else {
        return preference.getString("bot_" + viewId + "_title_value");
      }
    }
  }

  public String getHelp(boolean location, int viewId, Bundle bundle) {
    if (!preference.getBoolean("edit_mode")) {
      return bundle.getString("help");
    } else {
      if (location) {
        return preference.getString("top_" + viewId + "_help_value");
      } else {
        return preference.getString("bot_" + viewId + "_help_value");
      }
    }
  }

  public String textSelector() {
    return preference.getBoolean("edit_mode") ? context.getString(R.string.DialogEdit)
        : context.getString(R.string.DialogAdd);
  }

  public Balloon.Builder helpBuilder() {
    return new Balloon.Builder(context)
        .setArrowSize(10)
        .setArrowOrientation(ArrowOrientation.TOP)
        .setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
        .setArrowPosition(0.5f)
        .setWidth(BalloonSizeSpec.WRAP)
        .setHeight(BalloonSizeSpec.WRAP)
        .setPadding(6)
        .setTextSize(20f)
        .setCornerRadius(4f)
        .setAlpha(0.8f)
        .setTextColor(ContextCompat.getColor(context, R.color.white))
        .setBalloonAnimation(BalloonAnimation.FADE);
  }
}
