package com.sample.api3transport.ui.Inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sample.api3transport.R;

import java.util.ArrayList;

public class AntennaAdapter extends RecyclerView.Adapter<AntennaAdapter.ViewHolder> {

    private ArrayList<AntennaItem> antennaItem;

    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(int antenna, boolean isChecked);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final CheckBox checkbox;
        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.txtName);
            checkbox = (CheckBox) view.findViewById(R.id.checkBox);
        }
        public TextView getTextView() {
            return textView;
        }
        public CheckBox getCheckBox() {
            return checkbox;
        }
    }

    public AntennaAdapter(ArrayList<AntennaItem> dataSet) {
        antennaItem = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.inventory_antenna_checkbox, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(String.valueOf(antennaItem.get(position).getAntennaID()));
        viewHolder.getCheckBox().setChecked(antennaItem.get(position).isChecked());
        viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position + 1, isChecked);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return antennaItem.size();
    }
}
