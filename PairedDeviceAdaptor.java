package com.example.nsifniotis.testapplicationone;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nsifniotis on 24/05/16.
 */
public class PairedDeviceAdaptor extends ArrayAdapter<BluetoothDevice>
{
    private ArrayList<BluetoothDevice> objects;

    public PairedDeviceAdaptor(Context context, int textViewResourceId, ArrayList<BluetoothDevice> objects)
    {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.paired_device, null);
        }

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 *
		 * Comments written by other coders are so sad :( :'( don't get me started on those variable names
		 */
        BluetoothDevice i = objects.get(position);

        if (i != null)
        {
            TextView txtName = (TextView)v.findViewById(R.id.paired_device_name);
            TextView txtMAC = (TextView)v.findViewById(R.id.paired_device_mac);

            txtName.setText(i.getName());
            txtMAC.setText(i.getAddress());
        }

        return v;
    }
}
