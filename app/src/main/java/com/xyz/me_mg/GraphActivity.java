package com.xyz.me_mg;

/**
 * Created by timscott on 17/04/2018.
 *
 * Graph Activity Class
 *
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.GridLabelRenderer;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


public class GraphActivity extends AppCompatActivity{

    BT_Service mService;
    boolean mBound = false;

    //GraphView variables
    private LineGraphSeries<DataPoint> EMG_series;
    private LineGraphSeries<DataPoint> Tho_series;

    private int lastX = 0;
    Viewport viewport;
    GraphView graph;
    GridLabelRenderer GLR;

    //Intent Vars
    int reps;
    int tho;


    //Bluetooth listen variables
    String data = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Intent intent = getIntent();
        if (intent.hasExtra("Reps")){
            reps = Integer.parseInt(intent.getStringExtra("Reps"));
        }
        if (intent.hasExtra("Tho")){
            tho = Integer.parseInt(intent.getStringExtra("Tho"));
        }

        // we get graph view instance
        graph = (GraphView) findViewById(R.id.graph);
        // data
        EMG_series = new LineGraphSeries<DataPoint>();
        Tho_series = new LineGraphSeries<DataPoint>();
        Tho_series.setColor(Color.GREEN);


        graph.addSeries(EMG_series);
        graph.addSeries(Tho_series);

        // customize a little bit viewport
        viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(1000);
        viewport.setScrollable(true);



        final Button start = findViewById(R.id.buttonStartGraph);
        final Button stop = findViewById(R.id.buttonStopGraph);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(),"GO!",Toast.LENGTH_LONG).show();

                mService.beginListenForData();

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // we add 100 new entries
                        while(!Thread.currentThread().isInterrupted()) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if(mService.new_value) {
                                                data = mService.getRead();
                                            }
                                            else{data = "0";}

                                            //Toast.makeText(getApplicationContext(),"Graph",Toast.LENGTH_SHORT).show();
                                            int x = lastX++;
                                            EMG_series.appendData(new DataPoint(x, Integer.parseInt(data)), false,1000);
                                            Tho_series.appendData(new DataPoint(x, tho), false, 1000);
                                        }
                                    });

                                    try{
                                        Thread.sleep(1);
                                    }
                                    catch(Exception e){}
                        }
                    }
                }).start();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    };

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BT_Service.LocalBinder binder = (BT_Service.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, BT_Service.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }



}



