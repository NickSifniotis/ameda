package com.example.nsifniotis.testapplicationone;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;


public class TestActivity extends AppCompatActivity {
    private static int REQUEST_ENABLE_BT = 1;
    private static String state;
    private static TextView status_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        status_bar = (TextView)findViewById(R.id.txtPairs);
        device_list(false);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        state = savedInstanceState.getString("state");

        if (state == "conecting")
            bluetooth(this.findViewById(R.id.txtPairs));
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
        device_list(true);
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
            //final ArrayAdapter<String> arrayAdaptor = new ArrayAdapter<String>(this, R.layout.paired_device);

            ArrayList<BluetoothDevice> pairedDevices = new ArrayList(adaptor.getBondedDevices());

            // if we have paired devices, list them
            if (pairedDevices.size() == 0)
            {
                status_bar.setText("Zero paired devices found. Try again once you've paired something.");
                return;
            }

            PairedDeviceAdaptor pd_adaptor = new PairedDeviceAdaptor(this, R.layout.paired_device, pairedDevices);
            final ListView listView = (ListView)findViewById(R.id.dataList);
            listView.setAdapter(pd_adaptor);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                    AMEDA.SetDevice((BluetoothDevice)(listView.getItemAtPosition(myItemInt)));

                    status_bar.setText("Selected device " + AMEDA.device.getName());
                }
            });
        }
    }


    /**
     * Toggle the list of paired bluetooth devices on or off.
     *
     * @param show
     */
    private void device_list(boolean show)
    {
        ListView viewer = (ListView) findViewById(R.id.dataList);
        if (show)
            viewer.setVisibility(View.VISIBLE);
        else
            viewer.setVisibility(View.GONE);
    }


    /**
     * Ping the AMEDA with the HELLO command. Expect to receive a reply READY.
     *
     * @param view
     */
    public void ping(View view)
    {
        device_list(false);

        try
        {
            TransmitString("HELLO");
            byte [] response = Receive();
        }
        catch (Exception e)
        {
            // there's no need to do anything here since the command is correct.
        }
    }


    /**
     * Event handler for the 'go home' button click.
     *
     * @param view
     */
    public void go_home_handler (View view)
    {
        device_list(false);
        GoHome();
    }


    /**
     * Handler for the AMEDA 'move to position' command button.
     *
     * @param view
     */
    public void move_to_pos_handler (View view)
    {
        device_list(false);
        EditText textor = ((EditText)findViewById(R.id.move_to_number_txt));
        String pos_val = "0";
        if (textor != null && textor.getText().length() > 0)
            pos_val = textor.getText().toString();

        MoveToPosition(Integer.parseInt(pos_val));
    }


    /**
     * Handler for the 'calibrate' command button.
     *
     * @param view
     */
    public void calibrate_btn_handler(View view)
    {
        device_list(false);
        Calibrate();
    }

    /**
     * Perform a horizontal calibration on the AMEDA
     */
    private void Calibrate()
    {
        try
        {
            TransmitString ("CALHZ");

            byte [] response = Receive();
        }
        catch (Exception e)
        {
            //
        }
    }


    /**
     * Resets the AMEDA back to the home position.
     */
    private void GoHome()
    {
        try
        {
            TransmitString("GOHME");
            byte [] response = Receive();
        }
        catch (Exception e)
        {
            // my code is not broken
        }
    }


    /**
     * Instructs the AMEDA to move to a new position / angle.
     *
     * @param pos
     */
    private void MoveToPosition (int pos)
    {
        if (pos >= 0 && pos <= 9)
        {
            try
            {
                TransmitString("GOTO" + pos);
            }
            catch (Exception e)
            {
                // my commands are always correct.
            }
        }
        else
            status_bar.setText("Invalid command, cannot move device to position " + pos);
    }


    /**
     * Transmits the given instruction string to the AMEDA device.
     *
     * Converts the string to an 8-byte char message.
     *
     * @param str
     */
    private void TransmitString (String str) throws Exception
    {
        if (str.length() != 5)
            throw new Exception ("Invalid command sent to AMEDA.");

        byte [] transmission = new byte[8];
        transmission[0] = (byte)'[';

        int checksum = 0;
        for (int i = 0; i < 5; i ++)
        {
            checksum += (int) str.charAt(i);
            transmission[i + 1] = (byte) str.charAt(i);
        }

        checksum %= 256;
        transmission[6] = (byte)checksum;
        transmission[7] = (byte)']';

        Transmit (transmission);
    }


    /**
     * Sends the message in the char array to the AMEDA device, if a connection exists.
     *
     * @param message
     */
    private void Transmit (byte [] message)
    {
        if (!AMEDA.IsDeviced())
        {
            status_bar.setText("Unable to transmit - no AMEDA device selected");
            return;
        }

        if (!AMEDA.IsConnected())
        {
            AMEDA.Connect(this);
        }

        if (AMEDA.IsConnected())
        {
            try
            {
                AMEDA.ostream.write(message);
            }
            catch (Exception e)
            {
                Toast toast = Toast.makeText(this, "Unable to complete transmission: " + e.getMessage(), Toast.LENGTH_LONG);
                toast.show();
            }
        }

        DisplayMessage(message);
    }


    private byte[] Receive()
    {
        // blocking call until 8 bytes are received from the AMEDA
        // get the bytes, and return them.

        if (!AMEDA.IsDeviced())
        {
            status_bar.setText("Unable to transmit - no AMEDA device selected");
            return null;
        }

        if (!AMEDA.IsConnected())
        {
            AMEDA.Connect(this);
        }

        byte[] res = new byte [8];

        boolean finished = false;
        int counter = 0;

        try
        {
            while (counter < 8)
            {
                int byteCount = AMEDA.istream.available();
                if (byteCount > 0)
                {
                    int rawByte = AMEDA.istream.read();
                    res[counter] = (byte) rawByte;
                    counter ++;
                }
            }
        }
        catch (Exception e)
        {

        }

        DisplayMessage(res);
        return res;
    }

    private void DisplayMessage(byte [] message)
    {
        // make sure that we are receiving the correct commands o transmit
        // purely for testing purposes.

        String output = "";
        for (int i = 0; i < 6; i ++)
            output += (char)message[i];

        output += ":" + Integer.toString(message[6]) + ":";
        output += (char)message[7];

        status_bar.setText(output);
    }
}
