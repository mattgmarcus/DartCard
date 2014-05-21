package edu.dartmouth.cs.dartcard;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecipientActivity extends Activity {
	private ArrayList<EditText> mNameFields;
	private ArrayList<EditText> mStreet1Fields;
	private ArrayList<EditText> mStreet2Fields;
	private ArrayList<EditText> mCityFields;
	private ArrayList<EditText> mStateFields;
	private ArrayList<EditText> mZipFields;
	private ArrayList<EditText> mMessageFields;
	
	private ActionBar mActionBar;

	private Button mNextButton;
	private Button mAddAnotherButton;
	
	private int mNumRecipients;
	
	private String mGenericMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recipient);
		
		//This line prevents the focus from automatically going into the recipient's
		//text fields when they're created
	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mNameFields = new ArrayList<EditText>();
		mStreet1Fields = new ArrayList<EditText>();
		mStreet2Fields = new ArrayList<EditText>();
		mCityFields = new ArrayList<EditText>();
		mStateFields = new ArrayList<EditText>();
		mZipFields = new ArrayList<EditText>();
		mMessageFields = new ArrayList<EditText>();
	    
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		//mActionBar.setTitle("Enter your personal information");
		
		mNextButton = (Button) findViewById(R.id.ui_recipient_activity_nextButton);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextClicked(v);
			}	
		});
		
		mAddAnotherButton = 
				(Button) findViewById(R.id.ui_recipient_activity_addanother_button);
		mAddAnotherButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddAnotherClicked(v);
			}
		});
		
		mNumRecipients = 0;

		
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			mGenericMessage = bundle.getString(getString(R.string.message_activity_intent_key));
		}		
		else {
			mGenericMessage = "";
		}
		
		createRecipientView();

	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

	
	private void onNextClicked(View v) {
		Intent intent = new Intent(this, PayActivity.class);
		ArrayList<Recipient> recipients = getRecipients();
		intent.putParcelableArrayListExtra(
				getString(R.string.recipient_activity_intent_key), recipients);
		startActivity(intent);
	}
	
	private ArrayList<Recipient> getRecipients() {
		ArrayList<Recipient> recipients = new ArrayList<Recipient>();
		
		for (int i = 0; i < mNameFields.size(); i++) {
			Recipient recipient = new Recipient(mNameFields.get(i).getText().toString(),
					mStreet1Fields.get(i).getText().toString(),
					mStreet2Fields.get(i).getText().toString(),
					mCityFields.get(i).getText().toString(),
					mStateFields.get(i).getText().toString(),
					mZipFields.get(i).getText().toString(),
					mMessageFields.get(i).getText().toString());
			recipients.add(recipient);
		}
		
		return recipients;
	}
	
	private void onAddAnotherClicked(View v) {
		createRecipientView();
	}	


	public void createRecipientView() {
		mNumRecipients++;
		
		LinearLayout parentLayout = 
				(LinearLayout) findViewById(R.id.ui_recipient_activity_linearlayout);
		
		//First, create the header textview
		TextView recipientHeader = new TextView(this);
		recipientHeader.setText("Recipient " + mNumRecipients + ":");
		
		//Second, create the inner linear layout for the recipient's information
		LinearLayout recipientLayout = new LinearLayout(this);
		recipientLayout.setOrientation(LinearLayout.VERTICAL);

		EditText nameField = new EditText(this);
		nameField.setHint(getString(R.string.ui_recipient_activity_enterName_hint));
		mNameFields.add(nameField);
		//nameField.setId(R.id.recipient_name);
		
		EditText street1Field = new EditText(this);
		street1Field.setHint(getString(R.string.ui_recipient_activity_enterAddress1_hint));
		mStreet1Fields.add(street1Field);

		EditText street2Field = new EditText(this);
		street2Field.setHint(getString(R.string.ui_recipient_activity_enterAddress2_hint));
		mStreet2Fields.add(street2Field);

		EditText cityField = new EditText(this);
		cityField.setHint(getString(R.string.ui_recipient_activity_enterCity_hint));
		mCityFields.add(cityField);

		EditText stateField = new EditText(this);
		stateField.setHint(getString(R.string.ui_recipient_activity_enterState_hint));
		mStateFields.add(stateField);

		EditText zipField = new EditText(this);
		zipField.setHint(getString(R.string.ui_recipient_activity_enterZip_hint));
		mZipFields.add(zipField);

		EditText messageField = new EditText(this);
		messageField.setText(mGenericMessage);
		mMessageFields.add(messageField);

		//Add all the text fields
		recipientLayout.addView(nameField);
		recipientLayout.addView(street1Field);
		recipientLayout.addView(street2Field);
		recipientLayout.addView(cityField);
		recipientLayout.addView(stateField);
		recipientLayout.addView(zipField);
		recipientLayout.addView(messageField);

		//Finally, add both the header and the linear layout to the parent view
		parentLayout.addView(recipientHeader);
		parentLayout.addView(recipientLayout);
	}

}
