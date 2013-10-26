package de.delphinus.uberspace.pushdoc;

/**
 * DoctorPush
 *
 * @author Shivan Taher <shi.taher@gmail.com>
 * @date 26.10.13
 */

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 26.10.13
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
	public void onReceive(Context context, Intent intent) {

		// Explicitly specify that GcmIntentService will handle the intent.

		ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());

		// Start the service, keeping the device awake while it is launching.

		startWakefulService(context, (intent.setComponent(comp)));
		setResultCode(Activity.RESULT_OK);

	}
}
