package lab1_202_03.uwaterloo.ca.lab3_202_03;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import ca.uwaterloo.sensortoy.LineGraphView;

// Accelerometer Sensor Event Listener
public class AccelerometerEventListener implements SensorEventListener{
    // Instantiate TextView and LineGraphView objects
    TextView output;
    LineGraphView graph;
    MagneticFieldSensorEventListener magFieldListener;

    // Constant for low pass filter
    final float CONSTANT = (float)0.75;

    // Instantiating the state machines states
    int currentState = 1;
    final int AWAITING_STATE = 1;
    final int STATE2 = 2;
    final int STATE3 = 3;
    final int STATE4 = 4;
    float[] currentValues = {0, 0, 0};
    int stepCount = 0;

    public AccelerometerEventListener (TextView outputView, LineGraphView graphView, MagneticFieldSensorEventListener magFieldListener){
        output = outputView;
        graph = graphView;
        this.magFieldListener = magFieldListener;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){    }

    public void onSensorChanged(SensorEvent event){ // SensorEvent represents a single message from a single sensor
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // Smooth values for x, y and z with a low pass filter
            currentValues[0] += (event.values[0] - currentValues[0]) / CONSTANT;
            currentValues[1] += (event.values[1] - currentValues[1]) / CONSTANT;
            currentValues[2] += (event.values[2] - currentValues[2]) / CONSTANT;

            // graph.addPoint(currentValues); // Add the current x, y, and z component readings to the graph
            pedometerFSM(); // Calling the state machine method

            // Output the step count
            output.setText(String.format("------ STEPS ------\n" +
                    "COUNT: " + stepCount));
        }
    }

    // Reset the step counter
    public void reset(){
        stepCount = 0;
    }

    // Returns current acceleration values
    public float[] getValues(){
        return currentValues;
    }

    // Tracks steps with a finite state machine
    private void pedometerFSM(){
        float value = currentValues[2];

        //Finite State Machine to track steps
        switch (currentState) {
            //Changes the state from nothing to peak if the stipulations are met
            case 1:
                if (value < -1.5){
                    currentState = STATE2;
                }
                break;
            //Changes the state from decreasing to increasing if the stipulations are met
            case 2:
                if (value > -1.5){
                    currentState = STATE3;
                }
                break;
            //Count a step if it adheres to the following conditions
            case 3:
                if (value > 2.4) {
                    currentState = STATE4;
                }
                break;
            //Checks if the value goes beyond a reasonable peak, if so, reset state, counts a step otherwise
            case 4:
                if(value > 7.5) {
                    currentState = AWAITING_STATE;
                } else if (value < 2.4) {
                    currentState = AWAITING_STATE;
                    stepCount++; // Increment the step counter
                    magFieldListener.updateNorthDisplacement(); // Update the displayed North displacement
                    magFieldListener.updateEastDisplacement(); // Update the displayed East displacement
                }
                break;
            default:
                break;
        }
    }
}