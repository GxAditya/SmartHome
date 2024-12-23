package com.example.smarthome.database;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smarthome.model.SmartDevice;

import java.util.ArrayList;
import java.util.List;

public class SmartHomeDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SmartHomeDevices.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    private static final String TABLE_DEVICES = "devices";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_MAC_ADDRESS = "mac_address";
    private static final String COLUMN_TYPE = "device_type";
    private static final String COLUMN_STATUS = "status";

    // Create table SQL
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_DEVICES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_MAC_ADDRESS + " TEXT UNIQUE, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_STATUS + " INTEGER)";

    public SmartHomeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        onCreate(db);
    }

    // Add a new device
    public long addDevice(SmartDevice device) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, device.getName());
        values.put(COLUMN_MAC_ADDRESS, device.getMacAddress());
        values.put(COLUMN_TYPE, device.getType());
        values.put(COLUMN_STATUS, device.getStatus());

        return db.insert(TABLE_DEVICES, null, values);
    }

    // Get all devices
    @SuppressLint("Range")
    public List<SmartDevice> getAllDevices() {
        List<SmartDevice> deviceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_DEVICES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") SmartDevice device = new SmartDevice(
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_MAC_ADDRESS)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TYPE))
                );
                device.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)));
                deviceList.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return deviceList;
    }

    // Delete a device by MAC address
    public int deleteDevice(String macAddress) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_DEVICES,
                COLUMN_MAC_ADDRESS + " = ?",
                new String[]{macAddress});
    }

    // Update device status
    public int updateDeviceStatus(String macAddress, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);

        return db.update(TABLE_DEVICES,
                values,
                COLUMN_MAC_ADDRESS + " = ?",
                new String[]{macAddress});
    }
}