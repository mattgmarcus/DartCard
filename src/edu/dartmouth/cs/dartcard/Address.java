package edu.dartmouth.cs.dartcard;

public class Address {
	
	private Long id;
	private String name;
	private String lineOne;
	private String lineTwo;
	private String city;
	private String state;
	private int zipCode;
	private String label;
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setName(String n) {
		this.name = n;
	}
	
	public void setLineOne(String lo) {
		this.lineOne = lo;
	}
	
	public void setLineTwo(String lt) {
		this.lineTwo = lt;
	}
	
	public void setCity(String c) {
		this.city = c;
	}
	
	public void setState(String st) {
		this.state = st;
	}
	
	public void setZip(int zip) {
		this.zipCode = zip;
	}
	
	public void setLabel(String l) {
		this.label = l;
	}
	
	public long getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getLineOne() {
		return this.lineOne;
	}
	
	public String getLineTwo() {
		return this.lineTwo;
	}
	
	public String getCity() {
		return this.city;
	}

	public String getState() {
		return this.state;
	}
	
	public int getZip() {
		return this.zipCode;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public String toString() {
		return "Id: " + this.id + "Name: " + this.name + "Line one: " + this.lineOne + "Line two: " + this.lineTwo
				+ "City: " + this.city + "State: " + this.state + "Zip: " + this.zipCode + "Label: " + this.label;
	}
}
