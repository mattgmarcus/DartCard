package edu.dartmouth.cs.dartcard;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.dartmouth.cs.dartcard_private.Passwords;

import android.content.Context;
import android.util.Log;

public class StripeUtilities extends HttpUtilities {
	private static final String BASE_URL = "https://api.stripe.com/v1";
	
	public static boolean charge(Map<String, String> information) {
		String url = BASE_URL + "/charges";
		
		key = Passwords.getStripeKey();

		return post(url, information);
	}
	
	
	public static String createCustomer(Map<String, String> information, Context context) {
		String url = BASE_URL + "/customers";
		
		key = Passwords.getStripeKey();

		String results = postForResults(url, information);

		if (null == results) {
			return null;
		}
		else {
			Gson gson = new GsonBuilder().create();
			CustomerResponse resp = gson.fromJson(results, CustomerResponse.class);
			return resp.getId();
		}
	}

	public static CardResponse createCard(Map<String, String> information, String id) {
		String url = BASE_URL + "/customers/" + id + "/cards";
		Log.d("StripeUtilities", "card");
		
		key = Passwords.getStripeKey();

		String results = postForResults(url, information);
		if (null == results) {
			return null;
		}
		else {
			Gson gson = new GsonBuilder().create();
			CardResponse resp = gson.fromJson(results, CardResponse.class);
			Log.d("Stripeutilitlies.createCard", resp.toString());
			return resp;
		}

	}
	
	public class CustomerResponse {
		private String id;
		
		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public String toString() {
			return "ID: " + id;
		}
	}
	
	public class CardResponse {
		private String customer;
		private String last4;
		private String type;
		private int exp_month;
		private int exp_year;
		
		public String getCustomer() {
			return customer;
		}
		
		public void setCustomer(String customer) {
			this.customer = customer;
		}
		
		public String getlast4() {
			return last4;
		}
		
		public void setlast4(String last4) {
			this.last4 = last4;
		}
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public int getExpMonth() {
			return exp_month;
		}

		public void setExpMonth(int exp_month) {
			this.exp_month = exp_month;
		}

		public int getExpYear() {
			return exp_year;
		}

		public void setExpYear(int exp_year) {
			this.exp_year = exp_year;
		}

		
		public String toString() {
			return "ID: " + customer + ", Type: " + type + ", Month: " + exp_month +
					" , Year: " + exp_year;
		}
	}
}
