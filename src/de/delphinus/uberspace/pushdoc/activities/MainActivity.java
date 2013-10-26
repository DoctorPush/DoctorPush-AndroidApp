package de.delphinus.uberspace.pushdoc.activities;

/**
 * DoctorPush
 *
 * @author Shivan Taher <shi.taher@gmail.com>
 * @date 26.10.13
 */

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.delphinus.uberspace.pushdoc.Config;
import de.delphinus.uberspace.pushdoc.R;
import de.delphinus.uberspace.pushdoc.util.Preferences;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Main UI for the demo app.
 */
public class MainActivity extends Activity {

	public static final String EXTRA_MESSAGE = "message";


	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;

	String regid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		mDisplay = (TextView) findViewById(R.id.display);

		context = getApplicationContext();

		Preferences preferences = Preferences.getInstance();
		preferences.setActivity(this);
		preferences.setContext(context);

		// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		if (preferences.checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = preferences.getRegistrationId();

			if (regid.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.i(Config.LOG_TAG, "No valid Google Play Services APK found.");
		}
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
						gcm = GoogleCloudMessaging.getInstance(context);
					}

					regid = gcm.register(Config.APP_ID);
					msg = "Device registered, registration ID=" + regid;

					Log.i(Config.LOG_TAG, msg);
					sendRegistrationToBackend();

					Preferences preferences = Preferences.getInstance();
					preferences.storeRegistrationId(regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}

				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				mDisplay.append(msg + "\n");
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
		String phoneNumber = preferences.getPhoneNumber();
		String jsonReg = "{androidDeviceID: \""+registrationId+"\", phoneNumber: \""+phoneNumber+"\"}";


//		Util.postJSON(Config.REST_URL+action, jsonReg, new Handler(new Handler.Callback() {
//			@Override
//			public boolean handleMessage(final Message msg) {
//				Log.i(Config.LOG_TAG, "response: "+msg.toString());
//
//				if (msg.getData().getInt("status") == 200) {
//					final Toast toast = Toast.makeText(MainActivity.this, "success", Toast.LENGTH_LONG);
//					toast.show();
//				} else {
//					final Toast toast = Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_LONG);
//					toast.show();
//				}
//				return false;
//			}
//		}));

	}
}
