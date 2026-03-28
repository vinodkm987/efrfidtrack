package com.sample.api3transport.ui.AntennaRFConfigSettings;

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
import com.sample.api3transport.R;
import com.sample.api3transport.databinding.FragmentAntennaRfconfigBinding;
import com.zebra.rfid.api3.ANTENNA_STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENVIRONMENT_MODE;
import com.zebra.rfid.api3.GPITrigger;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class AntennaRFConfigFragment extends Fragment {
    private static final String TAG = "ANTENNARFCONFIG_FRAGMENT";
    private FragmentAntennaRfconfigBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentAntennaRfconfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mReader == null || !mReader.isConnected()){
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> antennaArray = new ArrayList<>();
        if(mReader.isConnected()) {
            for (int i = 1; i <= mReader.ReaderCapabilities.getNumAntennaSupported(); i++) {
                antennaArray.add("Antenna " + i);
            }
        }
        ArrayAdapter<String> antennaAdaptor = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, antennaArray);
        antennaAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.antenna.setAdapter(antennaAdaptor);

        binding.antenna.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getRFConfig();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter<CharSequence> conditionType = ArrayAdapter.createFromResource(getActivity(),
                R.array.antenna_stop_condition_type, android.R.layout.simple_spinner_item);
        conditionType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAntennaStopCondition.setAdapter(conditionType);


        binding.spinnerAntennaStopCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (binding.spinnerAntennaStopCondition.getSelectedItem().toString()){
                    case "GPI":
                        binding.stopValueRow6.setVisibility(View.GONE);
                        binding.layoutgpi.setVisibility(View.VISIBLE);
                        break;

                    default:
                        binding.stopValueRow6.setVisibility(View.VISIBLE);
                        binding.layoutgpi.setVisibility(View.GONE);


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> signalParam = ArrayAdapter.createFromResource(getActivity(),
                R.array.signal_Params, android.R.layout.simple_spinner_item);
        signalParam.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSignalParams.setAdapter(signalParam);

        ArrayAdapter<CharSequence> environment = ArrayAdapter.createFromResource(getActivity(),
                R.array.environment_params, android.R.layout.simple_spinner_item);
        environment.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEnvironment.setAdapter(environment);

        binding.buttonSave.setOnClickListener(v -> {
            setRFConfig();
        });
    }

    private void getRFConfig() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<Antennas.AntennaRfConfig> antennaRfConfigAtomicReference = new AtomicReference<>();
        executor.execute(() -> {
            try {
                antennaRfConfigAtomicReference.set(mReader.Config.Antennas.getAntennaRfConfig(binding.antenna.getSelectedItemPosition() + 1));
                Antennas.AntennaRfConfig antennaRfConfig = mReader.Config.Antennas.getAntennaRfConfig(binding.antenna.getSelectedItemPosition() + 1);
                Log.d(TAG,"Power "+ antennaRfConfig.getTransmitPowerIndex());

            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {
               if (antennaRfConfigAtomicReference.get() != null) {
                    if(antennaRfConfigAtomicReference.get().getAntennaStopTriggerConfig().getStopTriggerType() != null){
                        int val = antennaRfConfigAtomicReference.get().getAntennaStopTriggerConfig().getStopTriggerType().getValue()-1;
                        Log.d(TAG,"triggr "+antennaRfConfigAtomicReference.get().getAntennaStopTriggerConfig().getStopTriggerType());
                        if(val>2)
                            val--;
                        binding.spinnerAntennaStopCondition.setSelection(val);
                    }
                    if((binding.spinnerAntennaStopCondition.getSelectedItem().toString().equals("GPI"))){
                          binding.etPort.setText(String.valueOf(antennaRfConfigAtomicReference.get().getAntennaStopTriggerConfig().getGpiTrigger().getPortNumber() ));
                          binding.spinnerSignalParams.setSelection(antennaRfConfigAtomicReference.get().getAntennaStopTriggerConfig().getGpiTrigger().getGpi_signal().getValue() );
                          binding.etDebounce.setText(String.valueOf(antennaRfConfigAtomicReference.get().getAntennaStopTriggerConfig().getGpiTrigger().getDebounceTime() ));

                    }
                    else{
                        binding.etStopConditionVal.setText(String.valueOf(antennaRfConfigAtomicReference.get().getAntennaStopTriggerConfig().getAntennaStopConditionValue()));
                    }

                    binding.etTransmitPower.setText(String.valueOf(antennaRfConfigAtomicReference.get().getTransmitPowerIndex()));

                    if(antennaRfConfigAtomicReference.get().getEnvironment_mode() != null) {
                        binding.spinnerEnvironment.setSelection(antennaRfConfigAtomicReference.get().getEnvironment_mode().getValue());
                    }

                }
            });
        });
    }

    private void setRFConfig() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                int antennaID = binding.antenna.getSelectedItemPosition()+1;
                Antennas.AntennaRfConfig antennaRfConfig = mReader.Config.Antennas.getAntennaRfConfig(antennaID);
                Antennas.AntennaRfConfig.AntennaStopTrigger antennaStopTrigger = antennaRfConfig.new AntennaStopTrigger();
                int val = binding.spinnerAntennaStopCondition.getSelectedItemPosition()+1;
                if(val>2)
                    val++;
                antennaStopTrigger.setStopTriggerType(ANTENNA_STOP_TRIGGER_TYPE.GetAntennaStopTriggerTypeValue(val));


                if(binding.spinnerAntennaStopCondition.getSelectedItem().toString().equals("GPI")){
                    GPITrigger gpiTrigger = new GPITrigger();
                    gpiTrigger.setPortNumber(Integer.parseInt(binding.etPort.getText().toString()));
                    gpiTrigger.setDebounceTime(Integer.parseInt(binding.etDebounce.getText().toString()));
                    int gpi_signal = binding.spinnerSignalParams.getSelectedItemPosition();
                    gpiTrigger.setGpi_signal(GPITrigger.GPI_SIGNAL.GetGPISignalTypeValue(gpi_signal));
                    antennaStopTrigger.setGpiTrigger(gpiTrigger);
                }
                else
                    antennaStopTrigger.setAntennaStopConditionValue(Integer.parseInt(binding.etStopConditionVal.getText().toString()));

                antennaRfConfig.setAntennaStopTriggerConfig(antennaStopTrigger);
                antennaRfConfig.setEnvironment_mode(ENVIRONMENT_MODE.getEnvironmentMode(binding.spinnerEnvironment.getSelectedItemPosition()));
                antennaRfConfig.setTransmitPowerIndex(Integer.parseInt(binding.etTransmitPower.getText().toString()));

                mReader.Config.Antennas.setAntennaRfConfig(antennaID, antennaRfConfig );


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
