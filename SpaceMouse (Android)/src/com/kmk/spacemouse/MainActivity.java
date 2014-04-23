package com.kmk.spacemouse;

import com.kmk.spacemouse.R;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener
{
	// Menedżer sensorów
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor gyroscope;
	
	// Nadpisanie metod klasy Activity (obsługa cyklu życia aktywności)
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Ustawiamy układ interfejsu
		setContentView(R.layout.activity_main);
		
		// Inicjalizujemy menedżer sensorów
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		// Rejestracja listenera
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		sensorManager.registerListener(this, accelerometer, 10000);
		
		gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		sensorManager.registerListener(this, gyroscope, 10000);
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		// Wyrejestrowujemy sensory
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		// Rejestrujemy sensory jeszcze raz
		sensorManager.registerListener(this, accelerometer, 10000);
		sensorManager.registerListener(this, gyroscope, 10000);
	}
	
	// Implementacja interfejsu SensorEventListener
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		Sensor sensor = event.sensor;
		
		if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
		{
			TextView x = (TextView) findViewById(R.id.accelerometer_x);
			TextView y = (TextView) findViewById(R.id.accelerometer_y);
			TextView z = (TextView) findViewById(R.id.accelerometer_z);
			
			x.setText("" + Math.round(1000*event.values[0])/1000.0 + " m/s²");
			y.setText("" + Math.round(1000*event.values[1])/1000.0 + " m/s²");
			z.setText("" + Math.round(1000*event.values[2])/1000.0 + " m/s²");
		}
		
		else if(sensor.getType() == Sensor.TYPE_GYROSCOPE)
		{
			TextView x = (TextView) findViewById(R.id.gyroscope_x);
			TextView y = (TextView) findViewById(R.id.gyroscope_y);
			TextView z = (TextView) findViewById(R.id.gyroscope_z);
			
			x.setText("" + event.values[0]);
			y.setText("" + event.values[1]);
			z.setText("" + event.values[2]);
			
			x.setText("" + Math.round(1000*event.values[0])/1000.0 + " rad/s");
			y.setText("" + Math.round(1000*event.values[1])/1000.0 + " rad/s");
			z.setText("" + Math.round(1000*event.values[1])/1000.0 + " rad/s");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
