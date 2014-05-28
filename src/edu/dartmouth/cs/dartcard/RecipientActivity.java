package edu.dartmouth.cs.dartcard;

import java.io.IOException;
import java.util.ArrayList;

import edu.dartmouth.cs.dartcard.DartCardDialogFragment.DialogExitListener;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract.Document;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecipientActivity extends Activity implements DialogExitListener {

	private static final String NAME_KEY = "NAME_KEY";
	private static final String STREET1_KEY = "ADDRESS1_KEY";
	private static final String STREET2_KEY = "ADDRESS2_KEY";
	private static final String CITY_KEY = "CITY_KEY";
	private static final String STATE_KEY = "STATE_KEY";
	private static final String ZIP_KEY = "ZIP_KEY";
	private static final String MESSAGE_KEY = "MESSAGE_KEY";
	private static final String NUMRECIPIENTS_KEY = "NUMRECIPIENTS_KEY";

	private ArrayList<TextView> mRecipientFields;
	private ArrayList<EditText> mNameFields;
	private ArrayList<EditText> mStreet1Fields;
	private ArrayList<EditText> mStreet2Fields;
	private ArrayList<EditText> mCityFields;
	private ArrayList<EditText> mStateFields;
	private ArrayList<EditText> mZipFields;
	private ArrayList<EditText> mMessageFields;

	// These variables deal with formatting the messages box
	private LayoutParams mMessageParams;

	private Button mNextButton;
	private Button mAddAnotherButton;

	private int mNumRecipients;

	private String mGenericMessage;

	private ProgressDialog mProgressDialog;

	private String mMessage;

	private Bundle mSavedInstanceState;
	private ActionBar mActionBar;

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

		mMessageParams = new LayoutParams(LayoutParams.FILL_PARENT, 500);

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

		if (mSavedInstanceState != null) {
			mNumRecipients = mSavedInstanceState.getInt(NUMRECIPIENTS_KEY);
			Log.d("TAG", "numRecipients is " + mNumRecipients);
			for (int i = 0; i < mNumRecipients; i++) {
				Log.d("TAG", "in loop, about to create recipient " + i);
				createRecipientView(i);
			}
			mSavedInstanceState = null;
		} else {
			createRecipientView(-1);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d("TAG", "in onSaveInstanceState");
		for (int i = 0; i < mNumRecipients; i++) {
			outState.putString(NAME_KEY + i, mNameFields.get(i).getText()
					.toString());
			Log.d("TAG", "saving name "
					+ mNameFields.get(i).getText().toString());
			outState.putString(STREET1_KEY + i, mStreet1Fields.get(i).getText()
					.toString());
			Log.d("TAG", "saving street "
					+ mStreet1Fields.get(i).getText().toString());
			outState.putString(STREET2_KEY + i, mStreet2Fields.get(i).getText()
					.toString());
			outState.putString(CITY_KEY + i, mCityFields.get(i).getText()
					.toString());
			outState.putString(STATE_KEY + i, mStateFields.get(i).getText()
					.toString());
			if (mZipFields.get(i).getText().toString().length() > 0) {
				outState.putString(ZIP_KEY + i, mZipFields.get(i).getText()
						.toString());
			}
			Log.d("TAG", "saving zip " + mZipFields.get(i).getText().toString());
			outState.putString(MESSAGE_KEY + i, mMessageFields.get(i).getText()
					.toString());
		}
		outState.putInt(NUMRECIPIENTS_KEY, mNumRecipients);
	}

	private void onNextClicked(View v) {
		mNextButton.setEnabled(false);
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("Checking your addresses");
		mProgressDialog.setMessage("This should take only a second");
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.show();

		ArrayList<Recipient> recipients = getRecipients();

		LobVerifyTask lob_task = new LobVerifyTask(this, recipients);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			lob_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					(Void[]) null);
		else
			lob_task.execute((Void[]) null);
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

		// First, create the header textview
		TextView recipientHeader = new TextView(this);
		if (num == -1) {
			recipientHeader.setText("Recipient " + mNumRecipients + ":");
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
		// nameField.setId(R.id.recipient_name);

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

		LimitedEditText messageField = new LimitedEditText(this);
		messageField.setMaxTextSize(350);
		messageField.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		messageField.setText(mGenericMessage);
		messageField.setLayoutParams(mMessageParams);
		mMessageFields.add(messageField);

		if (mSavedInstanceState != null) {
			nameField.setText(mSavedInstanceState.getString(NAME_KEY + num)
					.toString());
			Log.d("TAG",
					"setting name "
							+ mSavedInstanceState.getString(NAME_KEY + num)
									.toString());
			street1Field.setText(mSavedInstanceState.getString(
					STREET1_KEY + num).toString());
			Log.d("TAG",
					"setting street "
							+ mSavedInstanceState.getString(STREET1_KEY + num)
									.toString());
			street2Field.setText(mSavedInstanceState.getString(
					STREET2_KEY + num).toString());
			cityField.setText(mSavedInstanceState.getString(CITY_KEY + num)
					.toString());
			stateField.setText(mSavedInstanceState.getString(STATE_KEY + num)
					.toString());
			Log.d("TAG",
					"setting zip "
							+ mSavedInstanceState.getString(ZIP_KEY + num));
			zipField.setText((mSavedInstanceState.getString(ZIP_KEY + num)));
			messageField.setText(mSavedInstanceState.getString(
					MESSAGE_KEY + num).toString());
		} else {
			messageField.setText(mMessage);
		}
		Log.d("TAG", "added all saved text");

		// Add all the text fields
		recipientLayout.addView(nameField);
		recipientLayout.addView(street1Field);
		recipientLayout.addView(street2Field);
		recipientLayout.addView(cityField);
		recipientLayout.addView(stateField);
		recipientLayout.addView(zipField);
		recipientLayout.addView(messageField);

		// Finally, add both the header and the linear layout to the parent view
		parentLayout.addView(recipientHeader);
		parentLayout.addView(recipientLayout);
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
