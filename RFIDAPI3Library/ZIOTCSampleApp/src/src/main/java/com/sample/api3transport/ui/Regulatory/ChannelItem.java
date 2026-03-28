package com.sample.api3transport.ui.Regulatory;

public class ChannelItem {

    private String channelName;
    private boolean isChecked;


    public ChannelItem(String channelName, boolean isChecked) {
        this.channelName = channelName;
        this.isChecked = isChecked;
    }

    public String getChannelName() {
        return channelName;
    }

    public boolean isChecked() {
        return isChecked;
    }


}
