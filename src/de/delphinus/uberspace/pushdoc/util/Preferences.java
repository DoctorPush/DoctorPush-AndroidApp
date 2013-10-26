package de.delphinus.uberspace.pushdoc.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import de.delphinus.uberspace.pushdoc.Config;
import de.delphinus.uberspace.pushdoc.activities.MainActivity;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 26.10.13
 */
public class Preferences {
	private static Preferences ourInstance = new Preferences();

	public static Preferences getInstance() {
		return ourInstance;
	}

	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	Activity activity;
	Context context;

	private Preferences() {

	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param regId registration ID
	 */
	public void storeRegistrationId(String regId) {

		final SharedPreferences prefs = getGcmPreferences();
		int appVersion = this.getAppVersion();

		Log.i(Config.LOG_TAG, "Saving regId on app version " + appVersion);
		Log.i(Config.LOG_TAG, "RegID: " + regId);

		SharedPreferences.Editor editor = prefs.edit();

		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);

		editor.commit();
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public String getRegistrationId() {

		final SharedPreferences prefs = getGcmPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");

		if (registrationId.isEmpty()) {
			Log.i(Config.LOG_TAG, "Registration not found.");
			return "";
		}

		//
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		//

		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion();

		if (registeredVersion != currentVersion) {
			Log.i(Config.LOG_TAG, "App version changed.");
			return "";
		}

		return registrationId;

	}

	/**
	 * @return
	 */
	private SharedPreferences getGcmPreferences() {

		return this.activity.getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);

	}


	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	public int getAppVersion() {

		try {
			PackageInfo packageInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}

	}

	public String getPhoneNumber() {
		TelephonyManager telephonyManager = (TelephonyManager)this.context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getLine1Number();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	public boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.activity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this.activity,
						Config.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(Config.LOG_TAG, "This device is not supported.");
			}
			return false;
		}
		return true;
	}


}
