package com.example.pefproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String logTag = "com.example.pefproject.APP_MainActivity.java";
    private TextView morningTextView;
    private TextView eveningTextView;
    private TextView extraTextView;
    private TextView recordTextView;

    private String normalFirstLetter;
    private String medicineFirstLetter;

    private  SimpleDateFormat dateFormat;

    private Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(logTag, "onCreate: Start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Singleton.getInstance().loadData(this);

        calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        dateFormat = new SimpleDateFormat(Singleton.getInstance().getDateFormat(), Locale.getDefault());

        this.morningTextView = findViewById(R.id.morningTextView);
        this.eveningTextView = findViewById(R.id.eveningTextView);
        this.extraTextView = findViewById(R.id.extraTextView);
        this.recordTextView = findViewById(R.id.recordTextView);

        normalFirstLetter = getString(R.string.Normal).substring(0, getString(R.string.Normal).length()-(getString(R.string.Normal).length() - 1));
        medicineFirstLetter = getString(R.string.Medicine).substring(0, getString(R.string.Medicine).length()-(getString(R.string.Medicine).length() - 1));

        recordTextView.setText(getString(R.string.Record) + ": " + dateFormat.format(date));
        morningTextView.setText(getString(R.string.Morning) +":\n"+ normalFirstLetter + ":---\n" + medicineFirstLetter + ":---");
        eveningTextView.setText(getString(R.string.Evening) +":\n"+ normalFirstLetter + ":---\n" + medicineFirstLetter + ":---");
        extraTextView.setText(getString(R.string.Extra) +":\n"+ normalFirstLetter + ":---\n" + medicineFirstLetter + ":---");

        setUpChart();
        loadDayRecord();
        Log.i(logTag, "onCreate: End");
    }
    private void loadDayRecord(){
        Log.i(logTag, " loadDayRecord: Start");
        ArrayList<Record> records = Singleton.getInstance().getRecording();
        if (records.isEmpty()){
            return;
        }
        Date date = calendar.getTime();
        for (int i = 0; i < records.size(); i++){
            if (dateFormat.format(records.get(i).getDate()).equals(dateFormat.format(calendar.getTime()))){
                if (records.get(i).getType() == Record.PM){
                    eveningTextView.setText(getString(R.string.Evening) +":\n"
                            + normalFirstLetter + ": " + records.get(i).getPeakNormalAirflow() +"\n"
                            + medicineFirstLetter + ": " + records.get(i).getPeakMedicineAirflow());
                }else if (records.get(i).getType() == Record.AM) {
                    morningTextView.setText(getString(R.string.Morning) +":\n"
                            + normalFirstLetter + ": " + records.get(i).getPeakNormalAirflow() +"\n"
                            + medicineFirstLetter + ": " + records.get(i).getPeakMedicineAirflow());
                }else if (records.get(i).getType() == Record.EXTRA) {
                    extraTextView.setText(getString(R.string.Extra) +":\n"
                            + normalFirstLetter + ": " + records.get(i).getPeakNormalAirflow() +"\n"
                            + medicineFirstLetter + ": " + records.get(i).getPeakMedicineAirflow());
                }
            }
        }
        Log.i(logTag, " loadDayRecord: Ready");
    }
    private void setUpChart(){
        Log.i(logTag, " setUpChart: Start");
        BarChart barChart = findViewById(R.id.barView);

        ArrayList<BarEntry> morningAirflow = new ArrayList<>();
        ArrayList<BarEntry> eveningAirflow = new ArrayList<>();
        ArrayList<BarEntry> extraAirflow = new ArrayList<>();
        ArrayList<Record> records = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        if (Singleton.getInstance().getRecording().isEmpty()){
            return;
        }
        for (int x = 0; x < Singleton.getInstance().getRecording().size(); x++){
            records.add(Singleton.getInstance().getRecording().get(x));
            if (dates.isEmpty()){
                dates.add(dateFormat.format(records.get(x).getDate()));
                continue;
            }
            if (!dates.contains(dateFormat.format(records.get(x).getDate()))){
                dates.add(dateFormat.format(records.get(x).getDate()));
            }
        }
        while (dates.size() < 7){
            Record record = new Record();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(records.get(records.size()-1).getDate());
            calendar.add(Calendar.DATE, 1);
            record.setDate(calendar.getTime());
            records.add(record);
            if (!dates.contains(dateFormat.format(records.get(records.size()-1).getDate()))){
                dates.add(dateFormat.format(records.get(records.size()-1).getDate()));
            }
        }
        for (int i = 0; i < 7; i++){
            morningAirflow.add(new BarEntry(i, 0));
            eveningAirflow.add(new BarEntry(i, 0));
            extraAirflow.add(new BarEntry(i, 0));
            float [] zeroValues = {0,0,};
            morningAirflow.get(i).setVals(zeroValues);
            eveningAirflow.get(i).setVals(zeroValues);
            extraAirflow.get(i).setVals(zeroValues);
            for (int d  = 0; d < records.size(); d++){

                if (dateFormat.format(records.get(d).getDate()).equals(dates.get((dates.size()- 1) - i))) {
                    float [] values = {
                            records.get(d).getPeakNormalAirflow(),
                            records.get(d).getPeakMedicineAirflow(),
                    };
                    switch (records.get(d).getType()){
                        case Record.AM:
                            morningAirflow.get(i).setVals(values);
                            break;
                        case Record.PM:
                            eveningAirflow.get(i).setVals(values);
                            break;
                        case Record.EXTRA:
                            extraAirflow.get(i).setVals(values);
                            break;
                    }
                }
            }
        }


        int [] morningColors = {
                ContextCompat.getColor(this, R.color.white_230) ,
                ContextCompat.getColor(this, R.color.light_blue)
        };
        int [] eveningColors = {
                ContextCompat.getColor(this, R.color.grey) ,
                ContextCompat.getColor(this, R.color.dark_blue)
        };
        int [] extraColors = {
                ContextCompat.getColor(this, R.color.light_yellow) ,
                ContextCompat.getColor(this, R.color.yellow)
        };
        String [] colorLabels = {
                getString(R.string.Normal) ,
                getString(R.string.Medicine)
        };
        BarDataSet morningDataSet = new BarDataSet(morningAirflow, "");
        morningDataSet.setColors(morningColors);
        morningDataSet.setStackLabels(colorLabels);

        BarDataSet eveningDataSet = new BarDataSet(eveningAirflow, "");
        eveningDataSet.setColors(eveningColors);
        eveningDataSet.setStackLabels(colorLabels);

        BarDataSet extraDataSet = new BarDataSet(extraAirflow, "");
        extraDataSet.setColors(extraColors);
        extraDataSet.setStackLabels(colorLabels);

        BarData barData = new BarData(morningDataSet, eveningDataSet, extraDataSet);
        barChart.setData(barData);
        barData.setBarWidth(barChart.getXAxis().getGridLineWidth() / 3);

        barChart.groupBars(0f, 0f, 0f);
        barChart.setFitBars(true);
        //barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        barChart.getXAxis().setCenterAxisLabels(true);
        barChart.getXAxis().setAvoidFirstLastClipping(true);
        barChart.setVisibleXRange(0f, 7.4f);
        barChart.setScaleYEnabled(false);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setValueFormatter(new MyValueFormatter(dates));
        barChart.getDescription().setText(getString(R.string.PeakAirflow));
        barChart.setDrawValueAboveBar(false);
        Log.i(logTag, " setUpChart: Ready");
    }
    public void buttonPressed (View view) {
        //Get widgets view id
        Intent intent;
        switch (view.getId()) {
            // Buttons id:
            case R.id.NewRecordButton:
                intent = new Intent(this, NewRecordActivity.class);
                startActivity(intent);
                break;
            /*case R.id.OldRecordActivity:
                intent = new Intent(this, OldRecordActivity.class);
                startActivity(intent);
                break;
            case R.id.barView:
                break;*/
            default:
                intent = new Intent(this, OldRecordActivity.class);
                startActivity(intent);
                break;
        }
    }
    /*
    private void setTestRecords(){
        for (int i = 0; i < 1; i++) {
            Singleton.getInstance().addRecord(new Record());
            Singleton.getInstance().getRecording().get(i).addNormalAirflow(130 + 10*i);
            Singleton.getInstance().getRecording().get(i).addNormalAirflow(145+ 10*i);
            Singleton.getInstance().getRecording().get(i).addNormalAirflow(140+ 10*i);
            Singleton.getInstance().getRecording().get(i).addMedicineAirflow(222+ 10*i);
            Singleton.getInstance().getRecording().get(i).addMedicineAirflow(212+ 10*i);
            Singleton.getInstance().getRecording().get(i).addMedicineAirflow(232+ 10*i);
            Singleton.getInstance().getRecording().get(i).setType(Record.AM);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i);
            Singleton.getInstance().getRecording().get(i).setDate(calendar.getTime());
        }
        for (int i = 0; i < 0; i++) {
            Singleton.getInstance().addRecord(new Record());
            Singleton.getInstance().getRecording().get(Singleton.getInstance().getRecording().size() - 1).addNormalAirflow(330 + 10*i);
            Singleton.getInstance().getRecording().get(Singleton.getInstance().getRecording().size() - 1).addMedicineAirflow(432+ 10*i);

            Singleton.getInstance().getRecording().get(Singleton.getInstance().getRecording().size() - 1).setType(Record.PM);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i);
            Singleton.getInstance().getRecording().get(Singleton.getInstance().getRecording().size() - 1).setDate(calendar.getTime());
        }
        for (int i = 0; i < 0; i++) {
            Singleton.getInstance().addRecord(new Record());
            Singleton.getInstance().getRecording().get(Singleton.getInstance().getRecording().size() - 1).addNormalAirflow(330 + 10*i);
            Singleton.getInstance().getRecording().get(Singleton.getInstance().getRecording().size() - 1).addMedicineAirflow(432+ 10*i);

            Singleton.getInstance().getRecording().get(Singleton.getInstance().getRecording().size() - 1).setType(Record.EXTRA);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i);
            Singleton.getInstance().getRecording().get(Singleton.getInstance().getRecording().size() - 1).setDate(calendar.getTime());
        }
        for (int i = 0; i < Singleton.getInstance().getRecording().size(); i++){
            Log.i(logTag, "Record ["+ i +"] : " + Singleton.getInstance().getRecording().get(i).toString());
        }
    }
     */
    @Override
    protected void onPause() {
        Singleton.getInstance().saveData(this);
        super.onPause();
    }
    @Override
    protected void onResume() {
        Log.i(logTag, "onResume: Start");
        loadDayRecord();
        super.onResume();
        Log.i(logTag, "onResume: End");
    }
}