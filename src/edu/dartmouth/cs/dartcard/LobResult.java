package edu.dartmouth.cs.dartcard;

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
