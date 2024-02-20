package com.databits.scoutbuilder;

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
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.databits.scoutbuilder.adapter.MultiviewTypeAdapter;
import com.databits.scoutbuilder.adapter.RecyclerAdapter;
import com.databits.scoutbuilder.adapter.RecyclerItemClickListener;
import com.databits.scoutbuilder.adapter.SimpleItemTouchHelperCallback;
import com.databits.scoutbuilder.dialogs.CounterDialog;
import com.databits.scoutbuilder.dialogs.DualCounterDialog;
import com.databits.scoutbuilder.dialogs.ListDialog;
import com.databits.scoutbuilder.dialogs.PageSettingsDialog;
import com.databits.scoutbuilder.dialogs.SegmentDialog;
import com.databits.scoutbuilder.dialogs.TeamMatchDialog;
import com.databits.scoutbuilder.dialogs.TeamSelectDialog;
import com.databits.scoutbuilder.dialogs.TextboxDialog;
import com.databits.scoutbuilder.dialogs.YesNoDialog;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
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

public class MainActivity extends AppCompatActivity implements YesNoDialog.YesNoDialogListener,
    CounterDialog.CounterDialogListener, SegmentDialog.SegmentDialogListener,
    ListDialog.ListDialogListener, TextboxDialog.TextboxDialogListener,
    PageSettingsDialog.PageSettingsDialogListener, TeamMatchDialog.TeamMatchDialogListener, TeamSelectDialog.TeamSelectDialogListener, DualCounterDialog.DualCounterDialogListener {

    private static final String TAG = "MainActivity";
    private static final int NONE = 0;
    private static final int AUTO = 1;
    private static final int TELEOP = 2;
    private static final int BOTH = 3;
    private static final int EDIT = 4;

    Preference preference = PowerPreference.getDefaultFile();
    ItemTouchHelper.Callback callbackTop;
    ItemTouchHelper mItemTouchHelperTop;
    ItemTouchHelper.Callback callbackBot;
    ItemTouchHelper mItemTouchHelperBot;
    TableLayout[] tables;
    String[] cellTypes = {"YesNo", "Counter","DualCounter", "Segment", "List", "Text", "TeamSelect", "TeamMatch"};
    String[] cellTitles = {"YesNo_title", "Counter_Title", "Segment_Title", "List_Title", "Textbox_title", "TeamSelect_Title", "TeamMatch_Title", "CounterDisplay_Title"};
    private MultiviewTypeAdapter mAdapterTop;
    private MultiviewTypeAdapter mAdapterBot;
    private RecyclerView mRecyclerViewTop;
    private RecyclerView mRecyclerViewBot;
    private ClipboardManager clipboard;
    private int table_status;
    List<Cell> cellList = new ArrayList<>();

    // Create a menu in the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        //MenuItem top_add = menu.findItem(R.id.action_add_top);
        //MenuItem bot_add = menu.findItem(R.id.action_add_bot);
        MenuItem addButton = menu.findItem(R.id.action_add);

        if (preference.getBoolean("edit_mode", false)) {
            menu.findItem(R.id.edit_mode).setTitle("Edit Mode On");
        } else {
            menu.findItem(R.id.edit_mode).setTitle("Edit Mode Off");
        }

        //menu.findItem(R.id.)
//        preference.setInt("table_status", NONE);
//        preference.setBoolean("grid", false);
        // Table Status is used to determine which table to display and what buttons
//        table_status = preference.getInt("table_status", NONE);

        //if (preference.getBoolean("edit_mode", false)) {
        //    top_add.setVisible(true);
        //    top_add.setTitle("Edit Mode");
        //    top_add.setEnabled(false);
        //    bot_add.setVisible(false);
        //} else {
        //    switch (table_status) {
        //        case AUTO:
        //            top_add.setVisible(true);
        //            bot_add.setVisible(false);
        //            top_add.setTitle("Add Auto");
        //            break;
        //        case TELEOP:
        //            top_add.setVisible(false);
        //            bot_add.setVisible(true);
        //            top_add.setTitle("Add Teleop");
        //            break;
        //        case BOTH:
        //            top_add.setVisible(true);
        //            bot_add.setVisible(true);
        //            bot_add.setTitle("Add Teleop");
        //            top_add.setTitle("Add Auto");
        //            break;
        //        case EDIT:
        //            top_add.setVisible(true);
        //            top_add.setTitle("Edit Mode");
        //            top_add.setEnabled(false);
        //            bot_add.setVisible(false);
        //            break;
        //        case NONE:
        //        default:
        //            top_add.setVisible(true);
        //            top_add.setTitle("Add Cell");
        //            bot_add.setVisible(false);
        //            break;
        //    }
        //}

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
            botTables[0].findViewById(R.id.inner_table),
            botTables[1].findViewById(R.id.inner_table),
            botTables[2].findViewById(R.id.inner_table),
            topTables[0].findViewById(R.id.inner_table),
            topTables[1].findViewById(R.id.inner_table),
            topTables[2].findViewById(R.id.inner_table),
        };
//
//        // Set Table Auto/Teleop titles
//        for (int i = 0; i < 3; i++) {
//            TextView topText = topTables[i].findViewById(R.id.table_title);
//            TextView botText = botTables[i].findViewById(R.id.table_title);
//            topText.setTextSize(20);
//            topText.setTextColor(Color.WHITE);
//            botText.setTextSize(20);
//            botText.setTextColor(Color.WHITE);
//            topText.setText(toptitles[i]);
//            botText.setText(bottitles[i]);
//        }
//
//        // Set on click listeners for all buttons to cycle between icons
//        // Uses the team color to determine which icon to use
//        for (TableLayout table : tables) {
//            for (int i = 1; i < table.getChildCount(); i++) {
//                TableRow row = (TableRow) table.getChildAt(i);
//                for (int j = 0; j < row.getChildCount(); j++) {
//                    ImageButton button = (ImageButton) row.getChildAt(j);
//
//                    if (i == 3) {
//                        button.setTag("Both");
//                    } else {
//                        if (j == 0) {
//                            button.setTag("Cone");
//                        } else if (j == 1) {
//                            button.setTag("Cube");
//                        } else if (j == 2) {
//                            button.setTag("Cone");
//                        }
//                    }
//
//                    button.setImageResource(R.drawable.android_x);
//                    final int[] counter = {1};
//                    button.setOnClickListener(v -> {
//                        boolean red = preference.getBoolean("isRedteam");
//                        Log.d(TAG, "onCreate: " + button.getId());
//                        String curTag = String.valueOf(button.getTag());
//                        switch (curTag) {
//                            case "Both":
//                                if (counter[0] == 0) {
//                                    button.setImageResource(R.drawable.android_x);
//                                    counter[0]++;
//                                } else if (counter[0] == 1) {
//                                    if (red) {
//                                        button.setImageResource(R.drawable.red_cube44);
//                                    } else {
//                                        button.setImageResource(R.drawable.cube44);
//                                    }
//                                    counter[0]++;
//                                } else if (counter[0] == 2) {
//                                    if (red) {
//                                        button.setImageResource(R.drawable.red_cone44);
//                                    } else {
//                                        button.setImageResource(R.drawable.cone44);
//                                    }
//                                    counter[0] = 0;
//                                }
//                                break;
//                            case "Cone":
//                                if (counter[0] == 0) {
//                                    button.setImageResource(R.drawable.android_x);
//                                    counter[0]++;
//                                } else if (counter[0] == 1) {
//                                    if (red) {
//                                        button.setImageResource(R.drawable.red_cone44);
//                                    } else {
//                                        button.setImageResource(R.drawable.cone44);
//                                    }
//                                    counter[0] = 0;
//                                }
//                                break;
//                            case "Cube":
//                                if (counter[0] == 0) {
//                                    button.setImageResource(R.drawable.android_x);
//                                    counter[0]++;
//                                } else if (counter[0] == 1) {
//                                    if (red) {
//                                        button.setImageResource(R.drawable.red_cube44);
//                                    } else {
//                                        button.setImageResource(R.drawable.cube44);
//                                    }
//                                    counter[0] = 0;
//                                }
//                                break;
//                        }
//                    });
//                }
//            }
//        }

        // Adds all 5 types of cells to both recycler views
        //testItems(5);

        // Sorts the tables based on saved Table Status
        table_status = preference.getInt("table_status", NONE);
        tableSorter(table_status);

        // Change the team color based on the saved value
//        if (preference.getBoolean("isRedteam")) {
//            for (TableLayout table : tables) {
//                updateTableColor(table, R.color.map_red);
//            }
//        } else {
//            for (TableLayout table : tables) {
//                updateTableColor(table, R.color.map_blue);
//            }
//        }

//        NEEDED for some reason
        testItems(0);

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

        RecyclerView.ItemDecoration itemDecoration = new
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecyclerViewTop.addItemDecoration(itemDecoration);
        mRecyclerViewBot.addItemDecoration(itemDecoration);
    }

    private void editor(RecyclerView recyclerView, MultiviewTypeAdapter adapter, boolean enabled) {
        boolean isTop = recyclerView == mRecyclerViewTop;

        if (enabled) {
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        // TODO: Implement
                    }

                    @Override public void onItemClick(View view, int position) {
                        Cell cell = adapter.mCell.get(position);
                        if (cell != null) {
                            popupLauncher(cell.getType(), cell.getCellId(), position, isTop);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        //Cell cell = adapter.mCell.get(position);
                        //if (cell != null) {
                        //    clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        //    ClipData clip = ClipData.newPlainText("Cell", cell.toString());
                        //    clipboard.setPrimaryClip(clip);
                        //}
                    }
                }));
        } else {
            recyclerView.removeOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                    }

                    @Override public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    }
                }));
        }
    }

    private void testItems(int cells) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<MultiviewTypeAdapter> jsonAdapter = moshi.adapter(MultiviewTypeAdapter.class);

        CellParam cellParam;
        for (int i = 0; i < cells; i++) {
            String cellType = cellTypes[i];
            cellParam = new CellParam(cellType);
            switch (cellType) {
                case "YesNo":
                    cellParam.setType(getString(R.string.YesNoType));
                    cellParam.setHelpText("This is a YesNo text");
                    break;
                case "Counter":
                    cellParam.setType(getString(R.string.CounterType));
                    cellParam.setDefault(3);
                    cellParam.setMax(5);
                    cellParam.setMin(0);
                    cellParam.setUnit(1);
                    cellParam.setHelpText("This is a Counter text");
                    break;
                case "Segment":
                    cellParam.setType(getString(R.string.SegmentType));
                    cellParam.setSegments(6);
                    cellParam.setSegmentLabels(Arrays.asList("One", "2", "Three", "4", "Five", "6"));
                    cellParam.setHelpText("This is a Segment text");
                    break;
                case "List":
                    cellParam.setType(getString(R.string.ListType));
                    cellParam.setTotalEntries(3);
                    cellParam.setEntryLabels(Arrays.asList("Java", "C++", "Labview"));
                    cellParam.setHelpText("This is a List text");
                    break;
                case "Text":
                    cellParam.setType(getString(R.string.TextType));
                    cellParam.setTextHidden(false);
                    cellParam.setTextHint("Enter life here");
                    cellParam.setHelpText("This is a Text text");
                    break;
                case "TeamSelect":
                    cellParam.setType(getString(R.string.TeamSelectType));
                    cellParam.setHelpText("This is a TeamSelect text");
                    break;
                case "TeamMatch":
                    cellParam.setType(getString(R.string.TeamMatchType));
                    cellParam.setHelpText("This is a TeamMatch text");
                    break;
                case "CounterDisplay":
                    cellParam.setType("CounterDisplay");
                    cellParam.setHelpText("This is an example");
                    break;
            }
            Cell cell = new Cell(i, cellTitles[i], cellType, cellParam);
            cellList.add(cell);
        }

        MultiviewTypeAdapter myAdapter = new MultiviewTypeAdapter(cellList);

        String test = jsonAdapter.toJson(myAdapter);
        try {
            MultiviewTypeAdapter config = jsonAdapter.fromJson(test);
            MultiviewTypeAdapter configBot = jsonAdapter.fromJson(test);
            mAdapterTop = new MultiviewTypeAdapter(Objects.requireNonNull(config).mCell);
            mAdapterBot = new MultiviewTypeAdapter(Objects.requireNonNull(configBot).mCell);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error parsing JSON", e);
            mAdapterTop = new MultiviewTypeAdapter(Collections.emptyList());
            mAdapterBot = new MultiviewTypeAdapter(Collections.emptyList());
        }
        /* } */
        mAdapterTop.notifyDataSetChanged();
        mAdapterBot.notifyDataSetChanged();
        mRecyclerViewTop.setAdapter(mAdapterTop);
        mRecyclerViewBot.setAdapter(mAdapterBot);
    }

    public void tableSorter(int table_status) {
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

    // Use values from popup to create new recycler view item
    public void popupLauncher(String cellType, int realPos, int pos, boolean isTop) {
        switch (cellType) {
            case "YesNo":
                showYesNoDialog(preference.getString(realPos + "_title_value", "Title"),
                    preference.getString(realPos + "_help_value"), pos, realPos, isTop);
                break;
            case "Counter":
                showCounterDialog(preference.getString(realPos + "_title_value", "Title"),
                    preference.getString(realPos + "_help_value"), pos, realPos, isTop);
                break;
            case "Segment":
                showSegmentDialog(preference.getString(realPos + "_title_value", "Title"),
                    preference.getString(realPos + "_help_value"), pos, realPos, isTop);
                break;
            case "List":
                showListDialog(preference.getString(realPos + "_title_value", "Title"),
                    preference.getString(realPos + "_help_value"), pos, realPos, isTop);
                break;
            case "Text":
                showTextboxDialog(preference.getString(realPos + "_title_value", "Title"),
                    preference.getString(realPos + "_help_value"), pos, realPos, isTop);
                break;
            case "TeamSelect":
                showTeamSelectDialog(preference.getString(1 + "_title_value", "Title"),
                    preference.getString(realPos + "_help_value"), pos, realPos, isTop);
                break;
            case "TeamMatch":
                showTeamMatchDialog(preference.getString(1 + "_title_value", "Title"),
                    preference.getString(realPos + "_help_value"), pos, realPos, isTop);
                break;
            case "DualCounter":
                showDualCounterDialog(preference.getString(1 + "_title_value", "Title"),
                        preference.getString(realPos + "_help_value"), pos, realPos, isTop);
            case "Table":
                break;
        }
    }

    private void updateTableColor(TableLayout table, int colorResId/*, int textColor*/) {
        TableRow row = (TableRow) table.getChildAt(0);
        row.setBackgroundColor(ContextCompat.getColor(this, colorResId));
        TextView title = row.findViewById(R.id.table_title);
        title.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    // import cells from json string
    public void import_cells(String optional_json, RecyclerView mRecyclerView) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<MultiviewTypeAdapter> jsonAdapter = moshi.adapter(MultiviewTypeAdapter.class);
        RecyclerAdapter mRecyclerViewAdapter;
        try {
            MultiviewTypeAdapter config = jsonAdapter.fromJson(optional_json);
            mRecyclerViewAdapter = new RecyclerAdapter(Objects.requireNonNull(config).mCell);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            Log.d(TAG, "import: " + optional_json);
        } catch (IOException e) {
            Log.e(TAG, "Error parsing JSON", e);
            mRecyclerViewAdapter = new RecyclerAdapter(Collections.emptyList());
        }
        mRecyclerViewAdapter.notifyItemRangeInserted(0, mAdapterTop.getItemCount());
    }

    public void save_json() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<MultiviewTypeAdapter> jsonAdapter = moshi.adapter(MultiviewTypeAdapter.class);
        String jsonTop = jsonAdapter.toJson(mAdapterTop);
        String jsonBot = jsonAdapter.toJson(mAdapterBot);

        File file = new File(this.getExternalFilesDir(null), "exported_layout.json");
        try {
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(jsonTop.getBytes());
//                stream.write(jsonBot.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "export: " + jsonTop);
        Log.d(TAG, "export: " + jsonBot);
    }

    public String export_string() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<MultiviewTypeAdapter> jsonAdapter = moshi.adapter(MultiviewTypeAdapter.class);

        int table = preference.getInt("table_status", BOTH);
        String jsonTop = jsonAdapter.toJson(mAdapterTop);
        String jsonBot = jsonAdapter.toJson(mAdapterBot);
        Log.d(TAG, "export String: " + jsonTop);
        Log.d(TAG, "export String: " + jsonBot);
        // Choose which recycler view to export
        switch (table) {
            case NONE:
                return jsonTop;
            case BOTH:
                return jsonTop + "^" + jsonBot;
            default:
                return "Error: No table selected";
        }
    }

    public String exportTable() {
        // Export the table as a string
        StringBuilder export = new StringBuilder();
        for (TableLayout table : tables) {
            // Append a ^ between table 3 and 4
            if (table == tables[3]) {
                export.append("^");
            }
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
                    // Don't append a comma after the last item in the first table as welll
                    } else if (table == tables[tables.length - 4] && i == table.getChildCount() - 1 && j == row.getChildCount() - 1) {
                        export.append(id);
                    } else {
                        export.append(id).append(",");
                    }
                }
            }
        }
        return export.toString();
    }

    public void createItem(MultiviewTypeAdapter mAdapter, RecyclerView mRecyclerView, Cell newCell) {
        List<Cell> cells = new ArrayList<>();
        cells.add(newCell);
        mAdapter.mCell.addAll(cells);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyItemInserted(mAdapter.getItemCount());
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

        if (id == R.id.action_page_settings) {
            showPageSettingsDialog("Page Settings");
        }

        if (id == R.id.action_export) {
            String fullJson = export_string();

            // Make a dialog with text that can be copied
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.ExportTitle);
            // Create a TextView to display the export string
            TextView textView = new TextView(this);
            textView.setText(fullJson);

            // Set the TextView to be focusable and selectable
            textView.setFocusable(true);
            textView.setTextIsSelectable(true);
            textView.setSelectAllOnFocus(true);

            // Add the TextView to the dialog
            builder.setView(textView);

            // Create a ClipData object to hold the text
            ClipData clip_export_json = ClipData.newPlainText("Json Export", fullJson);

            builder.setPositiveButton(R.string.ExportButtonTitle, (dialog, which) -> {
                // Send the ClipData to the clipboard
                clipboard.setPrimaryClip(clip_export_json);
                save_json();
                dialog.dismiss();
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
            builder.show();
        }

        if (id == R.id.edit_mode) {
            boolean editMode = preference.getBoolean("edit_mode", false);
            preference.setBoolean("edit_mode", !editMode);

            if (editMode) {
                editor(mRecyclerViewTop, mAdapterTop, true);
                editor(mRecyclerViewBot, mAdapterBot, true);
//                for (TableLayout table : tables) {
//                    updateTableColor(table, R.color.green_900);
//                }
                Toast.makeText(this, "Edit Mode turned on, select a cell to edit it", Toast.LENGTH_LONG).show();
            } else {
                editor(mRecyclerViewTop, mAdapterTop, false);
                editor(mRecyclerViewBot, mAdapterBot, false);
                item.setTitle("Edit Mode Off");
//                if (preference.getBoolean("isRedteam")) {
//                    for (TableLayout table : tables) {
//                        updateTableColor(table, R.color.map_red);
//                    }
//                } else {
//                    for (TableLayout table : tables) {
//                        updateTableColor(table, R.color.map_blue);
//                    }
//                }
                Toast.makeText(this, "Edit Mode turned off", Toast.LENGTH_LONG).show();
            }

            invalidateOptionsMenu();
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

                    String top = clips.getText().toString().split("\\^")[0];
//                    String bot = clips.getText().toString().split("\\^")[1];
//                    String topTable = exportTable().split("\\^")[0];
//                    String botTable = exportTable().split("\\^")[1];

//                    Toast.makeText(this, topTable, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(this, botTable, Toast.LENGTH_SHORT).show();

                    import_cells(top, mRecyclerViewTop);
//                    import_cells(bot, mRecyclerViewBot);
                }

                dialog.dismiss();
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
            builder.show();
        }

        // Create a dialog with a list of cell types
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.CellBuilderDialogTitle);

        if (id == R.id.action_add) {
            builder.setItems(cellTypes, (dialog, which) -> popupLauncher(cellTypes[which],mRecyclerViewTop.getChildCount(), mRecyclerViewTop.getChildCount(), true));
            builder.show();

            // Add dialog for 2 tables
//            AlertDialog.Builder addBuilder = new AlertDialog.Builder(this);
//            AlertDialog.Builder typeBuilder = new AlertDialog.Builder(this);
//            addBuilder.setTitle("Would you like to add a cell to the top");
//
//            addBuilder.setNeutralButton("Cancel", (dialog, which) -> {
//                dialog.dismiss();
//            });
//            addBuilder.setPositiveButton("Bot", (dialog, which) -> {
//                typeBuilder.setItems(cellTypes, (typeDialog, typeWhich) -> popupLauncher(cellTypes[typeWhich],mRecyclerViewBot.getChildCount(),
//                    mRecyclerViewBot.getChildCount(), false));
//                typeBuilder.show();
//                dialog.dismiss();
//            });
//            addBuilder.setNegativeButton("Top", (dialog, which) -> {
//                typeBuilder.setItems(cellTypes, (typeDialog, typeWhich) -> popupLauncher(cellTypes[typeWhich],mRecyclerViewTop.getChildCount(),
//                    mRecyclerViewTop.getChildCount(),true));
//                typeBuilder.show();
//                dialog.dismiss();
//            });
//            addBuilder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPageSettingsDialog(String title) {
        DialogFragment dialog = new TextboxDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", 1);
        args.putBoolean("location", false);
        args.putInt("real_id", 1);
        args.putString("help", "help");
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "PageSettingsDialog");
    }

    private void showTeamSelectDialog(String title, String help, int viewId, int realViewId, boolean location) {
        DialogFragment dialog = new TeamSelectDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        args.putInt("real_id", realViewId);
        args.putString("help", help);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "TeamSelectDialog");
    }

    private void showTeamMatchDialog(String title, String help, int viewId, int realViewId, boolean location) {
        DialogFragment dialog = new TeamMatchDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        args.putInt("real_id", realViewId);
        args.putString("help", help);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "TitleDialog");
    }

    private void showTextboxDialog(String title, String help, int viewId, int realViewId, boolean location) {
        DialogFragment dialog = new TextboxDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        args.putInt("real_id", realViewId);
        args.putString("help", help);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "TextboxDialog");
    }

    public void showListDialog(String title, String help, int viewId, int realViewId, boolean location) {
        DialogFragment dialog = new ListDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        args.putInt("real_id", realViewId);
        args.putString("help", help);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "ListDialog");
    }

    private void showSegmentDialog(String title, String help, int viewId, int realViewId, boolean location) {
        DialogFragment dialog = new SegmentDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        args.putInt("real_id", realViewId);
        args.putString("help", help);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "SegmentDialog");
    }

    private void showCounterDialog(String title, String help, int viewId, int realViewId, boolean location) {
        DialogFragment dialog = new CounterDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        args.putInt("real_id", realViewId);
        args.putString("help", help);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "CounterDialog");
    }

    private void showDualCounterDialog(String title, String help, int viewId, int realViewId, boolean location) {
        DialogFragment dialog = new DualCounterDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        args.putInt("real_id", realViewId);
        args.putString("help", help);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "DualCounterDialog");
    }

    public void showYesNoDialog(String title, String help, int viewId, int realViewId, boolean location) {
        DialogFragment dialog = new YesNoDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("id", viewId);
        args.putBoolean("location", location);
        args.putInt("real_id", realViewId);
        args.putString("help", help);
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager().beginTransaction(), "YesNoDialog");
    }

    @Override
    public void onYesNoDialogPositiveClick(Cell newCell, boolean location, int position,
        int realPosition) {
        MultiviewTypeAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;

        String locationString = location ? "top" : "bot";

        if (!preference.getBoolean("edit_mode", false)) {
            createItem(mAdapter, mRecyclerView, newCell);
        } else {
            // Cell is null if we want to delete the cell
            if (newCell != null) {
                Cell curCell = mAdapter.mCell.get(position);
                curCell.setTitle(newCell.getTitle());
                curCell.setParam(newCell.getParam());
                mAdapter.notifyItemChanged(position);
            } else {
                preference.remove(locationString + "_" + position + "_title_value");
                preference.remove(locationString + "_" + position + "_help_value");
                mAdapter.mCell.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    @Override
    public void onCounterDialogPositiveClick(Cell newCell, boolean location, int position,
        int realPosition) {
        MultiviewTypeAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        if (!preference.getBoolean("edit_mode", false)) {
            createItem(mAdapter, mRecyclerView, newCell);
        } else {
            // Cell is null if we want to delete the cell
            if (newCell != null) {
                Cell curCell = mAdapter.mCell.get(position);
                curCell.setTitle(newCell.getTitle());
                curCell.setParam(newCell.getParam());
                mAdapter.notifyItemChanged(position);
            } else {
                preference.remove(position + "_default_picker_value");
                preference.remove(position + "_min_picker_value");
                preference.remove(position + "_max_picker_value");
                preference.remove(position + "_unit_picker_value");
                preference.remove(position + "_title_value");
                preference.remove(position + "_help_value");
                mAdapter.mCell.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    @Override
    public void onDualCounterDialogPositiveClick(Cell newCell, boolean location, int position,
                                             int realPosition) {
        MultiviewTypeAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        if (!preference.getBoolean("edit_mode", false)) {
            createItem(mAdapter, mRecyclerView, newCell);
        } else {
            // Cell is null if we want to delete the cell
            if (newCell != null) {
                Cell curCell = mAdapter.mCell.get(position);
                curCell.setTitle(newCell.getTitle());
                curCell.setParam(newCell.getParam());
                mAdapter.notifyItemChanged(position);
            } else {
                preference.remove(position + "_default_picker_value");
                preference.remove(position + "_min_picker_value");
                preference.remove(position + "_max_picker_value");
                preference.remove(position + "_unit_picker_value");
                preference.remove(position + "_title_value");
                preference.remove(position + "_help_value");
                mAdapter.mCell.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    @Override public void onSegmentDialogPositiveClick(Cell newCell, boolean location, int position,
        int realPosition) {
        MultiviewTypeAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        if (!preference.getBoolean("edit_mode", false)) {
            createItem(mAdapter, mRecyclerView, newCell);
        } else {
            // Cell is null if we want to delete the cell
            if (newCell != null) {
                Cell curCell = mAdapter.mCell.get(position);
                curCell.setTitle(newCell.getTitle());
                curCell.setParam(newCell.getParam());
                mAdapter.notifyItemChanged(position);
            } else {
                for (int i = 0; i < preference.getInt(position + "_segment_count"); i++) {
                    preference.remove(1 + "_segment_text_" + i);
                }
                preference.remove(position + "_segment_count");
                preference.remove(position + "_title_value");
                preference.remove(position + "_help_value");
                mAdapter.mCell.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    @Override public void onListDialogPositiveClick(Cell newCell, boolean location, int position,
        int realId) {
        MultiviewTypeAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        if (!preference.getBoolean("edit_mode", false)) {
            createItem(mAdapter, mRecyclerView, newCell);
        } else {
            // Cell is null if we want to delete the cell
            if (newCell != null) {
                Cell curCell = mAdapter.mCell.get(position);
                curCell.setTitle(newCell.getTitle());
                curCell.setParam(newCell.getParam());
                mAdapter.notifyItemChanged(position);
            } else {
                // TODO: Remove all list items in ListDialog directly
                preference.remove(position + "_list_count");
                preference.remove(position + "_title_value");
                preference.remove(position + "_help_value");
                mAdapter.mCell.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    @Override public void onTextboxDialogPositiveClick(Cell newCell, boolean location, int position,
        int realId) {
        MultiviewTypeAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        if (!preference.getBoolean("edit_mode", false)) {
            createItem(mAdapter, mRecyclerView, newCell);
        } else {
            // Cell is null if we want to delete the cell
            if (newCell != null) {
                Cell curCell = mAdapter.mCell.get(position);
                curCell.setTitle(newCell.getTitle());
                curCell.setParam(newCell.getParam());
                mAdapter.notifyItemChanged(position);
            } else {
                preference.remove(position + "_hint_value");
                preference.remove(position + "_title_value");
                preference.remove(position + "_help_value");
                mAdapter.mCell.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    @Override public void onTeamSelectDialogPositiveClick(Cell newCell, boolean location, int position,
        int realPosition) {
        MultiviewTypeAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        if (!preference.getBoolean("edit_mode", false)) {
            createItem(mAdapter, mRecyclerView, newCell);
        } else {
            // Cell is null if we want to delete the cell
            if (newCell != null) {
                Cell curCell = mAdapter.mCell.get(position);
                curCell.setTitle(newCell.getTitle());
                curCell.setParam(newCell.getParam());
                mAdapter.notifyItemChanged(position);
            } else {
                preference.remove(position + "_help_value");
                preference.remove(position + "_title_value");
                mAdapter.mCell.remove(position);
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    //public void createItem(RecyclerAdapter mAdapter, RecyclerView mRecyclerView, Cell newCell) {
    //    List<Cell> cells = new ArrayList<>();
    //    cells.add(newCell);
    //    mAdapter.mCell.addAll(cells);
    //    mRecyclerView.setAdapter(mAdapter);
    //    mAdapter.notifyItemInserted(mAdapter.mCell.size());
    //}

    @Override public void onTeamMatchDialogPositiveClick(Cell newCell, boolean location, int position,
        int realPosition) {
        MultiviewTypeAdapter mAdapter = location ? mAdapterTop : mAdapterBot;
        RecyclerView mRecyclerView = location ? mRecyclerViewTop : mRecyclerViewBot;
        if (!preference.getBoolean("edit_mode", false)) {
            createItem(mAdapter, mRecyclerView, newCell);
        } else {
            // Cell is null if we want to delete the cell
            if (newCell != null) {
                Cell curCell = mAdapter.mCell.get(position);
                curCell.setTitle(newCell.getTitle());
                curCell.setParam(newCell.getParam());
                mAdapter.notifyItemChanged(position);
            } else {
                preference.remove(position + "_help_value");
                preference.remove(position + "_title_value");
                mAdapter.mCell.remove(position);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyItemRemoved(position);
            }
        }
    }

    @Override public void onPageSettingsDialogPositiveClick(int table_status, boolean grid) {
        tableSorter(table_status);

        // Toggle and update the text
        if (grid) {
            mRecyclerViewTop.setLayoutManager(new GridLayoutManager(this, 2));
            mRecyclerViewBot.setLayoutManager(new GridLayoutManager(this, 2));
            preference.setBoolean("grid", true);

        } else {
            mRecyclerViewTop.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerViewBot.setLayoutManager(new LinearLayoutManager(this));
            preference.setBoolean("grid", false);
        }
    }
}