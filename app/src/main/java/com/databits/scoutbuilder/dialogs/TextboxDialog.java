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
import com.google.android.material.textfield.TextInputLayout;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.util.concurrent.atomic.AtomicReference;

public class TextboxDialog extends DialogFragment {
  Bundle bundle;

  Preference preference = PowerPreference.getDefaultFile();

  AtomicReference<TextInputLayout> popup_hint_example = new AtomicReference<>();


  public interface TextboxDialogListener {
    void onTextboxDialogPositiveClick(Cell newCell, boolean location);
  }

  TextboxDialogListener listener;

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      listener = (TextboxDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(requireActivity()
          + " must implement TextboxDialogListener");
    }
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    // Get the layout inflater
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_textbox, null);

    String title = getTitle(v);

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);

    TextView picker_hint;

    int viewId = bundle.getInt("id");

    // Sets the saved title and hint text
    popup_hint_example.set(v.findViewById(R.id.textbox_text_layout));
    picker_hint = v.findViewById(R.id.popup_title_hint_text);
    popup_hint_example.get().setHint(preference.getString(viewId + "_hint_value"));

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
        if (s.length() >= 0) {
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
          String cellType = getString(R.string.TextType);
          CellParam cellParam = new CellParam(cellType);
          cellParam.setCellType(cellType);
          String cellTitle = picker_title.getText().toString();

          String newTitle = picker_title.getText().toString();
          String hint = picker_hint.getText().toString();
          if (!newTitle.isEmpty()) {
            preference.putString(viewId + "_title_value", title);
          }
          if (!hint.isEmpty()) {
            preference.putString(viewId + "_hint_value", hint);

            cellParam.setCellTextHint(hint);
          }

          preference.setString(1 + "_title_value", cellTitle);
          Cell newCell = new Cell(viewId,picker_title.getText().toString(), cellType, cellParam);

          listener.onTextboxDialogPositiveClick(newCell, bundle.getBoolean("location"));
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (TextboxDialog.this.getDialog() != null) {
            TextboxDialog.this.getDialog().cancel();
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