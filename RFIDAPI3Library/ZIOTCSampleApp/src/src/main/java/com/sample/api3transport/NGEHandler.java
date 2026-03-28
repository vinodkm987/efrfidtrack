package com.sample.api3transport;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.api3transport.ui.Firmware.FWUpdateDataViewModel;
import com.sample.api3transport.ui.Inventory.TagDataViewModel;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.AccessFilter;
import com.zebra.rfid.api3.AntennaInfo;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.CableLossCompensation;
import com.zebra.rfid.api3.ENUM_OPERATING_MODE;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.FILTER_ACTION;
import com.zebra.rfid.api3.FILTER_MATCH_PATTERN;
import com.zebra.rfid.api3.GPITrigger;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.LOCK_DATA_FIELD;
import com.zebra.rfid.api3.LOCK_PRIVILEGE;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.MultiLocateParams;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.PreFilters;
import com.zebra.rfid.api3.READER_POWER_STATE;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATE_AWARE_ACTION;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.StartTrigger;
import com.zebra.rfid.api3.StopTrigger;
import com.zebra.rfid.api3.TARGET;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TagPatternBase;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NGEHandler {

    final static String TAG = "RFID_SAMPLE";
    private Readers readers;
    private ArrayList<ReaderDevice> availableRFIDReaderList;
    private ReaderDevice readerDevice;
    public static RFIDReader mReader;
    TextView textView;
    private EventHandler eventHandler;
    private MainActivity context;
    String readername = "RFD40+_212735201D0086";

    public static boolean isInventoryRunning = false;

    public static TagDataViewModel tagDataViewModel;
    public static FWUpdateDataViewModel fwUpdateDataViewModel;
    public static MenuViewModel menuViewModel;

    public static String hostName = "";
    public static String ProtocolType;
    public static int ZIOTCport = 443;
    public static int LLRPport = 5084;
    public static int timeoutMilliSeconds = 2000;
    public static ArrayList<String> tagIDs = new ArrayList<>();
    void onCreate(MainActivity activity) {
        context = activity;
//        textView = activity.statusTextViewRFID;
//        // SDK
//        if (readers == null) {

//            readers = new Readers(activity, ENUM_TRANSPORT.SERVICE_USB);
//        try {
//            availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
//        } catch (InvalidUsageException e) {
//            throw new RuntimeException(e);
//        }

            ///readers = new Readers(activity.getApplicationContext(), ENUM_TRANSPORT.ZIOTC);
            readers = new Readers(activity.getApplicationContext(), ENUM_TRANSPORT.RE_SERIAL);
        try {
            availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
        } catch (InvalidUsageException e) {
            throw new RuntimeException(e);
        }

//            RFIDReader mRfidReader = new RFIDReader("192.168.1.9", 443, 2000, "ZIOTC", "ZIOTC");
//            //RFIDReader mRfidReader = new RFIDReader("10.17.231.215", 443, 2000, "ZIOTC", "ZIOTC");
//            reader = mRfidReader;
//        }
//        new GetReaderListTask().execute();
    }

    public void Test() {
        // call use case
        //Test1();
    }

    public void MultiTag() {

        //Ability to search up to 4 tags simultaneously
        TagPatternBase[] tagpatterns = new TagPatternBase[3];
        tagpatterns[0] = new TagPatternBase();
        tagpatterns[0].setTagPattern("10000179F2E24166066F6894A9706ACD");
        tagpatterns[0].setBitOffset(32);
        tagpatterns[0].setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);


        tagpatterns[1] = new TagPatternBase();
        tagpatterns[1].setTagPattern("100001791F0E1B5BCF856BF09B02F868");
        tagpatterns[1].setBitOffset(32);
        tagpatterns[1].setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);




        tagpatterns[2] = new TagPatternBase();
        tagpatterns[2].setTagPattern("10000179F3FB785478D184F26A790DE1");
        tagpatterns[2].setBitOffset(32);
        tagpatterns[2].setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);

        //tagpatterns[3] = new TagPatternBase();
        //tagpatterns[3].setTagPattern("1000016DA1F0DB102465E72738AEFC52");
        //tagpatterns[3].setBitOffset(32);
        //tagpatterns[3].setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);

        try {

            // run locate with default params
            mReader.Actions.TagLocationing.PerformMultiLocate(tagpatterns, null, null);
            Thread.sleep(3000);
            mReader.Actions.TagLocationing.Stop();

            // with customized locate parameters
            MultiLocateParams multi_params = new MultiLocateParams();
            // set thresholds
            multi_params.lock_on = 50;
            multi_params.lock_off = 10;
            multi_params.lock_off_time = 1;

            multi_params.getDetectionProfile().setTime(250);
            multi_params.getDetectionProfile().setLevel(20);

            multi_params.getLowProfile().setTime(150);
            multi_params.getLowProfile().setLevel(45);
            multi_params.getLowProfile().setLevel(1);

            multi_params.getMediumProfile().setTime(80);
            multi_params.getMediumProfile().setLevel(65);
            multi_params.getMediumProfile().setLevel(2);

            multi_params.getHighProfile().setTime(50);
            multi_params.getHighProfile().setLevel(80);
            multi_params.getHighProfile().setLevel(2);

            multi_params.getMaxProfile().setTime(90);
            multi_params.getMaxProfile().setLevel(3);

            mReader.Actions.TagLocationing.PerformMultiLocate(tagpatterns, multi_params, null);
            Thread.sleep(3000);
            mReader.Actions.TagLocationing.Stop();
        } catch (InvalidUsageException e) {
            //e.printStackTrace();
        } catch (OperationFailureException e) {
            //e.printStackTrace();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }
    public void powerState(){

        for (READER_POWER_STATE powerstate : READER_POWER_STATE.values()) {
            try {
                //READER_POWER_STATE powerstate = reader.Config.getReaderPowerState();
                if (powerstate != READER_POWER_STATE.POWER_STATE_OFF)
                    mReader.Config.setReaderPowerState(powerstate);
                powerstate = mReader.Config.getReaderPowerState();
                Log.d(TAG,"Current power state " + powerstate);
            } catch (InvalidUsageException e) {
                //e.printStackTrace();
            } catch (OperationFailureException e) {
                //e.printStackTrace();
            }
        }
        try {
            READER_POWER_STATE powerstate = READER_POWER_STATE.POWER_STATE_STANDBY;
            mReader.Config.setReaderPowerState(powerstate);
            powerstate = mReader.Config.getReaderPowerState();
            Log.d(TAG,"Last power state " + powerstate);
        }
        catch (InvalidUsageException e) {
            //e.printStackTrace();
        } catch (OperationFailureException e) {
            //e.printStackTrace();
        }

    }

    public void UserFeedback() {
        //User can control LED and Beeper based on canned profiles.
      /*  for (int i = 0; i < 10; i++)*/{
            try {

                mReader.Config.setUserFeedback(0);
                Thread.sleep(5000);
            } catch (InvalidUsageException e) {
                //e.printStackTrace();
            } catch (OperationFailureException e) {
                //e.printStackTrace();
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }
    public void batteryHealth(){
        int bH = 0;
        try {

            bH = mReader.Config.getBatteryHealth();

        } catch (InvalidUsageException e) {
            //e.printStackTrace();
        } catch (OperationFailureException e) {
            //e.printStackTrace();
        }
        Log.d(TAG,"Battery Health "+bH);
    }

    public void enableInfoEvent(){
        /*
        RFID antenna power level is limited to 24dbm when reader is in cradle, setting the antenna power level to value higher than 24db when sled is in cradle will get “Value Not Allowed” error
“Antenna Power Level Adjusted” information event is sent to host if antenna power level is higher than 24dbm when reader is placed in cradle

        * */
       // reader.Events.setInfoEvent(true);
        //reader.Events.setCradleEvent(true);
        try {
         
            mReader.Config.getDeviceStatus(false,false,false,true);
        } catch (InvalidUsageException e) {
            //e.printStackTrace();
        } catch (OperationFailureException e) {
            //e.printStackTrace();
        }
    }

    public void setSingulationControl(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Antennas.SingulationControl singulationControl;
                try {
                    singulationControl = mReader.Config.Antennas.getSingulationControl(3);
                    singulationControl.setSession(SESSION.GetSession(1));
                    singulationControl.setTagPopulation((short) 22);
                    singulationControl.Action.setInventoryState(INVENTORY_STATE.GetInventoryState(2));

                    switch (2) {
                        case 0:
                            singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                            break;
                        case 1:
                            singulationControl.Action.setSLFlag(SL_FLAG.SL_FLAG_DEASSERTED);
                            break;
                        case 2:
                            singulationControl.Action.setSLFlag(SL_FLAG.SL_FLAG_ASSERTED);
                            break;
                    }
                    mReader.Config.Antennas.setSingulationControl(1, singulationControl);
                } catch (InvalidUsageException e) {
                    if (e != null && e.getStackTrace().length > 0) {
                        Log.e(TAG, e.getStackTrace()[0].toString());
                    }
                } catch (OperationFailureException e) {
                    if (e != null && e.getStackTrace().length > 0) {
                        Log.e(TAG, e.getStackTrace()[0].toString());
                    }
                }
                return null;
            }
        }.execute();
    }

    public void triggerSettings(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    StartTrigger startTrigger = mReader.Config.getStartTrigger();
                    StopTrigger stopTrigger = mReader.Config.getStopTrigger();
                    mReader.Config.setStartTrigger(startTrigger);
                    mReader.Config.setStopTrigger(stopTrigger);

                } catch (InvalidUsageException | OperationFailureException e) {
                    if (e.getStackTrace().length > 0) {
                        Log.e(TAG, e.getStackTrace()[0].toString());
                    }
                }
                return null;
            }


        }.execute();
    }

    public void addPrefilter() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String tag = "11112222";
                PreFilters filters = new PreFilters();
                PreFilters.PreFilter filter = filters.new PreFilter();
                filter.setAntennaID((short) 1);// Set this filter for Antenna ID 1
                filter.setTagPattern(tag);// Tags which starts with passed pattern
                filter.setTagPatternBitCount(32);
                filter.setBitOffset(32); // skip PC bits (always it should be in bit length)
                filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
                filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
                filter.StateAwareAction.setTarget(TARGET.TARGET_SL); // inventoried flag of session S1 of matching tags to B
                filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_ASRT_SL_NOT_DSRT_SL);
                // not to select tags that match the criteria
                try {
                    mReader.Actions.PreFilters.add(filter);
                } catch (InvalidUsageException e) {
                    //e.printStackTrace();
                } catch (OperationFailureException e) {
                    //e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    public void doRead() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

// read operation
                try {
                    CableLossCompensation cableLoss =  mReader.Config.getCableLossCompensation(1);
                    //reader.Actions.TagAccess.readEvent( readAccessParams, null, null);
                    Log.d("test", "test");
                } catch (InvalidUsageException e) {
                    //throw new RuntimeException(e);
                    showMessage("get cable loss failed ");
                    return null;
                } catch (OperationFailureException e) {
                    //throw new RuntimeException(e);
                    showMessage("get cable loss failed "  );
                    return null;

                }
                showMessage("get cable loss  success ");
                return null;
            }

        }.execute();

    }

    public void doWrite() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

// read operation
                try {
                    CableLossCompensation[] cableLossCompensationInfo = new CableLossCompensation[3];
                    cableLossCompensationInfo[0] = new CableLossCompensation();
                    cableLossCompensationInfo[0].setAntennaID(1);
                    cableLossCompensationInfo[0].setCableLossPer100Feet(5);
                    cableLossCompensationInfo[0].setCableLengthInFeet(5);
                    cableLossCompensationInfo[1] = new CableLossCompensation();
                    cableLossCompensationInfo[1].setAntennaID(2);
                    cableLossCompensationInfo[1].setCableLossPer100Feet(10);
                    cableLossCompensationInfo[1].setCableLengthInFeet(10);
                    cableLossCompensationInfo[2] = new CableLossCompensation();
                    cableLossCompensationInfo[2].setAntennaID(3);
                    cableLossCompensationInfo[2].setCableLossPer100Feet(20);
                    cableLossCompensationInfo[2].setCableLengthInFeet(20);

                    RFIDResults result = mReader.Config.setCableLossCompensation(cableLossCompensationInfo);
                    //reader.Actions.TagAccess.readEvent( readAccessParams, null, null);
                    Log.d("test", "test");
                } catch (InvalidUsageException e) {
                    //throw new RuntimeException(e);
                    showMessage("get cable loss failed ");
                    return null;
                } catch (OperationFailureException e) {
                    //throw new RuntimeException(e);
                    showMessage("get cable loss failed "  );
                    return null;

                }
                showMessage("get cable loss  success ");
                return null;
            }

        }.execute();

    }

    private void showMessage(String message) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void doLock() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            TagData tagData = null;
            String tagId = "e28011700000020d2a411a23";
            //String tagId = "11112222000000021f0b8976";
            TagAccess tagAccess = new TagAccess();
            TagAccess.LockAccessParams lockAccessParams = tagAccess.new LockAccessParams();
            lockAccessParams.setAccessPassword(11112222);
            LOCK_PRIVILEGE lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_READ_WRITE;
            lockAccessParams.setLockPrivilege(LOCK_DATA_FIELD.LOCK_EPC_MEMORY, lockPrivilege);
// data length in words
            try {
                mReader.Actions.TagAccess.lockWait(tagId, lockAccessParams, null, false);
            } catch (InvalidUsageException e) {
                //throw new RuntimeException(e);
                showMessage("lock failed ");
                return  ;
            } catch (OperationFailureException e) {
                //throw new RuntimeException(e);
                showMessage("lock failed ");
                return ;
            }
            showMessage("lock success ");
            return ;

        });
    }

    public void deleteSelects() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mReader.Actions.PreFilters.deleteAll();
                } catch (InvalidUsageException e) {
                    //throw new RuntimeException(e);
                    showMessage("delete all failed ");
                    return null ;
                } catch (OperationFailureException e) {
                    //throw new RuntimeException(e);
                    showMessage("delete all failed ");
                    return null;
                }
                showMessage("delete all success ");
                return null;
            }

        }.execute();
    }

    public void getcapablities() {
    }


//    private class GetReaderListTask extends AsyncTask<Void, Void, String> {
//        @Override
//        protected String doInBackground(Void... voids) {
//            /*
//            if (readers != null) {
//                Log.d(TAG, "GetReaderListTask");
//                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
//                if (availableRFIDReaderList != null) {
//                    if (availableRFIDReaderList.size() != 0) {
//                        // search reader specified by name
//                        for (ReaderDevice device : availableRFIDReaderList) {
//                            if (device.getName().equals(readername)) {
//                                readerDevice = device;
//                                reader = readerDevice.getRFIDReader();
//                                return connect();
//                            }
//                        }
//                    }
//                }
//            }
//            */
//            return connect();
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
//            textView.setText(result);
//
//        }
//    }
//
//    void onDestroy() {
//        dispose();
//    }
//
//    String onResume() {
//        return connect();
//    }
//
//    void onPause() {
//        disconnect();
//    }
//
//    private synchronized String connect() {
//        Log.d(TAG, "connect");
//        if (mReader != null) {
//            try {
//                if (!mReader.isConnected()) {
//                    // Establish connection to the RFID Reader
//                    mReader.setPassword("Zebra@123");
//                    //reader.setAdminPassword("admin");
//                    mReader.connect();
//                    ConfigureReader();
//                    return "Connected";
//                }
//            } catch (InvalidUsageException e) {
//                //e.printStackTrace();
//            } catch (OperationFailureException e) {
//                //e.printStackTrace();
//                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
//                return "Connection failed" + e.getVendorMessage() + " " + e.getStatusDescription();
//            }
//        }
//        return "";
//    }

    public void ConfigureReader() {
        if (mReader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                mReader.Events.addEventsListener(eventHandler);
                // HH event
                mReader.Events.setHandheldEvent(true);
                // tag event with tag data
                mReader.Events.setTagReadEvent(true);
                mReader.Events.setAttachTagDataWithReadEvent(false);
                mReader.Events.setReaderDisconnectEvent(true);
                mReader.Events.setInfoEvent(true);
                mReader.Events.setCradleEvent(true);
                mReader.Events.setBatteryEvent(true);
                mReader.Events.setFirmwareUpdateEvent(true);
                mReader.Events.setHeartBeatEvent(true);
                // set trigger mode as rfid so scanner beam will not come
                mReader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, false);
                // set start and stop triggers
                mReader.Config.setStartTrigger(triggerInfo.StartTrigger);
                mReader.Config.setStopTrigger(triggerInfo.StopTrigger);
                // set antenna configurations
                Antennas.AntennaRfConfig config = mReader.Config.Antennas.getAntennaRfConfig(1);
                config.setTransmitPowerIndex(270);
                config.setrfModeTableIndex(0);
                config.setTari(0);
                mReader.Config.Antennas.setAntennaRfConfig(1, config);
                // Set the singulation control
                Antennas.SingulationControl s1_singulationControl = mReader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S0);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                mReader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
                // delete any prefilters
                mReader.Actions.PreFilters.deleteAll();
                //
            } catch (InvalidUsageException | OperationFailureException e) {
                //e.printStackTrace();
            }
        }
    }

    private synchronized void disconnect() {
        Log.d(TAG, "Disconnect");
        try {
            if (mReader != null) {
                if (eventHandler != null)
                    mReader.Events.removeEventsListener(eventHandler);
                mReader.disconnect();
                //Toast.makeText(getApplicationContext(), "Disconnecting reader", Toast.LENGTH_LONG).show();
                //reader = null;
            }
        } catch (InvalidUsageException e) {
            //e.printStackTrace();
        } catch (OperationFailureException e) {
            //e.printStackTrace();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private synchronized void dispose() {
        disconnect();
        try {
            if (mReader != null) {
                //Toast.makeText(getApplicationContext(), "Disconnecting reader", Toast.LENGTH_LONG).show();
                mReader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    synchronized void performInventory() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                AntennaInfo antennaInfo = new AntennaInfo();
                short[] antennaList = {1,2,3};
                antennaInfo.setAntennaID(antennaList);
                mReader.Config.setInventoryModeSettings("hours", 1);
                mReader.Config.setOperatingMode(ENUM_OPERATING_MODE.INVENTORY_MODE);
                //mReader.Actions.Inventory.perform(null, null, antennaInfo);
                return ;
            }
        });


    }

    synchronized void stopInventory() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mReader.Actions.Inventory.stop();
                } catch (InvalidUsageException e) {
                    //e.printStackTrace();
                } catch (OperationFailureException e) {
                    //e.printStackTrace();
                }
                return ;
            }
        });

    }

    public void setstarttrigger() {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                StartTrigger startTrigger;
                try {
                    startTrigger = mReader.Config.getStartTrigger();
                    startTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
                    startTrigger.GPI = new GPITrigger[1];
                    startTrigger.GPI[0] = new GPITrigger();
                    startTrigger.GPI[0].setPortNumber(1);
                    startTrigger.GPI[0].setSignal(true);

                    mReader.Config.setStartTrigger(startTrigger);
                } catch (InvalidUsageException e) {
                    return;
                } catch (OperationFailureException e) {
                    return;
                }


            }
        });
    }

    public void setstoptrigger() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                StopTrigger stopTrigger;
                try {
                    stopTrigger = mReader.Config.getStopTrigger();
                    stopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_ACCESS_N_ATTEMPTS_WITH_TIMEOUT);
                    stopTrigger.AccessCount.setN((short)1);
                    stopTrigger.AccessCount.setTimeout(5000);
                   // stopTrigger.GPI = new GPITrigger[1];
                    //stopTrigger.GPI[0] = new GPITrigger();
                    //stopTrigger.GPI[0].setPortNumber(1);
                    //stopTrigger.GPI[0].setSignal(true);
                    //stopTrigger.GPI[0].setTimeout(100);
                    mReader.Config.setStopTrigger(stopTrigger);
                } catch (InvalidUsageException e) {
                    return;
                } catch (OperationFailureException e) {
                    return;
                }


            }
        });
    }

    public void doKill() {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                StopTrigger stopTrigger;
                try {

                    TagData tagData = null;
                    String tagId = "e280689400005001b043b044";
                    //String tagId = "11112222000000021f0b8976";
                    TagAccess tagAccess = new TagAccess();
                    TagAccess.KillAccessParams killAccessParams = tagAccess.new KillAccessParams();
                    killAccessParams.setKillPassword(0);
                    AntennaInfo antennaInfo = new AntennaInfo();
                    mReader.Actions.TagAccess.killWait(tagId,killAccessParams,null);
                } catch (InvalidUsageException e) {
                    return;
                } catch (OperationFailureException e) {
                    return;
                }
            }
        });
    }

    public void dosequence() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {

                AccessFilter accessFilter = new AccessFilter();
                byte[] tagMask = new byte[]{(byte) 0xff, (byte) 0xff
                };

                // Tag Pattern A
                accessFilter.TagPatternA.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
                accessFilter.TagPatternA.setTagPattern(new byte[]
                        {
                                // 0x2f, 0x22
                                0x22, 0x44
                                // (byte)0x83, (byte)0x91
                        });
                accessFilter.TagPatternA.setTagPatternBitCount(16);
                accessFilter.TagPatternA.setBitOffset(32);
                accessFilter.TagPatternA.setTagMask(tagMask);
                accessFilter.TagPatternA.setTagMaskBitCount(tagMask.length * 8);
                accessFilter.setAccessFilterMatchPattern(FILTER_MATCH_PATTERN.A);
                int accessOpcount = 0;
                TagAccess tagAccess = new TagAccess();
                TagAccess.Sequence opSequence = tagAccess.new Sequence(tagAccess);

                TagAccess.Sequence.Operation op1 = opSequence.new Operation();
                op1.setAccessOperationCode(ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ);
                op1.ReadAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
                op1.ReadAccessParams.setAccessPassword(0);
                op1.ReadAccessParams.setByteOffset(2);
                // op2.WriteAccessParams.setWriteData(new byte[]{(byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB});
                //op2.WriteAccessParams.setWriteDataLength(4);
                try {
                    mReader.Actions.TagAccess.OperationSequence.add(op1);

                    accessOpcount++;

                    TagAccess.Sequence.Operation op2 = opSequence.new Operation();
                    op2.setAccessOperationCode(ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ);
                    op2.ReadAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_USER);
                    op2.ReadAccessParams.setAccessPassword(0);
                    op2.ReadAccessParams.setByteOffset(2);
                    // op1.WriteAccessParams.setWriteData(new byte[]{(byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88});
                    // op1.WriteAccessParams.setWriteDataLength(4);
                    mReader.Actions.TagAccess.OperationSequence.add(op2);
                    accessOpcount++;
                    // add Write Access operation - Write to Reserved memory bank
                    TagAccess.Sequence.Operation op3 = opSequence.new Operation();
                    op3.setAccessOperationCode(ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ);
                    op3.ReadAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_RESERVED);
                    op3.ReadAccessParams.setAccessPassword(0);
                    op3.ReadAccessParams.setByteOffset(2);
                    // op2.WriteAccessParams.setWriteData(new byte[]{(byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB});
                    //op2.WriteAccessParams.setWriteDataLength(4);
                    mReader.Actions.TagAccess.OperationSequence.add(op3);
                    accessOpcount++;
                    TagAccess.Sequence.Operation op4 = opSequence.new Operation();
                    op4.setAccessOperationCode(ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ);
                    op4.ReadAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
                    op4.ReadAccessParams.setAccessPassword(0);
                    op4.ReadAccessParams.setByteOffset(2);
                    // op2.WriteAccessParams.setWriteData(new byte[]{(byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB});
                    //op2.WriteAccessParams.setWriteDataLength(4);
                    mReader.Actions.TagAccess.OperationSequence.add(op4);
                    accessOpcount++;

                    TriggerInfo triggerInfo = new TriggerInfo();
                    // perform access sequence
                    mReader.Actions.TagAccess.OperationSequence.performSequence(accessFilter, triggerInfo, null);
                } catch (InvalidUsageException e) {
                    throw new RuntimeException(e);
                } catch (OperationFailureException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    // Read/Status Notify handler
    // Implement the RfidEventsLister class to receive event notifications
    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = mReader.Actions.getReadTags(100);
            if (myTags != null) {
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[index].getTagID());
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }
                    if (myTags[index].isContainsLocationInfo()) {
                        short dist = myTags[index].LocationInfo.getRelativeDistance();
                        short num = myTags[index].LocationInfo.getTagNumber();
                        Log.d(TAG, "Tag relative distance " + dist + " # " + num);
                    }
                }
                context.runOnUiThread(() -> tagDataViewModel.setTagItems(myTags));
//                if(myTags != null )
//                    new AsyncDataUpdate().execute(myTags);

            }

        }

        // Status Event Notification
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            context.handleTriggerPress(true);
                            return null;
                        }
                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            context.handleTriggerPress(false);
                            return null;
                        }
                    }.execute();
                }
            }
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {

                        disconnect();
                        return null;
                    }
                }.execute();
            }
            if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.BATTERY_EVENT){
                int data = rfidStatusEvents.StatusEventData.BatteryData.getLevel();
                //  = rfidStatusEvents.StatusEventData.InfoData.getCause();
                Log.d(TAG,"Battery level : "+data );
            }

            if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.CRADLE_EVENT){
                boolean data = rfidStatusEvents.StatusEventData.cradleData.isOnCradle();
                //  = rfidStatusEvents.StatusEventData.InfoData.getCause();
                Log.d(TAG,"Cradle data : "+data );
            }
            if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.INFO_EVENT){
                rfidStatusEvents.StatusEventData.cradleData.getCause();
                String data = rfidStatusEvents.StatusEventData.InfoData.getCause();
                Log.d(TAG,"InfoData : "+data );
            }
            if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.FIRMWARE_UPDATE_EVENT){
                String status = rfidStatusEvents.StatusEventData.FWEventData.getStatus();
                int imageDownloadProgress = rfidStatusEvents.StatusEventData.FWEventData.getImageDownloadProgress();
                int overallUpdateProgress = rfidStatusEvents.StatusEventData.FWEventData.getOverallUpdateProgress();
                Log.d(TAG,"FW status: "+status+", idp: "+imageDownloadProgress+", ovp: "+overallUpdateProgress );
                context.runOnUiThread(() -> fwUpdateDataViewModel.setStatus(new String[]{status, String.valueOf(imageDownloadProgress), String.valueOf(overallUpdateProgress)}));
            }
        }
    }

//    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
//        @Override
//        protected Void doInBackground(TagData[]... params) {
//            context.handleTagdata(params[0]);
//
//            return null;
//        }
//    }

    interface ResponseHandlerInterface {
        void handleTagdata(TagData[] tagData);

        void handleTriggerPress(boolean pressed);
        //void handleStatusEvents(Events.StatusEventData eventData);
    }

}
