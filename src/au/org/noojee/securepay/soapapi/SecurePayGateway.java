package au.org.noojee.securepay.soapapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SecurePayGateway
{
	private Logger logger = LogManager.getLogger(this.getClass());
	private String baseURL;
	public static final int PAGE_SIZE = 50;

	private static SecurePayGateway self;

	public enum HTTPMethod
	{
		GET, POST, PUT, DELETE
	}

	static synchronized public SecurePayGateway getInstance()
	{
		if (self == null)
			self = new SecurePayGateway();

		return SecurePayGateway.self;
	}

	private SecurePayGateway()
	{
		System.setProperty("http.maxConnections", "8"); // set globally only
														// once
	}

	public void setBaseURL(String baseURL)
	{
		this.baseURL = baseURL;
	}

	public HTTPResponse sendRequest(String request) throws MalformedURLException, SecurePayException
	{
		return request(HTTPMethod.POST, new URL(baseURL), request);

	}

	private HTTPResponse request(HTTPMethod method, URL url, String xmlBody) throws SecurePayException
	{

		HTTPResponse response;
		response = _request(method, url, xmlBody);

		if (response.getResponseCode() >= 300)
		{
			throw new SecurePayException(response.getResponseCode(), response.getResponseBody());
		}

		return response;
	}

	/**
	 * Returns a raw response string.
	 * 
	 * @throws SecurePayException
	 */
	private HTTPResponse _request(HTTPMethod method, URL url, String xmlBody) throws SecurePayException
	{
		HTTPResponse response = null;

		try
		{

			logger.debug(method + " url: " + url);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod(method.toString());
			connection.setDoOutput(true);
			connection.setAllowUserInteraction(false); // no users here so don't do
														// anything silly.

			connection.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");

			connection.connect();

			// Write the body if any exist.
			if (xmlBody != null)
			{
				logger.debug("xmlBody: " + xmlBody);
				try (OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"))
				{
					osw.write(xmlBody.toString());
					osw.flush();
					osw.close();
				}

			}

			int responseCode = connection.getResponseCode();

			// 404 returns HTML so no point trying to parse it.
			if (responseCode == 404)
				throw new SecurePayException("The passed url was not found " + url.toString());

			String body = "";
			String error = "";

			try (InputStream streamBody = connection.getInputStream())
			{
				body = fastStreamReader(streamBody);
			}
			catch (IOException e)
			{
				try (InputStream streamError = connection.getErrorStream())
				{
					error = fastStreamReader(streamError);
				}
			}

			// Read the response.
			if (responseCode < 300)
			{
				// logger.error("Response body" + body);
				response = new HTTPResponse(responseCode, connection.getResponseMessage(), body);
			}
			else
			{

				response = new HTTPResponse(responseCode, connection.getResponseMessage(), error);

				logger.error(response);
				logger.error("EndPoint responsible for error: " + method.toString() + " " + url);
				logger.error("Subumitted body responsible for error: " + xmlBody);
			}
		}
		catch (IOException e)
		{
			throw new SecurePayException(e);
		}

		return response;

	}

	String fastStreamReader(InputStream inputStream) throws SecurePayException
	{
		if (inputStream != null)
		{
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[4000];
			int length;
			try
			{
				while ((length = inputStream.read(buffer)) != -1)
				{
					result.write(buffer, 0, length);
				}
				return result.toString(StandardCharsets.UTF_8.name());

			}
			catch (IOException e)
			{
				throw new SecurePayException(e);
			}

		}
		return "";
	}

}