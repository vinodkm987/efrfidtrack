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
import com.sample.api3transport.databinding.FragmentDevicestatusBinding;
import com.zebra.rfid.api3.DeviceStatus;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceStatusFragment extends Fragment {
    private static final String TAG = "DEVICE_STATUS";
    private FragmentDevicestatusBinding binding;
    Snackbar snackbar;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDevicestatusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mReader == null || !mReader.isConnected()){
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        updateDeviceStatus(view);
    }

    private void updateDeviceStatus(View view){
        DeviceStatus deviceStatus = new DeviceStatus();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<RFIDResults> rfidResults = new AtomicReference<>();
        executor.execute(() -> {
            try {
                snackbar  = Snackbar.make(view, "fetching...", Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
                rfidResults.set(mReader.Config.getDeviceStatus(deviceStatus));
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {
                if(rfidResults.get() == RFIDResults.RFID_API_SUCCESS){
                    StringBuilder deviceStatusString = new StringBuilder();
                    deviceStatusString.append("no of antenna : ").append(deviceStatus.getNumOfAntennas());
                    for(Map.Entry<Integer, String> antennaState : deviceStatus.getAntennaStatus().entrySet()){
                        deviceStatusString.append("\nAntenna ").append(antennaState.getKey()).append(" : ").append(antennaState.getValue());
                    }
                    deviceStatusString.append("\nPowerNegotiation : ").append(deviceStatus.getPowerNegotiation());
                    deviceStatusString.append("\nPowerSource : ").append(deviceStatus.getPowerSource());
                    deviceStatusString.append("\nRadioActivity : ").append(deviceStatus.getRadioActivity());
                    deviceStatusString.append("\nRadioConnection : ").append(deviceStatus.getRadioConnection());
                    deviceStatusString.append("\nSystemTime : ").append(deviceStatus.getSystemTime());
                    deviceStatusString.append("\nTemperature : ").append(deviceStatus.getTemperature());
                    deviceStatusString.append("\nUptime : ").append(deviceStatus.getUptime());

                    if(binding != null) {
                        snackbar = Snackbar.make(view, "done", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        binding.deviceStatus.setText(deviceStatusString);
                    }
                }
            });
        });
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        snackbar.dismiss();
        binding = null;
    }
}
