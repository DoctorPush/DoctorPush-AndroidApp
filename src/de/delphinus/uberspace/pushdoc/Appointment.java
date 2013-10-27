package de.delphinus.uberspace.pushdoc;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.delphinus.uberspace.pushdoc.util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 27.10.13
 */
public class Appointment {
	private int id;
	private String name;
	private String localeDate;
	private Date date;
	private String address;
	private String jsonData;
	private int secondsToDrive = 0;
	private int personsBefore = 0;

	public Appointment() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocaleDate() {
		return localeDate;
	}

	public void setLocaleDate(String localeDate) {
		this.localeDate = localeDate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public String getAddress() {
		return address;
	}

	public int getSecondsToDrive() {
		return secondsToDrive;
	}

	public void setSecondsToDrive(int secondsToDrive) {
		this.secondsToDrive = secondsToDrive;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPersonsBefore() {
		return personsBefore;
	}

	public void setPersonsBefore(int personsBefore) {
		this.personsBefore = personsBefore;
	}

	public void fromJsonData(JSONObject jsonAppointment) {
		try {
			JSONObject medic = jsonAppointment.getJSONObject("medic");

			final String name = medic.getString("title") + " " + medic.getString("prename") + " " + medic.getString("name");
			final String jsonDate = jsonAppointment.getString("start");

			SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

			final Date date = parserSDF.parse(jsonDate);
			final String localeDate = date.toLocaleString();

			this.setId(jsonAppointment.getInt("id"));
			this.setDate(date);
			this.setLocaleDate(localeDate);
			this.setName(name);
			this.setAddress(medic.getString("address"));

			//this.setPersonsBefore(medic.getInt("waiting_persons"));

			this.setJsonData(jsonAppointment.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void updateLocation(Context context, String mode) {
		// Get the location manager
		final LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		// Define the criteria how to select the location provider -> use
		// default
		Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));

		if(location == null || address == null)
			return;

		String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + location.getLatitude() + "," + location.getLongitude() + "&destination=" + URLEncoder.encode(address) + "&sensor=true&avoid=highways&mode="+mode;

		Log.i(Config.LOG_TAG, "url: " + url);

		Util.getJSON(url,
				new Handler(new Handler.Callback() {
					@Override
					public boolean handleMessage(Message msg) {
						try {
							JSONObject json = new JSONObject(msg.getData().getString("json"));
							secondsToDrive = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						return false;
					}
				}));
	}
}
