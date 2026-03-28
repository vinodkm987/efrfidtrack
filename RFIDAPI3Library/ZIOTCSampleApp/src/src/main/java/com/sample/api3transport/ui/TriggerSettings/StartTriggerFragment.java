package com.sample.api3transport.ui.TriggerSettings;


import static com.sample.api3transport.RFIDHandler.mReader;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sample.api3transport.R;
import com.sample.api3transport.databinding.FragmentStartTriggerBinding;
import com.zebra.rfid.api3.GPITrigger;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.StartTrigger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartTriggerFragment extends Fragment {
    private static final String TAG = "START_TRIGGER";
    private FragmentStartTriggerBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentStartTriggerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<CharSequence> sessionAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.start_trigger, android.R.layout.simple_spinner_item);
        sessionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStartTrigger.setAdapter(sessionAdapter);

        binding.buttonSave.setOnClickListener(v -> {
            setStartTrigger();
        });


    }

    private void setStartTrigger() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                StartTrigger startTrigger = mReader.Config.getStartTrigger();
                if ("Immediate".equals(binding.spinnerStartTrigger.getSelectedItem())) {
                    startTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
                }else if("GPI".equals(binding.spinnerStartTrigger.getSelectedItem())) {
                    startTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_GPI);
                startTrigger.GPI = new GPITrigger[1];
                startTrigger.GPI[0] = new GPITrigger();
                startTrigger.GPI[0].setPortNumber(Integer.parseInt(binding.etPort.getText().toString()));
                startTrigger.GPI[0].setSignal(binding.toggleSignal.isChecked());
                }

                mReader.Config.setStartTrigger(startTrigger);
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