package edu.dartmouth.cs.dartcard;

import android.os.Parcel;
import android.os.Parcelable;

public class Recipient implements Parcelable {
	private String fullName;
	private String street1;
	private String street2;
	private String city;
	private String state;
	private String zip;
	private String message;
	
	
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
