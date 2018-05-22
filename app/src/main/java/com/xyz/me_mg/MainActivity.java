package com.xyz.me_mg;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by timscott on 29/04/2018.
 *
 * MainActivity Class, started by SplashActivity
 *
 * MainActivity remains avaiable for duration of program,
 * Bluetooth Service is started and bound to this activity
 *
 * The three fragments (selected by navbar) sit on top of this class
 *
 * Database coding largelt references the sqlite_1 example by Dr. P.J Radcliffe
 *
 */


public class MainActivity extends AppCompatActivity{

    //logcat tag
    static final String TAG = "MainActivity";

    // Database definitions
    public static final String DB_NAME = "myDB.db";

    public static final String DB_TABLE = "Routines";
    public static final String C1 = "id";
    public static final String C2 = "Routine";
    public static final String C3 = "Tho";
    public static final String C4 = "Reps";

    public static final String DB_TABLE_2 = "Logs";
    public static final String A1 = "id";
    public static final String A2 = "Routine";
    public static final String A3 = "StartTime";
    public static final String A4 = "EndTime";
    public static final String A5 = "GoodReps";
    public static final String A6 = "BadReps";

    public static SQLiteDatabase db;

    //Cursor to hold query results
    Cursor result;

    // Binding Bluetooth variables
    BT_Service mService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        Log.d(TAG, "onCreate: Main started");


        //--- creates/opens database in private area for application.
        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        try // try to open the Routines Table, or create and prefill
        {
            result = db.query(DB_TABLE, null,
                    null, null, null, null, null);
        } catch (SQLiteException e) { // DB_TABLE does not exist so create it, and fill it.
            Log.d(TAG, "onCreate: Creating Routines Table");

            String CREATE = "";
            CREATE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE + " ( " + C1 + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + C2 + " TEXT," + C3 + " INTEGER," + C4 + " INTEGER ) ;";
            db.execSQL(CREATE);
            fillRoutines();
        }

        try // try to open LOGS TABLE, or create and prefill
        {
            result = db.query(DB_TABLE_2, null,
                    null, null, null, null, null);
        } catch (SQLiteException e) { // DB_TABLE does not exist so create it, and fill it.
            Log.d(TAG, "onCreate: Creating Logs Table");

            String CREATE = "";
            CREATE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_2 + " ( " + A1 + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + A2 + " TEXT," + A3 + " TEXT," + A4 + " TEXT," + A5 + " INTEGER," + A6 + " INTEGER ) ;";
            db.execSQL(CREATE);
            fillDummyLog();
        }





        // Set up bottom nav actions
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_exercise:
                                selectedFragment = ExerciseFragment.newInstance();
                                break;
                            case R.id.navigation_report:
                                selectedFragment = ReportFragment.newInstance();
                                break;
                            case R.id.navigation_extras:
                                selectedFragment = ExtrasFragment.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.main_container, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //Manually display the Exercise fragment first
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_container, ExerciseFragment.newInstance());
        transaction.commit();
    }



    @Override
    protected void onStart() {
        super.onStart();
        // Bind to Bluetooth Service
        Intent intent = new Intent(this, BT_Service.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind Bluetooth Service
        unbindService(mConnection);
        mBound = false;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BT_Service.LocalBinder binder = (BT_Service.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    private void fillRoutines() {
        //--- clear table and reset the autoinc counter.
        db.execSQL("DELETE FROM " + DB_TABLE + " ;");
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DB_TABLE + "'");

        //--- create SQL instruction
        String sql_tmp = "INSERT INTO " + DB_TABLE + " ( " + C2 + ", " + C3 + ", " + C4 + " ) " + " VALUES ";
        db.execSQL(sql_tmp + " ('Knee Routine', '400', '20') ; ");
        db.execSQL(sql_tmp + " ('Arm Routine', '500', '10') ; ");
    }

    private void fillDummyLog() {
        //--- clear table and reset the autoinc counter.
        db.execSQL("DELETE FROM " + DB_TABLE_2 + " ;");
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DB_TABLE_2 + "'");

        //--- create SQL instruction
        String sql_tmp = "INSERT INTO " + DB_TABLE_2 + " ( " + A2 + ", " + A3 + ", " + A4 + ", " + A5 + ", " + A6 + " ) " + " VALUES ";
        db.execSQL(sql_tmp + " ('Knee Routine', '2018/05/15 14:00:00', '2018/05/15 14:15:00', '12', '3') ; ");
        db.execSQL(sql_tmp + " ('Arm Routine', '2018/05/18 15:00:00', '2018/05/18 15:30:00', '5', '5') ; ");
    }

}
