package de.delphinus.uberspace.pushdoc;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 26.10.13
 */
public class AppointmentArrayAdapter extends ArrayAdapter<JSONObject> {

	private final Context context;
	private final JSONObject[] jsonAppointments;

	public AppointmentArrayAdapter(Context context, int textViewResourceId, JSONObject[] objects) {
		super(context, textViewResourceId, objects);

		this.context = context;
		this.jsonAppointments = objects;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.appointment_row, parent, false);
		final TextView nameView = (TextView) rowView.findViewById(R.id.firstLine);
		final TextView addressView = (TextView) rowView.findViewById(R.id.secondLine);

		try {
			JSONObject medic = jsonAppointments[position].getJSONObject("medic");

			final String name = medic.getString("title") + " " + medic.getString("prename") + " " + medic.getString("name");
			final String jsonDate = jsonAppointments[position].getString("start");

			SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

			final String localeDate = parserSDF.parse(jsonDate).toLocaleString();

			nameView.setText(name);
			addressView.setText(localeDate);

		} catch (Exception e) {
			Log.i(Config.LOG_TAG, e.toString());
			e.printStackTrace();
		}

		return rowView;
	}
}
