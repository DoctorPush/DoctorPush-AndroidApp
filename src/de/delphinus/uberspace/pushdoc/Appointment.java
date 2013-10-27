package de.delphinus.uberspace.pushdoc;

import org.json.JSONException;
import org.json.JSONObject;

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
	private String name;
	private String localeDate;
	private Date date;
	private String jsonData;

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

	public void fromJsonData(JSONObject jsonAppointment) {
		try {
			JSONObject medic = jsonAppointment.getJSONObject("medic");

			final String name = medic.getString("title") + " " + medic.getString("prename") + " " + medic.getString("name");
			final String jsonDate = jsonAppointment.getString("start");

			SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

			final Date date = parserSDF.parse(jsonDate);
			final String localeDate = date.toLocaleString();

			this.setDate(date);
			this.setLocaleDate(localeDate);
			this.setName(name);
			this.setJsonData(jsonAppointment.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
