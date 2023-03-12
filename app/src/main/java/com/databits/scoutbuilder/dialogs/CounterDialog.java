package com.databits.scoutbuilder.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.databits.scoutbuilder.R;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.databits.scoutbuilder.Utils;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.travijuu.numberpicker.library.NumberPicker;

public class CounterDialog extends DialogFragment {
  Bundle bundle;

  Preference preference = PowerPreference.getDefaultFile();

  int viewId;
  int realId;
  boolean location;

  int max;
  int min;
  int def_value;
  int unit;

  public interface CounterDialogListener {
    void onCounterDialogPositiveClick(Cell newCell, boolean location, int position, int realId);
  }

  CounterDialogListener listener;

  Utils utils;

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      listener = (CounterDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(requireActivity()
          + " must implement CounterDialogListener");
    }
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    // Get the layout inflater
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_counter, null);

    utils = new Utils(requireContext());

    String title = utils.getTitle(location, realId, bundle);
    String help = utils.getHelp(location, realId, bundle);

    Balloon.Builder helpBuilder = utils.helpBuilder();
    helpBuilder.setText(help);

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);
    NumberPicker picker_min = v.findViewById(R.id.number_counter_min);
    NumberPicker picker_max = v.findViewById(R.id.number_counter_max);
    NumberPicker picker_default = v.findViewById(R.id.number_counter_default);
    NumberPicker picker_example = v.findViewById(R.id.number_counter_example);
    TextView picker_help = v.findViewById(R.id.popup_help_text);
    TextView popup_warning = v.findViewById(R.id.popupWarning);
    ImageButton helpIcon = v.findViewById(R.id.help_button);

    viewId = bundle.getInt("id");
    realId = bundle.getInt("real_id");
    location = bundle.getBoolean("location");

    if (location) {
      max = preference.getInt("top_" + viewId + "_max_picker_value");
      min = preference.getInt("top_" + viewId + "_min_picker_value");
      def_value = preference.getInt("top_" + viewId + "_default_picker_value", min);
      unit = preference.getInt("top_" + viewId + "_unit_picker_value", 1);
    } else {
      max = preference.getInt("bot_" + viewId + "_max_picker_value");
      min = preference.getInt("bot_" + viewId + "_min_picker_value");
      def_value = preference.getInt("bot_" + viewId + "_default_picker_value", min);
      unit = preference.getInt("bot_" + viewId + "_unit_picker_value", 1);
    }

    picker_example.setMin(min);
    picker_example.setMax(max);
    picker_example.setValue(def_value);
    picker_example.setUnit(unit);
    picker_min.setValue(min);
    picker_max.setValue(max);
    picker_default.setValue(def_value);

    if (min == picker_max.getValue()) {
      popup_warning.setText(R.string.CounterMinEquals);
    } else if (min >= picker_max.getValue()) {
      popup_warning.setText(R.string.CounterMinGreaterEqual);
    } else {
      popup_warning.setText("");
    }

    if (max == picker_min.getValue()) {
      popup_warning.setText(R.string.CounterMaxEquals);
    } else if (max <= picker_min.getValue()) {
      popup_warning.setText(R.string.CounterMaxLessEqual);
    } else {
      popup_warning.setText("");
    }

    picker_default.setValueChangedListener((value, action) -> {
      if (location) {
        max = preference.getInt("top_" + viewId + "_max_picker_value");
        min = preference.getInt("top_" + viewId + "_min_picker_value");
        preference.setInt("top_" + viewId + "_default_picker_value", value);
      } else {
        max = preference.getInt("bot_" + viewId + "_max_picker_value");
        min = preference.getInt("bot_" + viewId + "_min_picker_value");
        preference.setInt("bot_" + viewId + "_default_picker_value", value);
      }
      picker_example.setMin(value);
      picker_example.setValue(value);
      if (min > value) {
        popup_warning.setText(R.string.CounterDefGreater);
      } else if (max < value) {
        popup_warning.setText(R.string.CounterDefLess);
      } else {
        popup_warning.setText("");
      }
    });

    picker_min.setValueChangedListener((value, action) -> {
      if (location) {
        max = preference.getInt("top_" + viewId + "_max_picker_value");
        min = preference.getInt("top_" + viewId + "_min_picker_value");
        def_value = preference.getInt("top_" + viewId + "_default_picker_value");
        preference.setInt("top_" + viewId + "_min_picker_value", value);
      } else {
        max = preference.getInt("bot_" + viewId + "_max_picker_value");
        min = preference.getInt("bot_" + viewId + "_min_picker_value");
        def_value = preference.getInt("top_" + viewId + "_default_picker_value");
        preference.setInt("bot_" + viewId + "_min_picker_value", value);
      }

      picker_example.setMin(value);
      picker_example.setValue(value);
      if (max == value) {
        popup_warning.setText(R.string.CounterMaxEquals);
      } else if (max <= value) {
        popup_warning.setText(R.string.CounterMinLessEquals);
      } else if (value < def_value) {
        popup_warning.setText(R.string.CounterDefLess);
      } else {
        popup_warning.setText("");
      }
    });

    picker_max.setValueChangedListener((value, action) -> {
      if (location) {
        max = preference.getInt("top_" + viewId + "_max_picker_value");
        min = preference.getInt("top_" + viewId + "_min_picker_value");
        def_value = preference.getInt("top_" + viewId + "_default_picker_value");
        preference.setInt("top_" + viewId + "_max_picker_value", value);
      } else {
        max = preference.getInt("bot_" + viewId + "_max_picker_value");
        min = preference.getInt("bot_" + viewId + "_min_picker_value");
        def_value = preference.getInt("top_" + viewId + "_default_picker_value");
        preference.setInt("bot_" + viewId + "_max_picker_value", value);
      }


      picker_example.setMax(value);
      picker_example.setValue(value);
      if (min == value) {
        popup_warning.setText(R.string.CounterMaxEquals);
      } else if (min >= value) {
        popup_warning.setText(R.string.CounterMinGreaterEqual);
      } else {
        popup_warning.setText("");
      }
    });

    // Set default title to both TextViews
    picker_title.setText(title);
    exampleTitle.setText(title);

    // Automatically bring up the keyboard
    picker_title.requestFocus();

    helpIcon.setOnClickListener(v1 -> helpBuilder.build().showAlignBottom(helpIcon));

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

    AppCompatButton deleteButton = v.findViewById(R.id.popup_delete_button);
    if (preference.getBoolean("edit_mode")) {
      deleteButton.setVisibility(View.VISIBLE);
      deleteButton.setOnClickListener(v1 -> {
        listener.onCounterDialogPositiveClick(null, location, viewId, realId);
        if (CounterDialog.this.getDialog() != null) {
          CounterDialog.this.getDialog().cancel();
        }
      });
    }

    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
        // Add action buttons
        .setPositiveButton(utils.textSelector(), (dialog, id) -> {
          // Create cell object to be returned to the activity
          String cellType = getString(R.string.CounterType);
          CellParam cellParam = new CellParam(cellType);
          cellParam.setType(cellType);
          String cellTitle = picker_title.getText().toString();
          int new_max = picker_max.getValue();
          int new_min = picker_min.getValue();
          int default_value = picker_default.getValue();
          int unit_value = 1;
          String newHelp = picker_help.getText().toString();

          cellParam.setDefault(default_value);
          cellParam.setMax(new_max);
          cellParam.setMin(new_min);
          cellParam.setUnit(unit_value);
          if (!newHelp.isEmpty()) {
            cellParam.setHelpText(newHelp);
          } else {
            cellParam.setHelpText("Default");
          }

          Cell newCell = new Cell(viewId,cellTitle, cellType, cellParam);

          if (location) {
            preference.setInt("top_" + viewId + "_max_picker_value", new_max);
            preference.setInt("top_" + viewId + "_min_picker_value", new_min);
            preference.setInt("top_" + viewId + "_default_picker_value", default_value);
            preference.setInt("top_" + viewId + "_unit_picker_value", unit_value);
            preference.setInt("top_" + viewId + "_unit_picker_value", unit_value);
            preference.setString("top_" + viewId + "_title_value", cellTitle);
            preference.putString("top_" + viewId + "_help_value", newHelp);
          } else {
            preference.setInt("bot_" + viewId + "_max_picker_value", new_max);
            preference.setInt("bot_" + viewId + "_min_picker_value", new_min);
            preference.setInt("bot_" + viewId + "_default_picker_value", default_value);
            preference.setInt("bot_" + viewId + "_unit_picker_value", unit_value);
            preference.setInt("bot_" + viewId + "_unit_picker_value", unit_value);
            preference.setString("bot_" + viewId + "_title_value", cellTitle);
            preference.putString("bot_" + viewId + "_help_value", newHelp);
          }

          listener.onCounterDialogPositiveClick(newCell, location, viewId, realId);
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (CounterDialog.this.getDialog() != null) {
            CounterDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }
}