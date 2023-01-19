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

public class YesNoDialog extends DialogFragment {
  Bundle bundle;
  Preference preference = PowerPreference.getDefaultFile();

  public interface YesNoDialogListener {
    void onYesNoDialogPositiveClick(Cell newCell, boolean location);
  }

  YesNoDialogListener listener;

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      listener = (YesNoDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(requireActivity()
          + " must implement YesNoDialogListener");
    }
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    // Get the layout inflater
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_yesno, null);

    String title = getTitle(v);

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);

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
        .setPositiveButton(getString(R.string.DialogAdd), (dialog, id) -> {
          // Create cell object to be returned to the activity
          String cellType = "YesNo";
          CellParam cellParam = new CellParam(cellType);
          cellParam.setCellType(cellType);
          Cell newCell = new Cell(bundle.getInt("id"),picker_title.getText().toString(), cellType, cellParam);

          listener.onYesNoDialogPositiveClick(newCell, bundle.getBoolean("location"));
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (YesNoDialog.this.getDialog() != null) {
            YesNoDialog.this.getDialog().cancel();
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