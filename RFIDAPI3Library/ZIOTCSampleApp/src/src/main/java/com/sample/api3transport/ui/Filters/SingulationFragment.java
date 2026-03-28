package com.sample.api3transport.ui.Filters;


import static com.sample.api3transport.RFIDHandler.ProtocolType;
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
import com.sample.api3transport.databinding.FragmentSingulationBinding;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class SingulationFragment extends Fragment {
    private static final String TAG = "SINGULATION_FRAGMENT";
    private FragmentSingulationBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentSingulationBinding.inflate(inflater, container, false);
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
                getSingulation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> sessionAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.session_array, android.R.layout.simple_spinner_item);
        sessionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSession.setAdapter(sessionAdapter);

        ArrayAdapter<CharSequence> inventoryAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.inventory_state_array, android.R.layout.simple_spinner_item);
        inventoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerInventoryState.setAdapter(inventoryAdapter);

        ArrayAdapter<CharSequence> sLFlagAdaptor = ArrayAdapter.createFromResource(getActivity(),
                R.array.sl_flags_array, android.R.layout.simple_spinner_item);
        sLFlagAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSlFlag.setAdapter(sLFlagAdaptor);

        binding.buttonSave.setOnClickListener(v -> {
            setsingulationControl(view);
        });
    }

    private void getSingulation() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<Antennas.SingulationControl> singulationControl = new AtomicReference<>();
        executor.execute(() -> {
            try {
                singulationControl.set(mReader.Config.Antennas.getSingulationControl(binding.antenna.getSelectedItemPosition() + 1));
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {
                if (singulationControl.get() != null) {
                    if(singulationControl.get().getSession() != null){
                        binding.spinnerSession.setSelection(singulationControl.get().getSession().getValue());
                    }
                    binding.etTagPopulation.setText(String.valueOf(singulationControl.get().getTagPopulation()));
                    if(singulationControl.get().Action.getInventoryState() != null) {
                        binding.spinnerInventoryState.setSelection(singulationControl.get().Action.getInventoryState().getValue());
                    }
                    if(singulationControl.get().Action.getSLFlag() != null){
                        binding.spinnerSlFlag.setSelection(singulationControl.get().Action.getSLFlag().getValue());
                    }
                }
            });
        });
    }

    private void setsingulationControl(View view) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Antennas.SingulationControl singulationControl = mReader.Config.Antennas.getSingulationControl(binding.antenna.getSelectedItemPosition() + 1);
                singulationControl.setSession(SESSION.GetSession(binding.spinnerSession.getSelectedItemPosition()));
                singulationControl.setTagPopulation(Short.parseShort(binding.etTagPopulation.getText().toString()));
                singulationControl.Action.setInventoryState(INVENTORY_STATE.GetInventoryState(binding.spinnerInventoryState.getSelectedItemPosition()));
                singulationControl.Action.setSLFlag(SL_FLAG.GetSLFlag(binding.spinnerSlFlag.getSelectedItemPosition()));
                if(ProtocolType.equals("LLRP"))
                singulationControl.Action.setPerformStateAwareSingulationAction(true);  //only for LLRP
                mReader.Config.Antennas.setSingulationControl(binding.antenna.getSelectedItemPosition() + 1, singulationControl);
                Snackbar.make(view, "Setting singulation success" , Snackbar.LENGTH_SHORT).show();

            } catch (InvalidUsageException | OperationFailureException e) {
                Snackbar.make(view, "Setting singulation Failure" , Snackbar.LENGTH_SHORT).show();
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