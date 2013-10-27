package de.delphinus.uberspace.pushdoc.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import de.delphinus.uberspace.pushdoc.Appointment;
import de.delphinus.uberspace.pushdoc.R;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 27.10.13
 */
public class DetailActivity extends FragmentActivity {
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		Appointment appointment = new Appointment();

		try {
			appointment.fromJsonData(new JSONObject(extras.getString("jsonAppointment")));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		setContentView(R.layout.activity_appointment_details);

		final TextView address = (TextView) findViewById(R.id.location);
		final TextView name = (TextView) findViewById(R.id.appointmentName);
		final TextView date = (TextView) findViewById(R.id.appointmentDate);
		final ImageButton navigateButton = (ImageButton) findViewById(R.id.mapsButton);

		address.setText(appointment.getAddress());
		name.setText(appointment.getName());
		date.setText(appointment.getLocaleDate());

		navigateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?q=" + address.getText())
				);
				startActivity(intent);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}