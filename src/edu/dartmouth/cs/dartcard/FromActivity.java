package edu.dartmouth.cs.dartcard;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class FromActivity extends Activity {
	private EditText mNameField;
	private EditText mAddress1Field;
	private EditText mAddress2Field;
	private EditText mCityField;
	private EditText mStateField;
	private EditText mZipField;
	
	private ActionBar mActionBar;

	private Button mNextButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_from);
		
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		
		mNameField = (EditText) findViewById(R.id.ui_from_activity_enterName);
		mAddress1Field = (EditText) findViewById(R.id.ui_from_activity_enterAddress1);
		mAddress2Field = (EditText) findViewById(R.id.ui_from_activity_enterAddress2);
		mCityField = (EditText) findViewById(R.id.ui_from_activity_enterCity);
		mStateField = (EditText) findViewById(R.id.ui_from_activity_enterState);
		mZipField = (EditText) findViewById(R.id.ui_from_activity_enterZip);

		mNextButton = (Button) findViewById(R.id.ui_from_activity_nextButton);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextClicked(v);
			}	
		});

	}


	
	public void onNextClicked(View v) {
		Recipient recipient = new Recipient(mNameField.getText().toString(), 
				mAddress1Field.getText().toString(), mAddress2Field.getText().toString(),
				mCityField.getText().toString(), mStateField.getText().toString(),
				mZipField.getText().toString());
		
		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(getString(R.string.from_activity_intent_key), recipient);		

		startActivity(intent);
	}


}
