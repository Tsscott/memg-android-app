package com.xyz.me_mg;

/**
 * Created by timscott on 17/04/2018.
 *
 * Graph Activity Class
 *
 * Activity to display EMG real time plot vs Threshold
 *
 * The Graphing code extends from this example:
 * http://www.ssaurel.com/blog/create-a-real-time-line-graph-in-android-with-graphview/
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
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.GridLabelRenderer;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.xyz.me_mg.MainActivity.A2;
import static com.xyz.me_mg.MainActivity.A3;
import static com.xyz.me_mg.MainActivity.A4;
import static com.xyz.me_mg.MainActivity.A5;
import static com.xyz.me_mg.MainActivity.A6;
import static com.xyz.me_mg.MainActivity.DB_TABLE_2;
import static com.xyz.me_mg.MainActivity.db;


public class GraphActivity extends AppCompatActivity{

    static final String TAG = "GraphActivity";

    //BT Service
    BT_Service mService;
    boolean mBound = false;
    String data = "0";


    //GraphView variables
    private LineGraphSeries<DataPoint> EMG_series;
    private LineGraphSeries<DataPoint> Tho_series;
    private int lastX = 0;
    Viewport viewport;
    GraphView graph;


    //Intent Vars
    int reps;
    int tho;
    String routine;

    //DB Vars
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date start_time;
    int good = 0;
    int bad = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // log start time
        start_time = new Date();


        Intent intent = getIntent();
        if (intent.hasExtra("Reps")){
            reps = Integer.parseInt(intent.getStringExtra("Reps"));
        }
        if (intent.hasExtra("Tho")){
            tho = Integer.parseInt(intent.getStringExtra("Tho"));
        }
        if (intent.hasExtra("Routine")){
            routine = intent.getStringExtra("Routine");
        }

        // get graph view instance
        graph = (GraphView) findViewById(R.id.graph);

        // customize graphview
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);// It will remove the background grids
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);// remove horizontal x labels and line
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.setBackgroundColor(Color.TRANSPARENT);

        // data
        EMG_series = new LineGraphSeries<DataPoint>();
        Tho_series = new LineGraphSeries<DataPoint>();
        Tho_series.setColor(Color.GREEN);
        graph.addSeries(EMG_series);
        graph.addSeries(Tho_series);

        // customize viewport
        viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(1000);
        viewport.setScrollable(true);


        //Set title to routine from intent
        final TextView title = findViewById(R.id.titleTextView);
        title.setText(routine);

        final Button start = findViewById(R.id.buttonStartGraph);
        final Button stop = findViewById(R.id.buttonStopGraph);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: start");

                Toast.makeText(getApplicationContext(),"GO!",Toast.LENGTH_LONG).show();

                try{
                    mService.resetConnection();
                }
                catch(IOException e){
                    Log.d(TAG, "onCreate: reset IO exception");
                }

                mService.beginListenForData();

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // we add 100 new entries
                        while(!Thread.currentThread().isInterrupted()) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            // Check for received value
                                            if(mService.new_value) {
                                                data = mService.getRead();
                                            }
                                            else{data = "0";}

                                            // Check for good/bad flag
                                            if(Objects.equals(data, "x")){

                                                Toast.makeText(getApplicationContext(),"Good!",Toast.LENGTH_SHORT).show();
                                                good++;
                                            }
                                            else if(Objects.equals(data, "y")){
                                                bad++;
                                            }
                                            else{
                                                // update graph
                                                int x = lastX++;
                                                EMG_series.appendData(new DataPoint(x, Integer.parseInt(data)), false,1000);
                                                Tho_series.appendData(new DataPoint(x, tho), false, 1000);
                                            }

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
                Log.d(TAG, "onClick: stop");
                finish();
            }
        });
    };

    @Override
    protected void onPause() {
        super.onPause();
        // add the exercise to the database
        add_to_db();
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

    public void add_to_db(){
        String sql_tmp = "INSERT INTO " + DB_TABLE_2 + " ( " + A2 + ", " + A3 + ", " + A4 + ", " + A5 + ", " + A6 + " ) " + " VALUES ";

        String a = String.valueOf(good);
        String b = String.valueOf(bad);
        String c = dateFormat.format(start_time);

        Date end = new Date();

        String d = dateFormat.format(end);

        String e = routine;

        db.execSQL(sql_tmp + " ('" + e + "', '" + c + "', '" + d + "', '" + a + "', '" + b + "') ; ");
    }


}



