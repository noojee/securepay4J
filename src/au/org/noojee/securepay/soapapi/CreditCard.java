package au.org.noojee.securepay.soapapi;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Random;

import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.crypto.generators.BCrypt;
import org.javamoney.moneta.Money;

public class CreditCard implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Random RANDOM = new SecureRandom();

	private String description;

	private CreditCardCompany creditCardType;

	private String token;

	// This value is normally blank except momentarily during data entry.
	private String cardNo;
	// as above.
	private String CVV;

	private String last4Digits;

	private CCMonth expiryMonth;
	private CCYear expiryYear;

	/**
	 * A one way has of the cc no. and expiry date to uniquely identify this card when interacting with the payment
	 * gateway.
	 */
	private String cardID;

	/**
	 * Checks if the field is a valid credit card number.
	 * 
	 * @param card The card number to validate.
	 * @return Whether the card number is valid.
	 */
	public static boolean isValidCardNo(final String cardNo)
	{
		String card = cardNo.replaceAll("[^0-9]+", ""); // remove all non-numerics
		if ((card == null) || (card.length() < 13) || (card.length() > 19))
		{
			return false;
		}

		if (!luhnCheck(card))
		{
			return false;
		}

		CreditCardCompany cc = CreditCardCompany.gleanCompany(card);
		if (cc == null)
			return false;

		return true;
	}

	/**
	 * Checks for a valid credit card number.
	 * 
	 * @param cardNumber Credit Card Number.
	 * @return Whether the card number passes the luhnCheck.
	 */
	protected static boolean luhnCheck(String cardNumber)
	{
		// number must be validated as 0..9 numeric first!!
		int digits = cardNumber.length();
		int oddOrEven = digits & 1;
		long sum = 0;
		for (int count = 0; count < digits; count++)
		{
			int digit = 0;
			try
			{
				digit = Integer.parseInt(cardNumber.charAt(count) + "");
			}
			catch (NumberFormatException e)
			{
				return false;
			}

			if (((count & 1) ^ oddOrEven) == 0)
			{ // not
				digit *= 2;
				if (digit > 9)
				{
					digit -= 9;
				}
			}
			sum += digit;
		}

		return (sum == 0) ? false : (sum % 10 == 0);
	}

	boolean isValidCVV(String value)
	{
		if (Strings.isBlank(value))
			return false;

		value = value.trim();

		if (value.length() != 3)
			return false;

		if (value.matches("[0-9]*"))
			return false;

		return true;
	}

	public static boolean isExpiryValid(CCMonth month, CCYear year)
	{
		LocalDate expiry = LocalDate.of(year.toInt(), month.toInt(), 31);

		LocalDate now = LocalDate.now();

		if (now.isAfter(expiry))
			return false;
		else
			return true;
	}

	public boolean hasToken()
	{
		return Strings.isBlank(this.token);
	}

	public Money calculateTotalFee(Money unpaid, MerchantRate rate)
	{
		return rate.addMargin(unpaid);
	}

	public Money calculateMerchantFee(Money unpaid, MerchantRate rate)
	{
		return rate.calculateMerchantFee(unpaid);
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	public String getDescription()
	{
		return description;
	}

	public CreditCardCompany getCreditCardType()
	{
		return creditCardType;
	}

	public String getToken()
	{
		return token;
	}

	public String getCardNo()
	{
		return cardNo;
	}

	public void setCardNo(String cardNo)
	{
		this.cardNo = cardNo;
	}

	public String getCVV()
	{
		return CVV;
	}

	public String getLast4Digits()
	{
		return last4Digits;
	}

	public CCMonth getExpiryMonth()
	{
		return expiryMonth;
	}

	public CCYear getExpiryYear()
	{
		return expiryYear;
	}

	public void setExpiry(CCYear ccYear, CCMonth ccMonth)
	{
		this.expiryYear = ccYear;
		this.expiryMonth = ccMonth;

	}

	public void generateCardID()
	{
		this.cardID = BCrypt.generate(BCrypt.passwordToByteArray(this.cardNo.toCharArray()), getNextSalt(), 4).toString();
		
	}

	/**
	 * Returns a random salt to be used to hash a password.
	 *
	 * @return a 16 bytes random salt
	 */
	public static byte[] getNextSalt()
	{
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return salt;
	}

	public String getExpiryDate()
	{
		return getExpiryMonth().getMonth() + "/" + getExpiryYear().toInt();
	}

	/**
	 * SecurePay refer to this as a Payor ID but it really represents the card.
	 * 
	 * @return
	 */
	public String getCardID()
	{
		return cardID;
	}

	public void clearCardNo()
	{
		this.cardNo = null;

	}

	public void clearCVV()
	{
		this.CVV = null;
	}

}
