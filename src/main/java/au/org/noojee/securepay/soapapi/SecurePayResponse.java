package au.org.noojee.securepay.soapapi;

import java.io.Serializable;

public class SecurePayResponse implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int responseCode = -1;
	private String responseText;

	private boolean successful = false;
	private String httpResponseBody; 
	
	// When the response is for a payment transaction then this will hold the resulting transaction id.
	private String transactionID;

	public SecurePayResponse(String httpResponseBody)
	{
		this.httpResponseBody = httpResponseBody;
		
	}
	public void setSuccessful()
	{
		this.successful = true;
	}

	public String getTransactionID()
	{
		return transactionID;
	}
	
	public int getResponseCode() throws SecurePayException
	{
		this.responseCode =  SecurePay.getXMLNodeValueAsInt(httpResponseBody, "responseCode");

		return responseCode;
	}

	public String getResponseText() throws SecurePayException
	{
		this.responseText = SecurePay.getXMLNodeValue(httpResponseBody, "responseText");

		return responseText;
	}

	public boolean isSuccessful()
	{
		return successful;
	}
	
	public String getHTTPResponseBody()
	{
		return httpResponseBody;
	}
	
	@Override
	public String toString()
	{
		return "Code: " + responseCode + " Message: " + responseText;
	}

	public void setTransactionID(String transactionID)
	{
		this.transactionID = transactionID;
		
	}

}
