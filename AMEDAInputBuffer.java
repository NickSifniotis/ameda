package com.example.nsifniotis.testapplicationone;

import android.bluetooth.BluetoothSocket;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Created by nsifniotis on 3/06/16.
 *
 * A wrapper buffer thread to bufferise inputs received from the AMEDA
 * @TODO right now it only remembers the latest input. Change this to properly buffer inputs,
 * maybe using a list or a queue
 */
public class AMEDAInputBuffer extends Thread
{
    private BufferedInputStream ameda_input;
    private String[] buffer;
    private int num_commands;
    private final int _BUFFER_SIZE = 10;


    /*
        Default constructor does nothing. We don't want to be calling this constructor.
     */
    public AMEDAInputBuffer() { }


    public AMEDAInputBuffer(BluetoothSocket socket) throws IOException
    {
        ameda_input = new BufferedInputStream(socket.getInputStream());

        num_commands = 0;
        buffer = new String [_BUFFER_SIZE];
    }

    @Override
    public void run()
    {
        byte [] holding_buffer;
        while (!Thread.currentThread().isInterrupted())
        {
            holding_buffer = Receive();

            if (holding_buffer != null)
            {
                num_commands++;
                buffer[0] = bytes_to_string(holding_buffer);
            }
        }

        // interrupted, so clean up nicely
        try {
            ameda_input.close();
        }
        catch (Exception e)
        {

        }
    }

    public String readCommand()
    {
        String res = null;
        if (num_commands != 0)
        {
            num_commands --;
            res = buffer[0];
        }

        return res;
    }

    public boolean CommandsAvailable ()
    {
        return (num_commands != 0);
    }

    private byte[] Receive()
    {
        // blocking call until 8 bytes are received from the AMEDA
        // get the bytes, and return them.

        byte[] res = null;

        try {
            if (ameda_input.available() >= 8) {
                res = new byte[8];
                ameda_input.read(res, 0, 8);
            }
        }
        catch (Exception e)
        {

        }

        return res;
    }

    private String bytes_to_string(byte [] buffer)
    {
        String res = "";

        for (byte b: buffer)
            res += (char)b;

        return res;
    }
}
