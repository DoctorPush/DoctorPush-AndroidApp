package de.delphinus.uberspace.pushdoc;

/**
 * DoctorPush
 *
 * @author Shivan Taher <shi.taher@gmail.com>
 * @date 26.10.13
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.delphinus.uberspace.pushdoc.util.Util;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 26.10.13
 */
public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		Log.i("DoctorPush", "onHandleIntent: type="+messageType);

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			if (!GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
				return;

			Log.i(Config.LOG_TAG, "Received: " + extras.toString());

			try {
				String message = extras.getString("message");

				Util.sendNotification(this, "DoctorPush", message);
			} catch (Exception e) {
				Log.i(Config.LOG_TAG, "JSON Exception: "+e);
			}

		}

		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
}
