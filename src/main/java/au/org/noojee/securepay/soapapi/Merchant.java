package au.org.noojee.securepay.soapapi;

public interface Merchant
{

	String getID();

	String getPassword();

	String getPeriodicBaseURL();
	
	String getPaymentBaseURL();

	/**
	 * Return you timezone offset from GMT in minutes.
	 * @return
	 */
	int getZoneOffsetMinutes();

	

}
