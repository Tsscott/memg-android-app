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
 */

public class BT_Service extends Service {
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
            // Return this instance of LocalService so clients can call public methods
            return BT_Service.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
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

        if(BTopen) {
            try {
                closeBT();
            } catch (IOException e) {
            }
        }
    }

    void findBT()
    {
        Log.d("Debug","FindBT");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            status = "Bluetooth is not Enabled.";
            Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if(getApplicationContext() instanceof Activity){
                ((Activity)getApplicationContext()).startActivityForResult(enableBluetooth, 0);
            }
            else{
                Log.d("BluetoothService", "hmmm context is not an activity.... trouble");
            }
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("Casa del Tim"))
                {
                    mmDevice = device;
                    break;
                }
                else
                {
                    status = "Me-MG not found!.";
                    Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
                }
            }
        }
    }




    void sendData() throws IOException
    {
//        String msg = myTextbox.getText().toString();
//        msg += "\n";
//        mmOutputStream.write(msg.getBytes());
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        stopWorker = false;
        status = "Me-MG Connected!";
        Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
        BTopen = true;
    }

    void closeBT() throws IOException
    {
        Log.d("Debug","CloseBT");
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        status = "Me-MG Disconnected!";
        Toast.makeText(getApplicationContext(),status,Toast.LENGTH_LONG).show();
        BTopen = false;
    }

    InputStream getMmInputStream() throws Exception{
        return mmSocket.getInputStream() ;
    }

    Boolean getStopWorker(){
        return stopWorker;
    }

    void setStopWorker(boolean set){
        stopWorker = set;
    }

    void beginListenForData()
    {
        Log.d("Graph","begin listen");

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
