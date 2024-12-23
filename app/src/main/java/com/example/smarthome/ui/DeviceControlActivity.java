package com.example.smarthome.ui;


import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthome.R;
import com.example.smarthome.bluetooth.BluetoothConnectionManager;
import com.example.smarthome.database.SmartHomeDbHelper;
import com.example.smarthome.model.SmartDevice;
import com.example.smarthome.utils.ErrorHandler;

public class DeviceControlActivity extends AppCompatActivity {
    private SmartDevice currentDevice;
    private SmartHomeDbHelper dbHelper;
    private BluetoothConnectionManager bluetoothManager;
    private ErrorHandler errorHandler;

    private ImageView deviceIcon;
    private TextView deviceNameText;
    private Switch powerSwitch;
    private SeekBar intensitySeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determine device type and set appropriate layout
        String deviceType = getIntent().getStringExtra("DEVICE_TYPE");
        if (deviceType.equalsIgnoreCase("light")) {
            setContentView(R.layout.activity_light_control);
        } else if (deviceType.equalsIgnoreCase("fan")) {
            setContentView(R.layout.activity_fan_control);
        }

        // Initialize components
        initializeComponents();

        // Setup device control
        setupDeviceControl();
    }

    private void initializeComponents() {
        // Initialize database and error handling
        dbHelper = new SmartHomeDbHelper(this);
        errorHandler = new ErrorHandler(this);

        // Find views
        deviceIcon = findViewById(R.id.image_device_icon);
        deviceNameText = findViewById(R.id.text_device_name);
        powerSwitch = findViewById(R.id.switch_power);
        intensitySeekBar = findViewById(R.id.seekbar_intensity);

        // Get device from database
        String macAddress = getIntent().getStringExtra("DEVICE_MAC");
        currentDevice = findDeviceByMacAddress(macAddress);

        // Initialize Bluetooth connection
        bluetoothManager = new BluetoothConnectionManager(this);
    }

    private SmartDevice findDeviceByMacAddress(String macAddress) {
        for (SmartDevice device : dbHelper.getAllDevices()) {
            if (device.getMacAddress().equals(macAddress)) {
                return device;
            }
        }
        errorHandler.showError("Device not found");
        finish();
        return null;
    }

    private void setupDeviceControl() {
        // Set device name and icon
        deviceNameText.setText(currentDevice.getName());

        if (currentDevice.getType().equalsIgnoreCase("light")) {
            deviceIcon.setImageResource(R.drawable.ic_light);
        } else if (currentDevice.getType().equalsIgnoreCase("fan")) {
            deviceIcon.setImageResource(R.drawable.ic_fan);
        }

        // Power switch setup
        powerSwitch.setChecked(currentDevice.getStatus() == 1);
        powerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Send Bluetooth command
                String command = isChecked ? "POWER_ON" : "POWER_OFF";
                if (bluetoothManager.sendCommand(command)) {
                    // Update device status in database
                    currentDevice.setStatus(isChecked ? 1 : 0);
                    dbHelper.updateDeviceStatus(currentDevice.getMacAddress(), currentDevice.getStatus());
                }
            }
        });

        // Intensity/Speed control for light/fan
        intensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Send Bluetooth command based on device type
                    String command = currentDevice.getType().equalsIgnoreCase("light")
                            ? "LIGHT_INTENSITY:" + progress
                            : "FAN_SPEED:" + progress;

                    bluetoothManager.sendCommand(command);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect Bluetooth when activity is destroyed
        bluetoothManager.disconnect();
    }
}
