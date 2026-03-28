package com.sample.api3transport.ui.Filters;


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
import com.sample.api3transport.databinding.FragmentPrefiltersBinding;
import com.zebra.rfid.api3.FILTER_ACTION;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.PreFilters;
import com.zebra.rfid.api3.STATE_AWARE_ACTION;
import com.zebra.rfid.api3.TARGET;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class PreFiltersFragment extends Fragment {
    private static final String TAG = "PREFILTER_FRAGMENT";
    private FragmentPrefiltersBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentPrefiltersBinding.inflate(inflater, container, false);
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

        ArrayAdapter<CharSequence> targetAdaptor = ArrayAdapter.createFromResource(getActivity(),
                R.array.pre_filter_target_options, android.R.layout.simple_spinner_item);
        targetAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.prefilterTarget.setAdapter(targetAdaptor);

        ArrayAdapter<CharSequence> actionAdaptor = ArrayAdapter.createFromResource(getActivity(),
                R.array.pre_filter_action_array, android.R.layout.simple_spinner_item);
        actionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.prefilterAction.setAdapter(actionAdaptor);

        ArrayAdapter<CharSequence> memBankAdaptor = ArrayAdapter.createFromResource(getActivity(),
                R.array.pre_filter_memory_bank_array, android.R.layout.simple_spinner_item);
        memBankAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.prefilterMembank.setAdapter(memBankAdaptor);

        ArrayAdapter<CharSequence> prefilterIndex = ArrayAdapter.createFromResource(getActivity(),
                R.array.pre_filter_index, android.R.layout.simple_spinner_item);
        prefilterIndex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.prefilterIndex.setAdapter(prefilterIndex);
        
        binding.prefilterIndex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                getPreFilter(view, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.buttonSave.setOnClickListener(v -> {
            PreFilters filters = new PreFilters();
            PreFilters.PreFilter filter = filters.new PreFilter();
            filter.setAntennaID((short) (binding.antenna.getSelectedItemPosition() + 1));
            filter.setTagPattern(binding.etMask.getText().toString());
            filter.setTagPatternBitCount(Integer.parseInt(binding.etLength.getText().toString()));
            filter.setBitOffset(Integer.parseInt(binding.etPointer.getText().toString()));
            filter.setMemoryBank(MEMORY_BANK.GetMemoryBankValue(binding.prefilterMembank.getSelectedItem().toString()));
            filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE);
            filter.StateAwareAction.setTarget(TARGET.getTarget(binding.prefilterTarget.getSelectedItemPosition()));
            filter.StateAwareAction.setStateAwareAction(getStateAwareActionFromString(binding.prefilterAction.getSelectedItem().toString()));

            setPreFilter(view, filter);
        });

        binding.buttonDelete.setOnClickListener(v -> {
            deletePrefilter(view);
        });

    }

    private STATE_AWARE_ACTION getStateAwareActionFromString(String strAction) {
        STATE_AWARE_ACTION action = null;
        if (strAction.equalsIgnoreCase("INV A NOT INV B OR ASRT SL NOT DSRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A_NOT_INV_B;
        if (strAction.equalsIgnoreCase("INV A OR ASRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A;
        if (strAction.equalsIgnoreCase("NOT INV B OR NOT DSRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_B;
        if (strAction.equalsIgnoreCase("INV A2BB2A NOT INV A OR NEG SL NOT ASRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_A2BB2A_NOT_INV_A;
        if (strAction.equalsIgnoreCase("INV B NOT INV A OR DSRT SL NOT ASRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A;
        if (strAction.equalsIgnoreCase("INV B OR DSRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B;
        if (strAction.equalsIgnoreCase("NOT INV A OR NOT ASRT SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_A;
        if (strAction.equalsIgnoreCase("NOT INV A2BB2A OR NOT NEG SL"))
            action = STATE_AWARE_ACTION.STATE_AWARE_ACTION_NOT_INV_A2BB2A;
        return action;
    }

    private void getPreFilter(View view, int index) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<PreFilters.PreFilter> preFilter = new AtomicReference<>();
        executor.execute(() -> {
            try {
                preFilter.set(mReader.Actions.PreFilters.getPreFilter(index));
            } catch (InvalidUsageException e) {
                Snackbar.make(view, "Not Found in filter index "+index, Snackbar.LENGTH_SHORT).show();
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {
                if(preFilter.get() != null){
                    StringBuilder tagPattern = new StringBuilder();
                    for (byte b : preFilter.get().getTagPattern()) {
                        tagPattern.append(String.format("%02X", b));
                    }
                    binding.etMask.setText(tagPattern);
                    binding.etLength.setText(String.valueOf(preFilter.get().getTagPatternBitCount()));
                    binding.etPointer.setText(String.valueOf(preFilter.get().getBitOffset()));
                    binding.prefilterMembank.setSelection(preFilter.get().getMemoryBank().getValue());
                    binding.prefilterTarget.setSelection(preFilter.get().StateAwareAction.getTarget().getValue());
                    binding.prefilterAction.setSelection(preFilter.get().StateAwareAction.getStateAwareAction().getValue());
                    binding.etTruncate.setText(String.valueOf(preFilter.get().getTruncateAction().getValue()));
                    binding.antenna.setSelection(preFilter.get().getAntennaID() - 1);
                }
            });
        });
    }

    private void setPreFilter(View view, PreFilters.PreFilter filter) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                mReader.Actions.PreFilters.add(filter);
                Snackbar.make(view, "Pre filter save successfull"  , Snackbar.LENGTH_LONG).show();
            } catch (InvalidUsageException | OperationFailureException e) {
                Snackbar.make(view, "Pre filter save failure"  , Snackbar.LENGTH_LONG).show();
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
        });
    }


    private void deletePrefilter(View view) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                mReader.Actions.PreFilters.deleteAll();
                    Snackbar.make(view, "Pre filter delete successfull"  , Snackbar.LENGTH_LONG).show();
            } catch (InvalidUsageException | OperationFailureException e) {
                //   Snackbar.make(view, "Pre filter save failure"  , Snackbar.LENGTH_LONG).show();
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