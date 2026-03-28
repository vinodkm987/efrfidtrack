package com.sample.api3transport.ui.Firmware;


import static android.os.Build.VERSION.SDK_INT;
import static com.sample.api3transport.RFIDHandler.fwUpdateDataViewModel;
import static com.sample.api3transport.RFIDHandler.mReader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.sample.api3transport.databinding.FragmentFirmwareBinding;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirmwareFragment extends Fragment {
    private static final String TAG = "FIRMWARE_FRAGMENT";
    private FragmentFirmwareBinding binding;
    private String selectedFilePath;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentFirmwareBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mReader == null || !mReader.isConnected()){
            Snackbar.make(view, "Reader Not Connected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        binding.readerFw.setText(mReader.ReaderCapabilities.getFirwareVersion());

        binding.btSelectFw.setOnClickListener(view1 -> {
            if (SDK_INT >= 30) {
                if (!Environment.isExternalStorageManager()) {
                    Snackbar.make(view, "Permission needed!", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Settings", v -> {
                                try {
                                    Uri uri = Uri.parse("package:" + requireActivity().getPackageName());
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                                    startActivity(intent);
                                } catch (Exception ex) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                    startActivity(intent);
                                }
                            }).show();
                }else{
                    openFileStorage();
                }
            }else{
                if(isReadStoragePermissionGranted()) {
                    openFileStorage();
                }else{
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });

        binding.btUpdateFw.setOnClickListener(view12 -> {
            if (selectedFilePath!=null&&!selectedFilePath.isEmpty()) {
                updateFirmware(selectedFilePath);
            } else {
                Snackbar.make(view, "Please select the file", Snackbar.LENGTH_SHORT).show();
            }
        });

        fwUpdateDataViewModel.getStatus().observe(requireActivity(), fwUpdateStatus -> {
            binding.precentage.setText(String.format("%s%%", fwUpdateStatus[1]));
            binding.status.setText(fwUpdateStatus[0]);

        });
    }

    private void openFileStorage(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
        intent.putExtra("DocumentsContract.EXTRA_INITIAL_URI", uri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getData().toString().contains("content://com.android.providers")) {
                                ShowPlugInPathChangeDialog();
                            } else {
                                selectedFilePath = data.getData().getPath();
                                binding.selectedFw.setText(selectedFilePath);
                            }
                        }
                    }
                }

                private void ShowPlugInPathChangeDialog() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setMessage("Don't select file from shortcut path")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, id) -> {
                                dialog.dismiss();
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
    );

    public  boolean isReadStoragePermissionGranted() {
        int result = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    openFileStorage();
                } else {
                    Toast.makeText(requireActivity(), "Storage Permission required to access External storage", Toast.LENGTH_SHORT).show();
                }
            }
    );


    private void updateFirmware(String Path) {
        Context context = requireContext().getApplicationContext();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                mReader.Config.updateFirmware(Path, ip);
            } catch (InvalidUsageException | OperationFailureException e) {
                if (e.getStackTrace().length > 0) {
                    Log.e(TAG, e.getStackTrace()[0].toString());
                }
            }
            handler.post(() -> {

            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}