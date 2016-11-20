package lab1_202_03.uwaterloo.ca.lab3_202_03;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import ca.uwaterloo.mapper.MapLoader;
import ca.uwaterloo.mapper.MapView;
import ca.uwaterloo.mapper.NavigationalMap;
import ca.uwaterloo.sensortoy.LineGraphView;

public class MainActivity extends AppCompatActivity {
    // For data extraction only
    File root = android.os.Environment.getExternalStorageDirectory();
    File dir = new File (root.getAbsolutePath() + "/download");
    File file = new File(dir, "Output.csv");

    // Instantiate LineGraphView object
    LineGraphView accelGraph;
    LineGraphView magFieldGraph;
    MapView mv;

    // Instantiate global listeners for resetting purposes
    AccelerometerEventListener accelListener;
    MagneticFieldSensorEventListener magFieldListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout); // Create the parent layout
        layout.setOrientation(LinearLayout.VERTICAL); // Set the orientation to vertical

        mv = new MapView(getApplicationContext(), 1200, 1200, 49, 50); // Create the map view
        registerForContextMenu(mv);

        // Add the Map View to the layout
        NavigationalMap map = MapLoader.loadMap(getExternalFilesDir(null), "E2-3344.svg"); // Load E2-3344 Map
        mv.setMap(map);
        layout.addView(mv);
        mv.setVisibility(View.VISIBLE);

        // Instantiating accelerometer graph
        // Create the line graph that tracks the x, y, and z values of the accelerometer
        // accelGraph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"));
        // layout.addView(accelGraph); // Add the graph to the parent layout
        // accelGraph.setVisibility(View.VISIBLE); // Make the graph visible

        // Create the line graph that tracks the x, y, and z values of the magnetic field
        magFieldGraph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"));
        layout.addView(magFieldGraph); // Add the graph to the parent layout
        magFieldGraph.setVisibility(View.VISIBLE); // Make the graph visible

        // Accelerometer Label for outputting accelerometer readings
        TextView accelSensorLabel = new TextView(getApplicationContext());
        accelSensorLabel.setTextColor(Color.BLACK);
        layout.addView(accelSensorLabel); // Add the label to the parent layout

        // Magnetic Field Label for outputting magnetic field sensor readings
        TextView magFieldSensorLabel = new TextView(getApplicationContext());
        magFieldSensorLabel.setTextColor(Color.BLACK);
        layout.addView(magFieldSensorLabel); // Add the label to the parent layout

        // Request the sensor manager
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get instances of each sensor with the sensor manager
        Sensor linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // Get the accelerometer sensor
        Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magFSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); // Get the magnetic field sensor

        // Instantiate event listeners to receive the events from the sensors
        magFieldListener = new MagneticFieldSensorEventListener(magFieldSensorLabel, magFieldGraph);
        accelListener = new AccelerometerEventListener(accelSensorLabel, accelGraph, magFieldListener);

        // Register each listener to the respective sensor
        sensorManager.registerListener(accelListener, linearAccelSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magFieldListener, magFSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(magFieldListener, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Button to reset the step account pedometer
        Button resetButton = new Button(getApplicationContext());
        resetButton.setText("Reset Step Count");
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                accelListener.reset(); // Reset the accelerometer sensor
                magFieldListener.reset(); // Reset the displacement readings
            }
        });
        layout.addView(resetButton); // Add the button to the parent layout

//        dir.mkdir();
//        int interval = 50;
//        int duration = 20000;
//        TimerTask timer1Task = new TimerTask() {
//            @Override
//            public void run() {
////                writeToFile(accelListener.getOrientation());
//            }
//        };
//
//        TimerTask timer2Task = new TimerTask() {
//            @Override
//            public void run() {
//                System.exit(0);
//            }
//        };
//
//        Timer timer1 = new Timer();
//        Timer timer2 = new Timer();
//        timer1.schedule(timer1Task, interval, interval);
//        timer2.schedule(timer2Task, duration);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        mv.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        return super.onContextItemSelected(item) || mv.onContextItemSelected(item);
    }

    // Method for data extraction and file write out
    private void writeToFile(float[] values){
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file, true));
            pw.append(values[0] + ", " + values[1] + ", " + values[2] + "\n");
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
