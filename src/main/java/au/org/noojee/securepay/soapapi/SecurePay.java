package au.org.noojee.securepay.soapapi;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class SecurePay
{
	final String API_VERSION = "spxml-4.2";

	static final CurrencyUnit currencyUnit = CurrencyUnit.AUD;

	private Merchant merchant;

	public SecurePay(Merchant merchant)
	{
		this.merchant = merchant;
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

	public SecurePayResponse storeCard(CreditCard card)
			throws SecurePayException
	{
		String request = this.generateAddPayorRequest(card);
		

		SecurePayResponse response = sendRequest(merchant.getPeriodicBaseURL(), request);

		if (response.getResponseCode() == 0)
			response.setSuccessful();

		return response;

	}

	public SecurePayResponse updateStoredCard(CreditCard card)
			throws SecurePayException
	{
		String request = this.generateUpdatePayorRequest(card);
		

		SecurePayResponse response = sendRequest(merchant.getPeriodicBaseURL(), request);

		if (response.getResponseCode() == 0)
			response.setSuccessful();

		return response;

	}

	/**
	 * Check if the securepay servers are up and responding.
	 * 
	 * @return
	 * @throws SecurePayException
	 */
	public SecurePayResponse pingPaymentServer()
			throws SecurePayException
	{
		String request = this.generateEchoRequest();
		

		SecurePayResponse response = _sendRequest(merchant.getPaymentBaseURL(), request);
		response.setSuccessful();

		return response;

	}
	
	/**
	 * Check if the securepay servers are up and responding.
	 * 
	 * @return
	 * @throws SecurePayException
	 */
	public SecurePayResponse pingPeriodicServer()
			throws SecurePayException
	{
		String request = this.generateEchoRequest();
		

		SecurePayResponse response = _sendRequest(merchant.getPeriodicBaseURL(), request);
		response.setSuccessful();

		return response;

	}


	/**
	 * Debit a credit card that was previously stored.
	 * @param cardId
	 * @param transactionReference
	 * @param amount
	 * @return
	 * @throws SecurePayException
	 */
	public SecurePayResponse debitStoredCard(String cardId, String transactionReference, Money amount)
			throws SecurePayException
	{
		String request = generateDebitPayorRequest(cardId, transactionReference, amount);

		SecurePayResponse response = sendRequest(merchant.getPeriodicBaseURL(), request);

		switch (response.getResponseCode())
		{
			case 0:
			case 8:
				response.setSuccessful();
				response.setTransactionID(getXMLNodeValue(response.getHTTPResponseBody(), "txnID"));
				break;
		}

		return response;

	}

	/**
	 * Directly debit a credit card.
	 * @param CardNo
	 * @param expiryMonth
	 * @param expiryYear
	 * @param transactionReference
	 * @param amount
	 * @return
	 * @throws SecurePayException
	 */
	public SecurePayResponse debitCard(CreditCard creditCard, String transactionReference, Money amount)
			throws SecurePayException
	{
		String request = generateDebitRequest(creditCard, transactionReference, amount);

		SecurePayResponse response = sendRequest(merchant.getPaymentBaseURL(), request);

		switch (response.getResponseCode())
		{
			case 0:
			case 8:
				response.setSuccessful();
				response.setTransactionID(getXMLNodeValue(response.getHTTPResponseBody(), "txnID"));
				break;
		}

		return response;

	}

	private SecurePayResponse sendRequest(String baseURL, String tokenRequest) throws SecurePayException
	{
		return _sendRequest(baseURL, tokenRequest);
	}

	private SecurePayResponse _sendRequest(String baseURL, String tokenRequest) throws SecurePayException
	{
		SecurePayGateway gateway = SecurePayGateway.getInstance();
		gateway.setBaseURL(baseURL);

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

		return new SecurePayResponse(response.getResponseBody());
	}

	String generateDebitRequest(CreditCard creditCard, String transactionReference,
			Money amount)
	{

		Money amountInCents = amount.multipliedBy(100);

		String request = getXMLHeader()
				

				+ "<RequestType>Payment</RequestType>\n" 
				+ "<Payment>\n" 
				+ " 	<TxnList count=\"1\">\n" 
				+ " 		<Txn ID=\"1\">\n" 
				+ "		 	<txnType>0</txnType>\n" 
				+ " 			<txnSource>23</txnSource>\n" 
				+ " 			<amount>" + formatNoDecimals(amountInCents) + "</amount>\n" 
				+ " 			<recurring>no</recurring>\n" 
				+ " 			<currency>AUD</currency>\n" 
				+ " 			<purchaseOrderNo>" + transactionReference + "</purchaseOrderNo>\n" 
				+ " 			<CreditCardInfo>\n" 
				+ " 				<cardNumber>" + creditCard.getCardNo() + "</cardNumber>\n" 
				+ " 				<expiryDate>" + creditCard.getExpiryMonth().getMonth() + "/" + creditCard.getExpiryYear().toInt() + "</expiryDate>\n" 
				+ " 			</CreditCardInfo>\n" 
				+ " 		</Txn>\n" 
				+ " 	</TxnList>\n" 
				+ "</Payment>\n" 
				+ "</SecurePayMessage>";

		return request;

	}

	String generateDebitPayorRequest(String cardId, String transactionReference, Money amount)
	{

		Money amountInCents = amount.multipliedBy(100);

		String request = getXMLHeader()
				+ " <RequestType>Periodic</RequestType>\n"
				+ " <Periodic>\n"
				+ " 	<PeriodicList count=\"1\">\n"
				+ " 		<PeriodicItem ID=\"1\">\n"
				+ " 			<actionType>trigger</actionType>\n"
				+ " 			<transactionReference>" + transactionReference + "</transactionReference>\n"
				+ " 			<clientID>" + cardId + "</clientID>\n"
				+ " 			<amount>" + formatNoDecimals(amountInCents) + "</amount>\n"
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
				+ "					<cardNumber>" + card.getStrippedCardNo() + "</cardNumber>\n"
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
				+ "					<cardNumber>" + card.getStrippedCardNo() + "</cardNumber>\n"
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

	/**
	 * Use to determine if the api servers are up and we have the correct configuration.
	 * 
	 * @return
	 */
	private String generateEchoRequest()
	{
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<SecurePayMessage>\n"
				+ "	<MessageInfo>\n"
				+ " 		<messageID>8af793f9af34bea0cf40f5fb79f383</messageID>\n"
				+ " 		<messageTimestamp>20042403095953349000+660</messageTimestamp>\n"
				+ " 		<timeoutValue>60</timeoutValue>\n"
				+ " 		<apiVersion>" + API_VERSION + "</apiVersion>\n"
				+ "	</MessageInfo>\n"
				+ "<MerchantInfo>\n"
				+ "		<merchantID>" + merchant.getID() + "</merchantID>\n"
				+ "		<password>" + merchant.getPassword() + "</password>\n"
				+ "</MerchantInfo>\n"
				+ "<RequestType>Echo</RequestType>\n"
				+ "</SecurePayMessage>";
	}

	static int getXMLNodeValueAsInt(String xml, String node) throws SecurePayException
	{
		String value = getXMLNodeValue(xml, node);

		return Integer.valueOf(value);
	}

	static String getXMLNodeValue(String xml, String node) throws SecurePayException
	{
		int start = xml.indexOf("<" + node + ">");
		int end = xml.lastIndexOf("</" + node + ">");

		if (start == -1 || end == -1)

			throw new SecurePayException("The node: '" + node + "' was not found in the response."
					+ "The xml content was: " + xml);

		String nodeValue = xml.substring(start + node.length() + 2, end);

		return nodeValue;

	}

	public static Money asMoney(double value)
	{
		return Money.of(currencyUnit, value);
	}

	public static Money asMoney(String value)
	{
		return Money.of(currencyUnit, new BigDecimal(value));
	}

	private String formatNoDecimals(Money money)
	{
//		MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
//				AmountFormatQueryBuilder.of(Locale.ENGLISH)
//						.set(CurrencyStyle.NAME).set("pattern", pattern).build());
//		
		
//		
		

		String result;
		if (money == null)
			result = "";
		else
			result = "" + money.getAmountMajorLong();

		return result;

	}

}
