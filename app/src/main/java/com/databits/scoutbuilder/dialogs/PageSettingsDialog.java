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

public class PageSettingsDialog extends DialogFragment {
  Bundle bundle;
  Preference preference = PowerPreference.getDefaultFile();

  public interface PageSettingsDialogListener {
    void onPageSettingsDialogPositiveClick(Cell newCell, boolean location);
  }

  PageSettingsDialogListener listener;

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      listener = (PageSettingsDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(requireActivity()
          + " must implement PageSettingsDialogListener");
    }
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    // Get the layout inflater
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_page, null);


    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
        // Add action buttons
        .setPositiveButton(getString(R.string.DialogAdd), (dialog, id) -> {


          //listener.onPageSettingsDialogPositiveClick(newCell, bundle.getBoolean("location"));
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (PageSettingsDialog.this.getDialog() != null) {
            PageSettingsDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }

}