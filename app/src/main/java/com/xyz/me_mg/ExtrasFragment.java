package com.xyz.me_mg;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by timscott on 29/04/2018.
 *
 * Extras Fragment Class
 *
 * This screen simply demonstrates ability to use HW sensors
 *
 * References:
 * https://developer.android.com/guide/topics/sensors/sensors_environment
 *
 *
 */

public class ExtrasFragment extends Fragment implements SensorEventListener {
    public static ExtrasFragment newInstance() {
        ExtrasFragment fragment = new ExtrasFragment();
        return fragment;
    }

    static final String TAG = "ExtrasFragment";


    private SensorManager mSensorManager;
    private Sensor mPressure;

    EditText pressure;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_extras, container, false);

        pressure = view.findViewById(R.id.pressureEditText);


        mSensorManager = (SensorManager)getContext().getSystemService(SENSOR_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            mPressure= mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float pressure_read = event.values[0];
        pressure.setText(String.valueOf(pressure_read));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
