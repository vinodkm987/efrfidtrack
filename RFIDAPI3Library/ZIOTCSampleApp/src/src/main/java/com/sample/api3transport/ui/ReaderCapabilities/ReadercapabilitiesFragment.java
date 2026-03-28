package com.sample.api3transport.ui.ReaderCapabilities;

import static com.sample.api3transport.RFIDHandler.mReader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.sample.api3transport.databinding.FragmentReadercapabilitiesBinding;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.NETWORK_CONFIG;
import com.zebra.rfid.api3.Network_IPConfig;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ReadercapabilitiesFragment extends Fragment {

    private static final String TAG = "READER_CAPABILITIES";
    private FragmentReadercapabilitiesBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentReadercapabilitiesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mReader == null || !mReader.isConnected()){
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        StringBuilder readerDetails = new StringBuilder();
         readerDetails.append("Model : ")
                 .append(mReader.ReaderCapabilities.getModelName())
                 .append("\n").append("Serial no : ")
                 .append(mReader.ReaderCapabilities.getSerialNumber())
                 .append("\n").append("Firwmare : ")
                 .append(mReader.ReaderCapabilities.getFirwareVersion())
                 .append("\n").append("Reader ID : ")
                 .append(mReader.ReaderCapabilities.ReaderID.getID())
                 .append("\n").append("ReaderID type : ")
                 .append(mReader.ReaderCapabilities.ReaderID.getReaderIDType())
                 .append("\n").append("minTx : ")
                 .append(mReader.ReaderCapabilities.getTransmitPowerLevelValues()[0])
                 .append("\n").append("maxTx :").append(mReader.ReaderCapabilities.getTransmitPowerLevelValues()[mReader.ReaderCapabilities.getTransmitPowerLevelValues().length-1]);
        readerDetails.append("\nNum of antennas supported : ")
                .append(mReader.ReaderCapabilities.getNumAntennaSupported());
        binding.readerDetails.setText(readerDetails);


        StringBuilder supportedRegions = new StringBuilder();
         Log.d(TAG,"Supported Regions");
         for (int i=0; i<mReader.ReaderCapabilities.SupportedRegions.length(); i++){
             supportedRegions.append(" Country :")
                     .append(mReader.ReaderCapabilities.SupportedRegions.getRegionInfo(i).getName())
                     .append("\ncode : ").append(mReader.ReaderCapabilities.SupportedRegions.getRegionInfo(i).getName());
//             for(int j=0; j< mReader.ReaderCapabilities.SupportedRegions.getRegionInfo(i).getSupportedChannels().length; j++)
//                 supportedRegions.append("\nChannels : ")
//                         .append(mReader.ReaderCapabilities.SupportedRegions.getRegionInfo(i).getSupportedChannels()[j]);
             supportedRegions.append("\nHoppingConfigurable : ").append(mReader.ReaderCapabilities.SupportedRegions.getRegionInfo(i).isHoppingConfigurable());
             Log.d(TAG, String.valueOf(mReader.ReaderCapabilities.SupportedRegions.getRegionInfo(i).getName()));
         }
         binding.supportedRegions.setText(supportedRegions);

        updateNetworkState();
    }

    private void updateNetworkState(){
        Network_IPConfig network_ipConfig = new Network_IPConfig();
        network_ipConfig.setType(NETWORK_CONFIG.ENET);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<RFIDResults> rfidResults = new AtomicReference<>();
        executor.execute(() -> {
            try {
                Snackbar.make(requireView(), "fetching...", Snackbar.LENGTH_INDEFINITE).show();
                rfidResults.set(mReader.Config.Nw_getNetworkStatus(network_ipConfig));
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {
                if(rfidResults.get() == RFIDResults.RFID_API_SUCCESS){
                    StringBuilder networkStat = new StringBuilder();
                    networkStat.append("IP address : ")
                            .append(network_ipConfig.getipaddress())
                            .append("\ndnsAddress : ")
                            .append(network_ipConfig.getdns())
                            .append("\nsubnetMask : ")

                            .append(network_ipConfig.getnetmask())
                            .append("\ngateway address : ")
                            .append(network_ipConfig.getgateway())
                            .append("\nmac address : ")
                            .append(network_ipConfig.getMacAddress());

                    if(binding != null) {
                        Snackbar.make(requireView(), "done", Snackbar.LENGTH_SHORT).show();
                        binding.netwokStat.setText(networkStat);
                    }
                }
            });
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
