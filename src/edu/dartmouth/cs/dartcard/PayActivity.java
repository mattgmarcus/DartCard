package edu.dartmouth.cs.dartcard;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class PayActivity extends Activity {
	private ArrayList<Recipient> mRecipients;
	
	private Button mPayButton;
	private ActionBar mActionBar;
	
	private Double COST_PER_CARD = 1.15;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay);
		
		mPayButton = (Button) findViewById(R.id.ui_pay_activity_paybutton);
		
		//This line prevents the focus from automatically going into the
		//text fields when they're created
	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		//mActionBar.setTitle("Enter your personal information");

		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			mRecipients = bundle.getParcelableArrayList(
					getString(R.string.recipient_activity_intent_key));
		}
		
		setRecipientListview();
		
		setPayButtonText();
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

	private void setRecipientListview() {
		ArrayList<String> recipientNames = new ArrayList<String>();
		for (Recipient recipient : mRecipients) {
			recipientNames.add(recipient.getName());
		}
		
		ListView listView = (ListView) findViewById(R.id.ui_pay_activity_recipients_list);
		listView.setAdapter(new ArrayAdapter<String>(this,
				R.layout.list_item, recipientNames));
	}
	
	private void setPayButtonText() {
		int numRecipients = mRecipients.size();
		
		double totalCost = COST_PER_CARD * numRecipients;
		
		DecimalFormat format = new DecimalFormat("#.##");
		format.setMinimumFractionDigits(2);
		mPayButton.setText("Pay: $" + format.format(totalCost));
	}


}
