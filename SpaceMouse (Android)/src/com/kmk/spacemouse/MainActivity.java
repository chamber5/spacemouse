package com.kmk.spacemouse;

import java.util.Timer;
import java.util.TimerTask;
import com.kmk.spacemouse.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener
{
	// Stałe
	public static final int UPDATE_VALUES = 1;
	
	// Status rejestracji
	boolean isRecording = false;
	
	// Sumy i ilość pomiarów
	double accXSum, accYSum, accZSum;
	double gyrXSum, gyrYSum, gyrZSum;
	
	
	int accDataCount;
	int gyrDataCount;
	
	// Timer
	Timer timer;
	long delay = 50;
	
	// Handler do aktualizacji UI
	Handler handler;
	
	// Menedżer sensorów
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor gyroscope;
	
	// Metody odpowiedzialne za nagrywanie położenia
	
	public void startRecording()
	{
		reset();
		isRecording = true;
	}
	
	public void stopRecording()
	{
		isRecording = false;
	}
	
	// Resetowanie pomiarów
	public void reset()
	{
		accDataCount = 0;
		gyrDataCount = 0;
		
		accXSum = 0; accYSum = 0; accZSum = 0;
		gyrXSum = 0; gyrYSum = 0; gyrZSum = 0;
	}
	
	// Nadpisanie metod klasy Activity (obsługa cyklu życia aktywności)
	
	@SuppressLint("HandlerLeak")
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
		sensorManager.registerListener(this, accelerometer, 50000);
		
		gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		sensorManager.registerListener(this, gyroscope, 50000);
		
		// Wykrywanie trzymania przycisku
		final Button recorder = (Button) findViewById(R.id.recorder);
		recorder.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					startRecording();
				}
				
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					stopRecording();
				}
				
				return false;
			}
		});
		
		// Handler aktualizujący UI
		handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if(msg.what == UPDATE_VALUES)
				{

					TextView accX = (TextView) findViewById(R.id.accelerometer_x);
					TextView accY = (TextView) findViewById(R.id.accelerometer_y);
					TextView accZ = (TextView) findViewById(R.id.accelerometer_z);
					
					TextView gyrX = (TextView) findViewById(R.id.gyroscope_x);
					TextView gyrY = (TextView) findViewById(R.id.gyroscope_y);
					TextView gyrZ = (TextView) findViewById(R.id.gyroscope_z);
					
					
					if(accDataCount > 0 && gyrDataCount > 0)
					{
						
						accX.setText(Math.round(1000.0*accXSum/(double) accDataCount)/1000.0 + " m/s²");
						accY.setText(Math.round(1000.0*accYSum/(double) accDataCount)/1000.0 + " m/s²");
						accZ.setText(Math.round(1000.0*accZSum/(double) accDataCount)/1000.0 + " m/s²");
						
						gyrX.setText(Math.round(1000.0*gyrXSum/(double) gyrDataCount)/1000.0 + " rad/s");
						gyrY.setText(Math.round(1000.0*gyrYSum/(double) gyrDataCount)/1000.0 + " rad/s");
						gyrZ.setText(Math.round(1000.0*gyrZSum/(double) gyrDataCount)/1000.0 + " rad/s");
					}
					
					reset();
				}
				
				super.handleMessage(msg);
			}
		};
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		// Wyrejestrowujemy sensory
		stopRecording();
		sensorManager.unregisterListener(this);
		
		// Usuwamy timera
		timer.cancel();
		timer.purge();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		// Rejestrujemy sensory jeszcze raz
		sensorManager.registerListener(this, accelerometer, 50000);
		sensorManager.registerListener(this, gyroscope, 50000);
		
		// Tworzymy timera
		timer = new Timer("avgCounter");
		timer.scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				Message msg = handler.obtainMessage();
				msg.what = UPDATE_VALUES;
				handler.sendMessage(msg);
			}
		}, delay, delay);
	}
	
	// Implementacja interfejsu SensorEventListener
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		Sensor sensor = event.sensor;
		
		if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
		{
			accXSum += event.values[0];
			accYSum += event.values[1];
			accZSum += event.values[2];
			
			accDataCount++;
		}
		
		else if(sensor.getType() == Sensor.TYPE_GYROSCOPE)
		{
			gyrXSum += event.values[0];
			gyrYSum += event.values[1];
			gyrZSum += event.values[2];
			
			gyrDataCount++;
		}
		
		else
		{
			return;  // Innych sensorów nie potrzebuję
		}
		
		// Proces rejestracji
		
		if(!isRecording)
			return;
		
		if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)  // Przemieszczenie
		{
			
		}
		
		if(sensor.getType() == Sensor.TYPE_GYROSCOPE)  // Obrót
		{
			
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
