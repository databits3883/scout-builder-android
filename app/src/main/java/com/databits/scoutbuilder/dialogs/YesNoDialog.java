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
import com.databits.scoutbuilder.Utils;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

public class YesNoDialog extends DialogFragment {
  Bundle bundle;
  Preference preference = PowerPreference.getDefaultFile();

  int viewId;
  int realId;
  boolean location;

  public interface YesNoDialogListener {
    void onYesNoDialogPositiveClick(Cell newCell, boolean location, int position, int realId);
  }

  YesNoDialogListener listener;

  Utils utils;

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

    utils = new Utils(requireContext());

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_yesno, null);
    viewId = bundle.getInt("id");
    realId = bundle.getInt("real_id");
    location = bundle.getBoolean("location");

    String title = utils.getTitle(location, realId, bundle);
    String help = utils.getHelp(location, realId, bundle);

    Balloon.Builder helpBuilder = utils.helpBuilder();
    helpBuilder.setText(help);

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView picker_help = v.findViewById(R.id.popup_help_text);
    ImageButton helpIcon = v.findViewById(R.id.help_button);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);

    // Set default title to both TextViews
    picker_title.setText(title);
    exampleTitle.setText(title);

    // Automatically bring up the keyboard
    picker_title.requestFocus();

    helpIcon.setOnClickListener(v1 -> helpBuilder.build().showAlignBottom(helpIcon));

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

    AppCompatButton deleteButton = v.findViewById(R.id.popup_delete_button);
    if (preference.getBoolean("edit_mode")) {
      deleteButton.setVisibility(View.VISIBLE);
      deleteButton.setOnClickListener(v1 -> {
        listener.onYesNoDialogPositiveClick(null, location, viewId, realId);
        if (YesNoDialog.this.getDialog() != null) {
          YesNoDialog.this.getDialog().cancel();
        }
      });
    }

    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
        // Add action buttons
        .setPositiveButton(utils.textSelector(), (dialog, id) -> {
          // Create cell object to be returned to the activity
          String cellType = getString(R.string.YesNoType);
          String newTitle = picker_title.getText().toString();
          String newHelp = picker_help.getText().toString();
          CellParam cellParam = new CellParam(cellType);
          cellParam.setType(cellType);
          cellParam.setHelpText(newHelp);
          Cell newCell = new Cell(viewId, newTitle, cellType, cellParam);

          if (location) {
            preference.putString("top_" + viewId + "_title_value", newTitle);
            preference.putString("top_" + viewId + "_help_value", newHelp);
          } else {
            preference.putString("bot_" + viewId + "_title_value", newTitle);
            preference.putString("bot_" + viewId + "_help_value", newHelp);
          }

          listener.onYesNoDialogPositiveClick(newCell, location, viewId, realId);
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (YesNoDialog.this.getDialog() != null) {
            YesNoDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }


}