package com.sample.api3transport.ui.Regulatory;


import static com.sample.api3transport.RFIDHandler.mReader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.sample.api3transport.databinding.FragmentRegulatoryBinding;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RegionInfo;
import com.zebra.rfid.api3.RegulatoryConfig;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class RegulatoryFragment extends Fragment {
    private static final String TAG = "REGULATORY_FRAGMENT";
    private FragmentRegulatoryBinding binding;
    RegionInfo regionInfo;
    ArrayList<String> channelList = new ArrayList<String>();
    ChannelAdapter channelAdapter;
    Snackbar snackbar;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentRegulatoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mReader == null || !mReader.isConnected()){
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> regionArray = new ArrayList<>();
        if(mReader.isConnected()) {
            for (int i = 0; i < mReader.ReaderCapabilities.SupportedRegions.length(); i++) {
                regionArray.add(mReader.ReaderCapabilities.SupportedRegions.getRegionInfo(i).getName());
            }
        }
        ArrayAdapter<String> antennaAdaptor = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, regionArray);
        antennaAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.supportedRegions.setAdapter(antennaAdaptor);

        binding.supportedRegions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                channelList.clear();
                regionInfo = mReader.ReaderCapabilities.SupportedRegions.getRegionInfo(position);
                getChannels(regionInfo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("", "");;
            }
        });

        binding.buttonSave.setOnClickListener(v -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    RegulatoryConfig regConfig = new RegulatoryConfig();
                    regConfig.setStandardName(regionInfo.getStandardName());
                    regConfig.setRegion(regionInfo.getName());
                    if(channelList.size() > 0) {
                        String [] chList = new String [channelList.size()];
                        //chList = (String[]) channelList.toArray();
                        chList = channelList.toArray(new String[channelList.size()]);
                        regConfig.setEnabledChannels(chList);
                    }
                    else{
                        snackbar = Snackbar.make(view, "Select atleast one channel", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        return;
                    }
                    snackbar  = Snackbar.make(view, "Updating...", Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                    mReader.Config.setRegulatoryConfig(regConfig);
                    if(binding != null) {
                        snackbar = Snackbar.make(view, "Set region Success", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                } catch (InvalidUsageException | OperationFailureException e) {
                    if (e.getStackTrace().length > 0) {
                        Log.e(TAG, e.getStackTrace()[0].toString());
                        if(binding != null) {
                            snackbar = Snackbar.make(view, "Set region failed", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    }
                }
            });
        });

    }

    private void getChannels(RegionInfo region) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<RegionInfo> regionInfo = new AtomicReference<>();
        executor.execute(() -> {
            try {
                regionInfo.set(mReader.Config.getRegionInfo(region));
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {
                ArrayList<ChannelItem> channelArray = new ArrayList<>();
                if(regionInfo.get() != null) {
                    String[] supportedChannels = regionInfo.get().getSupportedChannels();
                    // String[] supportedChannels = {"test1", "test2","test3","test4","test5","test6","test7", "test8","test9","test10","test11","test12"};
                    for (String supportedChannel : supportedChannels) {
                        channelArray.add(new ChannelItem(supportedChannel, false));
                    }
                    channelAdapter = new ChannelAdapter(channelArray);
                    binding.rvChannel.setAdapter(channelAdapter);

                    channelAdapter.setOnItemClickListener((channel, isChecked) -> {
                        Log.d(TAG, channel + " " + isChecked);
                        channelList.add(channel);

                    });
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(snackbar != null) {
            snackbar.dismiss();
        }
        binding = null;
    }

}