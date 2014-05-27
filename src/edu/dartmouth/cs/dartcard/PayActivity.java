package edu.dartmouth.cs.dartcard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.stripe.android.model.Card;
import com.stripe.model.Customer;

import edu.dartmouth.cs.dartcard.RecipientActivity.LobTask;
import edu.dartmouth.cs.dartcard.StripeUtilities.CardResponse;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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
	
	private Double COST_PER_CARD = 1.50;
	
	private CardDBHelper mCardDBHelper;
	private Spinner mCardChoices;

	private ArrayList<edu.dartmouth.cs.dartcard.Card> mUserCards;
	
	private boolean isUsingSavedCard;
	private String customerId;
	
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
		
		mCardDBHelper = new CardDBHelper(this);
		mUserCards = mCardDBHelper.fetchCards();
		isUsingSavedCard = false;
		customerId = "";

		mCardChoices = (Spinner) findViewById(R.id.ui_pay_activity_recipient_card_choices);
		populateCardChoices();
		
		setPayButtonText();
		
		mPayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPayClicked(v);
			}
		});

	}
	
	private void populateCardChoices() {
	    ArrayList<String> cardTypes = new ArrayList<String>();
	    cardTypes.add("Select a saved card");
	    for (int i = 0; i < mUserCards.size(); i++) {
	    	cardTypes.add(mUserCards.get(i).getType() + "--" + mUserCards.get(i).getLastFour());
	    }
		ArrayAdapter<CharSequence> adapter = 
	    		new ArrayAdapter(this, android.R.layout.simple_list_item_1, cardTypes);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mCardChoices.setAdapter(adapter);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		mCardChoices.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        //Update position to account for the first choice in the spinner
		    	position = position - 1;
		    	if (-1 == position) {
		    		isUsingSavedCard = false;

		    		enableFields();
		    		mEmailField.setText("");
		    		mCardField.setText("");
		    		mMonthSpinner.setSelection(0);
		    		mYearSpinner.setSelection(0);
		    	}
		    	else {
		    		isUsingSavedCard = true;
		    		
		    		edu.dartmouth.cs.dartcard.Card card = mUserCards.get(position);
		    		String email = card.getEmail();
		    		if (email.isEmpty())
		    			mEmailField.setText("None saved");
		    		else
		    			mEmailField.setText(email);
		    		
		    		String type = card.getType();
		    		if ("American Express" == type) {
			    		mCardField.setText("**** ****** *"+card.getLastFour());
		    		}
		    		else if ("Diners Club" == type) {
		    			String lastFour = card.getLastFour();
			    		mCardField.setText("**** **** **"+lastFour.substring(0, 2) 
			    				+ " " + lastFour.substring(2, 4));
		    		}
		    		else if (("Visa" == type) || ("MasterCard" == type) || ("Discover" == type)
		    				|| ("JCB" == type)) {
			    		mCardField.setText("**** **** **** "+card.getLastFour());
		    		}
		    		mMonthSpinner.setSelection(card.getExpMonth());
		    		mYearSpinner.setSelection(getYearChoice(card.getExpYear()));
		    		disableFields();
		    		
		    		customerId = card.getCusId();
		    	}
		    }
		    
		    private void enableFields() {
	    		mEmailField.setEnabled(true);
	    		mCardField.setEnabled(true);
	    		mCVCField.setEnabled(true);
	    		mMonthSpinner.setEnabled(true);
	    		mYearSpinner.setEnabled(true);
	    		
	    		mRememberSwitch.setSelected(false);
	    		mRememberSwitch.setEnabled(true);
		    }
		    
		    private void disableFields() {
	    		mEmailField.setEnabled(false);
	    		mCardField.setEnabled(false);
	    		mCVCField.setEnabled(false);
	    		mMonthSpinner.setEnabled(false);
	    		mYearSpinner.setEnabled(false);
	    		
	    		mRememberSwitch.setSelected(true);
	    		mRememberSwitch.setEnabled(false);
		    }
		    
		    private int getYearChoice(int year) {
		    	if (2013 > year) {
		    		return 0;
		    	}
		    	else {
		    		//Maps 2014 to 1, 2015 to 2, etc.
		    		return year - 2013;
		    	}
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {}

		});

	}

	
	private void onPayClicked(View v) {
		mPayButton.setEnabled(false);
		
		boolean validCard = true;

		Map<String, String> params;
		if (!isUsingSavedCard)  {
			params = makeCardParams();
			validCard = validateCard(params);
		}
		else {
			params = new HashMap<String, String>();
			params.put(getString(R.string.customer_id), customerId);
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
			String name = recipient.getName();
			if (name.equals(""))
				recipientNames.add("No name entered");
			else
				recipientNames.add(name);
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
	    	if (s.length() > 0) {
		    	switch(s.toString().charAt(0)) {
		    	//To do for format: xxxx xxxxxx xxxxx
		    	case '3':
			        // Remove spacing char
			        if (s.length() > 0 && (s.length() % 5) == 0) {
			            final char c = s.charAt(s.length() - 1);
			            if (space == c) {
			                s.delete(s.length() - 1, s.length());
			            }
			        }
			        if (s.length() > 0 && (s.length() % 5) == 0) {
			            char c = s.charAt(s.length() - 1);
			            if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
			                s.insert(s.length() - 1, String.valueOf(space));
			            }
			        }
		    	default:
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
			            // Only if it's a digit where there should be a space we insert a space
			            if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
			                s.insert(s.length() - 1, String.valueOf(space));
			            }
			        }
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
				Log.d("doInBackground", "Using customer id");
				Map<String, String> chargeParams = new HashMap<String, String>();
				chargeParams.put("customer", params.get(getString(R.string.customer_id)));

				chargeParams.put("amount", 
						String.valueOf(costToCents(calculateCost(mRecipients.size()))));
				chargeParams.put("currency", "usd");
				
				return StripeUtilities.charge(chargeParams);
			}
			else if (mRememberSwitch.isChecked()){
				Log.d("doInBackground", "New customer with remember me");

				Map<String, String> customerParams = new HashMap<String, String>();
				String email = params.get(getString(R.string.email));
				customerParams.put("description", 
						"Customer for " + email);
				customerParams.put("email", email);
				
				String customerId = 
						StripeUtilities.createCustomer(customerParams, getApplicationContext());
				
				if (null != customerId) {					
					Map<String, String> cardParams = new HashMap<String, String>();
					cardParams.put("card[number]", params.get(getString(R.string.card_number)));
					cardParams.put("card[exp_month]", params.get(getString(R.string.exp_month)));
					cardParams.put("card[exp_year]", params.get(getString(R.string.exp_year)));
					cardParams.put("card[cvc]", params.get(getString(R.string.cvc)));
	
					CardResponse resp = StripeUtilities.createCard(cardParams, customerId);
					saveCard(email, resp);
					
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
			
			else {
				Log.d("doInBackground", "New customer without remember me");

				Map<String, String> chargeParams = new HashMap<String, String>();
				chargeParams.put("card[number]", params.get(getString(R.string.card_number)));
				chargeParams.put("card[exp_month]", params.get(getString(R.string.exp_month)));
				chargeParams.put("card[exp_year]", params.get(getString(R.string.exp_year)));
				chargeParams.put("card[cvc]", params.get(getString(R.string.cvc)));
				chargeParams.put("description", "Charge for " + params.get(getString(R.string.email)));

				chargeParams.put("amount", 
						String.valueOf(costToCents(calculateCost(mRecipients.size()))));
				chargeParams.put("currency", "usd");

				return StripeUtilities.charge(chargeParams);	

			}
		}
		
		private void saveCard(String email, CardResponse resp) {
			edu.dartmouth.cs.dartcard.Card card = new edu.dartmouth.cs.dartcard.Card(email, resp.getCustomer(), 
					resp.getlast4(), resp.getType(), resp.getExpMonth(), resp.getExpYear());
			
			mCardDBHelper.insertCard(card);
			
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
	};



}
