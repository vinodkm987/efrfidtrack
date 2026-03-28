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
import com.sample.api3transport.databinding.FragmentReadWriteBinding;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.AccessFilter;
import com.zebra.rfid.api3.AntennaInfo;
import com.zebra.rfid.api3.FILTER_MATCH_PATTERN;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ReadWriteFragment extends Fragment {
    private static final String TAG = "READ_WRITE_FRAGMENT";
    private FragmentReadWriteBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentReadWriteBinding.inflate(inflater, container, false);
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
                R.array.memory_bank_array, android.R.layout.simple_spinner_item);
        memBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMembank.setAdapter(memBankAdapter);

        binding.buttonRead.setOnClickListener(v -> {
            String tagId = binding.tagID.autoCompleteView.getText().toString();
            TagAccess tagAccess = new TagAccess();
            TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
            readAccessParams.setAccessPassword(Long.parseLong(binding.etPassword.getText().toString(), 16));
            readAccessParams.setCount(Integer.parseInt(binding.etLength.getText().toString()));
            readAccessParams.setMemoryBank(MEMORY_BANK.GetMemoryBankValue(binding.spinnerMembank.getSelectedItem().toString()));
            readAccessParams.setOffset(Integer.parseInt(binding.etOffset.getText().toString()));

            AntennaInfo antennaInfo = new AntennaInfo();
            short[] antennaID = new short[1];
            antennaID[0] = (short) (binding.antenna.getSelectedItemPosition() + 1);
            antennaInfo.setAntennaID(antennaID);

            readTag(view, tagId, readAccessParams, antennaInfo);
        });

        binding.buttonWrite.setOnClickListener(v -> {
            String tagId =binding.tagID.autoCompleteView.getText().toString();
            TagAccess tagAccess = new TagAccess();
            TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
            String writeData = binding.etData.getText().toString();
            writeAccessParams.setAccessPassword(Long.parseLong(binding.etPassword.getText().toString(), 16));
            writeAccessParams.setMemoryBank(MEMORY_BANK.GetMemoryBankValue(binding.spinnerMembank.getSelectedItem().toString()));
            writeAccessParams.setOffset(Integer.parseInt(binding.etOffset.getText().toString())); // start writing from word offset 0
            writeAccessParams.setWriteData(writeData);
            writeAccessParams.setWriteDataLength(writeData.length() / 4);

            AntennaInfo antennaInfo = new AntennaInfo();
            short[] antennaID = new short[1];
            antennaID[0] = (short) (binding.antenna.getSelectedItemPosition() + 1);
            antennaInfo.setAntennaID(antennaID);

            writeTag(view, tagId, writeAccessParams, antennaInfo);
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

    private void writeTag(View view, String tagId, TagAccess.WriteAccessParams writeAccessParams, AntennaInfo antennaInfo) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        TagData tagData = new TagData();
        executor.execute(() -> {
            try {
                mReader.Actions.TagAccess.writeWait(tagId, writeAccessParams, antennaInfo, tagData);
                AccessFilter accessFilter = setAccessFilter(tagId);
                //mReader.Actions.TagAccess.writeEvent(writeAccessParams, null, antennaInfo);
            } catch (InvalidUsageException | OperationFailureException e) {
                Snackbar.make(view, "Write Failed " + tagData.getOpStatus() , Snackbar.LENGTH_LONG).show();
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
                return;
            }
            handler.post(() -> {
                if (tagData.getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                    Snackbar.make(view, "Write Success", Snackbar.LENGTH_LONG).show();
                    //binding.etData.setText(tagData.getTagID());
                } else {
                    Snackbar.make(view, "Write Failed", Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    private void readTag(View view, String tagId, TagAccess.ReadAccessParams readAccessParams, AntennaInfo antennaInfo) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        AtomicReference<TagData> tagData = new AtomicReference<>();
        executor.execute(() -> {
            try {
                tagData.set(mReader.Actions.TagAccess.readWait(tagId, readAccessParams, antennaInfo));
            } catch (InvalidUsageException | OperationFailureException e) {
                Snackbar.make(view, "Read failed", Snackbar.LENGTH_LONG).show();
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
                return;
            }
            handler.post(() -> {
                if (tagData.get().getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                    Snackbar.make(view, "Read Success", Snackbar.LENGTH_LONG).show();
                    binding.etData.setText(tagData.get().getMemoryBankData());
                }
                else {
                    Snackbar.make(view, "Read Failed", Snackbar.LENGTH_LONG).show();
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