package com.sample.api3transport.ui.TagSettings;

import static com.sample.api3transport.RFIDHandler.ProtocolType;
import static com.sample.api3transport.RFIDHandler.mReader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.sample.api3transport.databinding.FragmentTagSettingsBinding;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TagStorageSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TagSettingsFragment extends Fragment {
    private static final String TAG = "TAG_SETTINGS_FRAGMENT";
    private FragmentTagSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTagSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mReader == null || !mReader.isConnected()){
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mReader != null && mReader.isConnected()) {
            getTagReportSettings();
        } else {
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_LONG).show();
        }

//        ArrayList<CheckBox> checkBoxes = new ArrayList<>();
//        checkBoxes.add(binding.cbPhase);
//        checkBoxes.add(binding.cbAntenna);
//        checkBoxes.add(binding.cbPc);
//        checkBoxes.add(binding.cbRssi);
//        checkBoxes.add(binding.cbTagSeen);
//        checkBoxes.add(binding.cbChannel);
//        checkBoxes.add(binding.cbCrc);
//        checkBoxes.add(binding.cbFirstseen);
//        checkBoxes.add(binding.cbLastSeen);
//
//        binding.cbAllFieldscheck.setOnCheckedChangeListener((buttonView, isChecked) ->
//        {
//            for (int i = 0; i < checkBoxes.size(); i++) {
//                CheckBox currentChecBox = checkBoxes.get(i);
//                currentChecBox.setChecked(isChecked);
//            }});

        if(ProtocolType.equals("LLRP")){
            binding.userRow.setVisibility(View.GONE);
            binding.tidRow.setVisibility(View.GONE);
            binding.lastSeenRow.setVisibility(View.VISIBLE);
            binding.firstSeenRow.setVisibility(View.VISIBLE);
        }
        else {
            binding.lastSeenRow.setVisibility(View.GONE);
            binding.firstSeenRow.setVisibility(View.GONE);
            binding.userRow.setVisibility(View.VISIBLE);
            binding.tidRow.setVisibility(View.VISIBLE);
        }

        binding.buttonSave.setOnClickListener(v -> {
            Snackbar.make(view, "Updating Settings...", Snackbar.LENGTH_LONG).show();
            TagStorageSettings tagStorageSettings = new TagStorageSettings();
            TAG_FIELD[] tag_fields = new TAG_FIELD[15];
            int index = 0;
            if (binding.cbRssi.isChecked())
                tag_fields[index++] = TAG_FIELD.PEAK_RSSI;
            if (binding.cbPhase.isChecked())
                tag_fields[index++] = TAG_FIELD.PHASE_INFO;
            if (binding.cbPc.isChecked())
                tag_fields[index++] = TAG_FIELD.PC;
            if (binding.cbChannel.isChecked())
                tag_fields[index++] = TAG_FIELD.CHANNEL_INDEX;
            if (binding.cbTagSeen.isChecked())
                tag_fields[index++] = TAG_FIELD.TAG_SEEN_COUNT;
            if (binding.cbCrc.isChecked())
                tag_fields[index++] = TAG_FIELD.CRC;
            if (binding.cbAntenna.isChecked())
                tag_fields[index++] = TAG_FIELD.ANTENNA_ID;
//            if (binding.cbEpc.isChecked())
//                tag_fields[index++] = TAG_FIELD.EPC;
            if (binding.cbTid.isChecked())
                tag_fields[index++] = TAG_FIELD.TID;
            if (binding.cbUser.isChecked())
                tag_fields[index++] = TAG_FIELD.USER;
            if(binding.cbFirstseen.isChecked())
                tag_fields[index++] = TAG_FIELD.FIRST_SEEN_TIME_STAMP;
            if(binding.cbLastSeen.isChecked())
                tag_fields[index++] = TAG_FIELD.LAST_SEEN_TIME_STAMP;
            tagStorageSettings.setTagFields(tag_fields);
            setTagReportSettings(tagStorageSettings);
        });


    }

    private synchronized void setTagReportSettings(TagStorageSettings tagStorageSettings) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                mReader.Config.setTagStorageSettings(tagStorageSettings);
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
        });
    }

    private synchronized void getTagReportSettings() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        final TagStorageSettings[] tagStorageSettings = new TagStorageSettings[1];
        executor.execute(() -> {
            try {
                tagStorageSettings[0] = mReader.Config.getTagStorageSettings();
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {
                if (tagStorageSettings[0] != null) {
                    TAG_FIELD[] tagFields = tagStorageSettings[0].getTagFields();
                    if(Arrays.asList(tagFields).contains(TAG_FIELD.ALL_TAG_FIELDS)){
                        binding.cbPc.setChecked(true);
                        binding.cbRssi.setChecked(true);
                        binding.cbPhase.setChecked(true);
                        binding.cbChannel.setChecked(true);
                        binding.cbTagSeen.setChecked(true);
                        binding.cbCrc.setChecked(true);
                        binding.cbAntenna.setChecked(true);
                        binding.cbLastSeen.setChecked(true);
                        binding.cbFirstseen.setChecked(true);
                        return;
                    }
                    for (TAG_FIELD tagField : tagFields) {
                        if (tagField == TAG_FIELD.PEAK_RSSI)
                            binding.cbRssi.setChecked(true);
                        if (tagField == TAG_FIELD.PHASE_INFO)
                            binding.cbPhase.setChecked(true);
                        if (tagField == TAG_FIELD.PC)
                            binding.cbPc.setChecked(true);
                        if (tagField == TAG_FIELD.CHANNEL_INDEX)
                            binding.cbChannel.setChecked(true);
                        if (tagField == TAG_FIELD.TAG_SEEN_COUNT)
                            binding.cbTagSeen.setChecked(true);
                        if (tagField == TAG_FIELD.CRC)
                            binding.cbCrc.setChecked(true);
                        if (tagField == TAG_FIELD.ANTENNA_ID)
                            binding.cbAntenna.setChecked(true);

//                        if (tagField == TAG_FIELD.EPC)
//                            binding.cbEpc.setChecked(true);
                        if (tagField == TAG_FIELD.TID)
                            binding.cbTid.setChecked(true);
                        if (tagField == TAG_FIELD.USER)
                            binding.cbUser.setChecked(true);
                        // if (tagField == TAG_FIELD.ALL_TAG_FIELDS)
                        //    binding.cbAllFieldscheck.setChecked(true);
                        if (tagField == TAG_FIELD.LAST_SEEN_TIME_STAMP)
                            binding.cbLastSeen.setChecked(true);
                        if (tagField == TAG_FIELD.FIRST_SEEN_TIME_STAMP)
                            binding.cbFirstseen.setChecked(true);
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
