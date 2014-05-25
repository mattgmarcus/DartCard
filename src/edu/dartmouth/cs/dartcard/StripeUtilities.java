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
		Log.d("StripeUtilities", "charge");
		
		key = Passwords.getStripeKey();

		return post(url, information);
	}
	
	
	public static String createCustomer(Map<String, String> information, Context context) {
		String url = BASE_URL + "/customers";
		Log.d("StripeUtilities", "customers");
		
		key = Passwords.getStripeKey();

		String results = postForResults(url, information);

		if (null == results) {
			return null;
		}
		else {
			Gson gson = new GsonBuilder().create();
			CustomerResponse resp = gson.fromJson(results, CustomerResponse.class);
			Log.d("Stripeutilitlies.createCustomer", resp.toString());
			return resp.getId();
		}
	}

	public static boolean createCard(Map<String, String> information, String id) {
		String url = BASE_URL + "/customers/" + id + "/cards";
		Log.d("StripeUtilities", "card");
		
		key = Passwords.getStripeKey();

		return post(url, information);
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
}
