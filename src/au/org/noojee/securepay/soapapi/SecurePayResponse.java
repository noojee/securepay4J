package au.org.noojee.securepay.soapapi;

public class SecurePayResponse
{
	private int responseCode;
	private String responseText;

	private boolean successful = false;

	public SecurePayResponse(int responseCode, String responseText)
	{
		this.responseCode = responseCode;
		this.responseText = responseText;
	}

	public void setSuccessful()
	{
		this.successful = true;
	}

	public int getResponseCode()
	{
		return responseCode;
	}

	public String getResponseText()
	{
		return responseText;
	}

	public boolean isSuccessful()
	{
		return successful;
	}

}
