package com.sample.api3transport.ui.TriggerSettings;


import static com.sample.api3transport.RFIDHandler.mReader;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sample.api3transport.R;
import com.sample.api3transport.databinding.FragmentStopTriggerBinding;
import com.zebra.rfid.api3.GPITrigger;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.StopTrigger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StopTriggerFragment extends Fragment {
    private static final String TAG = "STOP_TRIGGER";
    private FragmentStopTriggerBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentStopTriggerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayAdapter<CharSequence> sessionAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.stop_trigger, android.R.layout.simple_spinner_item);
        sessionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStopTrigger.setAdapter(sessionAdapter);

        binding.spinnerStopTrigger.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (binding.spinnerStopTrigger.getSelectedItem().toString()){
                    case "Immediate":
                        binding.layoutGpi.setVisibility(View.GONE);
                        binding.layoutStopCondition.setVisibility(View.GONE);
                        break;
                    case "GPI":
                        binding.layoutGpi.setVisibility(View.VISIBLE);
                        binding.layoutStopCondition.setVisibility(View.GONE);
                        break;
                    case "Duration":
                        binding.layoutGpi.setVisibility(View.GONE);
                        binding.layoutStopCondition.setVisibility(View.VISIBLE);
                        binding.tvStopCondition.setText("Duration");
                        break;
                    case "Tag Observation":
                        binding.layoutGpi.setVisibility(View.GONE);
                        binding.layoutStopCondition.setVisibility(View.VISIBLE);
                        binding.tvStopCondition.setText("Tag Count");
                        break;
                    case "N attempts":
                        binding.layoutGpi.setVisibility(View.GONE);
                        binding.layoutStopCondition.setVisibility(View.VISIBLE);
                        binding.tvStopCondition.setText("Antenna Cycle");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.buttonSave.setOnClickListener(v -> {
            setStopTrigger();
        });



    }

    private void setStopTrigger() {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                //StopTrigger stopTrigger = mReader.Config.getStopTrigger();
                StopTrigger stopTrigger = new StopTrigger();

                if(binding.spinnerStopTrigger.getSelectedItem().equals("Immediate")){
                    stopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
                }else if(binding.spinnerStopTrigger.getSelectedItem().equals("GPI")){
                    stopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_GPI_WITH_TIMEOUT);
                    stopTrigger.GPI = new GPITrigger[1];
                    stopTrigger.GPI[0] = new GPITrigger();
                    stopTrigger.GPI[0].setPortNumber(Integer.parseInt(binding.etPort.getText().toString()));
                    stopTrigger.GPI[0].setSignal(binding.toggleSignal.isChecked());
                }else if(binding.spinnerStopTrigger.getSelectedItem().equals("Duration")){
                    stopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_DURATION);
                    stopTrigger.setDurationMilliSeconds(Integer.parseInt(binding.etStopCondition.getText().toString()));
                }else if(binding.spinnerStopTrigger.getSelectedItem().equals("Tag Observation")){
                    stopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT);
                    stopTrigger.TagObservation.setN(Short.parseShort(binding.etStopCondition.getText().toString()));
                    stopTrigger.TagObservation.setTimeout(3000);
                }
                else if(binding.spinnerStopTrigger.getSelectedItem().equals("N attempts")){
                    stopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_N_ATTEMPTS_WITH_TIMEOUT);
//                    TODO
                    stopTrigger.NumAttempts.setN(Short.parseShort(binding.etStopCondition.getText().toString()));
                    stopTrigger.NumAttempts.setTimeout(3000);
                }

                mReader.Config.setStopTrigger(stopTrigger);
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