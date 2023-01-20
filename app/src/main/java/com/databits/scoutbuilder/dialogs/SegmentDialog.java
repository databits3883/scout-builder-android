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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.addisonelliott.segmentedbutton.SegmentedButton;
import com.databits.scoutbuilder.R;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.travijuu.numberpicker.library.NumberPicker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SegmentDialog extends DialogFragment {
  Bundle bundle;

  Preference preference = PowerPreference.getDefaultFile();


  public interface SegmentDialogListener {
    void onSegmentDialogPositiveClick(Cell newCell, boolean location);
  }

  SegmentDialogListener listener;

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

    String title = getTitle(v);

    int viewId = bundle.getInt("id");

    AtomicInteger segment_count =
        new AtomicInteger(preference.getInt(viewId + "_segment_count", 2));

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);

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
      preference.setInt(viewId + "_segment_count", value);

      segment_count.set(value);

      // Sets visibility after user changes the segment count
      for (int i = 0; i < buttons.length; i++) {
        buttons[i].setVisibility(i < value ? View.VISIBLE : View.GONE);
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
        segment_texts[i].setVisibility(i < segment_count.get() ? View.VISIBLE : View.GONE);
      }

      builder.setTitle(R.string.SegmentTextTitle);
      builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

      builder.setView(dialogView);
      builder.setPositiveButton(R.string.set, (dialog, which) -> {
        for (int i = 0; i < segment_count.get(); i++) {
          String enteredText = segment_texts[i].getText().toString();
          segment_labels.add(enteredText);
          buttons[i].setText(enteredText);
        }
        dialog.dismiss();
      });
      AlertDialog dialog = builder.create();
      dialog.show();
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
          String cellType = getString(R.string.SegmentType);
          CellParam cellParam = new CellParam(cellType);
          cellParam.setCellType(cellType);
          String cellTitle = picker_title.getText().toString();

          cellParam.setCellSegments(segment_count.get());
          cellParam.setCellSegmentLabels(segment_labels);

          preference.setString(1 + "_title_value", cellTitle);
          Cell newCell = new Cell(viewId,picker_title.getText().toString(), cellType, cellParam);

          listener.onSegmentDialogPositiveClick(newCell, bundle.getBoolean("location"));
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (SegmentDialog.this.getDialog() != null) {
            SegmentDialog.this.getDialog().cancel();
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