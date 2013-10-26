package de.delphinus.uberspace.pushdoc.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import de.delphinus.uberspace.pushdoc.R;

/**
 * DoctorPush
 *
 * @author Shivan Taher <zn31415926535@gmail.com>
 * @date 26.10.13
 */
public class PhoneNumberActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_phone_number);

		final Button button = (Button) findViewById(R.id.submitPhoneNumberButton);
		final EditText numberText = (EditText) findViewById(R.id.phoneNumberText);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String phoneNumber = numberText.getText().toString();

				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				intent.putExtra("phoneNumber", phoneNumber);

				startActivity(intent);

			}
		});

	}
}