package edu.dartmouth.cs.dartcard;

import edu.dartmouth.cs.dartcard.DartCardDialogFragment.DialogExitListener;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.widget.LinearLayout.LayoutParams;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MessageActivity extends Activity implements DialogExitListener {
	private LimitedEditText mMessageField;
	
	private ActionBar mActionBar;

	private Button mNextButton;
	
	private String mMessageString = "";
	private static final String MESSAGE_KEY = "MESSAGE_KEY";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		mActionBar = getActionBar();
		mActionBar.setTitle("DartCard");
		
		if (savedInstanceState != null) {
			mMessageString = savedInstanceState.getString(MESSAGE_KEY);
		}

		mMessageField = (LimitedEditText) findViewById(R.id.ui_message_activity_enterMessage);
		mMessageField.setText(mMessageString);
		mMessageField.setMaxTextSize(350);
		mMessageField.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		mNextButton = (Button) findViewById(R.id.ui_message_activity_nextButton);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextClicked(v);
			}	
		});

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the image capture uri before the activity goes into background
		outState.putString(MESSAGE_KEY, mMessageString);
	}
	
	@Override
    protected void onPause() {
		super.onPause();
		mMessageString = mMessageField.getText().toString();
	}
	
		
	
	public void onNextClicked(View v) {
		String message = mMessageField.getText().toString();
		if (isMessageValid(message)){
			Intent intent = new Intent(this, RecipientActivity.class);
			//Pass along message
			intent.putExtra(getString(R.string.message_activity_intent_key),
					message);
			//Pass along the from address
			intent.putExtra(getString(R.string.from_activity_intent_key),
					getIntent().getExtras().getParcelable(getString(R.string.from_activity_intent_key)));
	
			startActivity(intent);
		}
		else{
			DartCardDialogFragment frag = DartCardDialogFragment
					.newInstance(Globals.DIALOG_MESSAGE_ERRORS);
			frag.show(this.getFragmentManager(), "message error dialog");
		}
	}
	
	//Method to check if a message is valid, based upon not being empty, not having any words over
	//28 characters, and not having too many lines
	public static boolean isMessageValid(String message){
		if (message==null || message.isEmpty())
			return false;
		
		String[] words = message.split(" ");
		for (String word : words){
			if (word.length() > 27){
				return false;
			}
		}
		
		int newCounter = 0;
		int i=0;
		while (i < message.length()) {
		       if (message.charAt(i) == '\n') {
		              newCounter++;
		       }
		       i++;
		}
		
		if (newCounter > 10) {
		  return false;
		}
		
		return true;
	}

	@Override
	public void onSavePhotoExit(boolean savePhoto) {}

	@Override
	public void onTrySaveAgainExit(boolean tryAgain) {}

	@Override
	public void onReturn() {}
}