package edu.dartmouth.cs.dartcard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.stripe.android.model.Card;
import com.stripe.model.Customer;

import edu.dartmouth.cs.dartcard.RecipientActivity.LobTask;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

public class PayActivity extends Activity {
	private ArrayList<Recipient> mRecipients;

	private EditText mEmailField;
	private EditText mCardField;
	private EditText mCVCField;
	
	private Spinner mMonthSpinner;
	private Spinner mYearSpinner;
	
	private Switch mRememberSwitch;
		
	private Button mPayButton;
	
	private ActionBar mActionBar;
	
	private ProgressDialog mProgressDialog;
	
	private Double COST_PER_CARD = 1.15;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay);
		
		mEmailField = (EditText) findViewById(R.id.ui_pay_activity_recipient_email);
		mCardField = (EditText) findViewById(R.id.ui_pay_activity_recipient_card);
		mCVCField = (EditText) findViewById(R.id.ui_pay_activity_recipient_cvc);
		
		mCardField.addTextChangedListener(new CreditCardFormatWatcher());
		
		mMonthSpinner = (Spinner) findViewById(R.id.ui_pay_activity_recipient_expiry_month);
		mYearSpinner = (Spinner) findViewById(R.id.ui_pay_activity_recipient_expiry_year);
		
		mRememberSwitch = (Switch) findViewById(R.id.ui_pay_activity_recipient_remember);

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
			setRecipientListview();
		}
		else {
			mRecipients = new ArrayList<Recipient>();
		}
		
		setPayButtonText();
		
		mPayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPayClicked(v);
			}
		});

	}
	
	private String getCustomerId() {
		SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getString(getString(R.string.customer_id), null);
	}
	
	private void onPayClicked(View v) {
		mPayButton.setEnabled(false);
		
		String customer_id = getCustomerId();
		boolean validCard = true;
		boolean rememberMe = mRememberSwitch.isChecked();

		Map<String, String> params;
		if ((null == customer_id) || (!rememberMe))  {
			params = makeCardParams();
			validCard = validateCard(params);
		}
		else {

			params = new HashMap<String, String>();
			params.put(getString(R.string.customer_id), customer_id);
		}

		if (validCard) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle("Processing");
			mProgressDialog.setMessage("This should take only a second");
			mProgressDialog.show();

			StripeTask stripe_task = new StripeTask(this, params);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				stripe_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
			else
				stripe_task.execute((Void[])null);
		}
		else {
			mPayButton.setEnabled(true);
		}
	}
	
	private Map<String, String> makeCardParams() {
		Map<String, String> cardParams = new HashMap<String, String>();
		cardParams.put(getString(R.string.card_number), mCardField.getText().toString());
		cardParams.put(getString(R.string.email), mEmailField.getText().toString());
		cardParams.put(getString(R.string.cvc), mCVCField.getText().toString());
		cardParams.put(getString(R.string.exp_month), mMonthSpinner.getSelectedItem().toString());
		cardParams.put(getString(R.string.exp_year), mYearSpinner.getSelectedItem().toString());

		return cardParams;
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
	
	private double calculateCost(int numRecipients) {
		return COST_PER_CARD * numRecipients;
	}
	
	private int costToCents(double cost) {
		return (int) (100*cost);
	}
	
	private void setPayButtonText() {
		int numRecipients = mRecipients.size();
		
		double totalCost = calculateCost(numRecipients);
		
		DecimalFormat format = new DecimalFormat("#.##");
		format.setMinimumFractionDigits(2);
		mPayButton.setText("Pay: $" + format.format(totalCost));
	}
	
	private boolean validateCard(Map<String, String> params) {
		boolean cardFlag = false;
		boolean numberFlag = false;
		boolean cvcFlag = false;
		boolean monthFlag = false;
		boolean yearFlag = false;
		boolean dateFlag = false;
		
		String month = params.get(getString(R.string.exp_month));
		String year = params.get(getString(R.string.exp_year));
		
		//First make sure they've selected a month and year
		//If either value is invalid, set it to an invalid number
		if (month.equals("MM")) {
			monthFlag = true;
			month = "13";
		}
		if (year.equals("YY")) {
			yearFlag = true;
			year = "11";
		}
		
		Card card = new Card(params.get(getString(R.string.card_number)), 
				Integer.parseInt(month), Integer.parseInt(year), 
				params.get(getString(R.string.cvc)));
		
		if (!card.validateCVC()) {
			cvcFlag = true;
		}
		if (!card.validateExpiryDate()) {
			dateFlag = true;
		}
		if (!card.validateNumber()) {
			numberFlag = true;
		}
		if (!card.validateCard()) {
			cardFlag = true;
		}
		
		//If any flag is true, return false
		return !(cardFlag || numberFlag || cvcFlag || 
				monthFlag || yearFlag || dateFlag);
	}
	
	
	public static class CreditCardFormatWatcher implements TextWatcher {
	    private static final char space = ' ';

	    @Override
	    public void afterTextChanged(Editable s) {
	        // Remove spacing char
	        if (s.length() > 0 && (s.length() % 5) == 0) {
	            final char c = s.charAt(s.length() - 1);
	            if (space == c) {
	                s.delete(s.length() - 1, s.length());
	            }
	        }
	        // Insert char where needed.
	        if (s.length() > 0 && (s.length() % 5) == 0) {
	            char c = s.charAt(s.length() - 1);
	            // Only if its a digit where there should be a space we insert a space
	            if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
	                s.insert(s.length() - 1, String.valueOf(space));
	            }
	        }
	    }
	    
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	}

	
	public class StripeTask extends AsyncTask<Void,Void,Boolean> {
		private Activity activity;
		private Map<String, String> params;
		
		public StripeTask(Activity activity, Map<String, String> params) {
			this.activity = activity;
			this.params = params;
		}
		
		@Override
		protected Boolean doInBackground(Void ... p) {

			if (params.containsKey(getString(R.string.customer_id))) {
				Map<String, String> chargeParams = new HashMap<String, String>();
				chargeParams.put("customer", params.get(getString(R.string.customer_id)));

				chargeParams.put("amount", 
						String.valueOf(costToCents(calculateCost(mRecipients.size()))));
				chargeParams.put("currency", "usd");
				
				return StripeUtilities.charge(chargeParams);
			}
			else {
				Map<String, String> customerParams = new HashMap<String, String>();
				String email = params.get(getString(R.string.email));
				customerParams.put("description", 
						"Customer for " + email);
				customerParams.put("email", email);
				
				String customerId = 
						StripeUtilities.createCustomer(customerParams, getApplicationContext());
				
				if (null != customerId) {
					saveId(customerId);
					
					Map<String, String> cardParams = new HashMap<String, String>();
					cardParams.put("card[number]", params.get(getString(R.string.card_number)));
					cardParams.put("card[exp_month]", params.get(getString(R.string.exp_month)));
					cardParams.put("card[exp_year]", params.get(getString(R.string.exp_year)));
					cardParams.put("card[cvc]", params.get(getString(R.string.cvc)));
	
					StripeUtilities.createCard(cardParams, customerId);
					
					Map<String, String> chargeParams = new HashMap<String, String>();
					chargeParams.put("customer", customerId);
	
					chargeParams.put("amount", 
							String.valueOf(costToCents(calculateCost(mRecipients.size()))));
					chargeParams.put("currency", "usd");
	
					return StripeUtilities.charge(chargeParams);	
				}
				else {
					return false;
				}
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			mProgressDialog.dismiss();
			if (!result) {
				Log.d("PayActivity.async.onpostexecute", "Failed!");
				
				mPayButton.setEnabled(true);	
			}
			else {
				Intent intent = new Intent(activity, HomeActivity.class);
				
				activity.startActivity(intent);
			}
		}
		
		private void saveId(String id) {
			SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(getString(R.string.customer_id), id);
			editor.commit();
		}
	};



}
