package com.databits.scoutbuilder;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.databits.scoutbuilder.dialogs.CounterDialog;
import com.databits.scoutbuilder.dialogs.ListDialog;
import com.databits.scoutbuilder.dialogs.SegmentDialog;
import com.databits.scoutbuilder.dialogs.TextboxDialog;
import com.databits.scoutbuilder.dialogs.YesNoDialog;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.databits.scoutbuilder.adapter.RecyclerAdapter;
import com.databits.scoutbuilder.adapter.SimpleItemTouchHelperCallback;
import com.preference.PowerPreference;
import com.preference.Preference;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements YesNoDialog.YesNoDialogListener,
    CounterDialog.CounterDialogListener, SegmentDialog.SegmentDialogListener,
    ListDialog.ListDialogListener, TextboxDialog.TextboxDialogListener {

    private static final String TAG = "MainActivity";
    private static final int NONE = 0;
    private static final int AUTO = 1;
    private static final int TELEOP = 2;
    private static final int BOTH = 3;

    Preference preference = PowerPreference.getDefaultFile();
    ItemTouchHelper.Callback callbackTop;
    ItemTouchHelper mItemTouchHelperTop;
    ItemTouchHelper.Callback callbackBot;
    ItemTouchHelper mItemTouchHelperBot;
    TableLayout[] tables;
    String[] cellTypes = {"YesNo", "Counter", "Segment", "List", "Text", /*"Table"*/};
    String[] cellTitles = {"YesNo_title", "Counter_Title", "Segment_Title", "List_Title", "Textbox_title"};
    String[] curTables = {"Auto", "Teleop"};
    private RecyclerAdapter mAdapterTop;
    private RecyclerAdapter mAdapterBot;
    private RecyclerView mRecyclerViewTop;
    private RecyclerView mRecyclerViewBot;
    private ClipboardManager clipboard;
    private boolean[] checkedTables;
    private int table_status;
    List<Cell> cellList = new ArrayList<>();

    // Create a menu in the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem top_add = menu.findItem(R.id.action_add_top);
        MenuItem bot_add = menu.findItem(R.id.action_add_bot);

        // Table Status is used to determine which table to display and what buttons
        table_status = preference.getInt("table_status", BOTH);
        switch (table_status) {
            case AUTO:
                top_add.setVisible(true);
                bot_add.setVisible(false);
                top_add.setTitle("Add Auto Cell");
                break;
            case TELEOP:
                top_add.setVisible(false);
                bot_add.setVisible(true);
                top_add.setTitle("Add Teleop Cell");
                break;
            case BOTH:
                top_add.setVisible(true);
                bot_add.setVisible(true);
                bot_add.setTitle("Add Teleop Cell");
                top_add.setTitle("Add Auto Cell");
                break;
            case NONE:
            default:
                top_add.setVisible(true);
                bot_add.setVisible(false);
                top_add.setTitle("Add Cell");
                break;
        }

        // Save table status for future use
        preference.putInt("table_status", table_status);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the Preference storage library
        PowerPreference.init(this);

        mRecyclerViewBot = findViewById(R.id.recycler_view_bot);
        mRecyclerViewTop = findViewById(R.id.recycler_view_top);

        View[] topTables = {findViewById(R.id.left_table), findViewById(R.id.center_table), findViewById(R.id.right_table)};
        View[] botTables = {findViewById(R.id.bot_left_table), findViewById(R.id.bot_center_table), findViewById(R.id.bot_right_table)};
        String[] toptitles = {"Left\nSide", "Autonomous\nCenter Side", "Right\nSide"};
        String[] bottitles = {"Left\nSide", "Teleop\nCenter Side", "Right\nSide"};

        tables = new TableLayout[]{
            topTables[0].findViewById(R.id.inner_table),
            topTables[1].findViewById(R.id.inner_table),
            topTables[2].findViewById(R.id.inner_table),
            botTables[0].findViewById(R.id.inner_table),
            botTables[1].findViewById(R.id.inner_table),
            botTables[2].findViewById(R.id.inner_table),
        };

        // Set Table Auto/Teleop titles
        for (int i = 0; i < 3; i++) {
            TextView topText = topTables[i].findViewById(R.id.table_title);
            TextView botText = botTables[i].findViewById(R.id.table_title);
            topText.setTextSize(20);
            topText.setTextColor(Color.WHITE);
            botText.setTextSize(20);
            botText.setTextColor(Color.WHITE);
            topText.setText(toptitles[i]);
            botText.setText(bottitles[i]);
        }

        // Set on click listeners for all buttons to cycle between icons
        // Uses the team color to determine which icon to use
        for (TableLayout table : tables) {
            for (int i = 1; i < table.getChildCount(); i++) {
                TableRow row = (TableRow) table.getChildAt(i);
                for (int j = 0; j < row.getChildCount(); j++) {
                    ImageButton button = (ImageButton) row.getChildAt(j);

                    if (i == 3) {
                        button.setTag("Both");
                    } else {
                        if (j == 0) {
                            button.setTag("Cone");
                        } else if (j == 1) {
                            button.setTag("Cube");
                        } else if (j == 2) {
                            button.setTag("Cone");
                        }
                    }

                    button.setImageResource(R.drawable.android_x);
                    final int[] counter = {1};
                    button.setOnClickListener(v -> {
                        boolean red = preference.getBoolean("isRedteam");
                        Log.d(TAG, "onCreate: " + button.getId());
                        String curTag = String.valueOf(button.getTag());
                        switch (curTag) {
                            case "Both":
                                if (counter[0] == 0) {
                                    button.setImageResource(R.drawable.android_x);
                                    counter[0]++;
                                } else if (counter[0] == 1) {
                                    if (red) {
                                        button.setImageResource(R.drawable.red_cube44);
                                    } else {
                                        button.setImageResource(R.drawable.cube44);
                                    }
                                    counter[0]++;
                                } else if (counter[0] == 2) {
                                    if (red) {
                                        button.setImageResource(R.drawable.red_cone44);
                                    } else {
                                        button.setImageResource(R.drawable.cone44);
                                    }
                                    counter[0] = 0;
                                }
                                break;
                            case "Cone":
                                if (counter[0] == 0) {
                                    button.setImageResource(R.drawable.android_x);
                                    counter[0]++;
                                } else if (counter[0] == 1) {
                                    if (red) {
                                        button.setImageResource(R.drawable.red_cone44);
                                    } else {
                                        button.setImageResource(R.drawable.cone44);
                                    }
                                    counter[0] = 0;
                                }
                                break;
                            case "Cube":
                                if (counter[0] == 0) {
                                    button.setImageResource(R.drawable.android_x);
                                    counter[0]++;
                                } else if (counter[0] == 1) {
                                    if (red) {
                                        button.setImageResource(R.drawable.red_cube44);
                                    } else {
                                        button.setImageResource(R.drawable.cube44);
                                    }
                                    counter[0] = 0;
                                }
                                break;
                        }


                    });
                }
            }
        }

        // Adds all 5 types of cells to both recycler views
        //testItems(5);

        // Sorts the tables based on saved Table Status
        table_status = preference.getInt("table_status", BOTH);
        tableSorter(table_status);

        // Change the team color based on the saved value
        if (preference.getBoolean("isRedteam")) {
            for (TableLayout table : tables) {
                updateTableColor(table, R.color.map_red);
            }
        } else {
            for (TableLayout table : tables) {
                updateTableColor(table, R.color.map_blue);
            }
        }

        mRecyclerViewTop.setAdapter(mAdapterTop);
        mRecyclerViewBot.setAdapter(mAdapterBot);

        callbackTop = new SimpleItemTouchHelperCallback(mAdapterTop);
        mItemTouchHelperTop = new ItemTouchHelper(callbackTop);

        callbackBot = new SimpleItemTouchHelperCallback(mAdapterBot);
        mItemTouchHelperBot = new ItemTouchHelper(callbackBot);

        // Turn on and off the grid layout based on the saved value
        if (preference.getBoolean("grid", true)) {
            mRecyclerViewTop.setLayoutManager(new GridLayoutManager(this, 2));
            mRecyclerViewBot.setLayoutManager(new GridLayoutManager(this, 2));

        } else {
            mRecyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerViewBot.setLayoutManager(new LinearLayoutManager(this));
        }

        // Turn on and off the ability to drag and drop
        if (preference.getBoolean("reorder", false)) {
            mItemTouchHelperTop.attachToRecyclerView(mRecyclerViewTop);
            mItemTouchHelperBot.attachToRecyclerView(mRecyclerViewBot);
        } else {
            mItemTouchHelperTop.attachToRecyclerView(null);
            mItemTouchHelperBot.attachToRecyclerView(null);
        }
    }

    private void testItems(int cells) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<RecyclerAdapter> jsonAdapter = moshi.adapter(RecyclerAdapter.class);

        for (int i = 0; i < cells; i++) {
            String cellType = cellTypes[i];
            CellParam cellParam = new CellParam(cellType);
            switch (cellType) {
                case "YesNo":
                    cellParam.setCellType(getString(R.string.YesNoType));
                    break;
                case "Counter":
                    cellParam.setCellType(getString(R.string.CounterType));
                    cellParam.setCellDefault(3);
                    cellParam.setCellMax(5);
                    cellParam.setCellMin(0);
                    cellParam.setCellUnit(1);
                case "Segment":
                    cellParam.setCellType(getString(R.string.SegmentType));
                    cellParam.setCellSegments(6);
                    cellParam.setCellSegmentLabels(Arrays.asList("One", "2", "Three", "4", "Five", "6"));
                    break;
                case "List":
                    cellParam.setCellType(getString(R.string.ListType));
                    cellParam.setCellTotalEntries(3);
                    cellParam.setCellEntryLabels(Arrays.asList("Java", "C++", "Labview"));
                    break;
                case "Text":
                    cellParam.setCellType(getString(R.string.TextType));
                    cellParam.setCellTextHidden(false);
                    cellParam.setCellTextHint("Enter life here");
                    break;
            }
            Cell cell = new Cell(i, cellTitles[i], cellType, cellParam);
            cellList.add(cell);
        }

        RecyclerAdapter myAdapter = new RecyclerAdapter(cellList);

        String test = jsonAdapter.toJson(myAdapter);
        try {
            RecyclerAdapter config = jsonAdapter.fromJson(test);
            RecyclerAdapter configBot = jsonAdapter.fromJson(test);
            mAdapterTop = new RecyclerAdapter(Objects.requireNonNull(config).mCell);
            mAdapterBot = new RecyclerAdapter(Objects.requireNonNull(configBot).mCell);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error parsing JSON", e);
            mAdapterTop = new RecyclerAdapter(Collections.emptyList());
            mAdapterBot = new RecyclerAdapter(Collections.emptyList());

        }
        /* } */
        mAdapterTop.notifyDataSetChanged();
        mAdapterBot.notifyDataSetChanged();
        mRecyclerViewTop.setAdapter(mAdapterTop);
        mRecyclerViewBot.setAdapter(mAdapterBot);
    }

    private void tableSorter(int table_status) {
        RecyclerView recyclerViewTop = findViewById(R.id.recycler_view_top);
        ViewGroup.LayoutParams topParam = recyclerViewTop.getLayoutParams();
        ConstraintLayout constraintLayout = findViewById(R.id.Main_layout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        TextView title;
        //Switch statement to sort the tables based on the table status
        switch (table_status) {
            case NONE:
                // recyclerViewTop attached to parent top
                // recyclerViewBot GONE
                constraintSet.connect(R.id.recycler_view_top, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
                constraintSet.setVisibility(R.id.recycler_view_bot, View.VISIBLE);
                topParam.height = ViewGroup.LayoutParams.MATCH_PARENT;

                for (int j = 0; j < 6; j++) {
                    tables[j].setVisibility(View.GONE);
                }
                title = tables[4].getChildAt(0).findViewById(R.id.table_title);
                title.setText("Auto\nCenter Side");
                mRecyclerViewTop.setVisibility(View.VISIBLE);
                mRecyclerViewBot.setVisibility(View.GONE);
                break;
            case AUTO:
                // recyclerViewTop attached to parent top
                // recyclerViewBot GONE
                constraintSet.connect(R.id.recycler_view_top, ConstraintSet.TOP, R.id.bot_center_table, ConstraintSet.BOTTOM, 0);
                constraintSet.setVisibility(R.id.recycler_view_bot, View.GONE);
                topParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                for (int j = 3; j < 6; j++) {
                    tables[j].setVisibility(View.GONE);
                    for (int k = 0; k < 3; k++) {
                        tables[k].setVisibility(View.VISIBLE);
                    }
                    title = tables[4].getChildAt(0).findViewById(R.id.table_title);
                    title.setText("Auto\nCenter Side");
                    mRecyclerViewTop.setVisibility(View.VISIBLE);
                    mRecyclerViewBot.setVisibility(View.GONE);
                }
                break;
            case TELEOP:
                // recyclerViewTop attached to parent top
                // recyclerViewBot GONE
                constraintSet.connect(R.id.recycler_view_top, ConstraintSet.TOP, R.id.bot_center_table, ConstraintSet.BOTTOM, 0);
                constraintSet.setVisibility(R.id.recycler_view_bot, View.GONE);
                topParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                for (int j = 3; j < 6; j++) {
                    tables[j].setVisibility(View.GONE);
                    for (int k = 0; k < 3; k++) {
                        tables[k].setVisibility(View.VISIBLE);
                    }
                    title = tables[4].getChildAt(0).findViewById(R.id.table_title);
                    title.setText("Teleop\nCenter Side");
                    mRecyclerViewTop.setVisibility(View.VISIBLE);
                    mRecyclerViewBot.setVisibility(View.GONE);
                }
                break;
            case BOTH:
                // recyclerViewTop attached to parent top
                // recyclerViewBot attached to center_table
                constraintSet.connect(R.id.recycler_view_top, ConstraintSet.TOP, R.id.center_table, ConstraintSet.BOTTOM, 0);
                constraintSet.connect(R.id.recycler_view_bot, ConstraintSet.TOP, R.id.bot_center_table, ConstraintSet.BOTTOM, 0);
                constraintSet.connect(R.id.inner_table, ConstraintSet.TOP, R.id.Main_layout, ConstraintSet.TOP, 0);
                constraintSet.setVisibility(R.id.recycler_view_top, View.VISIBLE);
                constraintSet.setVisibility(R.id.recycler_view_bot, View.VISIBLE);
                topParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                for (int j = 0; j < 6; j++) {
                    tables[j].setVisibility(View.VISIBLE);
                }
                title = tables[4].getChildAt(0).findViewById(R.id.table_title);
                title.setText("Teleop\nCenter Side");
                mRecyclerViewTop.setVisibility(View.VISIBLE);
                mRecyclerViewBot.setVisibility(View.VISIBLE);
                break;
        }
        supportInvalidateOptionsMenu();
    }

    private void updateTableColor(TableLayout table, int colorResId/*, int textColor*/) {
        TableRow row = (TableRow) table.getChildAt(0);
        row.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        TextView title = row.findViewById(R.id.table_title);
        title.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    // import cells from json string
    public void import_cells(String optional_json) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<RecyclerAdapter> jsonAdapter = moshi.adapter(RecyclerAdapter.class);
        try {
            RecyclerAdapter config = jsonAdapter.fromJson(optional_json);
            mAdapterTop = new RecyclerAdapter(Objects.requireNonNull(config).mCell);
            mAdapterBot = new RecyclerAdapter(Objects.requireNonNull(config).mCell);
            mRecyclerViewTop.setAdapter(mAdapterTop);
            mRecyclerViewBot.setAdapter(mAdapterBot);
            Log.d(TAG, "import: " + optional_json);
        } catch (IOException e) {
            Log.e(TAG, "Error parsing JSON", e);
            mAdapterTop = new RecyclerAdapter(Collections.emptyList());
            mAdapterBot = new RecyclerAdapter(Collections.emptyList());
        }
        mAdapterTop.notifyDataSetChanged();
        mAdapterBot.notifyDataSetChanged();

    }

    @NeedsPermission({android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void save_json() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<RecyclerAdapter> jsonAdapter = moshi.adapter(RecyclerAdapter.class);
        String jsonTop = jsonAdapter.toJson(mAdapterTop);
        String jsonBot = jsonAdapter.toJson(mAdapterBot);

        File file = new File(this.getExternalFilesDir(null), "exported_layout.json");
        try {
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(jsonTop.getBytes());
                stream.write(jsonBot.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "export: " + jsonTop);
        Log.d(TAG, "export: " + jsonBot);
    }

    public String export_string() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<RecyclerAdapter> jsonAdapter = moshi.adapter(RecyclerAdapter.class);
        String jsonTop = jsonAdapter.toJson(mAdapterTop);
        String jsonBot = jsonAdapter.toJson(mAdapterBot);

        Log.d(TAG, "export: " + jsonTop);
        Log.d(TAG, "export: " + jsonBot);
        return jsonTop;
    }

    // Use values from popup to create new recycler view item
    public void popupLauncher(String cellType, boolean isTop) {
        switch (cellType) {
            case "YesNo":
                showYesNoDialog(preference.getString(1 + "_title_value", "Title"), 1, isTop);
                break;
            case "Counter":
                showCounterDialog(preference.getString(1 + "_title_value", "Title"), 1, isTop);
                break;
            case "Segment":
                showSegmentDialog(preference.getString(1 + "_title_value", "Title"), 1, isTop);
                break;
            case "List":
                showListDialog(preference.getString(1 + "_title_value", "Title"), 1, isTop);
                break;
            case "Text":
                showTextboxDialog(preference.getString(1 + "_title_value", "Title"), 1, isTop);
                break;
            case "Table":
                break;
        }

    }

    public String exportTable() {
        // Export the table as a string
        StringBuilder export = new StringBuilder();
        for (TableLayout table : tables) {
            // 0 is the title row, so start at 1
            for (int i = 1; i < table.getChildCount(); i++) {
                TableRow row = (TableRow) table.getChildAt(i);
                for (int j = 0; j < row.getChildCount(); j++) {
                    ImageButton button = (ImageButton) row.getChildAt(j);
                    Drawable.ConstantState curDraw = button.getDrawable().getConstantState();
                    int id = 0;
                    // Loop through all buttons and set id based on which drawable is currently set
                    if (curDraw.equals(ContextCompat.getDrawable(this, R.drawable.android_x).getConstantState())) {
                        id = 0;
                    } else if (curDraw.equals(ContextCompat.getDrawable(this, R.drawable.red_cube44).getConstantState())
                        || curDraw.equals(ContextCompat.getDrawable(this, R.drawable.cube44).getConstantState())) {
                        id = 1;
                    } else if (curDraw.equals(ContextCompat.getDrawable(this, R.drawable.red_cone44).getConstantState())
                        || curDraw.equals(ContextCompat.getDrawable(this, R.drawable.cone44).getConstantState())) {
                        id = 2;
                    }

                    // Append the id to the export string with a comma separator except for the last table in the list
                    if (table == tables[tables.length - 1] && i == table.getChildCount() - 1 && j == row.getChildCount() - 1) {
                        export.append(id);
                    } else {
                        export.append(id).append(",");
                    }
                }
            }
        }
        return export.toString();
    }

    public void createItem(RecyclerAdapter mAdapter, RecyclerView mRecyclerView, Cell newCell) {
        List<Cell> cells = new ArrayList<>();
        cells.add(newCell);
        mAdapter.mCell.addAll(cells);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    // Handle menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        mRecyclerViewTop = findViewById(R.id.recycler_view_top);

        mRecyclerViewBot = findViewById(R.id.recycler_view_bot);

        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);


        if (id == R.id.action_reset) {
            PowerPreference.getDefaultFile().clearAsync();
            recreate();
        }

        if (id == R.id.action_debug) {
            PowerPreference.showDebugScreen(true);
        }

        if (id == R.id.action_color) {
            preference.setBoolean("isRedteam", !preference.getBoolean("isRedteam", false));
            // Change color of table if isRedteam is true using tables array
            if (preference.getBoolean("isRedteam")) {
                for (TableLayout table : tables) {
                    TableRow row = (TableRow) table.getChildAt(0);
                    row.setBackgroundColor(ContextCompat.getColor(this, R.color.map_red));
                    TextView title = row.findViewById(R.id.table_title);
                    title.setTextColor(Color.BLACK);
                    row.refreshDrawableState();
                }
            } else {
                for (TableLayout table : tables) {
                    TableRow row = (TableRow) table.getChildAt(0);
                    row.setBackgroundColor(ContextCompat.getColor(this, R.color.map_blue));
                    TextView title = row.findViewById(R.id.table_title);
                    title.setTextColor(Color.WHITE);
                    row.refreshDrawableState();
                }
            }
        }

        if (id == R.id.action_reorder) {
            boolean reorder = preference.getBoolean("reorder", false);

            // Toggle and update the text
            if (!reorder) {
                item.setTitle("Reordering On");
                preference.setBoolean("reorder", true);
                mItemTouchHelperTop.attachToRecyclerView(mRecyclerViewTop);
                mItemTouchHelperBot.attachToRecyclerView(mRecyclerViewBot);
            } else {
                item.setTitle("Reordering Off");
                preference.setBoolean("reorder", false);
                mItemTouchHelperTop.attachToRecyclerView(null);
                mItemTouchHelperBot.attachToRecyclerView(null);
            }
        }
        if (id == R.id.action_hide_table) {
            // Open a dialog to hide tables array
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Configure 2023 Tables");
            // Create a list of tables to remove
            checkedTables = new boolean[curTables.length];
            for (int i = 0; i < curTables.length; i++) {
                checkedTables[i] = preference.getBoolean(curTables[i] + "_checked", true);
            }
            builder.setMultiChoiceItems(curTables, checkedTables, (dialog, which, isChecked) ->
                            checkedTables[which] = isChecked)
                    .setPositiveButton("OK", (dialog, id1) -> {
                        if (checkedTables[0] && checkedTables[1]) {
                            table_status = BOTH;
                        } else if (checkedTables[0]) {
                            table_status = AUTO;
                        } else if (checkedTables[1]) {
                            table_status = TELEOP;
                        } else {
                            table_status = NONE;
                        }
                        tableSorter(table_status);
                        preference.putInt("table_status", table_status);
                        for (int i = 0; i < curTables.length; i++) {
                            preference.putBoolean(curTables[i] + "_checked", checkedTables[i]);
                        }

                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        if (id == R.id.action_launch) {

        }

        if (id == R.id.action_export) {
            String json = export_string();

            // Make a dialog with text that can be copied
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.ExportTitle);
            // Create a TextView to display the export string
            TextView textView = new TextView(this);
            textView.setText(json);

            // Set the TextView to be focusable and selectable
            textView.setFocusable(true);
            textView.setTextIsSelectable(true);
            textView.setSelectAllOnFocus(true);

            // Add the TextView to the dialog
            builder.setView(textView);

            // Create a ClipData object to hold the text
            ClipData clip_export_json = ClipData.newPlainText("label", json);

            // Send the ClipData to the clipboard
            clipboard.setPrimaryClip(clip_export_json);

            builder.setPositiveButton(R.string.ExportButtonTitle, (dialog, which) -> {
                clipboard.setPrimaryClip(clip_export_json);
                save_json();
                dialog.dismiss();
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
            builder.show();
        }

        if (id == R.id.action_import) {
            // Receive the ClipData from the clipboard
            // Make a dialog with text that can be copied
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.ImportTitle);

            ClipData clip_import_json = clipboard.getPrimaryClip();

            // Create a TextView to display the export string
            TextView textView = new TextView(this);
            textView.setText(clip_import_json.getItemAt(0).getText());

            // Add the TextView to the dialog
            builder.setView(textView);

            builder.setPositiveButton(R.string.ImportButtonTitle, (dialog, which) -> {
                // If the clipboard contains text
                if (clip_import_json.getItemCount() > 0) {

                    // Get the first item on the clipboard
                    ClipData.Item clips = clip_import_json.getItemAt(0);

                    import_cells(clips.getText().toString());
                }

                dialog.dismiss();
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
            builder.show();
        }

        if (id == R.id.action_columns) {
            boolean grid = preference.getBoolean("grid", true);

            // Toggle and update the text
            if (grid) {
                mRecyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerViewBot.setLayoutManager(new LinearLayoutManager(this));
                item.setTitle("2 Column mode off");
                preference.setBoolean("grid", false);
            } else {
                mRecyclerViewTop.setLayoutManager(new GridLayoutManager(this, 2));
                mRecyclerViewBot.setLayoutManager(new GridLayoutManager(this, 2));
                item.setTitle("2 Column mode on");
                preference.setBoolean("grid", true);
            }
        }
        // Create a dialog with a list of cell types
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.CellBuilderDialogTitle);

        if (id == R.id.action_add_top) {
            builder.setItems(cellTypes, (dialog, which) -> popupLauncher(cellTypes[which], true));
            builder.show();
        }

        if (id == R.id.action_add_bot) {
            builder.setItems(cellTypes, (dialog, which) -> popupLauncher(cellTypes[which], false));
            builder.show();
        }

        if (id == R.id.action_dark_mode) {
            // Night mode switch
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                preference.setInt("night_mode", AppCompatDelegate.MODE_NIGHT_NO);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                preference.setInt("night_mode", AppCompatDelegate.MODE_NIGHT_YES);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showTextboxDialog(String title, int viewId, boolean location) {
        DialogFragment dialog = new TextboxDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "TextboxDialog");
    }

    public void showListDialog(String title, int viewId, boolean location) {
        DialogFragment dialog = new ListDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "ListDialog");
    }

    private void showSegmentDialog(String title, int viewId, boolean location) {
        DialogFragment dialog = new SegmentDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "SegmentDialog");
    }

    private void showCounterDialog(String title, int viewId, boolean location) {
        DialogFragment dialog = new CounterDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "CounterDialog");
    }

    public void showYesNoDialog(String title, int viewId, boolean location) {
        DialogFragment dialog = new YesNoDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "YesNoDialog");
    }

    @Override
    public void onYesNoDialogPositiveClick(Cell newCell, boolean location) {
        RecyclerAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        createItem(mAdapter, mRecyclerView, newCell);
    }

    @Override
    public void onCounterDialogPositiveClick(Cell newCell, boolean location) {
        RecyclerAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        createItem(mAdapter, mRecyclerView, newCell);
    }

    @Override public void onSegmentDialogPositiveClick(Cell newCell, boolean location) {
        RecyclerAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        createItem(mAdapter, mRecyclerView, newCell);
    }

    @Override public void onListDialogPositiveClick(Cell newCell, boolean location) {
        RecyclerAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        createItem(mAdapter, mRecyclerView, newCell);
    }

    @Override public void onTextboxDialogPositiveClick(Cell newCell, boolean location) {
        RecyclerAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        createItem(mAdapter, mRecyclerView, newCell);
    }
}