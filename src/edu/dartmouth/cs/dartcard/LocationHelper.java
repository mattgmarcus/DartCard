package edu.dartmouth.cs.dartcard;

import android.util.Log;

import com.lob.Lob;
import com.lob.exception.LobException;
import com.lob.model.Address;
import com.lob.model.DeletedStatus;
import com.lob.model.AddressCollection;
import com.lob.model.Verify;

import edu.dartmouth.cs.dartcard_private.Passwords;

import java.util.HashMap;
import java.util.Map;

public class LocationHelper {

	public static boolean validateAddress(Map<String, String> address) {
		/*Lob.apiKey = Passwords.getLobKey();
		try {
			Verify v = Address.verify(address, Lob.apiKey);
			Log.d("locationhelper.validateaddress", v.toString());
			return true;
		}
		catch (LobException e) {
        	return false;
        }
        catch (Exception e) {
        	return false;
        }*/
		Log.d("Locationhelper", "validateAddress");
		
		return LobUtilities.verifyAddress(address);

	}
}
