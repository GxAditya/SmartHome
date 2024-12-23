package com.example.smarthome;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.adapter.DeviceAdapter;
import com.example.smarthome.database.SmartHomeDbHelper;
import com.example.smarthome.model.SmartDevice;
import com.example.smarthome.ui.DevicePairingActivity;
import com.example.smarthome.utils.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_BLUETOOTH = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_PAIR_DEVICE = 3;

    private RecyclerView deviceRecyclerView;
    private DeviceAdapter deviceAdapter;
    private List<SmartDevice> deviceList;
    private SmartHomeDbHelper dbHelper;
    private ErrorHandler errorHandler;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize error handler
        errorHandler = new ErrorHandler(this);

        // Check and request Bluetooth permissions
        checkBluetoothPermissions();

        // Initialize database helper
        dbHelper = new SmartHomeDbHelper(this);

        // Setup RecyclerView for devices
        setupDeviceRecyclerView();

        // Setup Add Device Button
        Button addDeviceButton = findViewById(R.id.btn_add_device);
        addDeviceButton.setOnClickListener(v -> openDevicePairingActivity());
    }

    @SuppressLint("MissingPermission")
    private void checkBluetoothPermissions() {
        // Check multiple Bluetooth-related permissions
        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_BLUETOOTH
            );
        }

        // Enable Bluetooth if not already enabled
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void setupDeviceRecyclerView() {
        deviceRecyclerView = findViewById(R.id.recycler_view_devices);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch devices from database
        deviceList = dbHelper.getAllDevices();
        deviceAdapter = new DeviceAdapter(this, deviceList, dbHelper);
        deviceRecyclerView.setAdapter(deviceAdapter);
    }

    private void openDevicePairingActivity() {
        Intent pairingIntent = new Intent(this, DevicePairingActivity.class);
        startActivityForResult(pairingIntent, REQUEST_PAIR_DEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PAIR_DEVICE && resultCode == RESULT_OK) {
            // Refresh device list after pairing
            deviceList.clear();
            deviceList.addAll(dbHelper.getAllDevices());
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    errorHandler.showPermissionError(
                            "Bluetooth permissions are required to use this app"
                    );
                    finish();
                    return;
                }
            }
        }
    }
}
