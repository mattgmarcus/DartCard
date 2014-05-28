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
	
	private LinearLayout mLayout;
	
	private ActionBar mActionBar;

	private Button mNextButton;
	
	private String mMessageString = "";
	private static final String MESSAGE_KEY = "MESSAGE_KEY";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("TAG", "version is " + Build.VERSION.SDK_INT);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		
		mLayout = (LinearLayout) findViewById(R.id.ui_message_relative_layout);
		/*
		mMessageField = new LimitedEditText(this);
		mMessageField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		mMessageField.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.4f));
		mMessageField.setHint("Enter a message");
		*/
		Log.d("TAG", "mMessageString is " + mMessageString);
		if (savedInstanceState != null) {
			mMessageString = savedInstanceState.getString(MESSAGE_KEY);
		}
		Log.d("TAG", "now, mMessageString is " + mMessageString);
		mMessageField = (LimitedEditText) findViewById(R.id.ui_message_activity_enterMessage);
		mMessageField.setText(mMessageString);
		mMessageField.setMaxTextSize(350);
		//mLayout.addView(mMessageField);

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
		Log.d("TAG", "in onSaveInstanceState");
		// Save the image capture uri before the activity goes into background
		outState.putString(MESSAGE_KEY, mMessageString);
		// save bitmap representing image in savedinstancestate 
		// bytearray - set image using that bytearray
	}
	
	@Override
    protected void onPause() {
		super.onPause();
		Log.d("TAG", "in onPause!");
		mMessageString = mMessageField.getText().toString();
		Log.d("TAG", "messageString is " + mMessageString);
	}
	
		
	
	public void onNextClicked(View v) {
		
		String message = mMessageField.getText().toString();
		if (isMessageValid(message)){
		
		Intent intent = new Intent(this, RecipientActivity.class);
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
	
	public static boolean isMessageValid(String message){
		
		if (message.isEmpty() || message==null)
			return false;
		
		String[] words = message.split(" ");
		for (String word : words){
			if (word.length() > 28){
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
		
		Log.d("TAG", "# of newlines in this string is " + newCounter);            
		Log.d("TAG", "Message contains newline: " + message.contains("\n"));
		if (newCounter > 10) {
		  return false;
		}
		
		return true;
		
	}

	@Override
	public void onSavePhotoExit(boolean savePhoto) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTrySaveAgainExit(boolean tryAgain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReturn() {
		// TODO Auto-generated method stub
		
	}
}