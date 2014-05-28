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


public class LobUtilities extends HttpUtilities {
	private static final String BASE_URL = "https://api.lob.com/v1";
	
	public static boolean verifyAddress(Map<String, String> address) {
		String url = BASE_URL + "/verify";
		Log.d("LobUtilities", "verifyAddress");
		
		key = Passwords.getLobKey();

		return post(url, address);
	}
	
	public static boolean sendPostcards(ArrayList<NameValuePair> parameters, String fileName) {
		String url = BASE_URL + "/postcards";
		Log.d("lobuitilies", "sendPostcards");
		
		key = Passwords.getLobKey();
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
		//Attach the parameters
		try {
			httppost.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e1) {
			return false;
		}

		//Attach the file
		/*MultipartEntity reqEntity = new MultipartEntity();
		FileBody file = new FileBody(new File(filename));
		reqEntity.addPart("front", file);
		httppost.setEntity(reqEntity);*/
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();        
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	    File file = new File(fileName);
	    FileBody fileBody = new FileBody(file);
	    builder.addPart("file", fileBody);  
	    HttpEntity entity = builder.build();
	    httppost.setEntity(entity);
	    
		//Set the authentication key
		String authKey = new String(Base64.encodeBase64((key+":").getBytes()));
	    httppost.setHeader("Authorization", "Basic " + authKey);

		HttpResponse response = null;

		try {
			response = httpClient.execute(httppost);
			String returnString = EntityUtils.toString(response.getEntity());

			Log.d("return string is", returnString);
		} catch (IOException e) {
			return false;
		}

		
		return true;
	}
}
