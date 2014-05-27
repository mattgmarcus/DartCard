package edu.dartmouth.cs.dartcard;

import android.os.Parcel;
import android.os.Parcelable;
import com.lob.Lob;
import com.lob.exception.LobException;
import com.lob.model.Address;
import com.lob.model.DeletedStatus;
import com.lob.model.AddressCollection;
import com.lob.model.Verify;
import java.util.HashMap;
import java.util.Map;

public class Recipient implements Parcelable {
	private String fullName;
	private String street1;
	private String street2;
	private String city;
	private String state;
	private String zip;
	private String message;
	
	public Recipient(String fullName, String street1, String street2, String city,
			String state, String zip) {
		this.fullName = fullName;
		this.street1 = street1;
		this.street2 = street2;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.message = "";
	}

	
	public Recipient(String fullName, String street1, String street2, String city,
			String state, String zip, String message) {
		this.fullName = fullName;
		this.street1 = street1;
		this.street2 = street2;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.message = message;
	}
	
	public Recipient(Parcel source) {
		fullName = source.readString();
		street1 = source.readString();
		street2 = source.readString();
		city = source.readString();
		state = source.readString();
		zip = source.readString();
		message = source.readString();
	}

	public String getName() {
		return fullName;
	}
	
	public String getStreet1() {
		return street1;
	}


	public void setStreet1(String street1) {
		this.street1 = street1;
	}


	public String getStreet2() {
		return street2;
	}


	public void setStreet2(String street2) {
		this.street2 = street2;
	}


	public String getCity() {
		return city;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	public String getZip() {
		return zip;
	}


	public void setZip(String zip) {
		this.zip = zip;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public Map<String, String> toAddressMap() {
		Map<String, String> addressMap = new HashMap<String, String>();
		addressMap.put("name", this.fullName);
		addressMap.put("address_line1", this.street1);
		addressMap.put("address_line2", this.street2);
		addressMap.put("address_city", this.city);
		addressMap.put("address_state", this.state);
		addressMap.put("address_zip", this.zip);
        
        return addressMap; 
	}

	public Map<String, String> toPostcardMap() {
		Map<String, String> addressMap = new HashMap<String, String>();
		addressMap.put("to[name]", this.fullName);
		addressMap.put("to[address_line1]", this.street1);
		addressMap.put("to[address_line2]", this.street2);
		addressMap.put("to[address_city]", this.city);
		addressMap.put("to[address_state]", this.state);
		addressMap.put("to[address_zip]", this.zip);
		addressMap.put("message", this.message);
        
        return addressMap; 
	}
	
	//Parcelable stuff
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(fullName);
		dest.writeString(street1);
		dest.writeString(street2);
		dest.writeString(city);
		dest.writeString(state);
		dest.writeString(zip);
		dest.writeString(message);
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
	      public Recipient createFromParcel(Parcel source) {
	            return new Recipient(source);
	      }
	      public Recipient[] newArray(int size) {
	            return new Recipient[size];
	      }
	};
}
