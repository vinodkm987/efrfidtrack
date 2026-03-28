package com.sample.api3transport.ui.AccessOperations;


import static com.sample.api3transport.RFIDHandler.mReader;
import static com.sample.api3transport.RFIDHandler.tagIDs;
import static com.zebra.rfid.api3.HexDump.hexStringToByteArray;

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
import com.sample.api3transport.R;
import com.sample.api3transport.databinding.FragmentLockBinding;
import com.zebra.rfid.api3.AccessFilter;
import com.zebra.rfid.api3.AntennaInfo;
import com.zebra.rfid.api3.FILTER_MATCH_PATTERN;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.LOCK_DATA_FIELD;
import com.zebra.rfid.api3.LOCK_PRIVILEGE;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.TagAccess;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LockFragment extends Fragment {
    private static final String TAG = "LOCK_FRAGMENT";
    private FragmentLockBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentLockBinding.inflate(inflater, container, false);
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

        ArrayAdapter<CharSequence> memBankAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.lock_memory_bank_array, android.R.layout.simple_spinner_item);
        memBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMembank.setAdapter(memBankAdapter);

        ArrayAdapter<CharSequence> lockPrivilegeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.lock_privilege_array, android.R.layout.simple_spinner_item);
        lockPrivilegeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLockPrivilege.setAdapter(lockPrivilegeAdapter);

        binding.buttonLock.setOnClickListener(v -> {
            String tagId = binding.tagID.autoCompleteView.getText().toString();
            TagAccess tagAccess = new TagAccess();
            TagAccess.LockAccessParams lockAccessParams = tagAccess.new LockAccessParams();
            lockAccessParams.setAccessPassword(Long.parseLong(binding.etPassword.getText().toString(), 16));
            lockAccessParams.setLockPrivilege(LOCK_DATA_FIELD.GetLockDataFieldName(binding.spinnerMembank.getSelectedItem().toString()),
                    LOCK_PRIVILEGE.GetLockPrivilegeName(binding.spinnerLockPrivilege.getSelectedItem().toString()));

            AntennaInfo antennaInfo = new AntennaInfo();
            short[] antennaID = new short[1];
            antennaID[0] = (short) (binding.antenna.getSelectedItemPosition() + 1);
            antennaInfo.setAntennaID(antennaID);

            lockTag(view, tagId, lockAccessParams, antennaInfo);
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

    private void lockTag(View view, String tagId, TagAccess.LockAccessParams lockAccessParams, AntennaInfo antennaInfo) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                mReader.Actions.TagAccess.lockWait(tagId, lockAccessParams, antennaInfo);
                AccessFilter accessFilter = setAccessFilter(tagId);
                //mReader.Actions.TagAccess.lockEvent( lockAccessParams, accessFilter, antennaInfo);

            } catch (InvalidUsageException | OperationFailureException e) {
                Snackbar.make(view, "Lock Failed" , Snackbar.LENGTH_LONG).show();
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
                return;
            }
            handler.post(() -> Snackbar.make(view, "Lock Success", Snackbar.LENGTH_LONG).show());
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}