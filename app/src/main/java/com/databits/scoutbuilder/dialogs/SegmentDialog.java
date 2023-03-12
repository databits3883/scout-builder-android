package com.databits.scoutbuilder.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.addisonelliott.segmentedbutton.SegmentedButton;
import com.databits.scoutbuilder.R;
import com.databits.scoutbuilder.Utils;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.skydoves.balloon.Balloon;
import com.travijuu.numberpicker.library.NumberPicker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SegmentDialog extends DialogFragment {
  Bundle bundle;

  int viewId;
  int realId;
  boolean location;

  Preference preference = PowerPreference.getDefaultFile();

  public interface SegmentDialogListener {
    void onSegmentDialogPositiveClick(Cell newCell, boolean location, int viewId, int realId);
  }

  SegmentDialogListener listener;

  Utils utils;

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      listener = (SegmentDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(requireActivity()
          + " must implement SegmentDialogListener");
    }
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    // Get the layout inflater
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_segment, null);

    utils = new Utils(requireContext());

    String title = utils.getTitle(location, realId, bundle);
    String help = utils.getHelp(location, realId, bundle);

    viewId = bundle.getInt("id");
    realId = bundle.getInt("real_id");
    location = bundle.getBoolean("location");

    Balloon.Builder helpBuilder = utils.helpBuilder();
    helpBuilder.setText(help);


    AtomicInteger segment_count;
    if (location) {
      segment_count =
          new AtomicInteger(preference.getInt("top_" + viewId + "_segment_count", 2));
    } else {
      segment_count =
          new AtomicInteger(preference.getInt("bot_" + viewId + "_segment_count", 2));
    }

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);
    TextView picker_help = v.findViewById(R.id.popup_help_text);

    ImageButton helpIcon = v.findViewById(R.id.help_button);

    TextView popup_warning = v.findViewById(R.id.popupWarning);
    popup_warning.setText(R.string.SegmentWarning);

    NumberPicker picker_segment_count = v.findViewById(R.id.segment_count_picker);

    SegmentedButton[] buttons = new SegmentedButton[]{
        v.findViewById(R.id.button_one),
        v.findViewById(R.id.button_two),
        v.findViewById(R.id.button_three),
        v.findViewById(R.id.button_four),
        v.findViewById(R.id.button_five),
        v.findViewById(R.id.button_six)
    };

    picker_segment_count.setValue(segment_count.get());

    // Sets visibility based on stored value
    for (int i = 0; i < buttons.length; i++) {
      buttons[i].setVisibility(i < segment_count.get() ? View.VISIBLE : View.GONE);
    }

    picker_segment_count.setValueChangedListener((value, action) -> {
      if (location) {
        preference.setInt("top_" + viewId + "_segment_count", value);
      } else {
        preference.setInt("bot_" + viewId + "_segment_count", value);
      }

      segment_count.set(value);

      // Sets visibility after user changes the segment count
      for (int i = 0; i < buttons.length; i++) {
        buttons[i].setVisibility(i < segment_count.get() ? View.VISIBLE : View.GONE);
        String saved;
        if (location) {
          saved = preference.getString("top_" + viewId + "_segment_text_" + i,
              String.valueOf((i + 1)));
        } else {
          saved = preference.getString("bot_" +  viewId + "_segment_text_" + i,
              String.valueOf((i + 1)));
        }
        buttons[i].setText(saved);
      }
    });

    List<String> segment_labels = new ArrayList<>();

    Button popup_text_button = v.findViewById(R.id.popup_text_button);
    popup_text_button.setOnClickListener(v2 -> {
      View dialogView = getLayoutInflater().inflate(R.layout.ui_segment_text, null);

      EditText[] segment_texts = new EditText[]{
          dialogView.findViewById(R.id.editText1),
          dialogView.findViewById(R.id.editText2),
          dialogView.findViewById(R.id.editText3),
          dialogView.findViewById(R.id.editText4),
          dialogView.findViewById(R.id.editText5),
          dialogView.findViewById(R.id.editText6)
      };

      for (int i = 0; i < segment_texts.length; i++) {
        String saved;
        segment_texts[i].setVisibility(i < segment_count.get() ? View.VISIBLE : View.GONE);
        if (location) {
          saved = preference.getString("top_" + viewId + "_segment_text_" + i,
              String.valueOf((i + 1)));
        } else {
          saved = preference.getString("bot_" +  viewId + "_segment_text_" + i,
              String.valueOf((i + 1)));
        }
        segment_texts[i].setText(saved);
      }

      builder.setTitle(R.string.SegmentTextTitle);
      builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

      builder.setView(dialogView);
      builder.setPositiveButton(R.string.set, (dialog, which) -> {
        for (int i = 0; i < segment_count.get(); i++) {
          String enteredText = segment_texts[i].getText().toString();
          segment_labels.add(enteredText);
          if (location) {
            preference.setString("top_" + viewId + "_segment_text_" + i, enteredText);
          } else {
            preference.setString("bot_" + viewId + "_segment_text_" + i, enteredText);
          }

          buttons[i].setText(enteredText);
        }
        dialog.dismiss();
      });
      AlertDialog dialog = builder.create();
      dialog.show();

      helpIcon.setOnClickListener(v1 -> helpBuilder.build().showAlignBottom(helpIcon));
    });


    // Set default title to both TextViews
    picker_title.setText(title);
    exampleTitle.setText(title);

    // Automatically bring up the keyboard
    picker_title.requestFocus();

    // Set the title of the example to the title of the picker as it types
    picker_title.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
          exampleTitle.setText(s);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    picker_help.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
          helpBuilder.setText(s.toString());
        }
      }
    });

    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
        // Add action buttons
        .setPositiveButton(utils.textSelector(), (dialog, id) -> {
          // Create cell object to be returned to the activity
          String cellType = getString(R.string.SegmentType);
          CellParam cellParam = new CellParam(cellType);
          cellParam.setType(cellType);
          String cellTitle = picker_title.getText().toString();

          cellParam.setSegments(segment_count.get());

          if (segment_labels.size() == 0) {
            // 1 because the user shouldn't see segment 0
            for (int i = 1; i < segment_count.get(); i++) {
              segment_labels.add(String.valueOf(i));
            }
          }
          cellParam.setSegmentLabels(segment_labels);

          if (location) {
            preference.setString("top_" + viewId + "_title_value", cellTitle);
          } else {
            preference.setString("bot_" + viewId + "_title_value", cellTitle);
          }

          Cell newCell = new Cell(viewId,cellTitle, cellType, cellParam);

          listener.onSegmentDialogPositiveClick(newCell, location, viewId, realId);
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (SegmentDialog.this.getDialog() != null) {
            SegmentDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }
}