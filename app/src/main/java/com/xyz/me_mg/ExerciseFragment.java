package com.xyz.me_mg;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;

import static com.xyz.me_mg.MainActivity.C2;
import static com.xyz.me_mg.MainActivity.C3;
import static com.xyz.me_mg.MainActivity.C4;
import static com.xyz.me_mg.MainActivity.DB_TABLE;
import static com.xyz.me_mg.MainActivity.db;

/**
 * Created by timscott on 29/04/2018.
 *
 * Exercise Fragment Class
 *
 * The three fragments (selected by navbar) sit on top of MainActivity
 *
 */

public class ExerciseFragment extends Fragment {
    public static ExerciseFragment newInstance() {
        ExerciseFragment fragment = new ExerciseFragment();
        return fragment;
    }

    static final String TAG = "ExerciseFragment";

    // Cursor to hold DB query result
    Cursor result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    // Coding fragments dictates that code normally found in onCreate for an activity,
    // go in onCreateView... if you want to access layout elements
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        // Rep / Tho display
        final EditText Rep = view.findViewById(R.id.repEditText);
        final EditText Tho = view.findViewById(R.id.thoEditText);

        // DB Query to populate spinner
        String cols[] = {C2};
        result = db.query(DB_TABLE, cols, null, null, null, null, null);

        String exercises[] = new String[result.getCount()+1];
        exercises[0] = "Choose"; // Default spinner value

        int i = 0;
        result.moveToFirst();

        while(i < result.getCount()){
            exercises[i+1] = result.getString(0);
            result.moveToNext();
            i++;
        }




        // Create Spinner
        final Spinner spinner = view.findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, exercises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // Define actions when spinner selection changes
                Log.d(TAG, "onItemSelected: SPINNER");

                // Create SQL to SELECT tho & reps WHERE routine equals current spinner value
                String condition = "'" + spinner.getItemAtPosition(position).toString() + "'";

                result = db.rawQuery("SELECT " + C3 + ", " + C4 + " FROM " + DB_TABLE + " WHERE " + C2 + " LIKE " + condition, null);
                if (result.getCount()<1) {
                    Rep.setText("");
                    Tho.setText("");
                    return;
                }

                // Write to Rep / Tho EditTexts
                result.moveToFirst();
                Rep.setText(result.getString(1));
                Tho.setText(result.getString(0));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not used but must be included for compile

            }
        });


        // Buttons and BT indication image
        final Button connectbtn = view.findViewById(R.id.connectButton);
        final Button start = view.findViewById(R.id.startButton);
        final ImageView bt = view.findViewById(R.id.btImageView);
        start.setEnabled(false);

        // Connect button onClick Action
        connectbtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                    Log.d(TAG, "onClick: CONNECT BUTTON ");

                    if (!((MainActivity)getActivity()).mService.BTopen) {

                        if(((MainActivity)getActivity()).mService.findBT()){

                            ((MainActivity)getActivity()).mService.openBT();
                            bt.setImageResource(R.drawable.ic_btconnected);
                            start.setEnabled(true);

                        }
                    }
                    else {
                        bt.setImageResource(R.drawable.ic_btconnected);
                        start.setEnabled(true);
                    }

                    }
                }
        );

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: START BUTTON");

                // Send Parameters of BT to ME-MG
                String update = "Rep Target: " + Rep.getText().toString() + " Activation Threshold: " + Tho.getText().toString();

                if (((MainActivity)getActivity()).mService.BTopen){
                    try {
                        ((MainActivity)getActivity()).mService.sendData(update);
                    }
                    catch(IOException e) {
                        Log.d("IO", "something bad");
                    }

                    // Start GraphActivity with intent extras
                    Intent i = new Intent(getActivity(), GraphActivity.class);
                    i.putExtra("Reps", Rep.getText().toString());
                    i.putExtra("Tho", Tho.getText().toString());
                    i.putExtra("Routine", spinner.getSelectedItem().toString());
                    startActivity(i);

                }
                else{
                    Toast.makeText(getContext(),"Bluetooth Failure",Toast.LENGTH_SHORT).show();
                }

            }
        });

        return view;
    }



}


