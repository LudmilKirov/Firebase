package com.datecs.firebasediplomna;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.hardware.camera2.params.BlackLevelPattern;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import static android.content.ContentValues.TAG;
import static android.graphics.ColorSpace.Model.RGB;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Tag";
    private static final int MAX_OUTPUT_VOLTAGE = 6;
    private static final int JOB_ID = 0;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef;
    private DatePicker mDatePicker;
    private Button mOkButton;
    private String requiredDate;
    private DateInfo dateInfo = new DateInfo();
    private PieChart pieChart;
    private List<PieEntry> value = new ArrayList<>();
    private PieData pieData;
    private Date date2;
    private Description description = new Description();
    private float averageTemperature;
    private float averageInput;
    private float averageOutput;
    private int connectionCountforNotification;
    private int connectionCount;
    private boolean wantNotification = true;
    private boolean isConnect = false;
    private SimpleDateFormat formater = new SimpleDateFormat("dd MM yyyy kk:mm:ss:SSSS");
    private JobScheduler scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get reference to firebase
        myRef = database.getReference().child("InputVoltage");
        //add a listener for notifications
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //If the user want notifications
                Log.d(TAG, "onDataChange: notifikacii"+wantNotification);
                if (wantNotification) {
                    scheduleJob(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        pieChart = findViewById(R.id.piechart);
        // create a Calendar object and use the Date to configure
        // the Calendar.Then initialize the DatePicker.
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        mDatePicker = (DatePicker) findViewById(R.id.dialog_date_date_picker);
        mDatePicker.init(year, month, day, null);

        mOkButton = (Button) findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                //When clicked add the date
                int year1 = mDatePicker.getYear();
                int month1 = mDatePicker.getMonth();
                int day1 = mDatePicker.getDayOfMonth();
                Date date1 = new GregorianCalendar(year1, month1, day1).getTime();
                //get the time that is needed in long format
                long requiredTime = date1.getTime();
                //get the date needed in date format
                date2 = new Date(requiredTime);
                //get the required date in a string format
                requiredDate = Long.toString(requiredTime);
                //set a listener for the statistic
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        showData(dataSnapshot);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    private void showData(DataSnapshot dataSnapshot) {
        //Get the day and the month and the year from the choosen date
        String selectedDate = date2.toString().substring(30, 34).concat(date2.toString().substring(8, 10));
        //Iterate over the elements in the database
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //Get the elements in the database as a array
            ArrayList<String> array;
            array = (ArrayList<String>) ds.getValue();
            //Get the current date same format as the selected date
            String currentDate = array.get(1).substring(0, 4).concat(array.get(1).substring(8, 10));
            //If they match with the selected date
            if (selectedDate.equals(currentDate)) {
                String neededValue = "";
                //Increase the counter and set there is a match
                isConnect = true;
                connectionCount++;

                //Set the number of different unicodes match to zero
                int countForBytes = 0;

                String unicode = array.get(0);
                byte[] arr;
                arr = unicode.getBytes(StandardCharsets.UTF_8);

                for (byte a : arr) {
                    //Increase the number of different unicodes
                    countForBytes++;
                    //Get the second element of the array of data from the raspberry
                    if (countForBytes == 2) {

                        neededValue = String.valueOf(a);
                    }
                }
                //Get total count of inputs for input voltage
                averageInput += Integer.valueOf(neededValue);
                // averageOutput += Integer.valueOf(array.get(0).toString().substring(8, 10));

                //Set the values for the date
                //dateInfo.setTemperature(ds.getValue().toString().substring(13, 15));
                //dateInfo.setOutputVoltage(String.valueOf(averageInput));
                // dateInfo.setInputVoltage(String.valueOf(averageOutput));
            }
        }
        //When the matches stop get the average values
        averageOutput /= connectionCount;
        averageInput /= connectionCount;

        if (isConnect) {
            //  BarData barData = new BarData((List<IBarDataSet>) pieDataSet);
            // barData.setBarWidth(0.33f);

            description.setText("Average data");
            description.setTextSize(20f);
            pieChart.setDescription(description);
            pieChart.setHoleRadius(0f);
            pieChart.setTransparentCircleRadius(0f);

            value.clear();
            // value.add(new PieEntry(averageOutput, "Ouput"));
            //value.add(new PieEntry(averageTemperature, "Temperature"));
            value.add(new PieEntry(averageInput, "Input"));

            PieDataSet pieDataSet = new PieDataSet(value, "");
            // add a lot of colors
            ArrayList<Integer> colors = new ArrayList<>();

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());
            //Set the colors for the chart
            pieDataSet.setColors(colors);
            PieData data = new PieData(pieDataSet);
            data.setValueFormatter(new PercentFormatter(pieChart));
            //Set the text size
            data.setValueTextSize(13f);
            //Set the text color
            data.setValueTextColor(Color.WHITE);
            //Set the data
            pieChart.setData(data);
            pieData = new PieData(pieDataSet);
            //Set the colors and the animations for the chart
            pieDataSet.setColors(ColorTemplate.PASTEL_COLORS);
            pieChart.setEntryLabelColor(0);
            pieChart.animateXY(1400, 1400);
            pieDataSet.setDrawIcons(false);
        }
        averageTemperature = 0;
        averageInput = 0;
        averageOutput = 0;
        connectionCount = 0;
    }

    private void scheduleJob(DataSnapshot dataSnapshot) {
        scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName serviceName = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        int temp = 0;

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            ArrayList<String> array;
            array = (ArrayList<String>) ds.getValue();
             connectionCountforNotification++;
            //Set the number of different unicodes match to zero
            int countForBytes = 0;

            String unicode = array.get(0);
            byte[] arr;
            arr = unicode.getBytes(StandardCharsets.UTF_8);

            for (byte a : arr) {
                //Increase the number of different unicodes
                countForBytes++;
                //Get the second element of the array of data from the raspberry
                if (countForBytes == 2) {
                    temp = (int) a;
                    Log.d(TAG, "scheduleJob: temperatura"+temp);
                }

            }
            boolean constraintSet = MAX_OUTPUT_VOLTAGE < temp;

            if (constraintSet) {
                JobInfo myJobInfo = builder.build();
                scheduler.schedule(myJobInfo);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = (MenuItem) menu.findItem(R.id.switchId);
        item.setActionView(R.layout.switch_layout);
        Switch switchAB = item
                .getActionView().findViewById(R.id.switchAB);
        switchAB.setChecked(true);

        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                wantNotification = isChecked;
                if (isChecked) {
                    Toast.makeText(getApplication(), "ON", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplication(), "OFF", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        return true;
    }
}
