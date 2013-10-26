package de.delphinus.uberspace.pushdoc.activities;

/**
 * DoctorPush
 *
 * @author Shivan Taher <shi.taher@gmail.com>
 * @date 26.10.13
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.delphinus.uberspace.pushdoc.Config;
import de.delphinus.uberspace.pushdoc.R;
import de.delphinus.uberspace.pushdoc.util.Preferences;
import de.delphinus.uberspace.pushdoc.util.ServerMessage;
import de.delphinus.uberspace.pushdoc.util.Util;

import java.io.IOException;


/**
 * Main UI for the demo app.
 */
public class MainActivity extends Activity {


	TextView mDisplay;
	GoogleCloudMessaging gcm;
	String phoneNumber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_appointments);
		mDisplay = (TextView) findViewById(R.id.display);

		Context context = getApplicationContext();

		Preferences preferences = Preferences.getInstance();
		preferences.setActivity(this);
		preferences.setContext(context);

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
				}else{
					registerInBackground();
				}

			}
		} else {
			Log.i(Config.LOG_TAG, "No valid Google Play Services APK found.");
		}
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
				} else {
					Log.i(Config.LOG_TAG, "user registration failed");
				}
				return false;
			}
		}));

	}
}
