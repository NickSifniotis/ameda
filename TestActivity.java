package com.example.nsifniotis.testapplicationone;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class TestActivity extends AppCompatActivity {

    private static int REQUEST_ENABLE_BT = 1;
    private static String state;
    private static TextView status_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        status_bar = (TextView)findViewById(R.id.txtPairs);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        state = savedInstanceState.getString("state");

        if (state == "conecting")
            bluetooth(this.findViewById(R.id.bluetooth_search));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString ("state", state);
    }

    /**
     * Try to find, and connect to, the AMEDA device.
     *
     * @param view
     */
    public void bluetooth(View view)
    {
        state = "connecting";

        BluetoothAdapter adaptor = BluetoothAdapter.getDefaultAdapter();

        if (adaptor == null)
        {
            status_bar.setText ("Status: No bluetooth adaptor found. Aborting.");
            return;
        }

        if (!adaptor.isEnabled())
        {
            status_bar.setText("Status: Attempting to enable bluetooth adaptor.");

            Intent enable_it = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult (enable_it, REQUEST_ENABLE_BT);
        }
        else
        {
            final ArrayAdapter<String> arrayAdaptor = new ArrayAdapter<String>(this, R.layout.activity_test);
            ListView listView = (ListView)findViewById(R.id.dataList);
            listView.setAdapter(arrayAdaptor);

            Set<BluetoothDevice> pairedDevices = adaptor.getBondedDevices();

            // if we have paired devices, list them
            if (pairedDevices.size() > 0)
                for (BluetoothDevice device : pairedDevices)
                    arrayAdaptor.add (device.getName() + "\n" + device.getAddress());
            else
                status_bar.setText("Status: Zero paired devices found.");

            // begin discovery process
            final BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (BluetoothDevice.ACTION_FOUND.equals (action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        arrayAdaptor.add (device.getName() + "\n" + device.getAddress());
                    }
                }
            };

            IntentFilter filter = new IntentFilter (BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
        }
    }


    /**
     * Ping the AMEDA with the HELLO command. Expect to receive a reply READY.
     *
     * @param view
     */
    public void ping(View view)
    {

    }
}
