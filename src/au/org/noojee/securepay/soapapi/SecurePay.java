package au.org.noojee.securepay.soapapi;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;

public class SecurePay
{
	final String API_VERSION = "spxml-4.2";

	static final CurrencyUnit currencyUnit = Monetary.getCurrency(Locale.getDefault());

	private Merchant merchant;

	SecurePay(Merchant merchant)
	{
		this.merchant = merchant;
	}

	public void connect()
	{
		// TODO Auto-generated method stub
	}

	public SecurePayResponse debit(CreditCard card, Money amount)
	{
		SecurePayResponse response = null;

		return response;
	}

	String getMessageID()
	{
		UUID uuid = UUID.randomUUID();
		String randomUUIDString = uuid.toString();
		randomUUIDString = randomUUIDString.replace("-", "");
		return randomUUIDString.substring(0, Math.min(randomUUIDString.length(), 30));
	}

	String getTimestamp()
	{
		LocalDateTime now = LocalDateTime.now();

		int offsetMinutes = merchant.getZoneOffsetMinutes();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyddMMhhmmssSSS000+" + offsetMinutes);

		return now.format(format);
	}

	public void addPayor(CreditCard card)
			throws MalformedURLException, SecurePayException
	{
		String tokenRequest = this.generateAddPayorRequest(card);

		sendRequest(tokenRequest);
	}

	public void updatePayor(CreditCard card)
			throws MalformedURLException, SecurePayException
	{
		String tokenRequest = this.generateUpdatePayorRequest(card);

		sendRequest(tokenRequest);

	}

	public SecurePayResponse debitPayor(CreditCard card, String transactionReference, Money amount) throws SecurePayException
	{
		String request = generateDebitPayorRequest(card, transactionReference, amount);

		SecurePayResponse response = sendRequest(request);

		switch (response.getResponseCode())
		{
			case 0:
			case 8:
				response.setSuccessful();
				break;
		}

		return response;

	}

	private SecurePayResponse sendRequest(String tokenRequest) throws SecurePayException
	{
		SecurePayGateway gateway = SecurePayGateway.getInstance();
		gateway.setBaseURL(merchant.getPeriodicBaseURL());

		HTTPResponse response;
		try
		{
			response = gateway.sendRequest(tokenRequest);
		}
		catch (MalformedURLException e)
		{
			throw new SecurePayException(e);
		}

		// check status code
		int statusCode = getXMLNodeValueAsInt(response.getResponseBody(), "statusCode");
		if (statusCode != 0)
		{
			String description = getXMLNodeValue(response.getResponseBody(), "statusDescription");

			throw new SecurePayException(statusCode, description);
		}

		int responseCode = getXMLNodeValueAsInt(response.getResponseBody(), "responseCode");
		String responseText = getXMLNodeValue(response.getResponseBody(), "responseText");

		return new SecurePayResponse(responseCode, responseText);
	}

	String generateDebitPayorRequest(CreditCard card, String transactionReference, Money amount)
	{

		Money amountInCents = amount.multiply(100);

		String request = getXMLHeader()
				+ " <RequestType>Periodic</RequestType>\n"
				+ " <Periodic>\n"
				+ " 	<PeriodicList count=\"1\">\n"
				+ " 		<PeriodicItem ID=\"1\">\n"
				+ " 			<actionType>trigger</actionType>\n"
				+ " 			<transactionReference>" + transactionReference + "</transactionReference>\n"
				+ " 			<clientID>" + card.getCardID() + "</clientID>\n"
				+ " 			<amount>" + amountInCents.getNumber().toString() + "</amount>\n"
				+ " 		</PeriodicItem>\n"
				+ " 	</PeriodicList>\n"
				+ " </Periodic>\n"
				+ "</SecurePayMessage>\n";
		return request;

	}

	String generateAddPayorRequest(CreditCard card)
	{
		String tokenRequest = getXMLHeader()
				+ "<RequestType>Periodic</RequestType>\n"
				+ "<Periodic>\n"
				+ "		<PeriodicList count=\"1\">\n"
				+ "			<PeriodicItem ID=\"1\">\n"
				+ "				<actionType>add</actionType>\n"
				+ "				<clientID>" + card.getCardID() + "</clientID>\n"
				+ "				<CreditCardInfo>\n"
				+ "					<cardNumber>" + card.getCardNo() + "</cardNumber>\n"
				+ "					<expiryDate>" + card.getExpiryDate() + "</expiryDate>\n"
				+ "				</CreditCardInfo>\n"
				+ "				<amount>1</amount>\n"
				+ "				<periodicType>4</periodicType>\n"
				+ "			</PeriodicItem>\n"
				+ "		</PeriodicList>\n"
				+ "</Periodic>\n"
				+ "</SecurePayMessage>\n";

		return tokenRequest;
	}

	String generateUpdatePayorRequest(CreditCard card)
	{
		String tokenRequest = getXMLHeader()
				+ "<RequestType>Periodic</RequestType>\n"
				+ "<Periodic>\n"
				+ "		<PeriodicList count=\"1\">\n"
				+ "			<PeriodicItem ID=\"1\">\n"
				+ "				<actionType>edit</actionType>\n"
				+ "				<clientID>" + card.getCardID() + "</clientID>\n"
				+ "				<CreditCardInfo>\n"
				+ "					<cardNumber>" + card.getCardNo() + "</cardNumber>\n"
				+ "					<expiryDate>" + card.getExpiryDate() + "</expiryDate>\n"
				+ "				</CreditCardInfo>\n"
				+ "				<periodicType>4</periodicType>\n"
				+ "			</PeriodicItem>\n"
				+ "		</PeriodicList>\n"
				+ "</Periodic>\n"
				+ "</SecurePayMessage>\n";

		return tokenRequest;
	}

	private String getXMLHeader()
	{
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<SecurePayMessage>\n"
				+ "<MessageInfo>\n"
				+ " 	<messageID>" + getMessageID() + "</messageID>\n"
				+ " 	<messageTimestamp>" + getTimestamp() + "</messageTimestamp>\n"
				+ "		<timeoutValue>60</timeoutValue>\n"
				+ "		<apiVersion>" + API_VERSION + "</apiVersion>\n"
				+ "</MessageInfo>\n"
				+ "<MerchantInfo>\n"
				+ "		<merchantID>" + merchant.getID() + "</merchantID>\n"
				+ "		<password>" + merchant.getPassword() + "</password>\n"
				+ "</MerchantInfo>\n";
	}

	TokenResponse parseTokenResponse(String response) throws SecurePayException
	{

		String responseCode = getXMLNodeValue(response, "responseCode");

		String responseText = getXMLNodeValue(response, "responseText");
		String successful = getXMLNodeValue(response, "successful");
		String tokenValue = getXMLNodeValue(response, "tokenValue");

		return new TokenResponse(responseCode, responseText, successful, tokenValue);

	}

	int getXMLNodeValueAsInt(String xml, String node) throws SecurePayException
	{
		String value = getXMLNodeValue(xml, node);

		return Integer.valueOf(value);
	}

	String getXMLNodeValue(String xml, String node) throws SecurePayException
	{
		int start = xml.indexOf("<" + node + ">");
		int end = xml.lastIndexOf("</" + node + ">");

		if (start == -1 || end == -1)

			throw new SecurePayException("The node: '" + node + "' was not found in the response.");

		String nodeValue = xml.substring(start + node.length() + 2, end);

		return nodeValue;

	}

	public static Money asMoney(double value)
	{
		return Money.of(value, currencyUnit);
	}

	public static Money asMoney(String value)
	{
		return Money.of(new BigDecimal(value), currencyUnit);
	}

	}
