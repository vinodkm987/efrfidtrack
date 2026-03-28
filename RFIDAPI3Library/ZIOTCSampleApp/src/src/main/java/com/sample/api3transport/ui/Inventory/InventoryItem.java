package com.sample.api3transport.ui.Inventory;

import com.zebra.rfid.api3.SYSTEMTIME;

public class InventoryItem {

    private String EPC;
    private String TID;
    private String User;
    private int PC;
    private String CRC;
    private short Phase;
    private short Antenna;
    private String Channel;
    private int count;
    private int RSSI;
    private SYSTEMTIME firstSeenTimeStamp;
    private SYSTEMTIME lastSeenTimeStamp;

    public InventoryItem(String EPC, String TID, String User, int count, int RSSI, int PC, String CRC,
                         short Phase, short Antenna, String Channel, SYSTEMTIME firstSeenTimeStamp, SYSTEMTIME lastSeenTimeStamp) {
        this.EPC = EPC;
        this.TID = TID;
        this.User = User;
        this.count = count;
        this.RSSI = RSSI;
        this.PC = PC;
        this.CRC = CRC;
        this.Phase = Phase;
        this.Antenna = Antenna;
        this.Channel = Channel;
        this.firstSeenTimeStamp = firstSeenTimeStamp;
        this.lastSeenTimeStamp = lastSeenTimeStamp;
    }

    public String getEPC() {
        return EPC;
    }

    public String getTID() {
        return TID;
    }

    public String getUser() {
        return User;
    }

    public int getPC() {
        return PC;
    }

    public String getCRC() {
        return CRC;
    }

    public short getPhase() {
        return Phase;
    }

    public short getAntenna() {
        return Antenna;
    }

    public String getChannel() {
        return Channel;
    }

    public int getCount() {
        return count;
    }

    public int getRSSI() {
        return RSSI;
    }

    public SYSTEMTIME getFirstSeenTimeStamp() {
        return firstSeenTimeStamp;
    }

    public SYSTEMTIME getLastSeenTimeStamp() {
        return lastSeenTimeStamp;
    }


}
