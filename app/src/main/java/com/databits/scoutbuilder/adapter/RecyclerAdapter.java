package com.databits.scoutbuilder.adapter;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.addisonelliott.segmentedbutton.SegmentedButton;
import com.databits.scoutbuilder.R;
import com.databits.scoutbuilder.model.Cell;
import com.databits.scoutbuilder.model.CellParam;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.travijuu.numberpicker.library.NumberPicker;

import java.io.IOException;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements
    ItemTouchHelperAdapter {
    public List<Cell> mCell;

    public RecyclerAdapter(List<Cell> cells) {
        mCell = cells;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Cell cell = mCell.get(position);
        try {
            holder.bind(cell);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mCell.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // Move the item in the list and notify the adapter that the item has moved
        Collections.swap(mCell, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        // Remove the item from the list and notify the adapter that the item has been removed
        //mCell.remove(position);
        //notifyItemRemoved(position);
    }

    @Override public void onItemSelected() {
        // Do nothing
    }

    @Override public void onItemClear() {
        // Do nothing
    }

    @SuppressLint("InflateParams")
    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView mCellTitleTextView;
        private final View mRootView;
        Moshi moshi = new Moshi.Builder().build();
        List<String> entryLabels = new ArrayList<>();
        List<String> segmentLabels = new ArrayList<>();

        public ViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            mCellTitleTextView = itemView.findViewById(R.id.cell_title);
        }

        public void bind(Cell cell) throws JSONException, IOException {
            mCellTitleTextView.setGravity(Gravity.CENTER);
            mCellTitleTextView.setText(cell.getTitle());

            JsonAdapter<CellParam> paramAdapter = moshi.adapter(CellParam.class);
            CellParam cellParams = paramAdapter.fromJson(paramAdapter.toJson(cell.getParam()));

            String cellType = cellParams.getCellType();
            LinearLayout cellContainerLayout = mRootView.findViewById(R.id.cell_container);
            cellContainerLayout.removeAllViews();
            switch (cellType) {
                case "YesNo":
                    View YesNo = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_yes_no_segmented, null);
                    cellContainerLayout.addView(YesNo);
                    break;
                case "Counter":
                    View Counter = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_inside_number_picker, null);
                    NumberPicker currentPicker = Counter.findViewById(R.id.number_counter_inside);
                    currentPicker.setMax(cellParams.getCellMax());
                    currentPicker.setMin(cellParams.getCellMin());
                    currentPicker.setUnit(cellParams.getCellUnit());
                    currentPicker.setValue(cellParams.getCellDefault());
                    currentPicker.setFocusable(false);
                    cellContainerLayout.addView(Counter);
                    break;
                case "Segment":
                    View inner = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_multi_segmented, null);

                    int segmentCount = cellParams.getCellSegments();

                    SegmentedButton[] buttons = {
                        inner.findViewById(R.id.button_one),
                        inner.findViewById(R.id.button_two),
                        inner.findViewById(R.id.button_three),
                        inner.findViewById(R.id.button_four),
                        inner.findViewById(R.id.button_five),
                        inner.findViewById(R.id.button_six)
                    };

                    for (int i = 0; i < segmentCount; i++) {
                        segmentLabels.add(cellParams.getCellSegmentLabels().get(i));
                    }

                    for (int i = 0; i < segmentLabels.size() & segmentCount > i; i++) {
                        buttons[i].setText(segmentLabels.get(i));
                    }

                    for (int i = 0; i < buttons.length; i++) {
                        buttons[i].setVisibility(i < segmentCount ? View.VISIBLE : View.GONE);
                    }

                    cellContainerLayout.addView(inner);
                    break;
                case "List":
                    Spinner spinner = new Spinner(mRootView.getContext());
                    // Loop through add each item to entryLabels
                    for (int i = 0; i < cellParams.getCellTotalEntries(); i++) {
                        entryLabels.add(cellParams.getCellEntryLabels().get(i));
                    }
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(mRootView.getContext(),
                        android.R.layout.simple_spinner_item, entryLabels);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerArrayAdapter);
                    cellContainerLayout.addView(spinner);
                    break;
                case "Text":
                    View textInner = LayoutInflater.from(mRootView.getContext()).inflate(R.layout.ui_textbox_maker, null);

                    TextInputLayout textInputLayout = textInner.findViewById(R.id.textbox_text_layout);

                    textInputLayout.setId(R.id.textbox_text_layout);
                    if (!cellParams.isCellTextHidden()) {
                        textInputLayout.setHint(cellParams.getCellTextHint());
                    }

                    cellContainerLayout.addView(textInner);
                    break;
                default:
                    TextView defaultTextView = new TextView(mRootView.getContext());
                    defaultTextView.setText(R.string.unrecognized_cell_type);
                    cellContainerLayout.addView(defaultTextView);
                    break;
            }
        }
    }
}