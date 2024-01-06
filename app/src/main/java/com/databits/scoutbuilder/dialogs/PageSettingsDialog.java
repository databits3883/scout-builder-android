package com.databits.scoutbuilder.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import com.databits.scoutbuilder.R;
import com.preference.PowerPreference;
import com.preference.Preference;

public class PageSettingsDialog extends DialogFragment {
  private static final int NONE = 0;
  private static final int AUTO = 1;
  private static final int TELEOP = 2;
  private static final int BOTH = 3;
  private static final boolean CROWD = true;
  private static final boolean PIT = false;
  Bundle bundle;
  Preference preference = PowerPreference.getDefaultFile();
  PageSettingsDialogListener listener;
  String[] curTables = { "Auto", "Teleop" };
  AlertDialog.Builder builder;
  private boolean[] checkedTables;
  private int table_status;

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
    builder = new AlertDialog.Builder(requireActivity());
    // Get the layout inflater
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_page, null);

    SwitchCompat columnSwitch = v.findViewById(R.id.column_switch);
    columnSwitch.setChecked(preference.getBoolean("grid", true));

    columnSwitch.setOnClickListener(v1 -> {
      if (columnSwitch.isChecked()) {
        columnSwitch.setText(R.string.two_column);
      } else {
        columnSwitch.setText(R.string.one_column);
      }
    });

    SwitchCompat tablesSwitch = v.findViewById(R.id.tables_switch);

    table_status = preference.getInt("table_status", NONE);

    // set table status based on radio buttons
    SwitchCompat autoSwitch = v.findViewById(R.id.auto_button);
    SwitchCompat teleopSwitch = v.findViewById(R.id.teleop_button);

    tablesSwitch.setChecked(preference.getBoolean("table_status") /*&& table_status != NONE*/);
    tablesSwitch.setOnClickListener(v1 -> {
      if (tablesSwitch.isChecked()) {
        tablesSwitch.setText(R.string.tables_enabled);
        autoSwitch.setVisibility(View.VISIBLE);
        teleopSwitch.setVisibility(View.VISIBLE);
        autoSwitch.setChecked(true);
        teleopSwitch.setChecked(true);
      } else {
        tablesSwitch.setText(R.string.tables_disabled);
        autoSwitch.setChecked(false);
        teleopSwitch.setChecked(false);
        autoSwitch.setVisibility(View.GONE);
        teleopSwitch.setVisibility(View.GONE);
        preference.putInt("table_status", table_status = NONE);
      }
    });

    if (tablesSwitch.isChecked()) {
      tablesSwitch.setText(R.string.tables_enabled);
      autoSwitch.setVisibility(View.VISIBLE);
      teleopSwitch.setVisibility(View.VISIBLE);
    } else {
      tablesSwitch.setText(R.string.tables_disabled);
      autoSwitch.setVisibility(View.GONE);
      teleopSwitch.setVisibility(View.GONE);
    }

    // Pass in the saved table settings
    checkedTables = new boolean[curTables.length];
    for (int i = 0; i < curTables.length; i++) {
      checkedTables[i] = preference.getBoolean(curTables[i] + "_checked", false);
    }

    // Turn on switch if table is checked
    autoSwitch.setChecked(checkedTables[0]);
    teleopSwitch.setChecked(checkedTables[1]);

    setTableStatus(checkedTables[0], checkedTables[1]);

    autoSwitch.setOnClickListener(v1 ->
        setTableStatus(autoSwitch.isChecked(), teleopSwitch.isChecked()));

    teleopSwitch.setOnClickListener(v1 ->
        setTableStatus(autoSwitch.isChecked(), teleopSwitch.isChecked()));

    SwitchCompat roleSwitch = v.findViewById(R.id.role_switch);
    roleSwitch.setChecked(preference.getBoolean("role_mode", CROWD));
    roleSwitch.setOnClickListener(v1 -> {
      if (roleSwitch.isChecked()) {
        roleSwitch.setText(R.string.crowd_role);
        preference.putBoolean("role_mode", CROWD);
      } else {
        roleSwitch.setText(R.string.pit_role);
        preference.putBoolean("role_mode", PIT);
      }
    });

    if (roleSwitch.isChecked()) {
      roleSwitch.setText(R.string.crowd_role);
    } else {
      roleSwitch.setText(R.string.pit_role);
    }

    builder.setView(v)
        // Add action buttons
        .setPositiveButton("Set page settings", (dialog, id) -> {
          setTableStatus(autoSwitch.isChecked(), teleopSwitch.isChecked());
          listener.onPageSettingsDialogPositiveClick(table_status, columnSwitch.isChecked());
        })
        .setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
          if (PageSettingsDialog.this.getDialog() != null) {
            PageSettingsDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }

  private void setTableStatus(boolean auto, boolean teleop) {
    checkedTables = new boolean[curTables.length];
    for (int i = 0; i < curTables.length; i++) {
      checkedTables[i] = preference.getBoolean(curTables[i] + "_checked", true);
    }

    // Check if passed in variables are different from current
    if (auto != checkedTables[0] || teleop != checkedTables[1]) {
      checkedTables[0] = auto;
      checkedTables[1] = teleop;
    }

    if (checkedTables[0] && checkedTables[1]) {
      table_status = BOTH;
      preference.putBoolean("tables_mode", true);
    } else if (checkedTables[0]) {
      table_status = AUTO;
      preference.putBoolean("tables_mode", true);
    } else if (checkedTables[1]) {
      table_status = TELEOP;
      preference.putBoolean("tables_mode", true);
    } else {
      table_status = NONE;
    }

    preference.putInt("table_status", table_status);

    for (int i = 0; i < curTables.length; i++) {
      preference.putBoolean(curTables[i] + "_checked", checkedTables[i]);
    }
  }

  public interface PageSettingsDialogListener {
    void onPageSettingsDialogPositiveClick(int table_status, boolean grid);
  }
}