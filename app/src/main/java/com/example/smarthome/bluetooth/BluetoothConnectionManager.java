package com.example.smarthome.bluetooth;


import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.example.smarthome.utils.ErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionManager {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Context context;
    private ErrorHandler errorHandler;

    public BluetoothConnectionManager(Context context) {
        this.context = context;
        this.errorHandler = new ErrorHandler(context);
    }

    public boolean connectToDevice(BluetoothDevice device) {
        try {
            // Check Bluetooth permissions
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                errorHandler.showPermissionError("Bluetooth Connection Permission Required");
                return false;
            }

            // Create a connection socket
            mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            mmSocket.connect();

            // Get input and output streams
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();

            return true;
        } catch (IOException connectException) {
            errorHandler.showConnectionError("Failed to connect to device: " + connectException.getMessage());
            return false;
        }
    }

    public boolean sendCommand(String command) {
        try {
            if (mmOutStream == null) {
                errorHandler.showError("No active connection");
                return false;
            }

            mmOutStream.write(command.getBytes());
            return true;
        } catch (IOException e) {
            errorHandler.showSendError("Failed to send command: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (mmSocket != null) {
                mmSocket.close();
            }
            if (mmInStream != null) {
                mmInStream.close();
            }
            if (mmOutStream != null) {
                mmOutStream.close();
            }
        } catch (IOException closeException) {
            errorHandler.showDisconnectionError("Error closing Bluetooth connection");
        }
    }
}