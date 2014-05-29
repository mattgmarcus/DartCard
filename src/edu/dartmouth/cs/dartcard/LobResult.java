package edu.dartmouth.cs.dartcard;

// This is a simple class that holds the result from the Lob postcard request. It's used
// to both know if the request was successful, and to get the url of the postcard result
public class LobResult {
	private boolean success;
	private String url;
	
	public LobResult(boolean success, String url) {
		this.success = success;
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public boolean getSuccess() {
		return success;
	}
}
