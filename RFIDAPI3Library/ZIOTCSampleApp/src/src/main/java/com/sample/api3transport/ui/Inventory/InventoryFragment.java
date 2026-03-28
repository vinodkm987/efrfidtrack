package com.sample.api3transport.ui.Inventory;


import static com.sample.api3transport.RFIDHandler.ProtocolType;
import static com.sample.api3transport.RFIDHandler.isInventoryRunning;
import static com.sample.api3transport.RFIDHandler.mReader;
import static com.sample.api3transport.RFIDHandler.tagDataViewModel;
import static com.sample.api3transport.RFIDHandler.tagIDs;
import static com.zebra.rfid.api3.HexDump.hexStringToByteArray;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.sample.api3transport.databinding.FragmentInventoryBinding;
import com.zebra.rfid.api3.ANTENNA_STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.AccessFilter;
import com.zebra.rfid.api3.AntennaInfo;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENUM_OPERATING_MODE;
import com.zebra.rfid.api3.FILTER_MATCH_PATTERN;
import com.zebra.rfid.api3.GPITrigger;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.UNIQUE_TAG_REPORT_SETTING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryFragment extends Fragment {
    private static final String TAG = "INVENTORY_FRAGMENT";
    private FragmentInventoryBinding binding;
    InventoryAdapter inventoryAdapter;
    AntennaAdapter antennaAdapter;
    AntennaInfo antennaInfo;
    private int tagSeenCount = 0;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentInventoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mReader == null || !mReader.isConnected()){
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (isInventoryRunning) {
            binding.fabInventory.setImageResource(android.R.drawable.ic_media_pause);
        }

        ArrayList<AntennaItem> antennaArray = new ArrayList<>();
        if(mReader.isConnected()) {
            for (int i = 1; i <= mReader.ReaderCapabilities.getNumAntennaSupported(); i++) {
                antennaArray.add(new AntennaItem(i, false));
            }
        }
        antennaAdapter = new AntennaAdapter(antennaArray);
        binding.rvAntenna.setAdapter(antennaAdapter);

        antennaInfo = new AntennaInfo();
        ArrayList<Short> antennaList = new ArrayList<>();

        antennaAdapter.setOnItemClickListener((antenna, isChecked) -> {
            if(isChecked){
                antennaList.add((short) antenna);
            }else{
                antennaList.remove(Short.valueOf((short) antenna));
            }
            Log.d(TAG, Arrays.toString(antennaList.toArray()));
            short[] antennaID = new short[antennaList.size()];
            for(int i=0; i < antennaList.size(); i++){
                antennaID[i] = antennaList.get(i);
            }
            Arrays.sort(antennaID);
            antennaInfo.setAntennaID(antennaID);
            Log.d(TAG, Arrays.toString(antennaID));
        });


        binding.fabInventory.setOnClickListener(v -> {
            if (mReader != null && mReader.isConnected()) {
                if (!isInventoryRunning) {
                    isInventoryRunning = true;
                    binding.fabInventory.setImageResource(android.R.drawable.ic_media_pause);
                    inventoryAdapter.clear();
                    tagSeenCount = 0;
                    startInventory();
                } else {
                    isInventoryRunning = false;
                    binding.fabInventory.setImageResource(android.R.drawable.ic_media_play);
                    stopInventory();
                }
            } else {
                Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_LONG).show();
            }
        });

        ArrayList<InventoryItem> arrayOfItems = new ArrayList<>();
        inventoryAdapter = new InventoryAdapter(requireActivity(), arrayOfItems);
        binding.inventoryList.setAdapter(inventoryAdapter);

        tagDataViewModel.getInventoryItem().observe(requireActivity(), tagItems -> {
            for(TagData data : tagItems){
                inventoryAdapter.add(new InventoryItem(data.getTagID(), data.getTID(), data.getUser(),
                        data.getTagSeenCount(), data.getPeakRSSI(), data.getPC(),
                        ProtocolType.equals("LLRP")? String.valueOf(data.getCRC()) :data.getStringCRC(),
                        data.getPhase(), data.getAntennaID(),
                        ProtocolType.equals("LLRP")? String.valueOf(data.getChannelIndex()) : data.getChannel(),
                        data.SeenTime.getUTCTime().getFirstSeenTimeStamp(), data.SeenTime.getUTCTime().getLastSeenTimeStamp()));

                try {
                    binding.tagCount.setText(String.format(Locale.US, "Tag Seen Count: %d", tagSeenCount++));
                    if (!tagIDs.contains(data.getTagID())) {
                        tagIDs.add(data.getTagID());
                    }
                }catch(Exception e){

                }
            }
        });
    }
    private AccessFilter setAccessFilter(String tagID) {
        int len = tagID.length()/2;
        AccessFilter accessFilter = new AccessFilter();
        byte[] tagMask = new byte[len];
        byte[] tagData = hexStringToByteArray(tagID.toString());
        for(int i= 0; i<len; i++){
            tagMask[i] = (byte)0xff;
        }
        // byte[] tagMask = new byte[]{(byte) 0xff, (byte) 0xff
        // };

        // Tag Pattern A
        accessFilter.TagPatternA.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
        accessFilter.TagPatternA.setTagPattern(tagData);
        accessFilter.TagPatternA.setTagPatternBitCount(len*8);
        accessFilter.TagPatternA.setBitOffset(32);
        accessFilter.TagPatternA.setTagMask(tagMask);
        accessFilter.TagPatternA.setTagMaskBitCount(tagMask.length*8);
        accessFilter.setAccessFilterMatchPattern(FILTER_MATCH_PATTERN.A);
        return accessFilter;

    }
    private synchronized void startInventory(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Antennas.AntennaRfConfig antennaRfConfig;
            try {
                antennaRfConfig = mReader.Config.Antennas.getAntennaRfConfig(1);
            } catch (InvalidUsageException e) {
                throw new RuntimeException(e);
            } catch (OperationFailureException e) {
                throw new RuntimeException(e);
            }
            Antennas.AntennaRfConfig.AntennaStopTrigger antennaStopTrigger= antennaRfConfig.new AntennaStopTrigger();
            antennaStopTrigger.setStopTriggerType(ANTENNA_STOP_TRIGGER_TYPE.GPI);
            GPITrigger gpiTrigger = new GPITrigger();
            gpiTrigger.setPortNumber(1);
            gpiTrigger.setDebounceTime(0);
            gpiTrigger.setGpi_signal(GPITrigger.GPI_SIGNAL.GPI_SIGNAL_LOW);
            antennaStopTrigger.setGpiTrigger(gpiTrigger);
            antennaRfConfig.setAntennaStopTriggerConfig(antennaStopTrigger);
            antennaRfConfig.setTransmitPowerIndex(27);
            //antennaRfConfig.setrfModeTableIndex(linkedProfile);
            //antennaRfConfig.setTari(tari);
            try {
                mReader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfig);
            } catch (InvalidUsageException e) {
                throw new RuntimeException(e);
            } catch (OperationFailureException e) {
                throw new RuntimeException(e);
            }
            try {
                if(ProtocolType.equals("ZIOTC"))
                {
                    mReader.Config.setUniqueTagReport(false);
                    UNIQUE_TAG_REPORT_SETTING set = mReader.Config.getUniqueTagReport();
                    mReader.Config.setOperatingMode(ENUM_OPERATING_MODE.CUSTOM_MODE);

                }

                mReader.Actions.Inventory.perform(null, null, antennaInfo);
                TagAccess tagAccess = new TagAccess();
                TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
                //readAccessParams.setAccessPassword(Long.decode("0X" + sPassword));
                readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
                readAccessParams.setOffset(0);
                readAccessParams.setCount(6);
                AccessFilter accessFilter = setAccessFilter("BEEF");
                //mReader.Actions.TagAccess.readEvent( readAccessParams, null, antennaInfo);
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
        });
    }

    private synchronized void stopInventory(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                mReader.Actions.Inventory.stop();
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}