package com.sample.api3transport.ui.Inventory;

public class AntennaItem {

    private int antennaID;
    private boolean isChecked;


    public AntennaItem(int antennaID, boolean isChecked) {
        this.antennaID = antennaID;
        this.isChecked = isChecked;
    }

    public int getAntennaID() {
        return antennaID;
    }

    public boolean isChecked() {
        return isChecked;
    }


}
