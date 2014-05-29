package edu.dartmouth.cs.dartcard;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import edu.dartmouth.cs.dartcard_private.Passwords;

import android.util.Log;

// This is a class built on top of HttpUtilities. It handles the requests specific to 
// using Lob's API. There are two calls that we use here. The first is address verification,
// which is first. The second is sending postcards
public class LobUtilities extends HttpUtilities {
	private static final String BASE_URL = "https://api.lob.com/v1";
	
	public static boolean verifyAddress(Map<String, String> address) {
		String url = BASE_URL + "/verify";
		
		key = Passwords.getLobKey();

		return post(url, address);
	}
	
	public static LobResult sendPostcards(ArrayList<NameValuePair> parameters, String fileName) {
		String url = BASE_URL + "/postcards";
		
		key = Passwords.getLobKey();
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	    File file = new File(fileName);
	    FileBody fileBody = new FileBody(file);
	    builder.addPart("front", fileBody);

	    for (NameValuePair pair : parameters) {
	    	builder.addTextBody(pair.getName(), pair.getValue());
	    }
	    HttpEntity entity = builder.build();
	    httppost.setEntity(entity);
	    
		//Set the authentication key
		String authKey = new String(Base64.encodeBase64((key+":").getBytes()));
	    httppost.setHeader("Authorization", "Basic " + authKey);

		HttpResponse response = null;
		String returnString = "";
		try {
			response = httpClient.execute(httppost);
			returnString = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			return new LobResult(false, null);
		}

		returnString = getUrl(returnString);
		return new LobResult(true, returnString);
	}
	
	private static String getUrl(String string) {
		int i = string.indexOf("http");
		string = string.substring(i, string.length() - 1);
		int j = string.indexOf(",");
		string = string.substring(0, j-1);
		
		return string;
	}
}
