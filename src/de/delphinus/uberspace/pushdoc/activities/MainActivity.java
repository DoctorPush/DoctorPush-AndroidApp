package de.delphinus.uberspace.pushdoc.activities;

/**
 * DoctorPush
 *
 * @author Shivan Taher <shi.taher@gmail.com>
 * @date 26.10.13
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.delphinus.uberspace.pushdoc.Appointment;
import de.delphinus.uberspace.pushdoc.AppointmentArrayAdapter;
import de.delphinus.uberspace.pushdoc.Config;
import de.delphinus.uberspace.pushdoc.R;
import de.delphinus.uberspace.pushdoc.util.Preferences;
import de.delphinus.uberspace.pushdoc.util.ServerMessage;
import de.delphinus.uberspace.pushdoc.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Main UI for the demo app.
 */
public class MainActivity extends Activity {

	private GoogleCloudMessaging gcm;
	private String phoneNumber;
	private CountDownTimer countDownTimer;

	private AppointmentArrayAdapter aaa;
	private AppointmentArrayAdapter oldAaa;

	private String mode = "driving";

	boolean forceUpdateAppointment = false;

	private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String serviceUrl = intent.getExtras().getString("serviceURL");

			Log.i(Config.LOG_TAG, "service url: " + serviceUrl);

			Util.getJSON(serviceUrl, new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(final Message msg) {
					Log.i(Config.LOG_TAG, "response status: "+msg.getData().getInt("status"));

					try {
						JSONObject jsonAppointment = new JSONObject(msg.getData().getString("json"));


						Appointment appointment = new Appointment();
						appointment.fromJsonData(jsonAppointment);

						Appointment existentAppointment = aaa.getAppointmentById(appointment.getId());

						boolean isOld = false;

						if(existentAppointment == null) {
							existentAppointment = oldAaa.getAppointmentById(appointment.getId());
							isOld = true;
						}

						TextView personsBeforeTextView = (TextView) findViewById(R.id.personsTextView);

						String state = "There are " + appointment.getPersonsBefore() + " persons waiting before you";
						personsBeforeTextView.setText(state);

						if(existentAppointment != null) {
							existentAppointment.fromJsonData(jsonAppointment);

							if(!isOld) {
								aaa.notifyDataSetChanged();
							} else {
								oldAaa.removeAppointment(appointment.getId());
								aaa.add(appointment);

								aaa.notifyDataSetChanged();
								oldAaa.notifyDataSetChanged();
							}

							updateAppointments();
						} else {
							addAppointment(appointment);
							updateAppointments();

							Preferences preferences = Preferences.getInstance();

							ArrayList<String> savedAppointments = preferences.getStringArrayPref(
									Config.PROPERTY_JSON_APPOINTMENTS
							);

							if(savedAppointments == null) {
								savedAppointments = new ArrayList<String>();
							}

							savedAppointments.add(msg.getData().getString("json"));
							preferences.setStringArrayPref(Config.PROPERTY_JSON_APPOINTMENTS, savedAppointments);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

					return false;

				}
			}));
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_appointments);

		Context context = getApplicationContext();

		Preferences preferences = Preferences.getInstance();
		preferences.setActivity(this);
		preferences.setContext(context);

		LocalBroadcastManager.getInstance(this).registerReceiver( messageReceiver, new IntentFilter("notification"));

		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			phoneNumber = extras.getString("phoneNumber");
		}

		if (preferences.checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);

			String registrationId = preferences.getRegistrationId();

			if (registrationId.isEmpty()) {

				if(phoneNumber == null){
					phoneNumber = preferences.getPhoneNumber();
				}

				if(phoneNumber == null) {
					Intent intent = new Intent(getApplicationContext(), PhoneNumberActivity.class);
					startActivity(intent);
					return;
				}else{
					registerInBackground();
					return;
				}

			}
		} else {
			Log.i(Config.LOG_TAG, "No valid Google Play Services APK found.");
		}

//		TextView chronometerTextView = (TextView) findViewById(R.id.chronometer);
//		chronometerTextView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mode = mode == "driving" ? "walking" : "driving";
//
//				forceUpdateAppointment = true;
//			}
//		});

		onUserReady();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Preferences.getInstance().checkPlayServices();
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
					}

					String registrationId = gcm.register(Config.APP_ID);

					msg = "Device registered, registration ID=" + registrationId;

					Log.i(Config.LOG_TAG, msg);

					Preferences preferences = Preferences.getInstance();
					preferences.storeRegistrationId(registrationId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}

				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				sendRegistrationToBackend();
			}
		}.execute(null, null, null);
	}

	//
	// click on send button
	//

	public void onClick(final View view) {

		Log.i(Config.LOG_TAG, "not implemented yet");

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void sendRegistrationToBackend() {

		Log.i(Config.LOG_TAG, "sending registration");

		Preferences preferences = Preferences.getInstance();

		String action = "/user";
		String registrationId = preferences.getRegistrationId();
		String jsonReg = ServerMessage.user(registrationId, phoneNumber);

		Log.i(Config.LOG_TAG, "phone number: "+phoneNumber);

		Util.postJSON(Config.REST_URL + action, jsonReg, new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(final Message msg) {
				Log.i(Config.LOG_TAG, "response: " + msg.toString());

				if (msg.getData().getInt("status") == 201) {
					Log.i(Config.LOG_TAG, "user registration successful");
					onUserReady();
				} else {
					Log.i(Config.LOG_TAG, "user registration failed");
				}
				return false;
			}
		}));

	}

	private void onUserReady() {

		ListView listView = (ListView) findViewById(R.id.appointmentListView);

		aaa = new AppointmentArrayAdapter(
				this.getApplicationContext(),
				android.R.layout.simple_list_item_1,
				new ArrayList<Appointment>()
		);

		listView.setAdapter(aaa);

		ListView oldListView = (ListView) findViewById(R.id.oldAppointmentsListView);

		oldAaa = new AppointmentArrayAdapter(
				this.getApplicationContext(),
				android.R.layout.simple_list_item_1,
				new ArrayList<Appointment>()
		);

		oldListView.setAdapter(oldAaa);

		//
		// read appointments from shared preferences
		//

		Preferences preferences = Preferences.getInstance();

		ArrayList<String> jsonAppointments = preferences.getStringArrayPref(Config.PROPERTY_JSON_APPOINTMENTS);

		for(String json : jsonAppointments) {
			Appointment appointment = new Appointment();

			try {
				appointment.fromJsonData(new JSONObject(json));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			this.addAppointment(appointment);
		}

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position,
									final long id) {
				Intent intent = new Intent(getApplicationContext(), DetailActivity.class);

				intent.putExtra("jsonAppointment", aaa.getAppointments().get(position).getJsonData());
				startActivity(intent);
			}
		});

		oldListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position,
									final long id) {
				Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
				intent.putExtra("jsonAppointment", oldAaa.getAppointments().get(position).getJsonData());
				startActivity(intent);
			}
		});

		this.updateAppointments();

	}

	private void updateAppointments() {

		//
		// countdown
		//

		final TextView chronometer = (TextView) findViewById(R.id.chronometer);

		if(this.countDownTimer == null && aaa.getAppointments().size() > 0) {
			this.countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
				public void onTick(long millisUntilFinished) {
					//chronometer.setText("seconds remaining: " + millisUntilFinished / 1000);

					if(aaa.getAppointments().size() == 0)
						return;

					Appointment firstAppointment = aaa.getAppointments().get(0);

					Date currentDate = Calendar.getInstance().getTime();

					int secondsToDrive = firstAppointment.getSecondsToDrive() * 1000;
					long ts = firstAppointment.getDate().getTime() - currentDate.getTime() - secondsToDrive;

					if(ts <= 0) {
						aaa.removeAppointment(firstAppointment.getId());
						chronometer.setText("00:00:00");
						oldAaa.add(firstAppointment);
						return;
					}

					int minutes = (int)(ts / (60 * 1000));
					int hours = minutes / 60;
					int seconds = (int)(ts / 1000) % 60;
					minutes %= 60;

					if((seconds == 0 && minutes % 5 == 0) || forceUpdateAppointment){
						firstAppointment.updateLocation(MainActivity.this, mode);
						forceUpdateAppointment = false;
						// Toast.makeText(getApplicationContext(), mode, Toast.LENGTH_LONG).show();
					}

					String timeLeft = String.format("%02d", hours) + ":" +
							String.format("%02d", minutes) + ":" +
							String.format("%02d", seconds);

					chronometer.setText(timeLeft);

				}

				public void onFinish() {
					chronometer.setText(R.string.now);
				}
			}.start();
		}

	}

	private void addAppointment(Appointment appointment) {

		aaa.add(appointment);
		appointment.updateLocation(MainActivity.this, mode);

	}

}
