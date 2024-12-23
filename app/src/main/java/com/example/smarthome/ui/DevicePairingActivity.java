package com.example.smarthome.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smarthome.R;
import com.example.smarthome.database.SmartHomeDbHelper;
import com.example.smarthome.model.SmartDevice;
import com.example.smarthome.utils.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

public class DevicePairingActivity extends AppCompatActivity {
    private static final String TAG = "DevicePairingActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_BLUETOOTH = 2;

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> discoveredDevices;
    private ArrayAdapter<String> deviceListAdapter;
    private ErrorHandler errorHandler;
    private SmartHomeDbHelper dbHelper;
    private Spinner deviceTypeSpinner;
    private String selectedDeviceType;
    private Button discoveryButton;

    // Broadcast Receiver for device discovery
    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null) {
                    String deviceName = device.getName() != null ? device.getName() : "Unknown Device";
                    Log.d(TAG, "Device found: " + deviceName + " - " + device.getAddress());

                    // Prevent duplicate devices
                    if (!discoveredDevices.contains(device)) {
                        discoveredDevices.add(device);
                        deviceListAdapter.add(deviceName + "\n" + device.getAddress());
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                discoveryButton.setEnabled(true);
                Toast.makeText(DevicePairingActivity.this,
                        "Device discovery completed",
                        Toast.LENGTH_SHORT).show();

                if (discoveredDevices.isEmpty()) {
                    Toast.makeText(DevicePairingActivity.this,
                            "No devices found. Ensure devices are in pairing mode.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_pairing);

        // Initialize components
        errorHandler = new ErrorHandler(this);
        dbHelper = new SmartHomeDbHelper(this);
        discoveredDevices = new ArrayList<>();

        // Check Bluetooth availability
        checkBluetoothAvailability();

        // Setup device type spinner
        setupDeviceTypeSpinner();

        // Setup device list
        setupDeviceList();

        // Setup discovery button
        discoveryButton = findViewById(R.id.btn_start_discovery);
        discoveryButton.setOnClickListener(v -> startDeviceDiscovery());
    }

    @SuppressLint("MissingPermission")
    private void checkBluetoothAvailability() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            errorHandler.showError("Bluetooth is not supported on this device");
            finish();
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void setupDeviceTypeSpinner() {
        deviceTypeSpinner = findViewById(R.id.spinner_device_type);
        String[] deviceTypes = {"Light", "Fan"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                deviceTypes
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceTypeSpinner.setAdapter(spinnerAdapter);

        deviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDeviceType = deviceTypes[position].toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDeviceType = "light"; // Default
            }
        });
    }

    private void setupDeviceList() {
        ListView deviceListView = findViewById(R.id.list_discovered_devices);
        deviceListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );
        deviceListView.setAdapter(deviceListAdapter);

        // Device selection listener
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < discoveredDevices.size()) {
                BluetoothDevice selectedDevice = discoveredDevices.get(position);

                @SuppressLint("MissingPermission")
                SmartDevice newDevice = new SmartDevice(
                        selectedDevice.getName() != null ? selectedDevice.getName() : "Unknown Device",
                        selectedDevice.getAddress(),
                        selectedDeviceType
                );
                dbHelper.addDevice(newDevice);

                // Return to main activity
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startDeviceDiscovery() {
        // Check Bluetooth permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            // Request Bluetooth scan permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    PERMISSION_REQUEST_BLUETOOTH);
            return;
        }

        // Clear previous discoveries
        discoveredDevices.clear();
        deviceListAdapter.clear();

        // Prepare intent filters
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryReceiver, filter);

        // Start discovery
        try {
            bluetoothAdapter.startDiscovery();
            discoveryButton.setEnabled(false);
            Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Started Bluetooth device discovery");
        } catch (Exception e) {
            Log.e(TAG, "Error starting device discovery", e);
            errorHandler.showError("Failed to start device discovery: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retry discovery
                startDeviceDiscovery();
            } else {
                errorHandler.showError("Bluetooth scan permission is required to discover devices");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth is now enabled", Toast.LENGTH_SHORT).show();
            } else {
                errorHandler.showError("Bluetooth must be enabled to discover devices");
                finish();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister receiver
        try {
            unregisterReceiver(discoveryReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver not registered", e);
        }

        // Cancel discovery if still running
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}