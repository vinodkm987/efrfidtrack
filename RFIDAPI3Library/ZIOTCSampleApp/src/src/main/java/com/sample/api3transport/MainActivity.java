package com.sample.api3transport;

import static com.sample.api3transport.RFIDHandler.menuViewModel;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.sample.api3transport.databinding.ActivityMainBinding;
import com.sample.api3transport.ui.Firmware.FWUpdateDataViewModel;
import com.sample.api3transport.ui.Inventory.TagDataViewModel;
import com.sample.api3transport.ui.Connect.ReaderConnection;
import com.zebra.rfid.api3.TagData;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ReaderConnection {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    RFIDHandler rfidHandler;
   // NGEHandler ngeHandler;
    public TextView textrfid = null;
    final static String TAG = "MAIN_ACTIVITY";

    TextView navIpAddress;
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE };
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 1;


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int permission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN);
            return permission == PackageManager.PERMISSION_GRANTED;
        }else{
            return true;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE_BLUETOOTH_CONNECT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_BLUETOOTH_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                rfidHandler = new RFIDHandler();
                rfidHandler.onCreate(this);


            } else {
                // The user has denied the permission.
                // Display an error message.
                //rfidHandler = new RFIDHandler();
                //rfidHandler.onCreate(this);
            }
        }
    }
    public void getPermissionFromUser()
    {
        // if(BA == null){
        //     BA = BluetoothAdapter.getDefaultAdapter();
        // }


        int i=0;
        for(String permission : PERMISSIONS){
            String title = "bluetooth permission needed";
            String msg="";
            if(i==0){
                msg = "Give the app permission to use Bluetooth";
            }else if(i==1) {
                msg = "Give permission to search for the current device on other Bluetooth devices";
            }

            if (ActivityCompat.checkSelfPermission(MainActivity.this, PERMISSIONS[i]) == PackageManager.PERMISSION_DENIED) {
                int finalI = i;
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(title);
                builder.setMessage(msg);
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{PERMISSIONS[finalI]}, 3);
                    }
                });
                builder.show();
            }
            i++;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        // RFID Handler
//        requestPermission();

        rfidHandler = new RFIDHandler();
        //ngeHandler = new NGEHandler();
        rfidHandler.onCreate(this);
        //ngeHandler.onCreate(this);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_connect, R.id.nav_inventory, R.id.nav_tag_settings, R.id.nav_singulation,
                R.id.nav_prefilter, R.id.nav_access_operations, R.id.nav_trigger_settings, R.id.nav_json,
                R.id.nav_regulatory, R.id.nav_firmware, R.id.nav_deviceStatus, R.id.nav_readerCapabilies,
                R.id.nav_antennaRFConfig)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        navIpAddress = headerView.findViewById(R.id.header_textView);


        RFIDHandler.tagDataViewModel = new ViewModelProvider(this).get(TagDataViewModel.class);
        RFIDHandler.fwUpdateDataViewModel = new ViewModelProvider(this).get(FWUpdateDataViewModel.class);
        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        menuViewModel.getSelectedItem().observe(this,selectedItem->{ Nav_menuvisiblity(selectedItem);
        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                rfidHandler.getAvailableReader();
//            }
//        }).start();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //rfidHandler = new RFIDHandler();
            //rfidHandler.onCreate(this);
            rfidHandler.dosequence();
            return true;
        }

        switch(id){
            case R.id.get_caps:
                rfidHandler.getcapablities();
                break;
            case R.id.action_start:
                rfidHandler.performInventory();
                break;
            case R.id.action_stop:
                rfidHandler.stopInventory();
                //textrfid.setText("");
                break;
            case R.id.add_filter:
                rfidHandler.addPrefilter();
                break;
            case R.id.do_read:
                rfidHandler.doRead();
                break;
            case R.id.do_write:
                rfidHandler.doWrite();
                break;
            case R.id.add_singulation:
                rfidHandler.setSingulationControl();
                break;
            case R.id.add_trigger:
                rfidHandler.triggerSettings();
                break;
            case R.id.del_selects:
                rfidHandler.deleteSelects();
                break;
            case R.id.do_lock:
                rfidHandler.doLock();
                break;
            case R.id.set_start_trigger:
                rfidHandler.setstarttrigger();
                break;
            case R.id.set_stop_trigger:
                rfidHandler.setstoptrigger();
                break;
            case R.id.do_kill:
                rfidHandler.doKill();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void handleTagdata(TagData[] tagData) {
        final StringBuilder sb = new StringBuilder();
        if(tagData == null)
            return;
        for (int index = 0; index < tagData.length; index++) {

            if (tagData[index].isContainsLocationInfo()) {
                short dist = tagData[index].LocationInfo.getRelativeDistance();
                short num = tagData[index].LocationInfo.getTagNumber();
                sb.append("# " + num + " "+ dist+"%\n");
            }
            else
                sb.append(tagData[index].getTagID() + "\n");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrfid.append(sb.toString());
            }
        });
    }


    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textrfid.setText("");
                }
            });
            rfidHandler.performInventory();
        } else
            rfidHandler.stopInventory();
    }


    @Override
    public void onConnected() {
        navIpAddress.setText(RFIDHandler.mReader.getHostName());
        rfidHandler.ConfigureReader();
    }

    public void Nav_menuvisiblity(Boolean isEditing){
        NavigationView  navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu myMenu = navigationView.getMenu();
        //Log.d(TAG,"Menus "+myMenu.getItem(1));
        //  nav_Menu.findItem(R.id.nav_settings).setVisible(false);
        Log.d(TAG,"Updating menu");
        MenuItem menuState;
        menuState = myMenu.findItem(R.id.nav_deviceStatus);
        menuState.setVisible(isEditing);

        myMenu.findItem(R.id.nav_firmware).setVisible(isEditing);
        myMenu.findItem(R.id.nav_antennaRFConfig).setVisible(isEditing);
        myMenu.findItem(R.id.nav_readerCapabilies).setVisible(isEditing);
        myMenu.findItem(R.id.nav_regulatory).setVisible(isEditing);
        myMenu.findItem(R.id.nav_json).setVisible(isEditing);
    }

}