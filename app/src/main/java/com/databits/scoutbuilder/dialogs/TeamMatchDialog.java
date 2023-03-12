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
import com.preference.PowerPreference;
import com.preference.Preference;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;

public class TeamMatchDialog extends DialogFragment {
  Bundle bundle;
  Preference preference = PowerPreference.getDefaultFile();

  public interface TeamMatchDialogListener {
    void onTeamMatchDialogPositiveClick(Cell newCell, boolean location, int position, int realId);
  }

  TeamMatchDialogListener listener;

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      listener = (TeamMatchDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(requireActivity()
          + " must implement TeamMatchDialogListener");
    }
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    // Get the layout inflater
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    Balloon.Builder helpBuilder = new Balloon.Builder(requireContext())
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
        .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        .setBalloonAnimation(BalloonAnimation.FADE);

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_teammatch, null);
    int viewId = bundle.getInt("id");
    int realId = bundle.getInt("real_id");
    boolean location = bundle.getBoolean("location");

    String help = getHelp(location, realId);
    helpBuilder.setText(help);

    TextView picker_help = v.findViewById(R.id.popup_help_text);
    ImageButton helpIcon = v.findViewById(R.id.help_button);

    helpIcon.setOnClickListener(v1 -> {
      helpBuilder.build().showAlignBottom(helpIcon);
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
        listener.onTeamMatchDialogPositiveClick(null, bundle.getBoolean("location"), bundle.getInt("id"), realId);
        if (TeamMatchDialog.this.getDialog() != null) {
          TeamMatchDialog.this.getDialog().cancel();
        }
      });
    }

    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
        // Add action buttons
        .setPositiveButton(textSelector(), (dialog, id) -> {
          // Create cell object to be returned to the activity
          String cellType = getString(R.string.TeamMatchType);
          String newHelp = picker_help.getText().toString();
          CellParam cellParam = new CellParam(cellType);
          cellParam.setType(cellType);
          cellParam.setHelpText(newHelp);
          Cell newCell = new Cell(bundle.getInt("id"),"title", cellType, cellParam);

          if (location) {
            preference.putString("top_" + viewId + "_help_value", newHelp);
          } else {
            preference.putString("bot_" + viewId + "_help_value", newHelp);
          }

          listener.onTeamMatchDialogPositiveClick(newCell, location, bundle.getInt("id"), realId);
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (TeamMatchDialog.this.getDialog() != null) {
            TeamMatchDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }

  public String textSelector() {
    return preference.getBoolean("edit_mode") ? getString(R.string.DialogEdit) : getString(R.string.DialogAdd);
  }

  public String getHelp(boolean location, int viewId) {
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
}