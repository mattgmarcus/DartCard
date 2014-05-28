package edu.dartmouth.cs.dartcard;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class MessageActivity extends Activity {
	private LimitedEditText mMessageField;
	
	private RelativeLayout mLayout;
	
	private Button mNextButton;
	
	private ActionBar mActionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		mLayout = (RelativeLayout) findViewById(R.id.ui_message_relative_layout);
		
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);

		mMessageField = new LimitedEditText(this);
		mMessageField.setMaxTextSize(350);
		mMessageField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		mMessageField.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1000));
		mMessageField.setHint("Enter a message");
		mLayout.addView(mMessageField);

		mNextButton = (Button) findViewById(R.id.ui_message_activity_nextButton);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextClicked(v);
			}	
		});

	}
	
	
	
	public void onNextClicked(View v) {
		Intent intent = new Intent(this, RecipientActivity.class);
		intent.putExtra(getString(R.string.message_activity_intent_key),
				mMessageField.getText().toString());
		//Pass along the from address
		intent.putExtra(getString(R.string.from_activity_intent_key),
				getIntent().getExtras().getParcelable(getString(R.string.from_activity_intent_key)));

		startActivity(intent);
	}

}
