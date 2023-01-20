package com.databits.scoutbuilder.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.databits.scoutbuilder.R;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.travijuu.numberpicker.library.NumberPicker;

public class CounterDialog extends DialogFragment {
  Bundle bundle;

  Preference preference = PowerPreference.getDefaultFile();


  public interface CounterDialogListener {
    void onCounterDialogPositiveClick(Cell newCell, boolean location);
  }

  CounterDialogListener listener;

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

    String title = getTitle(v);

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);
    NumberPicker picker_min = v.findViewById(R.id.number_counter_min);
    NumberPicker picker_max = v.findViewById(R.id.number_counter_max);
    NumberPicker picker_default = v.findViewById(R.id.number_counter_default);
    NumberPicker picker_example = v.findViewById(R.id.number_counter_example);
    TextView popup_warning = v.findViewById(R.id.popupWarning);

    int viewId = bundle.getInt("id");

    int max = preference.getInt(viewId + "_max_picker_value", 0);
    int min = preference.getInt(viewId + "_min_picker_value", 0);
    int def_value = preference.getInt(viewId + "_default_picker_value", min);
    int unit = preference.getInt(viewId + "_unit_picker_value", 1);

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
      int max_value = preference.getInt(viewId + "_max_picker_value");
      int min_value = preference.getInt(viewId + "_min_picker_value");

      picker_example.setMin(value);
      picker_example.setValue(value);
      preference.setInt(viewId + "_default_picker_value", value);
      if (min_value > value) {
        popup_warning.setText(R.string.CounterDefGreater);
      } else if (max_value < value) {
        popup_warning.setText(R.string.CounterDefLess);
      } else {
        popup_warning.setText("");
      }
    });

    picker_min.setValueChangedListener((value, action) -> {
      int max_value = preference.getInt(viewId + "_max_picker_value");

      picker_example.setMin(value);
      picker_example.setValue(value);
      preference.setInt(viewId + "_min_picker_value", value);
      if (max_value == value) {
        popup_warning.setText(R.string.CounterMaxEquals);
      } else if (max_value <= value) {
        popup_warning.setText(R.string.CounterMinLessEquals);
      } else if (value < preference.getInt(viewId + "_default_picker_value")) {
        popup_warning.setText(R.string.CounterDefLess);
      } else {
        popup_warning.setText("");
      }
    });

    picker_max.setValueChangedListener((value, action) -> {
      int min_value = preference.getInt(viewId + "_min_picker_value");

      picker_example.setMax(value);
      picker_example.setValue(value);
      preference.setInt(viewId + "_max_picker_value", value);
      if (min_value == value) {
        popup_warning.setText(R.string.CounterMaxEquals);
      } else if (min_value >= value) {
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

    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
        // Add action buttons
        .setPositiveButton(R.string.DialogAdd, (dialog, id) -> {
          // Create cell object to be returned to the activity
          String cellType = getString(R.string.counter);
          CellParam cellParam = new CellParam(cellType);
          cellParam.setCellType(cellType);
          String cellTitle = picker_title.getText().toString();
          int new_max = picker_max.getValue();
          int new_min = picker_min.getValue();
          int default_value = picker_default.getValue();
          int unit_value = 1;

          cellParam.setCellDefault(default_value);
          cellParam.setCellMax(new_max);
          cellParam.setCellMin(new_min);
          cellParam.setCellUnit(unit_value);
          preference.setInt(viewId + "_max_picker_value", new_max);
          preference.setInt(viewId + "_min_picker_value", new_min);
          preference.setInt(viewId + "_default_picker_value", default_value);
          preference.setInt(viewId + "_unit_picker_value", unit_value);
          preference.setString(1 + "_title_value", cellTitle);
          Cell newCell = new Cell(viewId,picker_title.getText().toString(), cellType, cellParam);

          listener.onCounterDialogPositiveClick(newCell, bundle.getBoolean("location"));
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (CounterDialog.this.getDialog() != null) {
            CounterDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }

  public String getTitle(View v) {
    TextView picker_title = v.findViewById(R.id.popup_title_text);
    String savedTitle = bundle.getString("title");
    return savedTitle;
  }
}