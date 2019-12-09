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
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
    private static final int MAX_TEMP=21;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef ;
    public static final String EXTRA_DATE = "com.example.criminalintent";
    private static final String ARG_DATE = "date";
    private DatePicker mDatePicker;
    private Button mOkButton;
    private String requiredDate;
    DateInfo dateInfo = new DateInfo();
    PieChart pieChart;
    List<PieEntry> value = new ArrayList<>();
    PieData pieData;
    Date date2;
    Description description=new Description();
    float averageTemperature;
    float averageInput;
    float averageOutput;
    int connectionCount;
    boolean isConnect=false;
    private SimpleDateFormat formater = new SimpleDateFormat("dd MM yyyy kk:mm:ss:SSSS");

    private static final int JOB_ID = 0;


    private JobScheduler scheduler;


//    final DatabaseReference temperature = myRef.child("Temp").child("value");





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scheduleJob();
        setContentView(R.layout.activity_main);
        pieChart = findViewById(R.id.piechart);
       //pieChart.setUsePercentValues(true);



//        value.add(new PieEntry(40f,"jan"));
//        value.add(new PieEntry(30f,"feb"));
//        Log.d(TAG, "onCreate: kur"+value);
//




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
                long requoredNumber = date1.getTime();
                date2=new Date(requoredNumber);
                requiredDate=Long.toString(requoredNumber);
                requiredDate.substring(0,5);
                myRef = database.getReference();
                Log.d(TAG, "onClick: kur"+requiredDate);
                if (requiredDate != null) {

                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //if(requiredDate!=null){
                            Log.d(TAG, "onDataChange: kurbaza" + requiredDate);
                            showData(dataSnapshot);
                            //}
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }


              //  Log.d(TAG, "onClick:kurjelanipurvi 4 cifri "+requiredDate.substring(0,5));
//                Log.d(TAG, "onClick: kur "+formater.format(date1.getTime()));
//                Log.d(TAG, "onClick: 1kur 1575395817631 " +formater.format(date1.getTime()));
            }
        });


//        myRef.startAt(15753).
    }

    private void showData(DataSnapshot dataSnapshot) {
        Log.d(TAG, "showData: kur");


        String selectedDate = date2.toString().substring(4,10).concat(date2.toString().substring(30,34));
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                   if(!(ds.getKey().equals("DataForMonitoring"))){
                Log.d(TAG, "showData: kurkey" + ds.getKey());

                Long dateLong = Long.valueOf(ds.getKey());
                Date date=new Date(dateLong);
                //String currentDate = String.valueOf(date.getDay()+date.getMonth()+date.getYear());
                String currentDate = date.toString().substring(4,10).concat(date.toString().substring(30,34));
                Log.d(TAG, "showData: date1"+currentDate);

                Log.d(TAG, "date1 i date"+" " +date2+date2.getDay() +" " + date2.getMonth()+" "+date2.getYear()+" "+dateLong+ "        "+ date+" "+date.getMonth()+" "+date.getYear());
                if (selectedDate.equals(currentDate)) {
                 isConnect=true;
                    connectionCount++;
                    Log.d(TAG, "showData: kurvata   " + ds.getValue());

                    if (ds.getValue().toString().contains(".")) {
                        dateInfo.setTemperature(ds.getValue().toString().substring(12, 13));
                    } else {
                        averageTemperature+=Integer.valueOf(ds.getValue().toString().substring(13, 15));
                        averageInput+=Integer.valueOf(ds.getValue().toString().substring(31, 33));
                        averageOutput+=Integer.valueOf(ds.getValue().toString().substring(50, 51));

                        dateInfo.setTemperature(ds.getValue().toString().substring(13, 15));
                        dateInfo.setOutputVoltage(ds.getValue().toString().substring(31, 33));
                        dateInfo.setInputVoltage(ds.getValue().toString().substring(50, 51));
                    }
                }
            }
            }
            averageOutput/=connectionCount;
            averageInput/=connectionCount;
            averageTemperature/=connectionCount;

            if(isConnect) {
              //  BarData barData = new BarData((List<IBarDataSet>) pieDataSet);
               // barData.setBarWidth(0.33f);

                description.setText("Average data");
                description.setTextSize(20f);
                pieChart.setDescription(description);
                pieChart.setHoleRadius(0f);
                pieChart.setTransparentCircleRadius(0f);





                value.clear();
                value.add(new PieEntry(averageOutput, "Ouput"));
                value.add(new PieEntry(averageTemperature, "Temperature"));
                value.add(new PieEntry(averageInput, "Input"));

               PieDataSet  pieDataSet = new PieDataSet(value, "");
                // add a lot of colors

                ArrayList<Integer> colors = new ArrayList<>();


                for (int c : ColorTemplate.PASTEL_COLORS)
                    colors.add(c);

                colors.add(ColorTemplate.getHoloBlue());

                pieDataSet.setColors(colors);
                PieData data = new PieData(pieDataSet);
                data.setValueFormatter(new PercentFormatter(pieChart));
                data.setValueTextSize(11f);
                data.setValueTextColor(Color.WHITE);
             //   data.setValueTypeface(tfLight);
                pieChart.setData(data);
                pieData = new PieData(pieDataSet);

               // pieChart.setData(pieData);


               pieDataSet.setColors(ColorTemplate.PASTEL_COLORS);
                pieChart.setEntryLabelColor(0);
                pieChart.animateXY(1400, 1400);
                pieDataSet.setDrawIcons(false);



                // entry label styling
             //   pieChart.setEntryLabelColor(Color.BLACK);
                //   pieChart.setEntryLabelTypeface(tfRegular);
               // pieChart.setEntryLabelTextSize(12f);
                Log.d(TAG, "showData:kurvalue   " + value);
                Log.d(TAG, "showData: kurec " + dateInfo.getTemperature());

            }
        averageTemperature=0;
        averageInput=0;
        averageOutput=0;
        connectionCount=0;
    }


    private void scheduleJob(){
        scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName serviceName = new ComponentName(getPackageName(),NotificationJobService.class.getName());
        JobInfo.Builder builder=new JobInfo.Builder(JOB_ID,serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        int temp = 23;
        boolean constraintSet = MAX_TEMP<temp;
        if (constraintSet) {
            JobInfo myJobInfo = builder.build();
            scheduler.schedule(myJobInfo);
            Toast.makeText(this,R.string.google_app_id,Toast.LENGTH_SHORT).show();
        }
    }

}
