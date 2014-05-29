package edu.dartmouth.cs.dartcard;

import java.util.ArrayList;
import java.util.List;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FromActivity extends Activity {
	private EditText mNameField;
	private EditText mAddress1Field;
	private EditText mAddress2Field;
	private EditText mCityField;
	private EditText mStateField;
	private EditText mZipField;
	private EditText mLabelField;

	private ActionBar mActionBar;

	private Button mNextButton;
	private Button mSaveButton;

	private AddressDBHelper mHelper;
	private Spinner mAddressSpinner;
	private ArrayList<Address> mAddressList;
	private List<String> mSpinnerArray;

	private boolean mAddressSelected;
	private int mPosition;
	private boolean mConfigurationChanged;

	private Bundle mSavedInstanceState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSavedInstanceState = savedInstanceState;
		setContentView(R.layout.activity_from);
		mActionBar = getActionBar();
		mActionBar.setTitle("DartCard");
		mNameField = (EditText) findViewById(R.id.ui_from_activity_enterName);
		mAddress1Field = (EditText) findViewById(R.id.ui_from_activity_enterAddress1);
		mAddress2Field = (EditText) findViewById(R.id.ui_from_activity_enterAddress2);
		mCityField = (EditText) findViewById(R.id.ui_from_activity_enterCity);
		mStateField = (EditText) findViewById(R.id.ui_from_activity_enterState);
		mZipField = (EditText) findViewById(R.id.ui_from_activity_enterZip);
		mLabelField = (EditText) findViewById(R.id.ui_from_activity_addressLabel);

		mNextButton = (Button) findViewById(R.id.ui_from_activity_nextButton);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextClicked(v);
			}
		});

		mSaveButton = (Button) findViewById(R.id.ui_from_activity_saveButton);
		mSaveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveClicked(v);
			}
		});
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("spinnerPos", mAddressSpinner.getSelectedItemPosition());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mConfigurationChanged = true;
	}

	public void onResume() {
		super.onResume();
		mAddressSelected = false;

		// load the saved addresses from the address database into
		// the spinner
		mSpinnerArray = new ArrayList<String>();
		mHelper = new AddressDBHelper(this);
		mAddressList = mHelper.fetchAddresses();
		if (mAddressList.size() != 0) {
			mSpinnerArray.add("New address");
			for (Address address : mAddressList) {
				mSpinnerArray.add(address.getLabel());
			}
		} else {
			mSpinnerArray.add("None saved");
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mSpinnerArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAddressSpinner = (Spinner) findViewById(R.id.address_spinner);
		mAddressSpinner.setAdapter(adapter);
		if (mSavedInstanceState != null) {
			mAddressSpinner.setSelection(mSavedInstanceState.getInt(
					"spinnerPos", 0));
		}
	}

	public void onStart() {
		super.onStart();
		// update address fields accordingly depending on which spinner
		// item is selected
		// clear fields if no saved address selected
		mAddressSpinner = (Spinner) findViewById(R.id.address_spinner);
		mAddressSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				position -= 1;
				mPosition = position + 1;
				if (position == -1) {
					if (mAddressSelected) {
						mAddressSelected = false;
						enableEditTexts();
						mNameField.setText("");
						mAddress1Field.setText("");
						mAddress2Field.setText("");
						mCityField.setText("");
						mStateField.setText("");
						mZipField.setText("");
						mLabelField.setText("");
						mSaveButton = (Button) findViewById(R.id.ui_from_activity_saveButton);
						mSaveButton.setText("Save");
					}
				} else {
					mAddressSelected = true;
					Address add = mAddressList.get(position);
					mNameField.setText(add.getName());
					mAddress1Field.setText(add.getLineOne());
					mAddress2Field.setText(add.getLineTwo());
					mCityField.setText(add.getCity());
					mStateField.setText(add.getState());
					mZipField.setText("" + add.getZip());
					mLabelField.setText(add.getLabel());
					disableEditTexts();
					mSaveButton = (Button) findViewById(R.id.ui_from_activity_saveButton);
					mSaveButton.setText("Delete");
				}
				mConfigurationChanged = false;
			}

			public void disableEditTexts() {
				mNameField.setEnabled(false);
				mNameField.setTextColor(Color.WHITE);
				mAddress1Field.setEnabled(false);
				mAddress1Field.setTextColor(Color.WHITE);
				mAddress2Field.setEnabled(false);
				mAddress2Field.setTextColor(Color.WHITE);
				mCityField.setEnabled(false);
				mCityField.setTextColor(Color.WHITE);
				mStateField.setEnabled(false);
				mStateField.setTextColor(Color.WHITE);
				mZipField.setEnabled(false);
				mZipField.setTextColor(Color.WHITE);
				mLabelField.setEnabled(false);
				mLabelField.setTextColor(Color.WHITE);
			}

			public void enableEditTexts() {
				mNameField.setEnabled(true);
				mAddress1Field.setEnabled(true);
				mAddress2Field.setEnabled(true);
				mCityField.setEnabled(true);
				mStateField.setEnabled(true);
				mZipField.setEnabled(true);
				mLabelField.setEnabled(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	//if all the fields have information, move on to message activity
	public void onNextClicked(View v) {
		if (mNameField.getText().toString().isEmpty()) {
			Toast.makeText(getApplicationContext(), "Name is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mAddress1Field.getText().toString().isEmpty()) {
			Toast.makeText(getApplicationContext(),
					"Address Line One is blank.", Toast.LENGTH_SHORT).show();
			return;
		}
		if (mCityField.getText().toString().isEmpty()) {
			Toast.makeText(getApplicationContext(), "City is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mStateField.getText().toString().isEmpty()) {
			Toast.makeText(getApplicationContext(), "State is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mZipField.getText().toString().isEmpty()) {
			Toast.makeText(getApplicationContext(), "Zip is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Recipient recipient = new Recipient(mNameField.getText().toString(),
				mAddress1Field.getText().toString(), mAddress2Field.getText()
						.toString(), mCityField.getText().toString(),
				mStateField.getText().toString(), mZipField.getText()
						.toString());

		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(getString(R.string.from_activity_intent_key), recipient);
		startActivity(intent);
	}

	//make an address object to be stored in the database, if all the fields
	//are correct, enter that address into the database and move to message activity
	public void onSaveClicked(View v) {
		AddressDBHelper helper = new AddressDBHelper(this);
		Address add = new Address();
		if (mAddressSelected) {
			for (Address address : mAddressList) {
				if (address.getLabel() == mSpinnerArray.get(mPosition)) {
					helper.removeAddress(address.getId());
					break;
				}
			}
			Toast.makeText(getApplicationContext(), "Address removed.",
					Toast.LENGTH_SHORT).show();
			finish();
			startActivity(getIntent());
			return;
		}
		if (!mNameField.getText().toString().isEmpty()) {
			add.setName(mNameField.getText().toString());
		} else {
			Toast.makeText(getApplicationContext(), "Name is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!mAddress1Field.getText().toString().isEmpty()) {
			add.setLineOne(mAddress1Field.getText().toString());
		} else {
			Toast.makeText(getApplicationContext(),
					"Address Line One is blank.", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!mAddress2Field.getText().toString().isEmpty()) {
			add.setLineTwo(mAddress2Field.getText().toString());
		} else {
			add.setLineTwo(" ");
		}
		if (!mCityField.getText().toString().isEmpty()) {
			add.setCity(mCityField.getText().toString());
		} else {
			Toast.makeText(getApplicationContext(), "City is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!mStateField.getText().toString().isEmpty()) {
			add.setState(mStateField.getText().toString());
		} else {
			Toast.makeText(getApplicationContext(), "State is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!mZipField.getText().toString().isEmpty()) {
			add.setZip(Integer.parseInt(mZipField.getText().toString()));
		} else {
			Toast.makeText(getApplicationContext(), "Zip is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (!mLabelField.getText().toString().isEmpty()) {
			add.setLabel(mLabelField.getText().toString());
		} else {
			Toast.makeText(getApplicationContext(), "Address label is blank.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		long id = helper.insertAddress(add);
		add.setId(id);
		Toast.makeText(getApplicationContext(),
				"Saved address \"" + mLabelField.getText().toString() + "\"",
				Toast.LENGTH_SHORT).show();

		Recipient recipient = new Recipient(mNameField.getText().toString(),
				mAddress1Field.getText().toString(), mAddress2Field.getText()
						.toString(), mCityField.getText().toString(),
				mStateField.getText().toString(), mZipField.getText()
						.toString());

		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(getString(R.string.from_activity_intent_key), recipient);

		startActivity(intent);
	}

}
