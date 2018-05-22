package com.xyz.me_mg;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by timscott on 29/04/2018.
 *
 * Bluetooth Service Class
 *
 * This service is bound to both MainActivity & GraphActivty
 *
 * This code largely references this tutorial:
 * https://github.com/mitchtabian/Sending-and-Receiving-Data-with-Bluetooth/tree/master/Bluetooth-Communication
 *
 */

public class BT_Service extends Service {

    static final String TAG = "BT_Service";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    // Test Int
    int number;

    //String for BT Read
    String read;
    Boolean new_value = false;

    //On toggle
    public Boolean BTopen = false; ;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    String status;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    volatile boolean stopWorker;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BT_Service getService() {
            Log.d(TAG, "getService: ");
            // Return this instance of LocalService so clients can call public methods
            return BT_Service.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return mBinder;
    }

    // Test Functions
    public void setNumber(int num){
        number = num;
    }
    public int getNumber(){
        return number;
    }

    public void onDestroy(){
        Log.d(TAG, "onDestroy: ");

        if(BTopen) {
            try {
                closeBT();
            } catch (IOException e) {
            }
        }
    }

    Boolean findBT()
    {
        Log.d(TAG, "findBT: ");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            // BT not supported on device
            status = "Bluetooth is not Supported.";
            Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
            return false;
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            status = "Enable Bluetooth!";
            Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
            return false;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("Casa del Tim"))
                {
                    mmDevice = device;
                    return true;
                }
                else
                {
                    status = "Me-MG not found!";
                    Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
                }
            }
        }

        return false;
    }




    void sendData(String msg) throws IOException {
        Log.d(TAG, "sendData: ");

        msg += "\n";
        mmOutputStream.write(msg.getBytes());
    }

    void resetConnection() throws IOException {
        Log.d(TAG, "resetConnection: ");

        if (mmInputStream != null) {
            try {mmInputStream.close();} catch (Exception e) {}
            mmInputStream = null;
        }

        if (mmOutputStream != null) {
            try {mmOutputStream.close();} catch (Exception e) {}
            mmOutputStream = null;
        }

        if (mmSocket != null) {
            try {mmSocket.close();} catch (Exception e) {}
            mmSocket = null;
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

    }


    void openBT()
    {
        Log.d(TAG, "openBT: ");

        new Thread(new Runnable() {

                @Override
                public void run() {

                    try{
                        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                        mmSocket.connect();
                        mmOutputStream = mmSocket.getOutputStream();
                        mmInputStream = mmSocket.getInputStream();
                        stopWorker = false;
                        BTopen = true;
                    }
                    catch(IOException e){
                        Log.d(TAG, "OpenBT ERROR! ");
                    }

                }
            }).start();

    }

    void closeBT() throws IOException
    {
        Log.d(TAG, "closeBT: ");
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        status = "Me-MG Disconnected!";
        Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
        BTopen = false;
    }


    void beginListenForData()
    {
        Log.d(TAG, "beginListenForData: ");

        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();

                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            read = data;
                                            new_value = true;
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    String getRead(){
        new_value = false;
        return read;
    }


}
