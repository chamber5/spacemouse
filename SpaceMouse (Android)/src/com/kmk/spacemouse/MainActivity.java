package com.kmk.spacemouse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import com.kmk.spacemouse.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener
{
	// Stałe
	public static final int CONNECTION_ERROR = 0x1001;
	public static final int CONNECTED = 0x1002;
	public static final int DISCONNECTED = 0x1003;
	
	// Handler
	Handler handler;
	
	// Kontekst
	Context context = this;
	
	// Status rejestracji
	boolean isRecording = false;
	
	// Menedżer sensorów
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor gyroscope;
	
	// Socket do przesyłania danych i PrintWriter
	private Socket socket;
	private PrintWriter out;
	
	// Metody odpowiedzialne za nagrywanie położenia
	
	public void startRecording()
	{
		isRecording = true;
	}
	
	public void stopRecording()
	{
		isRecording = false;
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
		
		// Tymczasowo - ustawienie adresu IP
		((EditText) findViewById(R.id.ip)).setText("192.168.252.101");
		
		// Handler
		handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if(msg.what == CONNECTION_ERROR)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle(R.string.cantConnectTitle);
					builder.setMessage(R.string.cantConnectMessage);
					builder.setPositiveButton("OK", null);
					
					builder.show();
				}
				
				else if(msg.what == CONNECTED)
				{
					((EditText) findViewById(R.id.ip)).setEnabled(false);
					((Button) findViewById(R.id.connection)).setText(R.string.disconnect);
				}
				
				else if(msg.what == DISCONNECTED)
				{
					((EditText) findViewById(R.id.ip)).setEnabled(true);
					((Button) findViewById(R.id.connection)).setText(R.string.connect);
				}
			}
		};
		
		
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
		
		// Łączenie i rozłączanie
		final Button connection = (Button) findViewById(R.id.connection);
		connection.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(socket == null)
				{
					new Thread(new Runnable()
					{
						public void run()
						{
							try
							{
								String ip = ((EditText) findViewById(R.id.ip)).getText().toString();
								socket = new Socket(ip, 32200);
								
								Message msg = new Message();
								msg.what = CONNECTED;
								handler.sendMessage(msg);
								
								out = new PrintWriter(socket.getOutputStream(), true);
								out.write("Hello!\n");
								out.flush();
							}
							catch(IOException ex)
							{
								Message msg = new Message();
								msg.what = CONNECTION_ERROR;
								handler.sendMessage(msg);
								
								throw new Error(ex.getMessage());
							}
						}
					}).start();
				}
				
				else
				{
					try
					{
						out.close();
						out = null;
						
						socket.close();
						socket = null;
						
						Message msg = new Message();
						msg.what = DISCONNECTED;
						handler.sendMessage(msg);
					}
					catch(IOException ex)
					{
						Message msg = new Message();
						msg.what = CONNECTION_ERROR;
						handler.handleMessage(msg);
						
						throw new Error(ex.getMessage());
					}
				}
			}
		});
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		
		// Wyrejestrowujemy sensory
		stopRecording();
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		// Rejestrujemy sensory jeszcze raz
		sensorManager.registerListener(this, accelerometer, 50000);
		sensorManager.registerListener(this, gyroscope, 50000);
	}
	
	// Implementacja interfejsu SensorEventListener
	
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		Sensor sensor = event.sensor;
		String msg = "";
		
		if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
		{
			msg = isRecording + " acc " + event.values[0] + " " + event.values[1] + " " + event.values[2] + "\n";
		}
		
		else if(sensor.getType() == Sensor.TYPE_GYROSCOPE)
		{
			msg = isRecording + " gyr " + event.values[0] + " " + event.values[1] + " " + event.values[2] + "\n";
		}
		
		else return;
		
		if(out != null)
		{
			out.write(msg);
			out.flush();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
