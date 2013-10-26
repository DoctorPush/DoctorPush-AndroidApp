package de.delphinus.uberspace.pushdoc.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.delphinus.uberspace.pushdoc.Config;
import de.delphinus.uberspace.pushdoc.R;
import de.delphinus.uberspace.pushdoc.activities.MainActivity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;


/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 26.10.13
 */
public class Util {
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	public static void sendNotification(Context context, String title, String msg) {
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

		Notification.Builder mBuilder =
				new Notification.Builder(context)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle(title)
						.setStyle(new Notification.InboxStyle().setBigContentTitle(msg));

		mBuilder.setContentIntent(contentIntent);

		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		mBuilder.setSound(alarmSound);

		long[] pattern = {0,500,100,0,500,500};
		mBuilder.setVibrate(pattern);

		mNotificationManager.notify(Config.NOTIFICATION_ID, mBuilder.build());
	}

	public static void postJSON(final String url, final String body, final Handler handler) {
		final Thread thread = new Thread() {

			@Override
			public void run() {

				final HttpClient client = new DefaultHttpClient();
				final HttpPost httpPost = new HttpPost(url);

				try {
					httpPost.setEntity(new StringEntity(body, "UTF8"));
				} catch (final UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				httpPost.setHeader("Content-type", "application/json");

				try {
					final HttpResponse response = client.execute(httpPost);
					if (response != null) {
						final StatusLine statusLine = response.getStatusLine();
						final int statusCode = statusLine.getStatusCode();
						final Message m = new Message();
						final Bundle b = new Bundle();
						b.putInt("status", statusCode);
						m.setData(b);
						handler.sendMessage(m);
					}
				} catch (final ClientProtocolException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		};

		thread.start();
	}

	public static void getJSON(final String url, final Handler handler) {

		final Thread thread = new Thread() {

			@Override
			public void run() {

				final StringBuilder builder = new StringBuilder();
				final HttpClient client = new DefaultHttpClient();
				final HttpGet httpGet = new HttpGet(url);
				try {
					final HttpResponse response = client.execute(httpGet);
					final StatusLine statusLine = response.getStatusLine();
					final int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {
						final HttpEntity entity = response.getEntity();
						final InputStream content = entity.getContent();
						final BufferedReader reader = new BufferedReader(new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line);
						}
					} else {
						Log.e(Config.LOG_TAG, "Failed to download file");
					}
				} catch (final ClientProtocolException e) {
					e.printStackTrace();
				} catch (final IOException e) {
					e.printStackTrace();
				}
				final Message m = new Message();
				final Bundle b = new Bundle();
				b.putString("json", builder.toString());
				m.setData(b);
				handler.sendMessage(m);

			}
		};

		thread.start();

	}
}
