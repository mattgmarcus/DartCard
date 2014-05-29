package edu.dartmouth.cs.dartcard;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.stripe.android.model.Card;
import com.stripe.model.Customer;

import edu.dartmouth.cs.dartcard.DartCardDialogFragment.DialogExitListener;
import edu.dartmouth.cs.dartcard.RecipientActivity.LobVerifyTask;
import edu.dartmouth.cs.dartcard.StripeUtilities.CardResponse;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
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
import android.widget.TextView;
import java.io.*;
import java.net.MalformedURLException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.graphics.pdf.*;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;

public class PayActivity extends Activity implements DialogExitListener {
	private ArrayList<Recipient> mRecipients;
	private Recipient mSender;

	private EditText mEmailField;
	private EditText mCardField;
	private EditText mCVCField;

	private Spinner mMonthSpinner;
	private Spinner mYearSpinner;

	private Switch mRememberSwitch;

	private Button mPayButton;

	private ProgressDialog mProgressDialog;

	private Double COST_PER_CARD = 1.50;

	private CardDBHelper mCardDBHelper;
	private Spinner mCardChoices;

	private ArrayList<edu.dartmouth.cs.dartcard.Card> mUserCards;

	private boolean isUsingSavedCard;
	private String customerId;

	private ActionBar mActionBar;

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
		mRememberSwitch.setTextOn("Yes");
		mRememberSwitch.setTextOff("No");		

		mPayButton = (Button) findViewById(R.id.ui_pay_activity_paybutton);

		// This line prevents the focus from automatically going into the
		// text fields when they're created
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mActionBar = getActionBar();
		mActionBar.setTitle("DartCard");

		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			mRecipients = bundle
					.getParcelableArrayList(getString(R.string.recipient_activity_intent_key));
			setRecipientListview();
			mSender = bundle
					.getParcelable(getString(R.string.from_activity_intent_key));
		} else {
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

	// This method takes the credit cards that have been saved and puts them in the dropdown
	private void populateCardChoices() {
		ArrayList<String> cardTypes = new ArrayList<String>();
		cardTypes.add("Select a saved card or add a new one");
		for (int i = 0; i < mUserCards.size(); i++) {
			cardTypes.add(mUserCards.get(i).getType() + "--"
					+ mUserCards.get(i).getLastFour());
		}
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, cardTypes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCardChoices.setAdapter(adapter);
	}

	@Override
	public void onStart() {
		super.onStart();

		mCardChoices.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				// Update position to account for the first choice in the
				// spinner
				position = position - 1;
				if (-1 == position) {
					if (isUsingSavedCard == true) {
						isUsingSavedCard = false;

						enableFields();
						mEmailField.setText("");
						mCardField.setText("");
						mMonthSpinner.setSelection(0);
						mYearSpinner.setSelection(0);
					}
				} else {
					isUsingSavedCard = true;

					edu.dartmouth.cs.dartcard.Card card = mUserCards
							.get(position);
					String email = card.getEmail();
					if (email.isEmpty())
						mEmailField.setText("None saved");
					else
						mEmailField.setText(email);

					String type = card.getType();
					if ("American Express" == type) {
						mCardField.setText("**** ****** *" + card.getLastFour());
					} else if ("Diners Club" == type) {
						String lastFour = card.getLastFour();
						mCardField.setText("**** **** **"
								+ lastFour.substring(0, 2) + " "
								+ lastFour.substring(2, 4));
					} else if (("Visa" == type) || ("MasterCard" == type)
							|| ("Discover" == type) || ("JCB" == type)) {
						mCardField.setText("**** **** **** "
								+ card.getLastFour());
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

			//Converts numbers from values over 2000 to double digit values
			private int getYearChoice(int year) {
				if (2013 > year) {
					return 0;
				} else {
					// Maps 2014 to 1, 2015 to 2, etc.
					return year - 2013;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}

		});

	}

	//When pay is clicked, make sure the card is valid. If it is, launch a progress
	//dialog and start the sending/payment process
	private void onPayClicked(View v) {
		mPayButton.setEnabled(false);

		boolean validCard = true;

		Map<String, String> params;
		if (!isUsingSavedCard) {
			params = makeCardParams();
			validCard = validateCard(params);
		} else {
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
				stripe_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						(Void[]) null);
			else
				stripe_task.execute((Void[]) null);
		} else {
			mPayButton.setEnabled(true);
		}
	}

	private Map<String, String> makeCardParams() {
		Map<String, String> cardParams = new HashMap<String, String>();
		cardParams.put(getString(R.string.card_number), mCardField.getText()
				.toString());
		cardParams.put(getString(R.string.email), mEmailField.getText()
				.toString());
		cardParams.put(getString(R.string.cvc), mCVCField.getText().toString());
		cardParams.put(getString(R.string.exp_month), mMonthSpinner
				.getSelectedItem().toString());
		cardParams.put(getString(R.string.exp_year), mYearSpinner
				.getSelectedItem().toString());

		return cardParams;
	}

	//Sets the recipient listview at the top of the view
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
		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
				recipientNames));
	}

	//Calculates the total cost of all the cards that are being sent
	private double calculateCost(int numRecipients) {
		return COST_PER_CARD * numRecipients;
	}

	private int costToCents(double cost) {
		return (int) (100 * cost);
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

		// First make sure they've selected a month and year
		// If either value is invalid, set it to an invalid number
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

		// If any flag is true, return false
		return !(cardFlag || numberFlag || cvcFlag || monthFlag || yearFlag || dateFlag);
	}

	//This class watches the text in the credit card field and, depending on what type of card
	//is being entered, will reformat the text so it appears the right way
	public static class CreditCardFormatWatcher implements TextWatcher {
		/*Some code borrowed from: http://stackoverflow.com/questions/11790102/format-credit-card-in-edit-text-in-android*/
		
	    private static final char space = ' ';

	    @Override
	    public void afterTextChanged(Editable s) {
	    	if (s.length() > 0) {
		    	switch(s.toString().charAt(0)) {
		    	//For format: xxxx xxxxxx xxxxx
		    	case '3':
			        // Remove spacing char
			        if (s.length() > 0 && (s.length() % 5) == 0) {
			            final char c = s.charAt(s.length() - 1);
			            if (space == c) {
			                s.delete(s.length() - 1, s.length());
			            }
			        }
			        if (s.length() > 0 && (s.length() % 9) == 0) {
			            final char c = s.charAt(s.length() - 1);
			            if (space == c) {
			                s.delete(s.length() - 1, s.length());
			            }
			        }
			        
			        if (s.length() > 0 && (s.length() % 5) == 0 && (s.length() < 6)) {
			        	char c = s.charAt(s.length() - 1);
			            if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length < 2) {
				        	s.insert(s.length() - 1, String.valueOf(space));
			            }
			        }
			        if (s.length() > 0 && (s.length() % 12) == 0 && (s.length() < 13)) {
			            char c = s.charAt(s.length() - 1);
			            if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length < 3) {
			            	s.insert(s.length() - 1, String.valueOf(space));
			            }
			        }
			        break;
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
			        break;
		    	}
	    	}
	    }
	    
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {}
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	}
	
	
	//This class is an AsyncTask for sending the requests to the Stripe API. Upon its success,
	//the next task that's called is LobPostCardTask
	public class StripeTask extends AsyncTask<Void, Void, Boolean> {
		private Activity activity;
		private Map<String, String> params;

		public StripeTask(Activity activity, Map<String, String> params) {
			this.activity = activity;
			this.params = params;
		}

		@Override
		protected Boolean doInBackground(Void... p) {
			//If there's already a customer for this request, do this
			if (params.containsKey(getString(R.string.customer_id))) {
				Map<String, String> chargeParams = new HashMap<String, String>();
				chargeParams.put("customer",
						params.get(getString(R.string.customer_id)));

				chargeParams.put("amount",
						String.valueOf(costToCents(calculateCost(mRecipients
								.size()))));
				chargeParams.put("currency", "usd");

				return StripeUtilities.charge(chargeParams);
			} 
			//If it's a new customer with the Remember Me switch checked, do this
			else if (mRememberSwitch.isChecked()) {
				Map<String, String> customerParams = new HashMap<String, String>();
				String email = params.get(getString(R.string.email));
				customerParams.put("description", "Customer for " + email);
				customerParams.put("email", email);

				String customerId = StripeUtilities.createCustomer(
						customerParams, getApplicationContext());

				if (null != customerId) {
					Map<String, String> cardParams = new HashMap<String, String>();
					cardParams.put("card[number]",
							params.get(getString(R.string.card_number)));
					cardParams.put("card[exp_month]",
							params.get(getString(R.string.exp_month)));
					cardParams.put("card[exp_year]",
							params.get(getString(R.string.exp_year)));
					cardParams.put("card[cvc]",
							params.get(getString(R.string.cvc)));

					CardResponse resp = StripeUtilities.createCard(cardParams,
							customerId);
					saveCard(email, resp);

					Map<String, String> chargeParams = new HashMap<String, String>();
					chargeParams.put("customer", customerId);

					chargeParams.put("amount", String
							.valueOf(costToCents(calculateCost(mRecipients
									.size()))));
					chargeParams.put("currency", "usd");

					return StripeUtilities.charge(chargeParams);
				} else {
					return false;
				}
			}
			//If it's just a new customer, do this
			else {
				Map<String, String> chargeParams = new HashMap<String, String>();
				chargeParams.put("card[number]",
						params.get(getString(R.string.card_number)));
				chargeParams.put("card[exp_month]",
						params.get(getString(R.string.exp_month)));
				chargeParams.put("card[exp_year]",
						params.get(getString(R.string.exp_year)));
				chargeParams.put("card[cvc]",
						params.get(getString(R.string.cvc)));
				chargeParams.put("description",
						"Charge for " + params.get(getString(R.string.email)));

				chargeParams.put("amount",
						String.valueOf(costToCents(calculateCost(mRecipients
								.size()))));
				chargeParams.put("currency", "usd");

				return StripeUtilities.charge(chargeParams);

			}
		}
		
		//Saves credit card to the database
		private void saveCard(String email, CardResponse resp) {
			edu.dartmouth.cs.dartcard.Card card = new edu.dartmouth.cs.dartcard.Card(
					email, resp.getCustomer(), resp.getlast4(), resp.getType(),
					resp.getExpMonth(), resp.getExpYear());

			mCardDBHelper.insertCard(card);

		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				mProgressDialog.dismiss();
				mPayButton.setEnabled(true);
	
				DartCardDialogFragment frag = DartCardDialogFragment
						.newInstance(Globals.DIALOG_LOB_ERRORS);
				frag.show(activity.getFragmentManager(), "lob dialog");

			} else {
				LobPostCardTask lob_task = new LobPostCardTask(activity);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
					lob_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
							(Void[]) null);
				else
					lob_task.execute((Void[]) null);
			}
		}
	};
	
	//This async task sends the request to the Lob API for sending the postcard
	public class LobPostCardTask extends AsyncTask<Void,Void,ArrayList<LobResult>> {
		private Activity activity;

		public LobPostCardTask(Activity activity) {
			this.activity = activity;
		}

		/**
		 * Calculates the transform the print an Image to fill the page
		 * https://github
		 * .com/rknoll/presit/blob/32fc951f5b799f74a0da3ec5210957750896cd95
		 * /Components
		 * /zxing.net.mobile-1.4.4/lib/android/19.1.0/content/support/
		 * v4/src/kitkat/android/support/v4/print/PrintHelperKitkat.java
		 * 
		 * @param imageWidth
		 *            with of bitmap
		 * @param imageHeight
		 *            height of bitmap
		 * @param content
		 *            The output page dimensions
		 * @param fittingMode
		 *            The mode of fitting {@link #SCALE_MODE_FILL} vs
		 *            {@link #SCALE_MODE_FIT}
		 * @return Matrix to be used in canvas.drawBitmap(bitmap, matrix, null)
		 *         call
		 */
		private Matrix getMatrix(int imageWidth, int imageHeight,
				RectF content, int fittingMode) {
			Matrix matrix = new Matrix();

			// Compute and apply scale to fill the page.
			float scale = content.width() / imageWidth;
			if (fittingMode == 2) {
				scale = Math.max(scale, content.height() / imageHeight);
			} else {
				scale = Math.min(scale, content.height() / imageHeight);
			}
			matrix.postScale(scale, scale);

			// Center the content.
			final float translateX = (content.width() - imageWidth * scale) / 2;
			final float translateY = (content.height() - imageHeight * scale) / 2;
			matrix.postTranslate(translateX, translateY);
			return matrix;
		}
		
		//This method takes the saved photo and converts it to a PDF file, to be used
		//by the API request
		private PdfDocument getPDFImage() {
			FileInputStream fis = null;
			try {
				fis = openFileInput(getString(R.string.selected_photo_name));
			} catch (FileNotFoundException e) {
				// Fail silently
			}

			Bitmap bmap = BitmapFactory.decodeStream(fis);
			try {
				fis.close();
			} catch (IOException e) {
				// Fail silently
			}

			PdfDocument document = new PdfDocument();
			Page page = document.startPage(new PageInfo.Builder(432, 288, 1)
					.create());
			RectF content = new RectF(page.getInfo().getContentRect());

			Matrix matrix = getMatrix(bmap.getWidth(), bmap.getHeight(),
					content, 2);

			page.getCanvas().drawBitmap(bmap, matrix, null);
			document.finishPage(page);

			File file = new File(getFilesDir(), getString(R.string.pdf_name));
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				// Fail silently
			}
			try {
				document.writeTo(os);
			} catch (IOException e) {
				// Fail silently
			}
			document.close();
			try {
				os.close();
			} catch (IOException e) {
				// Fail silently
			}

			// file path is Uri.fromFile(file)
			return document;
		}

		//Constructs the parameters for an individual postcard
		private ArrayList<NameValuePair> constructSingleParams(
				Recipient recipient) {
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

			// Set name on postcard (Note: this is optional)
			params.add(new BasicNameValuePair("name", "DartCard Postcard from "
					+ mEmailField.getText().toString()));

			// Put in Recipient information
			params.add(new BasicNameValuePair("to[name]", recipient.getName()));
			params.add(new BasicNameValuePair("to[address_line1]", recipient
					.getStreet1()));
			String street2 = recipient.getStreet2();
			if (!street2.isEmpty() && street2 != null) {
				params.add(new BasicNameValuePair("to[address_line2]", street2));
			}
			params.add(new BasicNameValuePair("to[address_city]", recipient
					.getCity()));
			params.add(new BasicNameValuePair("to[address_state]", recipient
					.getState().toUpperCase(Locale.ENGLISH)));
			params.add(new BasicNameValuePair("to[address_zip]", recipient
					.getZip()));
			params.add(new BasicNameValuePair("to[address_country]", "US"));

			// Put in Sender information
			params.add(new BasicNameValuePair("from[name]", mSender.getName()));
			params.add(new BasicNameValuePair("from[address_line1]", mSender
					.getStreet1()));
			String senderStreet2 = mSender.getStreet2();
			if (!senderStreet2.isEmpty() && senderStreet2 != null) {
				params.add(new BasicNameValuePair("from[address_line2]",
						senderStreet2));
			}
			params.add(new BasicNameValuePair("from[address_city]", mSender
					.getCity()));
			params.add(new BasicNameValuePair("from[address_state]", mSender
					.getState().toUpperCase(Locale.ENGLISH)));
			params.add(new BasicNameValuePair("from[address_zip]", mSender
					.getZip()));
			params.add(new BasicNameValuePair("from[address_country]", "US"));

			// Set message, which substitutes for back
			if (recipient.getMessage() != null && !recipient.getMessage().isEmpty()) {
				params.add(new BasicNameValuePair("message", recipient
						.getMessage()));
			} 

			return params;
		}

		//Constructs an arraylist of all the parameters for each postcard
		private ArrayList<ArrayList<NameValuePair>> constructAllParams() {
			ArrayList<ArrayList<NameValuePair>> allParams = new ArrayList<ArrayList<NameValuePair>>();

			// If the image is converted to PDF successfully, we really only
			// need to know the
			// file name for the Lob API calls
			PdfDocument image = getPDFImage();
			if (null == image) {
				return null;
			}

			for (Recipient recipient : mRecipients) {
				allParams.add(constructSingleParams(recipient));
			}

			return allParams;
		}

		//This method will execute a request to the Lob API for each postcard that's being sent
		@Override
		protected ArrayList<LobResult> doInBackground(Void ... params) {
			ArrayList<ArrayList<NameValuePair>> allParams = constructAllParams();
			ArrayList<LobResult> results = new ArrayList<LobResult>();
			
			String fileName = getFilesDir() + "/" + activity.getString(R.string.pdf_name);
			LobResult result;
			for (ArrayList<NameValuePair> parameters : allParams) {
				result = LobUtilities.sendPostcards(parameters, 
						fileName);
				results.add(result);
			}
			return results;
		}
		
		//Since the Lob API calls will return a string for each url for each postcard, this
		//will combine them all
		private String getPostcardUrls(ArrayList<LobResult> results) {
			String urls = "Check out your postcards here: ";
			
			int length = results.size();
			for (int i = 0; i < length - 1; i++) {
				urls = urls.concat(results.get(i).getUrl() + ", ");
			}
			
			urls = urls.concat(results.get(length - 1).getUrl());
			
			return urls;
		}
		
		@Override
		protected void onPostExecute(ArrayList<LobResult> result) {
			boolean success = true;
			for (LobResult res : result) {
				if (!res.getSuccess()) {
					success = false;
				}
			}
			if (success) {
				mProgressDialog.dismiss();
				mPayButton.setEnabled(true);	

				Intent intent = new Intent(activity, ResultActivity.class);
				String postcardUrls = getPostcardUrls(result);
				intent.putExtra("Postcardurls", postcardUrls);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				activity.startActivity(intent);
			}
			else {
				//If it was unsuccessful
				mProgressDialog.dismiss();
				mPayButton.setEnabled(true);	
	
				DartCardDialogFragment frag = DartCardDialogFragment
						.newInstance(Globals.DIALOG_LOB_ERRORS);
				frag.show(activity.getFragmentManager(), "lob dialog");
			}
		}
	}

	@Override
	public void onSavePhotoExit(boolean savePhoto) {}
	@Override
	public void onTrySaveAgainExit(boolean tryAgain) {}
	@Override
	public void onReturn() {}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

}
