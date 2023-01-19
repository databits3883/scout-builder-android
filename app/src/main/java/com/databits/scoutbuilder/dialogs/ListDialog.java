package com.databits.scoutbuilder.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.databits.scoutbuilder.R;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.preference.PowerPreference;
import com.preference.Preference;
import java.util.ArrayList;
import java.util.Collections;

public class ListDialog extends DialogFragment {
  Bundle bundle;

  Preference preference = PowerPreference.getDefaultFile();

  TextView picker_list_item;


  public interface ListDialogListener {
    void onListDialogPositiveClick(Cell newCell, boolean location);
  }

  ListDialogListener listener;

  // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      listener = (ListDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(requireActivity()
          + " must implement ListDialogListener");
    }
  }

  @NonNull @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
    // Get the layout inflater
    LayoutInflater inflater = requireActivity().getLayoutInflater();

    bundle = getArguments();
    // Inflate and set the layout for the dialog
    View v = inflater.inflate(R.layout.popup_list, null);

    String title = getTitle(v);

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);


    int viewId = bundle.getInt("id");

    picker_list_item = v.findViewById(R.id.popup_title_list_text);
    Button add_button = v.findViewById(R.id.popup_add_button);
    ArrayList<String> entries = new ArrayList<>(Collections.emptyList());
    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.ui_list_item, entries);
    AutoCompleteTextView dropdown = v.findViewById(R.id.menu_box);
    dropdown.setAdapter(adapter);
    adapter.setNotifyOnChange(true);

    add_button.setOnClickListener(view1 -> {
      if (!picker_list_item.getText().toString().isEmpty()) {
        entries.add(picker_list_item.getText().toString());
        adapter.notifyDataSetChanged();
        picker_list_item.setText("");
        int list_count = entries.size();

        preference.setInt(viewId + "_list_count", list_count);

      }
    });

    Button popup_clear_button = v.findViewById(R.id.popup_clear_button);

    popup_clear_button.setOnClickListener(view1 -> {
      for (String entry : entries) {
        preference.remove(viewId + "_list_item_" + entry);
      }
      entries.clear();
      adapter.notifyDataSetChanged();
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
          String cellType = getString(R.string.ListType);
          CellParam cellParam = new CellParam(cellType);
          cellParam.setCellType(cellType);
          String cellTitle = picker_title.getText().toString();

          for (int p = 0; p < entries.size(); p++) {
            String value = preference.getString(viewId + "_list_item_" + p, "Null Value");
            if (!value.equals("Null Value")) {
              entries.add(value);
            } else {
              preference.remove(viewId + "_list_item_" + p);
            }
          }

          for (String entry : entries) {
            if (!entry.equals("Null Value")) {
              preference.putString(viewId + "_list_item_" + entries.indexOf(entry), entry);
            }
          }

          cellParam.setCellTotalEntries(entries.size());
          cellParam.setCellEntryLabels(entries);

          preference.setString(1 + "_title_value", cellTitle);
          Cell newCell = new Cell(viewId,picker_title.getText().toString(), cellType, cellParam);

          listener.onListDialogPositiveClick(newCell, bundle.getBoolean("location"));
        })
        .setNegativeButton(R.string.cancel, (dialog, id) -> {
          if (ListDialog.this.getDialog() != null) {
            ListDialog.this.getDialog().cancel();
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