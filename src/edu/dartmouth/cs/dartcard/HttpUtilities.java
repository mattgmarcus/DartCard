/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.dartmouth.cs.dartcard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import edu.dartmouth.cs.dartcard_private.Passwords;


import android.content.Context;
import android.util.Log;

/**
 * Helper class used to handle HTTP requests.
 */
public class HttpUtilities {
	
	protected static final int MAX_ATTEMPTS = 5;
	protected static final int BACKOFF_MILLI_SECONDS = 2000;
	protected static final Random random = new Random();
	
	protected static String key;
	
	private static byte[] constructParams(Map<String, String> params) {
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		Log.d("Httputilities.constructparams", body);
		
		byte[] bytes = body.getBytes();
		
		return bytes;
	}
	
	private static HttpURLConnection makePostConnection(URL url, byte[] bytes) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setFixedLengthStreamingMode(bytes.length);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		
		String authKey = new String(Base64.encodeBase64((key+":").getBytes()));
		conn.setRequestProperty("Authorization", "Basic " + authKey);
		
		Log.d("Httputilities.makepostconnection", conn.getRequestProperties().toString());
		
		
		OutputStream out = conn.getOutputStream();
		out.write(bytes);
		out.close();
		
		return conn;
	}

	/**
	 * Issue a POST request to the server.
	 * 
	 * @param endpoint
	 *            POST address.
	 * @param params
	 *            request parameters.
	 * 
	 * @throws IOException
	 *             propagated from POST.
	 */
	public static boolean post(String endpoint, Map<String, String> params) {
		URL url = null;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			//Fail silently
		}
		
		byte[] bytes = constructParams(params);
		
		Log.d("HttpUtilities", "post");
		Log.d("HttpUtilities", "post-params" + params.toString());


		HttpURLConnection conn;

		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		for (int i = 1; i <= HttpUtilities.MAX_ATTEMPTS; i++) {
			try {
					conn = makePostConnection(url, bytes);
					Log.d("url should be ", conn.toString());

					// handle the response
					int status = conn.getResponseCode();
					Log.d("HttpUtilities", "post-status" + status);

					if (status == 200) {	
						Log.d("httputilities.post", "stauts is 200");
						BufferedReader br = 
								new BufferedReader(new InputStreamReader(conn.getInputStream()));
		                StringBuilder sb = new StringBuilder();
		                String line;
		                while ((line = br.readLine()) != null) {
		                    sb.append(line+"\n");
		                }
		                br.close();

		                Log.d("httputilities.post", sb.toString());
						if (null != conn) {
							conn.disconnect();
						}
						return true;
					}	
					
					else if (500 > status) {
						if (null != conn) {
							conn.disconnect();
						}
						return false;
					}
			} catch (IOException e) {
				try {
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Thread.currentThread().interrupt();
				}
				// increase backoff exponentially
				backoff *= 2;
			}
		}
		
		return false;
	}
	
	/**
	 * Issue a POST request to the server.
	 * 
	 * @param endpoint
	 *            POST address.
	 * @param params
	 *            request parameters.
	 * 
	 * @throws IOException
	 *             propagated from POST.
	 */
	public static String postForResults(String endpoint, Map<String, String> params) {
		URL url = null;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			//Fail silently
		}
		
		byte[] bytes = constructParams(params);
		
		Log.d("HttpUtilities", "post");
		Log.d("HttpUtilities", "post-params" + params.toString());


		HttpURLConnection conn;

		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		for (int i = 1; i <= HttpUtilities.MAX_ATTEMPTS; i++) {
			try {
					conn = makePostConnection(url, bytes);
					Log.d("url should be ", conn.toString());

					// handle the response
					int status = conn.getResponseCode();
					Log.d("HttpUtilities", "post-status" + status);

					if (status == 200) {	
						Log.d("httputilities.post", "stauts is 200");
						
						BufferedReader br = 
								new BufferedReader(new InputStreamReader(conn.getInputStream()));
		                StringBuilder sb = new StringBuilder();
		                String line;
		                while ((line = br.readLine()) != null) {
		                    sb.append(line+"\n");
		                }
		                br.close();
		                
						if (null != conn) {
							conn.disconnect();
						}

		                return sb.toString();
					}	
					
					else if (500 > status) {
						if (null != conn) {
							conn.disconnect();
						}
						return null;
					}
			} catch (IOException e) {
				try {
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Thread.currentThread().interrupt();
				}
				// increase backoff exponentially
				backoff *= 2;
			}
		}
		
		return null;

	}
}
