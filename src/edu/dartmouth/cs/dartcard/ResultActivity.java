package edu.dartmouth.cs.dartcard;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ResultActivity extends Activity {
	private TextView mUrlsText;
	private ActionBar mActionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);

		mUrlsText = (TextView) findViewById(R.id.ui_activity_result_urls);
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			String urls = bundle.getString("Postcardurls");
			mUrlsText.setText(urls);
		} 
		else {
			mUrlsText.setText("http://www.google.com is a link");
		}
	}
	
	public void onSendAnotherClicked(View v) {
		Intent i = new Intent(this, HomeActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}



}
