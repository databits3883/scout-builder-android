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
import androidx.fragment.app.DialogFragment;
import com.databits.scoutbuilder.R;
import com.databits.scoutbuilder.Utils;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.google.android.material.textfield.TextInputLayout;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.skydoves.balloon.Balloon;
import java.util.concurrent.atomic.AtomicReference;

public class TextboxDialog extends DialogFragment {
  Bundle bundle;

  int viewId;
  int realId;
  boolean location;

  Preference preference = PowerPreference.getDefaultFile();

  public interface TextboxDialogListener {
    void onTextboxDialogPositiveClick(Cell newCell, boolean location, int viewId, int realId);
  }

  TextboxDialogListener listener;

  Utils utils;

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

    utils = new Utils(requireContext());

    viewId = bundle.getInt("id");
    realId = bundle.getInt("real_id");
    location = bundle.getBoolean("location");

    String title = utils.getTitle(location, realId, bundle);
    String help = utils.getHelp(location, realId, bundle);

    Balloon.Builder helpBuilder = utils.helpBuilder();
    helpBuilder.setText(help);

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);
    TextView picker_help = v.findViewById(R.id.popup_help_text);

    ImageButton helpIcon = v.findViewById(R.id.help_button);

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


    helpIcon.setOnClickListener(v1 -> helpBuilder.build().showAlignBottom(helpIcon));


    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
        // Add action buttons
        .setPositiveButton(utils.textSelector(), (dialog, id) -> {
          // Create cell object to be returned to the activity
          String cellType = getString(R.string.TextType);
          CellParam cellParam = new CellParam(cellType);
          cellParam.setType(cellType);
          String cellTitle = picker_title.getText().toString();

          String newTitle = picker_title.getText().toString();
          String newHelp = picker_help.getText().toString();
          if (!newTitle.isEmpty()) {
            preference.putString(viewId + "_title_value", title);
          }
          if (!newHelp.isEmpty()) {
            preference.putString(viewId + "_help_value", newHelp);
            cellParam.setHelpText(newHelp);
          } else {
            cellParam.setHelpText("Default");
          }

          preference.setString(1 + "_title_value", cellTitle);
          Cell newCell = new Cell(viewId,picker_title.getText().toString(), cellType, cellParam);

          listener.onTextboxDialogPositiveClick(newCell, location, viewId, realId);
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (TextboxDialog.this.getDialog() != null) {
            TextboxDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }
}