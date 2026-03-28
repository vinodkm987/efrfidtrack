package com.sample.api3transport.ui.Connect;


import static com.sample.api3transport.RFIDHandler.LLRPport;
import static com.sample.api3transport.RFIDHandler.ProtocolType;
import static com.sample.api3transport.RFIDHandler.ZIOTCport;
import static com.sample.api3transport.RFIDHandler.hostName;
import static com.sample.api3transport.RFIDHandler.mReader;
import static com.sample.api3transport.RFIDHandler.menuViewModel;
import static com.sample.api3transport.RFIDHandler.timeoutMilliSeconds;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.sample.api3transport.MenuViewModel;
import com.sample.api3transport.R;
import com.sample.api3transport.RFIDHandler;
import com.sample.api3transport.databinding.FragmentConnectBinding;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectFragment extends Fragment {

    private static final String TAG = "CONNECT_FRAGMENT";
    private FragmentConnectBinding binding;
    private String password;
    private String status = "Connected";

    private Boolean isZIOTC = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            ProtocolType = savedInstanceState.getString("protocolType", "ZIOTC");
            binding.ZIOTC.setChecked(savedInstanceState.getBoolean("isZIOTCChecked", true));
            binding.LLRP.setChecked(savedInstanceState.getBoolean("isLLRPChecked", false));
        } else {
            ProtocolType = "ZIOTC";
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // put string value
        outState.putString("protocolType",ProtocolType);
        outState.putBoolean("isZIOTCChecked",isZIOTC);
        outState.putBoolean("isLLRPChecked", !isZIOTC);
        super.onSaveInstanceState(outState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        menuViewModel = new ViewModelProvider(requireActivity()).get(MenuViewModel.class);
        updateUI();

        binding.edittextIp.setText("fxr90-a24194.zebra.lan");//fxr90-a24194.zebra.lan

        binding.tvSdkVersion.setText(com.zebra.rfid.api3.BuildConfig.VERSION_NAME);

        binding.radiogrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton)group.findViewById(checkedId);
                Log.d(TAG,"radio "+radioButton.getText());
                // ProtocolType = radioButton.getText().toString();
                if(mReader!=null)
                    Log.d(TAG,"Isconnected "+ mReader.isConnected());
                if(radioButton.getText().toString().equals("ZIOTC")){
                    isZIOTC = true;
                    binding.edittextPassword.setVisibility(View.VISIBLE);
                    if(ProtocolType.equals("LLRP")){
                        Log.d(TAG,"Protocolstill LLRP");
                        if(mReader != null && mReader.isConnected()){
                            Log.d(TAG,"readerstill connected");
                            try {
                                mReader.disconnect();

                            } catch (InvalidUsageException | OperationFailureException e) {
                                if (e.getStackTrace().length > 0) {
                                    Log.e(TAG, e.getStackTrace()[0].toString());
                                }
                            }
                        }
                        updateUI();
                        Log.d(TAG,"Now protocl is ziotc");
                        ProtocolType = "ZIOTC";
                    }

                }
                else{
                    isZIOTC = false;
                    binding.edittextPassword.setVisibility(View.GONE);
                    // updateUI();
                    if( ProtocolType.equals("ZIOTC")){
                        if (mReader != null && mReader.isConnected()  ){
                            try {
                                mReader.disconnect();

                            } catch (InvalidUsageException | OperationFailureException e) {
                                if (e.getStackTrace().length > 0) {
                                    Log.e(TAG, e.getStackTrace()[0].toString());
                                }
                            }
                        }
                        updateUI();
                        ProtocolType = "LLRP";
                        Log.d("RadioButton","Checkchange LLRP");
                    }

                }

            }
        });

         /*   int selectedId = binding.radiogrp.getCheckedRadioButtonId();
            //Log.d(TAG,"radio "+selectedId);
            RadioButton radioButton = (RadioButton)binding.radiogrp.findViewById(selectedId);
               if(radioButton != null) ProtocolType = radioButton.getText().toString();*/
        if(ProtocolType.equals("ZIOTC")) {
            binding.ZIOTC.setChecked(true);
            isZIOTC = true;
        }
        //   Log.d(TAG,"radio "+radioButton.getText() + radioButton.getText());
        if( ProtocolType.equals("ZIOTC"))
            binding.edittextPassword.setText("Zebra@123");
        binding.buttonConnect.setOnClickListener(view1 -> {
            if (mReader != null && mReader.isConnected()) {
                try {
                    mReader.disconnect();
                    mReader = null;
                    updateUI();
                } catch (InvalidUsageException | OperationFailureException e) {
                    if (e.getStackTrace().length > 0) {
                        Log.e(TAG, e.getStackTrace()[0].toString());
                    }
                }
            } else {
                hostName = binding.edittextIp.getText().toString();
                Log.d(TAG, "hostName: " + hostName);
                if(hostName.isEmpty()){
                    binding.edittextIp.setError("Please enter valid IP");
                    return;
                }
                if( ProtocolType.equals("ZIOTC")){
                    password = binding.edittextPassword.getText().toString();
                    if(password.isEmpty()){
                        binding.edittextPassword.setError("Please enter valid password");
                        return;
                    }
                }
                connect(view);
            }
        });
    }

    private synchronized void connect(View view) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                status = "Connected";
                Snackbar.make(view, "connecting...", Snackbar.LENGTH_INDEFINITE).show();

                if(ProtocolType.equals("ZIOTC")){
                    mReader = new RFIDReader(hostName,ZIOTCport, timeoutMilliSeconds, "ZIOTC", "ZIOTC");
                    Log.d(TAG, String.valueOf(mReader.getHostName()));
                    mReader.setPassword(password);
                    mReader.secureConnection(false);
                }
                else if(ProtocolType.equals("LLRP")){
                    mReader = new RFIDReader(hostName, LLRPport, 5000);
                    Log.d(TAG, String.valueOf(mReader.getHostName()));
                }else {
                   // mReader = mReader;
                }
                mReader.connect();
            } catch (OperationFailureException oe) {
                Snackbar.make(view, "failed", Snackbar.LENGTH_SHORT).show();

                if (oe.getStackTrace().length > 0) {
                    Log.e(TAG, oe.getStackTrace()[0].toString());
                    status = oe.getVendorMessage();
                }
            } catch(InvalidUsageException ie ){
                if (ie.getStackTrace().length > 0) {
                    Log.e(TAG, ie.getStackTrace()[0].toString());
                    status = ie.getVendorMessage();
                }

            }
            handler.post(() -> {
                Snackbar.make(view, status, Snackbar.LENGTH_SHORT).show();
                if(status != null && status.equals("Connected")) {
                    ReaderConnection connection = (ReaderConnection) getActivity();
                    Objects.requireNonNull(connection).onConnected();
                    updateUI();
                }else{
                    Snackbar.make(view, "failed", Snackbar.LENGTH_SHORT).show();
                    mReader = null;
                }

            });
        });
    }

    private void updateUI() {
        if (mReader != null && mReader.isConnected()) {
            binding.buttonConnect.setText(R.string.disconnect);
            binding.tvStatus.setText(String.format("Connected to: %s", mReader.getHostName()));
            binding.tvReaderId.setText(mReader.ReaderCapabilities.ReaderID.getID());
            binding.tvModelName.setText(mReader.ReaderCapabilities.getModelName());
            if(mReader.ReaderCapabilities.getCommunicationStandard() != null )
                binding.tvCommunicationStandard.setText(mReader.ReaderCapabilities.getCommunicationStandard().toString());
            binding.tvCountryCode.setText(String.valueOf(mReader.ReaderCapabilities.getCountryCode()));
            binding.tvFirmwareVersion.setText(mReader.ReaderCapabilities.getFirwareVersion());
            binding.tvRssiFilter.setText(String.valueOf(mReader.ReaderCapabilities.isRSSIFilterSupported()));

            if(ProtocolType.equals("LLRP")){
                menuViewModel.selectItem(false);
            }

        } else {
            binding.buttonConnect.setText(R.string.connect);
            binding.tvStatus.setText(R.string.not_connected);
            binding.tvReaderId.setText("");
            binding.tvModelName.setText("");
            binding.tvCommunicationStandard.setText("");
            binding.tvCountryCode.setText("");
            binding.tvFirmwareVersion.setText("");
            binding.tvRssiFilter.setText("");
            menuViewModel.selectItem(true);

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }







}