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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.delphinus.uberspace.pushdoc.AppointmentArrayAdapter;
import de.delphinus.uberspace.pushdoc.Config;
import de.delphinus.uberspace.pushdoc.R;
import de.delphinus.uberspace.pushdoc.util.Preferences;
import de.delphinus.uberspace.pushdoc.util.ServerMessage;
import de.delphinus.uberspace.pushdoc.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * Main UI for the demo app.
 */
public class MainActivity extends Activity {

	// TODO: remove

	private final static String APPOINTMENT_DATA = "{\"id\":4,\"start\":\"2013-10-27T15:00:00.000Z\",\"end\":\"2013-10-27T16:00:00.000Z\",\"patient_id\":1,\"medic_id\":1,\"created_at\":\"2013-10-26T19:35:36.092Z\",\"updated_at\":\"2013-10-26T19:37:47.376Z\",\"patient\":{\"id\":1,\"name\":\"Kann\",\"prename\":\"René\",\"title\":\"\",\"record_id\":\"42\",\"address\":null,\"tel_number\":\"+491604421713\",\"created_at\":\"2013-10-26T15:26:17.994Z\",\"updated_at\":\"2013-10-26T15:26:17.994Z\"},\"medic\":{\"id\":1,\"name\":\"Hofmann\",\"prename\":\"Johann\",\"title\":\"Dr.\",\"created_at\":\"2013-10-26T15:29:22.669Z\",\"updated_at\":\"2013-10-26T15:30:51.815Z\"},\"history\":[{\"id\":12,\"trackable_id\":4,\"trackable_type\":\"Appointment\",\"owner_id\":2,\"owner_type\":\"AdminUser\",\"key\":\"appointment.create\",\"parameters\":{},\"recipient_id\":null,\"recipient_type\":null,\"created_at\":\"2013-10-26T19:35:36.120Z\",\"updated_at\":\"2013-10-26T19:35:36.120Z\",\"owner\":{\"id\":2,\"email\":\"rene@meye.md\",\"created_at\":\"2013-10-26T15:18:18.181Z\",\"updated_at\":\"2013-10-26T20:31:51.944Z\",\"username\":\"meye\",\"name\":\"Meye\",\"prename\":\"René\",\"title\":\"Schwester\"}},{\"id\":13,\"trackable_id\":4,\"trackable_type\":\"Appointment\",\"owner_id\":2,\"owner_type\":\"AdminUser\",\"key\":\"appointment.update\",\"parameters\":{\"start\":\"2013-10-27T15:00:00Z\",\"start_was\":\"2013-10-27T14:00:00Z\",\"end\":\"2013-10-27T16:30:00Z\",\"end_was\":\"2013-10-27T14:30:00Z\"},\"recipient_id\":null,\"recipient_type\":null,\"created_at\":\"2013-10-26T19:37:31.411Z\",\"updated_at\":\"2013-10-26T19:37:31.411Z\",\"owner\":{\"id\":2,\"email\":\"rene@meye.md\",\"created_at\":\"2013-10-26T15:18:18.181Z\",\"updated_at\":\"2013-10-26T20:31:51.944Z\",\"username\":\"meye\",\"name\":\"Meye\",\"prename\":\"René\",\"title\":\"Schwester\"}},{\"id\":14,\"trackable_id\":4,\"trackable_type\":\"Appointment\",\"owner_id\":2,\"owner_type\":\"AdminUser\",\"key\":\"appointment.update\",\"parameters\":{\"end\":\"2013-10-27T16:00:00Z\",\"end_was\":\"2013-10-27T16:30:00Z\"},\"recipient_id\":null,\"recipient_type\":null,\"created_at\":\"2013-10-26T19:37:47.396Z\",\"updated_at\":\"2013-10-26T19:37:47.396Z\",\"owner\":{\"id\":2,\"email\":\"rene@meye.md\",\"created_at\":\"2013-10-26T15:18:18.181Z\",\"updated_at\":\"2013-10-26T20:31:51.944Z\",\"username\":\"meye\",\"name\":\"Meye\",\"prename\":\"René\",\"title\":\"Schwester\"}}]}";

	TextView mDisplay;
	GoogleCloudMessaging gcm;
	String phoneNumber;

	private AppointmentArrayAdapter aaa;

	private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(Config.LOG_TAG, "onReceive: "+intent.getExtras().toString());
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_appointments);
		mDisplay = (TextView) findViewById(R.id.display);

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
				mDisplay.append(msg + "\n");
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

		try {
			aaa = new AppointmentArrayAdapter(
					this.getApplicationContext(),
					android.R.layout.simple_list_item_1,
					new JSONObject[]{new JSONObject(APPOINTMENT_DATA)}
			);

			listView.setAdapter(aaa);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		//this.addAppointment();

	}

	private void addAppointment() {

		try {
			aaa.add(new JSONObject(APPOINTMENT_DATA));
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

}
