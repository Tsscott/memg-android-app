package com.xyz.me_mg;

import android.database.Cursor;
import android.graphics.Color;
import android.icu.util.DateInterval;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.xyz.me_mg.MainActivity.A1;
import static com.xyz.me_mg.MainActivity.A2;
import static com.xyz.me_mg.MainActivity.A3;
import static com.xyz.me_mg.MainActivity.A4;
import static com.xyz.me_mg.MainActivity.A5;
import static com.xyz.me_mg.MainActivity.A6;
import static com.xyz.me_mg.MainActivity.C2;
import static com.xyz.me_mg.MainActivity.C3;
import static com.xyz.me_mg.MainActivity.C4;
import static com.xyz.me_mg.MainActivity.DB_TABLE;
import static com.xyz.me_mg.MainActivity.DB_TABLE_2;
import static com.xyz.me_mg.MainActivity.db;

/**
 * Created by timscott on 29/04/2018.
 *
 * Report Fragment Class
 *
 * The three fragments (selected by navbar) sit on top of MainActivity
 *
 * This fragment uses MPAndroid Chart
 * https://github.com/PhilJay/MPAndroidChart
 *
 * Piechart example code:
 * https://github.com/mitchtabian/Pie-Chart-Tutorial
 */

public class ReportFragment extends Fragment {
    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        return fragment;
    }

    static final String TAG = "ReportFragment";

    Integer[] yData = {0,0};
    private String[] xData = {"Good Reps", "Bad Reps"};
    PieChart pieChart;
    ProgressBar progress;
    TextView duration;
    RatingBar rating;
    Cursor result, result2;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    int reps_complete,target;
    float progress_value,rating_value;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // DB query to populate spinner
        String cols[] = {A1,A2,A3,A4,A5,A6};
        result = db.query(DB_TABLE_2, cols, null, null, null, null, null);

        String logs[] = new String[result.getCount()];

        int i = result.getCount();
        result.moveToFirst();

        while(i > 0){
            logs[i-1] = result.getString(0) + ": " + result.getString(1) + ": " + result.getString(2).substring(0,16);
            result.moveToNext();
            i--;
        }

        // UI elements
        pieChart = view.findViewById(R.id.pieChart);
        progress = view.findViewById(R.id.progressBar);
        duration = view.findViewById(R.id.textClock);
        rating = view.findViewById(R.id.ratingBar);

        progress.setScaleY(3f);
        progress.setMax(100);

        // Customize piechart
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(2);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setDrawEntryLabels(false);


        // Spinner
        final Spinner spinner = view.findViewById(R.id.spinner2);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, logs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // Define actions when spinner selection changes
                Log.d(TAG, "onItemSelected: SPINNER");

                // get id
                int log_num = Integer.parseInt(spinner.getItemAtPosition(position).toString().substring(0,spinner.getItemAtPosition(position).toString().indexOf(":")));

                // populate report page, based on id (primary key) from current spinner item
                result.moveToPosition(log_num-1);
                String start, end;
                start = result.getString(2);
                end = result.getString(3);

                try{
                    Long interval = findDayDiff(dateFormat.parse(start),dateFormat.parse(end));
                    duration.setText(interval.toString() + " mins");
                }
                catch(Exception e){}

                yData[0] = result.getInt(4);
                yData[1] = result.getInt(5);
                reps_complete =  yData[0]+yData[1];

                String condition = "'" + result.getString(1) + "'";

                result2 = db.rawQuery("SELECT " + C4 + " FROM " + DB_TABLE + " WHERE " + C2 + " LIKE " + condition, null);
                result2.moveToFirst();
                target = result2.getInt(0);
                progress_value = ((float)reps_complete / (float)target)*100;

                progress.setProgress((int)progress_value);

                addDataSet();

                rating_value = ((float)yData[0] / (float)target)*5;

                rating.setRating(rating_value);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    public static long findDayDiff(Date date1, Date date2) {
        Log.d(TAG, "findDayDiff: ");
        long difference = date2.getTime() - date1.getTime();
        return TimeUnit.MINUTES.convert(difference , TimeUnit.MILLISECONDS);
    }

    private void addDataSet() {
        Log.d(TAG, "addDataSet: ");
        // Create dataset and add to piechart

        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , i));
        }

        for(int i = 1; i < xData.length; i++){
            xEntrys.add(xData[i]);
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Good/Bad");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(8);
        pieDataSet.setValueTextColor(Color.WHITE);


        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.colorAccent));
        colors.add(getResources().getColor(R.color.colorPrimary));


        pieDataSet.setColors(colors);

        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(8);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART_INSIDE);

        //disable title
        Description desc = pieChart.getDescription();
        desc.setEnabled(false);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        // if good reps exist highlight that pie segment
        if(yData[1] > 0) {
            Highlight h = new Highlight(0, 0, 0); // dataset index for piechart is always 0
            pieChart.highlightValues(new Highlight[]{h});
        }
        else{
            pieDataSet.setValueTextColor(getResources().getColor(R.color.colorAccent));
            Highlight h = new Highlight(1, 0, 0); // dataset index for piechart is always 0
            pieChart.highlightValues(new Highlight[]{h});
        }
    }


}
