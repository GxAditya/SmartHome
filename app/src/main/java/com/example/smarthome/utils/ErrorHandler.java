package com.example.smarthome.utils;


import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

public class ErrorHandler {
    private Context context;

    public ErrorHandler(Context context) {
        this.context = context;
    }

    public void showError(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public void showConnectionError(String message) {
        Toast.makeText(context, "Connection Error: " + message, Toast.LENGTH_LONG).show();
    }

    public void showSendError(String message) {
        Toast.makeText(context, "Send Error: " + message, Toast.LENGTH_LONG).show();
    }

    public void showDisconnectionError(String message) {
        Toast.makeText(context, "Disconnection Error: " + message, Toast.LENGTH_SHORT).show();
    }

    public void showPermissionError(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}