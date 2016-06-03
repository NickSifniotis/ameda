package com.example.nsifniotis.testapplicationone;

import android.bluetooth.BluetoothSocket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by nsifniotis on 3/06/16.
 */
public class AMEDAOutputBuffer extends Thread
{
    private BufferedOutputStream ameda_output;
    private String pending_command;
    private Lock lock = new ReentrantLock();


    public AMEDAOutputBuffer () {}


    public AMEDAOutputBuffer (BluetoothSocket socket) throws IOException
    {
        ameda_output = new BufferedOutputStream(socket.getOutputStream());
        pending_command = null;
    }


    public void WriteToDevice (String command)
    {
        try {
            lock.lock();
            pending_command = command;
        }
        finally
        {
            lock.unlock();
        }
    }


    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            try {
                lock.lock();
                if (pending_command != null)
                {
                    ameda_output.write(string_to_bytes(pending_command), 0, 8);
                    ameda_output.flush();

                    pending_command = null;
                }
            }
            catch (Exception e)
            {
            }
            finally
            {
                lock.unlock();
            }
        }

        // clean up
        try {
            ameda_output.close();
        }
        catch (Exception e)
        {

        }
    }


    /*
        Convert the string command into a byte array that bos.write can play with.
     */
    private byte[] string_to_bytes (String s)
    {
        if (s == null)
            return null;

        byte [] res = new byte[s.length()];

        for (int i = 0; i < s.length(); i ++)
            res[i] = (byte)s.charAt(i);

        return res;
    }
}
