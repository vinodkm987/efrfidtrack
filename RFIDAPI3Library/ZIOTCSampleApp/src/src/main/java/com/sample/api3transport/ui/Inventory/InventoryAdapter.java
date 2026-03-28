package com.sample.api3transport.ui.Inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sample.api3transport.R;

import java.util.ArrayList;
import java.util.Objects;

public class InventoryAdapter extends ArrayAdapter<InventoryItem> {

    ArrayList<InventoryItem> items;

    public InventoryAdapter(@NonNull Context context, ArrayList<InventoryItem> items) {
        super(context, 0, items);
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.inventory_list_item, parent, false);
        }
        TextView tagDataEpc = convertView.findViewById(R.id.tag_data_epc);
        TextView tagDataTid = convertView.findViewById(R.id.tag_data_tid);
        TextView tagDataUser = convertView.findViewById(R.id.tag_data_user);
        TextView tagRSSI = convertView.findViewById(R.id.tag_rssi);
        TextView tagCount = convertView.findViewById(R.id.tag_seen_count);
        TextView tagPc = convertView.findViewById(R.id.tag_pc);
        TextView tagCrc = convertView.findViewById(R.id.tag_crc);
        TextView tagPhase = convertView.findViewById(R.id.tag_phase);
        TextView tagAntenna = convertView.findViewById(R.id.tag_antenna);
        TextView tagChannel = convertView.findViewById(R.id.tag_channel);
        TextView tagFirstSeenTimeStamp = convertView.findViewById(R.id.first_seen);
        TextView tagLastSeenTimeStamp = convertView.findViewById(R.id.last_seen);

        InventoryItem item = getItem(position);
        if(item.getEPC() != null){
            tagDataEpc.setText(String.format("EPC: %s", item.getEPC()));
        }else{
            tagDataEpc.setVisibility(View.GONE);
        }
        if(item.getTID() != null){
            tagDataTid.setText(String.format("TID: %s", item.getTID()));
        }else{
            tagDataTid.setVisibility(View.GONE);
        }
        if(item.getUser() != null){
            tagDataUser.setText(String.format("USER: %s", item.getUser()));
        }else{
            tagDataUser.setVisibility(View.GONE);
        }
        if(item.getRSSI() != 0){
            tagRSSI.setText(String.format("RSSI: %s", item.getRSSI()));
        }else{
            tagRSSI.setVisibility(View.GONE);
        }
        if(item.getCount() != 0){
            tagCount.setText(String.format("Read Count: %s", item.getCount()));
        }else{
            tagCount.setVisibility(View.GONE);
        }
        if(item.getPC() != 0){
            tagPc.setText(String.format("PC: %s", item.getPC()));
        }else{
            tagPc.setVisibility(View.GONE);
        }
        if(item.getCRC() != null && !Objects.equals(item.getCRC(), "0")){
            tagCrc.setText(String.format("CRC: %s", item.getCRC()));
        }else{
            tagCrc.setVisibility(View.GONE);
        }
        if(item.getPhase() != 0){
            tagPhase.setText(String.format("Phase: %s", item.getPhase()));
        }else{
            tagPhase.setVisibility(View.GONE);
        }
        if(item.getAntenna() != 0){
            tagAntenna.setText(String.format("Antenna: %s", item.getAntenna()));
        }else{
            tagAntenna.setVisibility(View.GONE);
        }
        if(item.getChannel() != null && !Objects.equals(item.getChannel(), "0")){
            tagChannel.setText(String.format("Channel: %s", item.getChannel()));
        }else{
            tagChannel.setVisibility(View.GONE);
        }
        if(item.getFirstSeenTimeStamp().Year != 0){
            tagFirstSeenTimeStamp.setText(String.format("First Seen: %s", item.getFirstSeenTimeStamp().ConvertTimetoString()));
        }else{
            tagFirstSeenTimeStamp.setVisibility(View.GONE);
        }
        if(item.getLastSeenTimeStamp().Year != 0){
            tagLastSeenTimeStamp.setText(String.format("Last Seen: %s", item.getLastSeenTimeStamp().ConvertTimetoString()));
        }else{
            tagLastSeenTimeStamp.setVisibility(View.GONE);
        }

        return convertView;
    }

}
