package com.sample.api3transport.ui.Regulatory;

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

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {

    private ArrayList<ChannelItem> chanelItem;

    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(String channel, boolean isChecked);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final CheckBox checkbox;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.channelTxtName);
            checkbox = view.findViewById(R.id.channelCheckBox);
        }
        public TextView getTextView() {
            return textView;
        }
        public CheckBox getCheckBox() {
            return checkbox;
        }
    }

    public ChannelAdapter(ArrayList<ChannelItem> dataSet) {
        chanelItem = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.regulatory_channel_checkbox, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(String.valueOf(chanelItem.get(position).getChannelName()));
        viewHolder.getCheckBox().setChecked(chanelItem.get(position).isChecked());
        viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(chanelItem.get(position).getChannelName(), isChecked);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chanelItem.size();
    }
}
