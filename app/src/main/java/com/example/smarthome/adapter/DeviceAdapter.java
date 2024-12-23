package com.example.smarthome.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.database.SmartHomeDbHelper;
import com.example.smarthome.model.SmartDevice;
import com.example.smarthome.ui.DeviceControlActivity;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private Context context;
    private List<SmartDevice> deviceList;
    private SmartHomeDbHelper dbHelper;

    public DeviceAdapter(Context context, List<SmartDevice> deviceList, SmartHomeDbHelper dbHelper) {
        this.context = context;
        this.deviceList = deviceList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        SmartDevice device = deviceList.get(position);

        // Set device name and type
        holder.deviceNameText.setText(device.getName());
        holder.deviceTypeText.setText(device.getType().toUpperCase());

        // Set device icon based on type
        if (device.getType().equalsIgnoreCase("light")) {
            holder.deviceIcon.setImageResource(R.drawable.ic_light);
        } else if (device.getType().equalsIgnoreCase("fan")) {
            holder.deviceIcon.setImageResource(R.drawable.ic_fan);
        }

        // Set device status icon
        holder.deviceStatusIcon.setImageResource(
                device.getStatus() == 1 ? R.drawable.ic_power_on : R.drawable.ic_power_off
        );

        // Open device control on card click
        holder.itemView.setOnClickListener(v -> {
            Intent controlIntent = new Intent(context, DeviceControlActivity.class);
            controlIntent.putExtra("DEVICE_MAC", device.getMacAddress());
            controlIntent.putExtra("DEVICE_TYPE", device.getType());
            context.startActivity(controlIntent);
        });

        // Delete device button
        holder.deleteButton.setOnClickListener(v -> {
            dbHelper.deleteDevice(device.getMacAddress());
            deviceList.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceNameText;
        TextView deviceTypeText;
        ImageView deviceIcon;
        ImageView deviceStatusIcon;
        ImageButton deleteButton;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameText = itemView.findViewById(R.id.text_device_name);
            deviceTypeText = itemView.findViewById(R.id.text_device_type);
            deviceIcon = itemView.findViewById(R.id.image_device_icon);
            deviceStatusIcon = itemView.findViewById(R.id.image_device_status);
            deleteButton = itemView.findViewById(R.id.btn_delete_device);
        }
    }
}
