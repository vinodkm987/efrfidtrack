package com.sample.api3transport.ui.AccessOperations;


import static com.sample.api3transport.RFIDHandler.mReader;
import static com.sample.api3transport.RFIDHandler.tagIDs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.sample.api3transport.databinding.FragmentKillBinding;
import com.zebra.rfid.api3.AntennaInfo;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.TagAccess;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KillFragment extends Fragment {
    private static final String TAG = "KILL_FRAGMENT";
    private FragmentKillBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentKillBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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


        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>
                (getActivity(), android.R.layout.select_dialog_item, tagIDs);
        binding.tagID.autoCompleteView.setAdapter(tagAdapter);

        binding.buttonKill.setOnClickListener(v -> {
            String tagId = binding.tagID.autoCompleteView.getText().toString();
            TagAccess tagAccess = new TagAccess();
            TagAccess.KillAccessParams killAccessParams = tagAccess.new KillAccessParams();
            killAccessParams.setKillPassword(Long.parseLong(binding.etPassword.getText().toString(), 16));

            AntennaInfo antennaInfo = new AntennaInfo();
            short[] antennaID = new short[1];
            antennaID[0] = (short) (binding.antenna.getSelectedItemPosition() + 1);
            antennaInfo.setAntennaID(antennaID);

            killTag(view, tagId, killAccessParams, antennaInfo);
        });
    }

    private void killTag(View view, String tagId, TagAccess.KillAccessParams killAccessParams, AntennaInfo antennaInfo) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                mReader.Actions.TagAccess.killWait(tagId, killAccessParams, antennaInfo);
            } catch (InvalidUsageException | OperationFailureException e) {
                Snackbar.make(view, "Kill Tag Failed", Snackbar.LENGTH_SHORT).show();
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
                return;
            }
            handler.post(() -> Snackbar.make(view, "Kill Tag Success", Snackbar.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}