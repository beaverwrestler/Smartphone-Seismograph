package com.example.artinsarkezians.physicsculminating_rev3;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.hardware.SensorEventListener;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    double x;        //Acceleration variables
    double y;
    double z;
    double netXY, netOVERALL;       //More var's
    double tempx, tempy, tempz;

    int currentPos = 0;
    double timeDiff =0;

    double newMaxTime = 0;
    boolean findEndTime = false;
    double maxElapsed = 0;

    double currentMaxNET = 0;
    double mostRecentTime = 0;

    int phoneDisplacement = 0;
    double tempNetOvl;
    double earthDist = 1;

    LineGraphSeries<DataPoint> NETseries;
    LineGraphSeries<DataPoint> Xseries;
    LineGraphSeries<DataPoint> Yseries;
    LineGraphSeries<DataPoint> Zseries;

    double avgNET = 1000;
    double totalNET = 0;
    int numRounds = 0;
    double tempNET = 0;
    double detectQuake =0;
    double sWave = 0;
    boolean measure = true;
    double magEarth = 0;

    int cX = Color.rgb(244, 67, 54);
    int cY = Color.rgb(76, 175, 80);
    int cZ = Color.rgb(33, 150, 243);
    int cNET = Color.rgb(33, 33, 33);

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Calcs _ac = new Calcs();            //Starting threads
        _ac.start();

        GraphView graphNET = (GraphView) findViewById(R.id.graphNET);
        NETseries = new LineGraphSeries<>();
        graphNET.addSeries(NETseries);
        NETseries.setColor(cNET);

        GraphView graphX = (GraphView) findViewById(R.id.graphX);
        Xseries = new LineGraphSeries<>();
        graphX.addSeries(Xseries);
        Xseries.setColor(cX);

        GraphView graphY = (GraphView) findViewById(R.id.graphY);
        Yseries = new LineGraphSeries<>();
        graphY.addSeries(Yseries);
        Yseries.setColor(cY);

        GraphView graphZ = (GraphView) findViewById(R.id.graphZ);
        Zseries = new LineGraphSeries<>();
        graphZ.addSeries(Zseries);
        Zseries.setColor(cZ);

        graphingInit();
    }

    public class Calcs extends Thread {

        public void run() {

            try{
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {}

            while (true) {

                if (netOVERALL > 0.06) {
                    detectQuake = System.currentTimeMillis();
                    measure = true;
                }

                while (measure) {

                    tempNET = netOVERALL;

                    numRounds++;
                    totalNET += tempNET;
                    avgNET = (totalNET / numRounds);

                    if (tempNET > (4 * avgNET))
                    {
                        sWave = System.currentTimeMillis();
                        timeDiff = sWave - detectQuake;
                        earthDist = Math.round(((timeDiff/1000) * 5.5)*10.0)/10.0;

                        measure = false;
                        avgNET = 0;
                        numRounds = 0;
                    }

                    if (netOVERALL < 0.06)
                        measure = false;
                }

                if (netOVERALL<0.06)
                    measure = false;

                magEarth = Math.round((Math.log10(phoneDisplacement)-2.48+(2.76*Math.log10(earthDist)))*10.0)/10.0;

            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
        }

        x = Math.round(x*1000.0)/1000.0;
        y = Math.round(y*1000.0)/1000.0;
        z = Math.round(z*1000.0)/1000.0;

        tempx = x;
        tempy = y;
        tempz = z;

        netXY = Math.sqrt((tempx * tempx) + (tempy * tempy));                                     //Use the pythagorean theorem to calculate the net acceleration
        netOVERALL = Math.sqrt((tempz * tempz) + (netXY * netXY));
        netOVERALL = Math.round(netOVERALL*1000.0)/1000.0;

        if (netOVERALL >= currentMaxNET)
        {
            newMaxTime = System.currentTimeMillis();
            findEndTime = true;
            currentMaxNET = netOVERALL;
        }
        else
        {
            if (findEndTime) {
                maxElapsed = System.currentTimeMillis() - newMaxTime;
                mostRecentTime = maxElapsed/1000;
                findEndTime = false;
            }
        }

        tempNetOvl = netOVERALL*1000000;
        phoneDisplacement = (int) (mostRecentTime*mostRecentTime*tempNetOvl)/10;

        updateGraph();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void updateGraph()
    {
        TextView t5 = (TextView) findViewById(R.id.ampVal);
        t5.setText(phoneDisplacement + "μm");

        if (earthDist <= 1) {
            TextView t6 = (TextView) findViewById(R.id.netaVal);
            t6.setText("<1 km");
        }
        else {
            TextView t6 = (TextView) findViewById(R.id.netaVal);
            t6.setText(earthDist + " km");
        }

        TextView t7 = (TextView) findViewById(R.id.magVal);
        t7.setText(magEarth + "");

        NETseries.appendData(new DataPoint(currentPos, netOVERALL), true, 150);
        Xseries.appendData(new DataPoint(currentPos, tempx), true, 150);
        Yseries.appendData(new DataPoint(currentPos, tempy), true, 150);
        Zseries.appendData(new DataPoint(currentPos, tempz), true, 150);

        currentPos++;
    }


    public void graphingInit()
    {
        // NET graph

        GraphView graphNET = (GraphView) findViewById(R.id.graphNET);

        graphNET.getViewport().setXAxisBoundsManual(true);                                          //set x axis range
        graphNET.getViewport().setMinX(0);
        graphNET.getViewport().setMaxX(150);

        graphNET.getViewport().setYAxisBoundsManual(true);                                          //set y axis range
        graphNET.getViewport().setMinY(0);
        graphNET.getViewport().setMaxY(2);

        graphNET.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.HORIZONTAL);      //remove vertical lines
        graphNET.getGridLabelRenderer().setHorizontalLabelsVisible(false);                                  //remove x axis labels
        graphNET.getGridLabelRenderer().setVerticalLabelsVisible(false);                                  //remove y axis labels

        //X graph

        GraphView graphX = (GraphView) findViewById(R.id.graphX);

        graphX.getViewport().setXAxisBoundsManual(true);                                          //set x axis range
        graphX.getViewport().setMinX(0);
        graphX.getViewport().setMaxX(150);

        graphX.getViewport().setYAxisBoundsManual(true);                                          //set y axis range
        graphX.getViewport().setMinY(-3);
        graphX.getViewport().setMaxY(3);

        graphX.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.HORIZONTAL);      //remove vertical lines
        graphX.getGridLabelRenderer().setHorizontalLabelsVisible(false);                                  //remove x axis labels
        graphX.getGridLabelRenderer().setVerticalLabelsVisible(false);                                  //remove y axis labels

        //y graph

        GraphView graphY = (GraphView) findViewById(R.id.graphY);

        graphY.getViewport().setXAxisBoundsManual(true);                                          //set x axis range
        graphY.getViewport().setMinX(0);
        graphY.getViewport().setMaxX(150);

        graphY.getViewport().setYAxisBoundsManual(true);                                          //set y axis range
        graphY.getViewport().setMinY(-3);
        graphY.getViewport().setMaxY(3);

        graphY.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.HORIZONTAL);      //remove vertical lines
        graphY.getGridLabelRenderer().setHorizontalLabelsVisible(false);                                  //remove x axis labels
        graphY.getGridLabelRenderer().setVerticalLabelsVisible(false);                                  //remove y axis labels

        //z graph

        GraphView graphZ = (GraphView) findViewById(R.id.graphZ);

        graphZ.getViewport().setXAxisBoundsManual(true);                                          //set x axis range
        graphZ.getViewport().setMinX(0);
        graphZ.getViewport().setMaxX(150);

        graphZ.getViewport().setYAxisBoundsManual(true);                                          //set y axis range
        graphZ.getViewport().setMinY(-3);
        graphZ.getViewport().setMaxY(3);

        graphZ.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.HORIZONTAL);      //remove vertical lines
        graphZ.getGridLabelRenderer().setHorizontalLabelsVisible(false);                                  //remove x axis labels
        graphZ.getGridLabelRenderer().setVerticalLabelsVisible(false);                                  //remove y axis labels

    }
}