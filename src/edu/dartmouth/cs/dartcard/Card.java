package edu.dartmouth.cs.dartcard;

public class Card {
	private Long id;
	private String cusId;
	private String lastFour;
	private String type;
	private int expMonth;
	private int expYear;
	private String email;
	
	public Card(String email, String id, String lastFour, String type,
			int expMonth, int expYear) {
		this.email = email;
		this.cusId = id;
		this.lastFour = lastFour;
		this.type = type;
		this.expMonth = expMonth;
		this.expYear = expYear;
	}

	public Card() {}

	public void setId(long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setCusId(String id) {
		this.cusId = id;
	}
	
	public String getCusId() {
		return cusId;
	}
	
	public void setLastFour(String lastFour) {
		this.lastFour = lastFour;
	}
	
	public String getLastFour() {
		return lastFour;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public void setExpMonth(int month) {
		this.expMonth = month;
	}
	
	public int getExpMonth() {
		return expMonth;
	}
	
	public void setExpYear(int year) {
		this.expYear = year;
	}
	
	public int getExpYear() {
		return expYear;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
}
