package com.xyz.me_mg;

import android.content.Intent;
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


public class ExerciseFragment extends Fragment {
    public static ExerciseFragment newInstance() {
        ExerciseFragment fragment = new ExerciseFragment();
        return fragment;
    }

    public Boolean BTopen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);


        // Rep / Tho display
        final EditText Rep = view.findViewById(R.id.repEditText);
        final EditText Tho = view.findViewById(R.id.thoEditText);

        // Spinner
        final Spinner spinner = view.findViewById(R.id.spinner);
        String exercises[] = {"Choose","Knee Routine", "Arm Routine"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, exercises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                if(spinner.getItemAtPosition(position).toString().equals("Knee Routine")){
                    Rep.setText("60");
                    Tho.setText("400");


                }
                else if(spinner.getItemAtPosition(position).toString().equals("Arm Routine")){
                    Rep.setText("45");
                    Tho.setText("600");
                }
                else{
                    Rep.setText("");
                    Tho.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final Button connectbtn = view.findViewById(R.id.connectButton);
        final Button start = view.findViewById(R.id.startButton);
        final ImageView bt = view.findViewById(R.id.btImageView);
        start.setEnabled(false);


        connectbtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {


                        if (!((MainActivity)getActivity()).mService.BTopen) {
                            Log.d("Exercise","connectBtn-connect");

                            ((MainActivity)getActivity()).mService.findBT();

                            try {
                                ((MainActivity)getActivity()).mService.openBT();
                            }
                            catch(IOException e){
                                Log.d("IO", "something bad");
                            }
                            bt.setImageResource(R.drawable.ic_btconnected);
                            start.setEnabled(true);
                        }
                        else {
                            Toast.makeText(getContext(),"Me-MG Connected!",Toast.LENGTH_LONG).show();
                            bt.setImageResource(R.drawable.ic_btconnected);
                            start.setEnabled(true);
                        }

                    }
                }
        );

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), GraphActivity.class);
                i.putExtra("Reps", Rep.getText().toString());
                i.putExtra("Tho", Tho.getText().toString());
                startActivity(i);
            }
        });

        return view;
    }



}


