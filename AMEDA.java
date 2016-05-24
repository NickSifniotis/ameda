package com.example.nsifniotis.testapplicationone;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by nsifniotis on 24/05/16.
 */
public class AMEDA
{
    public static BluetoothDevice device = null;
    public static InputStream istream = null;
    public static OutputStream ostream = null;
    public static BluetoothSocket socket = null;
    public static boolean connected = false;

    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Serial Port Service ID


    public static void SetDevice(BluetoothDevice d)
    {
        device = d;
    }

    public static boolean IsDeviced ()
    {
        return (!(device == null));
    }

    public static boolean Connect(Context context)
    {
        boolean res = false;

        try
        {
            socket = device.createInsecureRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();

            istream = socket.getInputStream();
            ostream = socket.getOutputStream();

            res = true;
        }
        catch (Exception e)
        {
            Toast toast = Toast.makeText(context, "Connection error: " + e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }

        return res;
    }

    public static boolean IsConnected()
    {
        return connected;
    }
}
