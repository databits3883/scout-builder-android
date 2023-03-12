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
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import com.databits.scoutbuilder.R;
import com.databits.scoutbuilder.Utils;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.skydoves.balloon.Balloon;
import java.util.ArrayList;
import java.util.Collections;

public class ListDialog extends DialogFragment {
  Bundle bundle;

  Preference preference = PowerPreference.getDefaultFile();

  TextView picker_list_item;

  int viewId;
  int realId;
  boolean location;

  public interface ListDialogListener {
    void onListDialogPositiveClick(Cell newCell, boolean location, int position, int realId);
  }

  ListDialogListener listener;

  Utils utils;

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

    String title = utils.getTitle(location, realId, bundle);
    String help = utils.getHelp(location, realId, bundle);

    Balloon.Builder helpBuilder = utils.helpBuilder();
    helpBuilder.setText(help);

    TextView picker_title = v.findViewById(R.id.popup_title_text);
    TextView exampleTitle = v.findViewById(R.id.popup_title_example);
    TextView picker_help = v.findViewById(R.id.popup_help_text);
    ImageButton helpIcon = v.findViewById(R.id.help_button);

    viewId = bundle.getInt("id");
    realId = bundle.getInt("real_id");
    location = bundle.getBoolean("location");

    picker_list_item = v.findViewById(R.id.popup_title_list_text);
    Button add_button = v.findViewById(R.id.popup_add_button);
    ArrayList<String> entries = new ArrayList<>(Collections.emptyList());
    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), R.layout.ui_list_item, entries);
    AutoCompleteTextView dropdown = v.findViewById(R.id.menu_box);
    dropdown.setAdapter(adapter);
    adapter.setNotifyOnChange(true);

    helpIcon.setOnClickListener(v1 -> helpBuilder.build().showAlignBottom(helpIcon));

    add_button.setOnClickListener(view1 -> {
      if (!picker_list_item.getText().toString().isEmpty()) {
        entries.add(picker_list_item.getText().toString());
        adapter.notifyDataSetChanged();
        picker_list_item.setText("");
        int list_count = entries.size();

        if (location) {
          preference.setInt("top_" + viewId + "_list_count", list_count);
        } else {
          preference.setInt("bot_" + viewId + "_list_count", list_count);
        }


      }
    });

    Button popup_clear_button = v.findViewById(R.id.popup_clear_button);

    popup_clear_button.setOnClickListener(view1 -> {
      for (String entry : entries) {
        if (location) {
          preference.remove("top_" + viewId + "_list_item_" + entry);
        } else {
          preference.remove("bot_" + viewId + "_list_item_" + entry);
        }
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
        listener.onListDialogPositiveClick(null, location, viewId, realId);
        if (ListDialog.this.getDialog() != null) {
          ListDialog.this.getDialog().cancel();
        }
      });
    }

    // Pass null as the parent view because its going in the dialog layout
    builder.setView(v)
        // Add action buttons
        .setPositiveButton(utils.textSelector(), (dialog, id) -> {
          // Create cell object to be returned to the activity
          String cellType = getString(R.string.ListType);
          CellParam cellParam = new CellParam(cellType);
          cellParam.setType(cellType);
          String newTitle = picker_title.getText().toString();
          String newHelp = picker_help.getText().toString();

          for (int p = 0; p < entries.size(); p++) {
            String value;
            if (location) {
              value = preference.getString("top_" + viewId + "_list_item_" + p, "Null Value");
            } else {
              value = preference.getString("bot_" + viewId + "_list_item_" + p, "Null Value");
            }

            if (!value.equals("Null Value")) {
              entries.add(value);
            } else {
              if (location) {
                preference.remove("top_" + viewId + "_list_item_" + p);
              } else {
                preference.remove("bot_" + viewId + "_list_item_" + p);
              }
            }
          }

          for (String entry : entries) {
            if (!entry.equals("Null Value")) {
              if (location) {
                preference.putString("top_" + viewId + "_list_item_" + entries.indexOf(entry), entry);
              } else {
                preference.putString("bot_" + viewId + "_list_item_" + entries.indexOf(entry), entry);
              }
            }
          }

          cellParam.setTotalEntries(entries.size());
          cellParam.setEntryLabels(entries);

          if (location) {
            preference.putString("top_" + viewId + "_title_value", newTitle);
            preference.putString("top_" + viewId + "_help_value", newHelp);
          } else {
            preference.putString("bot_" + viewId + "_title_value", newTitle);
            preference.putString("bot_" + viewId + "_help_value", newHelp);
          }

          Cell newCell = new Cell(viewId,newTitle, cellType, cellParam);

          listener.onListDialogPositiveClick(newCell, location, viewId, realId);
        })
        .setNegativeButton(R.string.cancel, (dialog, id) -> {
          if (ListDialog.this.getDialog() != null) {
            ListDialog.this.getDialog().cancel();
          }
        });
    return builder.create();
  }
}