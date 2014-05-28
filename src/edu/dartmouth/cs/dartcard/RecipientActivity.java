package edu.dartmouth.cs.dartcard;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.dartmouth.cs.dartcard.DartCardDialogFragment.DialogExitListener;

public class RecipientActivity extends Activity implements DialogExitListener {

	// private static final String VALIDITY_KEY = "VALIDITY_KEY";
	private static final String NAME_KEY = "NAME_KEY";
	private static final String STREET1_KEY = "ADDRESS1_KEY";
	private static final String STREET2_KEY = "ADDRESS2_KEY";
	private static final String CITY_KEY = "CITY_KEY";
	private static final String STATE_KEY = "STATE_KEY";
	private static final String ZIP_KEY = "ZIP_KEY";
	private static final String MESSAGE_KEY = "MESSAGE_KEY";
	private static final String NUMRECIPIENTS_KEY = "NUMRECIPIENTS_KEY";

	// private ArrayList<Boolean> mValidities;
	private ArrayList<TextView> mRecipientFields;
	private ArrayList<EditText> mNameFields;
	private ArrayList<EditText> mStreet1Fields;
	private ArrayList<EditText> mStreet2Fields;
	private ArrayList<EditText> mCityFields;
	private ArrayList<EditText> mStateFields;
	private ArrayList<EditText> mZipFields;
	private ArrayList<EditText> mMessageFields;
	private ArrayList<EditText> mLabelFields;
	private ArrayList<Button> mSaveButtons;
	private ArrayList<Spinner> mSpinners;
	private ArrayList<ArrayAdapter<String>> mAdapters;

	
	private long currentId;

	private Button mNextButton;
	private Button mAddAnotherButton;

	private int mNumRecipients;

	private String mGenericMessage;

	private ProgressDialog mProgressDialog;

	private String mMessage;

	private Bundle mSavedInstanceState;
	private ActionBar mActionBar;

	private RecipientAddressDbHelper mHelper;
	private ArrayList<Recipient> mRecipientList;
	private List<String> mSpinnerArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipient);

		mMessage = getIntent().getStringExtra(
				getString(R.string.message_activity_intent_key));

		mSavedInstanceState = savedInstanceState;

		// This line prevents the focus from automatically going into the
		// recipient's
		// text fields when they're created
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mNameFields = new ArrayList<EditText>();
		mStreet1Fields = new ArrayList<EditText>();
		mStreet2Fields = new ArrayList<EditText>();
		mCityFields = new ArrayList<EditText>();
		mStateFields = new ArrayList<EditText>();
		mZipFields = new ArrayList<EditText>();
		mMessageFields = new ArrayList<EditText>();
		mRecipientFields = new ArrayList<TextView>();
		mLabelFields = new ArrayList<EditText>();
		mSaveButtons = new ArrayList<Button>();
		mSpinners = new ArrayList<Spinner>();
		mAdapters = new ArrayList<ArrayAdapter<String>>();

		currentId = -1;
		
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);

		mActionBar.setDisplayShowHomeEnabled(false);

		mNextButton = (Button) findViewById(R.id.ui_recipient_activity_nextButton);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextClicked(v);
			}
		});

		mAddAnotherButton = (Button) findViewById(R.id.ui_recipient_activity_addanother_button);
		mAddAnotherButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddAnotherClicked(v);
			}
		});

		mNumRecipients = 0;

		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			mGenericMessage = bundle
					.getString(getString(R.string.message_activity_intent_key));
		} else {
			mGenericMessage = "";
		}

		createRecipientView(-1);
	}
	public void enableEditTexts(int pos) {
		mNameFields.get(pos).setEnabled(true);
		mStreet1Fields.get(pos).setEnabled(true);
		mStreet2Fields.get(pos).setEnabled(true);
		mCityFields.get(pos).setEnabled(true);
		mStateFields.get(pos).setEnabled(true);
		mZipFields.get(pos).setEnabled(true);
		mLabelFields.get(pos).setEnabled(true);
	}


	public void disableEditTexts(int pos) {
		mNameFields.get(pos).setEnabled(false);
		mNameFields.get(pos).setTextColor(Color.WHITE);
		mStreet1Fields.get(pos).setEnabled(false);
		mStreet1Fields.get(pos).setTextColor(Color.WHITE);
		mStreet2Fields.get(pos).setEnabled(false);
		mStreet2Fields.get(pos).setTextColor(Color.WHITE);
		mCityFields.get(pos).setEnabled(false);
		mCityFields.get(pos).setTextColor(Color.WHITE);
		mStateFields.get(pos).setEnabled(false);
		mStateFields.get(pos).setTextColor(Color.WHITE);
		mZipFields.get(pos).setEnabled(false);
		mZipFields.get(pos).setTextColor(Color.WHITE);
		mLabelFields.get(pos).setEnabled(false);
		mLabelFields.get(pos).setTextColor(Color.WHITE);
	}

	private void resetFields(int j) {
		enableEditTexts(j);
		currentId = -1;
		mNameFields.get(j).setText("");
		mStreet1Fields.get(j).setText("");
		mStreet2Fields.get(j).setText("");
		mCityFields.get(j).setText("");
		mStateFields.get(j).setText("");
		mZipFields.get(j).setText("");
		mLabelFields.get(j).setText("");
		Button saveButton = mSaveButtons.get(j);
		saveButton.setText("Save");
		
		Spinner spinner = mSpinners.get(j);
		//int position = spinner.getSelectedItemPosition();
		ArrayAdapter<String> adapter = mAdapters.get(j);
		adapter.remove(spinner.getSelectedItem().toString());
		spinner.setSelection(0);
		spinner.setAdapter(adapter);
	}
	private void setupSpinner(int i) {
		final int j = i;
		mSpinners.get(j).setOnItemSelectedListener(
				new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						position -= 1;
						// mPosition = position + 1;
						Button saveButton;
						if (position == -1) {
							
							// if (mAddressSelected == true) {
							// Log.d("TAG", "setting all shit to zero!");
							// mAddressSelected = false;
							enableEditTexts(j);
							currentId = -1;
							mNameFields.get(j).setText("");
							mStreet1Fields.get(j).setText("");
							mStreet2Fields.get(j).setText("");
							mCityFields.get(j).setText("");
							mStateFields.get(j).setText("");
							mZipFields.get(j).setText("");
							mLabelFields.get(j).setText("");
							saveButton = mSaveButtons.get(j);
							saveButton.setText("Save");
						} else {
							Recipient recip = mRecipientList.get(position);
							currentId = recip.getId();
							mNameFields.get(j).setText(recip.getName());
							mStreet1Fields.get(j).setText(recip.getStreet1());
							mStreet2Fields.get(j).setText(recip.getStreet2());
							mCityFields.get(j).setText(recip.getCity());
							mStateFields.get(j).setText(recip.getState());
							mZipFields.get(j).setText("" + recip.getZip());
							mLabelFields.get(j).setText(recip.getLabel());
							disableEditTexts(j);
							saveButton = mSaveButtons.get(j);
							saveButton.setText("Delete");
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
	}

	private void onNextClicked(View v) {
		boolean messageValid = true;
		ArrayList<Recipient> recipients = getRecipients();
		for (Recipient recipient : recipients) {
			if (!MessageActivity.isMessageValid(recipient.getMessage())) {
				DartCardDialogFragment frag = DartCardDialogFragment
						.newInstance(Globals.DIALOG_MESSAGE_ERRORS);
				frag.show(this.getFragmentManager(), "message error dialog");
				messageValid = false;
				break;
			}
		}

		if (messageValid) {
			mNextButton.setEnabled(false);
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle("Checking your addresses");
			mProgressDialog.setMessage("This should take only a second");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.show();

			LobVerifyTask lob_task = new LobVerifyTask(this, recipients);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				lob_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						(Void[]) null);
			else
				lob_task.execute((Void[]) null);
		}
	}

	private ArrayList<Recipient> getRecipients() {
		ArrayList<Recipient> recipients = new ArrayList<Recipient>();

		for (int i = 0; i < mNameFields.size(); i++) {
			Recipient recipient = new Recipient(mNameFields.get(i).getText()
					.toString(), mStreet1Fields.get(i).getText().toString(),
					mStreet2Fields.get(i).getText().toString(), mCityFields
							.get(i).getText().toString(), mStateFields.get(i)
							.getText().toString(), mZipFields.get(i).getText()
							.toString(), mMessageFields.get(i).getText()
							.toString());
			Log.d("recipientactivity", "before validate");

			recipients.add(recipient);
		}

		return recipients;
	}

	private void onAddAnotherClicked(View v) {
		createRecipientView(-1);
	}

	public void createRecipientView(int num) {
		if (num == -1) {
			mNumRecipients++;
		}
		Log.d("TAG", "creating recipient " + num);
		LinearLayout parentLayout = (LinearLayout) findViewById(R.id.ui_recipient_activity_linearlayout);

		Spinner recipSpinner = new Spinner(this);
		mSpinnerArray = new ArrayList<String>();
		mHelper = new RecipientAddressDbHelper(this);
		mRecipientList = mHelper.fetchAddresses();
		if (mRecipientList.size() != 0) {
			mSpinnerArray.add("New address");
			for (Recipient recipient : mRecipientList) {
				mSpinnerArray.add(recipient.getLabel());
			}
		} else {
			mSpinnerArray.add("None saved");
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mSpinnerArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		recipSpinner.setAdapter(adapter);
		mAdapters.add(adapter);
		if (mSavedInstanceState != null) {
			Log.d("TAG", "got " + mSavedInstanceState.getInt("spinnerPos", 0)
					+ " from bundle");
			recipSpinner.setSelection(mSavedInstanceState.getInt("spinnerPos",
					0));
		}

		mSpinners.add(recipSpinner);

		// First, create the header textview
		TextView recipientHeader = new TextView(this);
		if (num == -1) {
			recipientHeader.setText("Recipient " + mNumRecipients + ":");
			//want num to be one less than num recipients now, for reference into arrays
			num = mNumRecipients - 1;
		} else {
			recipientHeader.setText("Recipient " + (num + 1) + ":");
		}

		mRecipientFields.add(recipientHeader);

		// Second, create the inner linear layout for the recipient's
		// information
		LinearLayout recipientLayout = new LinearLayout(this);
		recipientLayout.setOrientation(LinearLayout.VERTICAL);

		EditText nameField = new EditText(this);
		nameField
				.setHint(getString(R.string.ui_recipient_activity_enterName_hint));
		nameField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		mNameFields.add(nameField);

		EditText street1Field = new EditText(this);
		street1Field
				.setHint(getString(R.string.ui_recipient_activity_enterAddress1_hint));
		street1Field.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		mStreet1Fields.add(street1Field);

		EditText street2Field = new EditText(this);
		street2Field
				.setHint(getString(R.string.ui_recipient_activity_enterAddress2_hint));
		street2Field.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		mStreet2Fields.add(street2Field);

		EditText cityField = new EditText(this);
		cityField
				.setHint(getString(R.string.ui_recipient_activity_enterCity_hint));
		cityField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		mCityFields.add(cityField);

		EditText stateField = new EditText(this);
		stateField
				.setHint(getString(R.string.ui_recipient_activity_enterState_hint));
		stateField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		stateField.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				2) });
		mStateFields.add(stateField);

		EditText zipField = new EditText(this);
		zipField.setHint(getString(R.string.ui_recipient_activity_enterZip_hint));
		zipField.setInputType(InputType.TYPE_CLASS_NUMBER);
		zipField.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });
		mZipFields.add(zipField);

		EditText labelField = new EditText(this);
		labelField.setHint("Label, if saving recipient");
		labelField.setInputType(InputType.TYPE_CLASS_TEXT);
		mLabelFields.add(labelField);

		Button saveButton = new Button(this);
		saveButton.setText("Save");
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 onSaveClicked(v);
			}
		});
		mSaveButtons.add(saveButton);

		LimitedEditText messageField = new LimitedEditText(this);
		messageField.setMaxTextSize(350);
		messageField.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		messageField.setText(mGenericMessage);
		mMessageFields.add(messageField);
		
		setupSpinner(num);

		messageField.setText(mMessage);
		Log.d("TAG", "added all saved text");

		// Add all the text fields
		recipientLayout.addView(recipSpinner);
		recipientLayout.addView(nameField);
		recipientLayout.addView(street1Field);
		recipientLayout.addView(street2Field);
		recipientLayout.addView(cityField);
		recipientLayout.addView(stateField);
		recipientLayout.addView(zipField);
		recipientLayout.addView(labelField);
		recipientLayout.addView(saveButton);
		recipientLayout.addView(messageField);

		// Finally, add both the header and the linear layout to the parent view
		parentLayout.addView(recipientHeader);
		parentLayout.addView(recipientLayout);
	}

	private void onSaveClicked(View v) {
		RecipientAddressDbHelper helper = new RecipientAddressDbHelper(this);
		Recipient recip = new Recipient();
		int counter = 0;
		String text = null;
		Button clickedButton = null;
		for (Button but : mSaveButtons) {
			if (but == v) {
				counter = mSaveButtons.indexOf(but);
				text = but.getText().toString();
				clickedButton = but;
			}
		}
		if (text.equals("Save")) {
			if ((mNameFields.get(counter).getText().toString().length() >= 1)) {
				recip.setName(mNameFields.get(counter).getText().toString());
			} else {
				Toast.makeText(getApplicationContext(), "Name is blank.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if ((mStreet1Fields.get(counter).getText().toString().length() >= 1)) {
				recip.setStreet1(mStreet1Fields.get(counter).getText().toString());
			} else {
				Toast.makeText(getApplicationContext(),
						"Address Line One is blank.", Toast.LENGTH_SHORT).show();
				return;
			}
			if ((mStreet2Fields.get(counter).getText().toString().length() >= 1)) {
				recip.setStreet2(mStreet2Fields.get(counter).getText().toString());
			} else {
				recip.setStreet2(" ");
			}
			if ((mCityFields.get(counter).getText().toString().length() >= 1)) {
				recip.setCity(mCityFields.get(counter).getText().toString());
			} else {
				Toast.makeText(getApplicationContext(), "City is blank.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if ((mStateFields.get(counter).getText().toString().length() >= 1)) {
				recip.setState(mStateFields.get(counter).getText().toString());
			} else {
				Toast.makeText(getApplicationContext(), "State is blank.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if ((mZipFields.get(counter).getText().toString().length() >= 1)) {
				recip.setZip(mZipFields.get(counter).getText().toString());
			} else {
				Toast.makeText(getApplicationContext(), "Zip is blank.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			if ((mLabelFields.get(counter).getText().toString().length() >= 1)) {
				recip.setLabel(mLabelFields.get(counter).getText().toString());
			} else {
				Toast.makeText(getApplicationContext(), "Address label is blank.",
						Toast.LENGTH_SHORT).show();
				return;
			}
			long id = helper.insertAddress(recip);
			recip.setId(id);
			Log.d("TAG", recip.toString());
			Toast.makeText(
					getApplicationContext(),
					"Saved address \""
							+ mLabelFields.get(counter).getText().toString() + "\"",
					Toast.LENGTH_SHORT).show();
		}
		else if (-1 != currentId) {
			helper.removeAddress(currentId);
			resetFields(counter);
		}
	}

	public class LobVerifyTask extends
			AsyncTask<Void, Void, ArrayList<Boolean>> {
		private Activity activity;
		private ArrayList<Recipient> recipients;

		public LobVerifyTask(Activity activity, ArrayList<Recipient> recipients) {
			this.activity = activity;
			this.recipients = recipients;
		}

		@Override
		protected ArrayList<Boolean> doInBackground(Void... params) {
			ArrayList<Boolean> results = new ArrayList<Boolean>();

			for (int i = 0; i < recipients.size(); i++) {
				results.add(i, LobUtilities.verifyAddress(recipients.get(i)
						.toAddressMap()));
			}
			return results;
		}

		@Override
		protected void onPostExecute(ArrayList<Boolean> results) {
			if (results.contains(false)) {
				if (mProgressDialog != null)
					mProgressDialog.dismiss();
				DartCardDialogFragment frag = DartCardDialogFragment
						.newInstance(Globals.DIALOG_RECIPIENT_ERRORS);
				frag.show(activity.getFragmentManager(), "recipient dialog");

				for (int i = 0; i < results.size(); i++) {
					// If the address is incorrect
					if (!results.get(i)) {
						TextView field = mRecipientFields.get(i);
						field.setTextAppearance(activity, R.style.boldText);
						// mValidities.set(i, false);
					} else {
						TextView field = mRecipientFields.get(i);
						field.setTextAppearance(activity, R.style.normalText);
					}
				}

				mNextButton.setEnabled(true);
			} else {
				Intent intent = new Intent(activity, PayActivity.class);
				if (null != recipients) {
					Log.d("here", "passing in values");
					intent.putParcelableArrayListExtra(
							getString(R.string.recipient_activity_intent_key),
							recipients);
					// Pass along the from address
					intent.putExtra(
							getString(R.string.from_activity_intent_key),
							getIntent()
									.getExtras()
									.getParcelable(
											getString(R.string.from_activity_intent_key)));

					activity.startActivity(intent);
				}

			}
		}
	}

	@Override
	public void onSavePhotoExit(boolean savePhoto) {
	}

	@Override
	public void onTrySaveAgainExit(boolean tryAgain) {
	}

	@Override
	public void onReturn() {
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

}
