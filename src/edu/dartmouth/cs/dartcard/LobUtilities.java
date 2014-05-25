package edu.dartmouth.cs.dartcard;

import java.io.IOException;
import java.util.Map;

import edu.dartmouth.cs.dartcard_private.Passwords;

import android.util.Log;


public class LobUtilities extends HttpUtilities {
	private static final String BASE_URL = "https://api.lob.com/v1";
	
	public static boolean verifyAddress(Map<String, String> address) {
		String url = BASE_URL + "/verify";
		Log.d("LobUtilities", "verifyAddress");
		
		key = Passwords.getLobKey();

		return post(url, address);
	}
	
	public static boolean sendPostcards() {
		return true;
	}
}
