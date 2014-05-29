package edu.dartmouth.cs.dartcard;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.dartmouth.cs.dartcard_private.Passwords;

import android.content.Context;
import android.util.Log;

//This is a class built on top of HttpUtilities. It handles the requests specific to 
//using Stripe's API. There are three calls that we use here. The first is charges
//a credit card. The second creates a new customer on Stripe's database. The third 
//creates a new card object to charge. 
//It also contains an inner class that is used for taking the response from the createCard
//call and storing the information in the saved card dataabase. It utilizes the google
//gson library from parsing json objects
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
		
		key = Passwords.getStripeKey();

		String results = postForResults(url, information);
		if (null == results) {
			return null;
		}
		else {
			Gson gson = new GsonBuilder().create();
			CardResponse resp = gson.fromJson(results, CardResponse.class);
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
