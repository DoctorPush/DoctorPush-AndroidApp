package de.delphinus.uberspace.pushdoc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.delphinus.uberspace.pushdoc.util.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 26.10.13
 */
public class AppointmentArrayAdapter extends ArrayAdapter<Appointment> {

	private final Context context;
	private final ArrayList<Appointment> appointments;

	public AppointmentArrayAdapter(Context context, int textViewResourceId, ArrayList<Appointment> objects) {
		super(context, textViewResourceId, objects);

		this.context = context;
		this.appointments = objects;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.appointment_row, parent, false);
		final TextView nameView = (TextView) rowView.findViewById(R.id.firstLine);
		final TextView addressView = (TextView) rowView.findViewById(R.id.secondLine);

		nameView.setText(appointments.get(position).getName());
		addressView.setText(appointments.get(position).getLocaleDate());

		return rowView;
	}

	@Override
	public void add(Appointment appointment){
		super.add(appointment);

		Collections.sort(this.appointments, new Comparator<Appointment>() {
			@Override
			public int compare(Appointment lhs, Appointment rhs) {
				return lhs.getDate().before(rhs.getDate()) ? -1  : 1;
			}
		});
	}

	public ArrayList<Appointment> getAppointments() {
		return appointments;
	}

	public Appointment getAppointmentById(final int id) {
		for(Appointment appointment : this.appointments) {
			if(id == appointment.getId())
				return appointment;
		}

		return null;
	}

	public void storeAppointments() {

		Preferences preferences = Preferences.getInstance();

		ArrayList<String> savedAppointments = preferences.getStringArrayPref(
				Config.PROPERTY_JSON_APPOINTMENTS
		);

		savedAppointments = new ArrayList<String>();

		for(Appointment appointment : this.appointments) {
			savedAppointments.add(appointment.getJsonData());
		}

		preferences.setStringArrayPref(Config.PROPERTY_JSON_APPOINTMENTS, savedAppointments);

	}

	public void removeAppointment(final int id) {
		for(int i=0; i<this.appointments.size(); i++) {
			if(id == this.appointments.get(i).getId()) {
				this.appointments.remove(i);
				this.notifyDataSetChanged();
				return;
			}
		}
	}

}
