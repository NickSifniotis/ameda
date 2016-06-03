package com.example.nsifniotis.testapplicationone;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;
import java.util.UUID;

/**
 * Created by nsifniotis on 24/05/16.
 *
 * AMEDA connection controller class.
 */
public class AMEDA
{
    public static AMEDAInputBuffer input_stream;
    public static AMEDAOutputBuffer output_stream;

    public static BluetoothDevice device = null;
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

            input_stream = new AMEDAInputBuffer(socket);
            output_stream = new AMEDAOutputBuffer(socket);

            input_stream.run();
            output_stream.run();

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

    public static String ReadCommand ()
    {
        return input_stream.readCommand();
    }


    public static void WriteCommand (String cmd)
    {
        output_stream.WriteToDevice(cmd);
    }


    public static void Close()
    {
        try {
            input_stream.interrupt();
            input_stream.join();
        }
        catch (Exception e) {}

        try {
            output_stream.interrupt();
            output_stream.join();
        }
        catch (Exception e) {}

        try {
            socket.close();
        }
        catch (Exception e) {}

        connected = false;
    }
}
