package au.org.noojee.securepay.soapapi;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Random;

import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.crypto.generators.BCrypt;
import org.joda.money.Money;

public class CreditCard 
{
	private static final Random RANDOM = new SecureRandom();

	private CreditCardIssuer creditCardIssuer;

	// This value is normally blank except momentarily during data entry.
	transient private String cardNo;
	// as above.
	transient private String CVV;

	private String last4Digits;

	private CCMonth expiryMonth;
	private CCYear expiryYear;

	/**
	 * A one way hash of the cc no. and expiry date to uniquely identify this card when interacting with the payment
	 * gateway.
	 */
	private String cardID;
	
	
	public CreditCard()
	{
	}
	
	public CreditCard(String cardNo)
	{
		setCardNo(cardNo);
	}

	/**
	 * Checks if the field is a valid credit card number.
	 * 
	 * @param card The card number to validate.
	 * @return Whether the card number is valid.
	 */
	public static boolean isValidCardNo(String cardNo)
	{
		cardNo = cardNo.replaceAll("[^0-9]+", ""); // remove all non-numerics
		if ((cardNo == null) || (cardNo.length() < 13) || (cardNo.length() > 19))
		{
			return false;
		}

		if (!luhnCheck(cardNo))
		{
			return false;
		}

		CreditCardIssuer cc = CreditCardIssuer.gleanIssuer(new CreditCard(cardNo));
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
	public static boolean luhnCheck(String cardNumber)
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

	public boolean isValidCVV(String value)
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

	public Money calculateTotalFee(Money unpaid, MerchantRate rate)
	{
		return rate.addMargin(unpaid);
	}

	public Money calculateMerchantFee(Money unpaid, MerchantRate rate)
	{
		return rate.calculateMerchantFee(unpaid);
	}

	public CreditCardIssuer getCreditCardIssuer()
	{
		return creditCardIssuer;
	}

	public String getCardNo()
	{
		return cardNo;
	}

	/**
	 * @return the card no. with a space inserted every 4 characters
	 * (from left to right) to make it easy to read.
	 */
	public String getFormattedCardNo()
	{
		String formatted = "";
		
		for (int i = 0; i< this.cardNo.length(); i++)
		{
			formatted += this.cardNo.charAt(i);
			if ((i > 0) && (((i+1) % 4) == 0))
				formatted += " ";
		}
		return formatted.trim();
	}
	public void setCardNo(String cardNo)
	{
		this.cardNo = cardNo;

		if (cardNo != null)
		{
			this.cardNo = getStrippedCardNo();

			if (cardNo.length() > 4)
				this.last4Digits = cardNo.substring(cardNo.length() - 4);
			
			this.creditCardIssuer = CreditCardIssuer.gleanIssuer(this);
		}
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
		this.setExpiryYear(ccYear);
		this.setExpiryMonth(ccMonth);

	}

	public void generateCardID()
	{
		this.cardID = BCrypt
				.generate(BCrypt.passwordToByteArray(this.getStrippedCardNo().toCharArray()), getNextSalt(), 4)
				.toString();
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

	public boolean hasCardID()
	{
		return !Strings.isBlank(cardID);
	}

	public void clearCardNo()
	{
		this.cardNo = null;

	}

	public void clearCVV()
	{
		this.setCVV(null);
	}

	public void setCreditCardIssuer(CreditCardIssuer creditCardIssuer)
	{
		this.creditCardIssuer = creditCardIssuer;

	}


	public void setExpiryMonth(CCMonth expiryMonth)
	{
		this.expiryMonth = expiryMonth;
	}

	public void setExpiryYear(CCYear expiryYear)
	{
		this.expiryYear = expiryYear;
	}

	public void setCVV(String cVV)
	{
		CVV = cVV;
	}

	public String getStrippedCardNo()
	{
		return cardNo == null ? "" : cardNo.replace(" ", "");
	}

}
