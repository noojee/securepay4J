package au.org.noojee.securepay.soapapi;

import java.io.IOException;

public class SecurePayException extends Exception
{
	private static final long serialVersionUID = 1L;
	private String errorMessage;
	private int errorCode;


	public SecurePayException(String errorMessage)
	{
		super(errorMessage);
		
		this.errorMessage = errorMessage;
	}

	public SecurePayException(IOException e)
	{
		super(e);
		this.errorMessage = e.getMessage();
	}

	public SecurePayException(int errorCode, String errorMessage)
	{
		super(errorMessage + " errorCode:" + errorCode);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	int getErrorCode()
	{
		return errorCode;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}

}
