package tw.edu.uch.sosdemo;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {
	
	private static final String PHONE_NO = "phoneNo";
	private static final String MESSAGE = "message";
	private static final String IS_SAVE = "isSave";
	
	private EditText txtPhoneNo;
	private EditText txtMessage;
	private SensorManager sensorManager;
	private SharedPreferences prefs;

	private String phoneNo;
	private String message;
	private boolean isSave = false;
	private long lastUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo); 
		txtMessage = (EditText) findViewById(R.id.txtMessage);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.unregisterListener(this);
		
		prefs = getPreferences(MODE_PRIVATE);
		lastUpdate = System.currentTimeMillis();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		txtPhoneNo.setText(prefs.getString(PHONE_NO, ""));	
		txtMessage.setText(prefs.getString(MESSAGE, ""));
		phoneNo = prefs.getString(PHONE_NO, "");
		message = prefs.getString(MESSAGE, "");
		isSave = prefs.getBoolean(IS_SAVE, false);
		
		if (isSave) {
			sensorManager.registerListener(this, 
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
					SensorManager.SENSOR_DELAY_NORMAL);
		} 
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		SharedPreferences.Editor prefEdit = prefs.edit();
		prefEdit.putString(PHONE_NO, phoneNo);
		prefEdit.putString(MESSAGE, message);
		prefEdit.putBoolean(IS_SAVE, isSave);
		prefEdit.commit();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}
	}

	public void startBtn_Click(View view) {
		if (!isSave) {
			Toast.makeText(this, "請輸入電話號碼和訊息內容！", Toast.LENGTH_SHORT).show();
			return;
		}
		
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
				SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void stopBtn_Click(View view) {
		sensorManager.unregisterListener(this);
	}
	
	public void saveBtn_Click(View view) {
		String pNo = txtPhoneNo.getText().toString();
		String msg = txtMessage.getText().toString();
		
		if (pNo.length() > 0 && msg.length() > 0) {
			phoneNo = pNo;
			message = msg;
			isSave = true;
		} else {
			isSave = false;
			
			Toast.makeText(this, "請輸入電話號碼和訊息內容！", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;

		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = System.currentTimeMillis();
		
		if (accelationSquareRoot >= 2) {
			if (actualTime - lastUpdate < 200) {
				return;
			}
			
			sendSMS();
			lastUpdate = actualTime;
		}
	}
	
	private void sendSMS() {
		SmsManager.getDefault().sendTextMessage(phoneNo, null, message, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
