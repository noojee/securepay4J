package au.org.noojee.securepay.soapapi;

import static org.junit.Assert.fail;

import java.net.MalformedURLException;

import org.junit.Test;

public class SecurePayTest
{

	@Test
	public void test() throws SecurePayException
	{
		
		Merchant merchant = new TestMerchantImp();

		SecurePay securePay = new SecurePay(merchant);
		
		System.out.println(securePay.getMessageID());
		System.out.println(securePay.getTimestamp());
		
		CreditCard card = new CreditCard();
		card.setCardNo("4444333322221111");
		card.setExpiry(CCYear._2019, CCMonth.AUG);
		card.generateCardID();
		
		
		try
		{
			System.out.println(card.getCardID());
			securePay.addPayor( card);
			securePay.updatePayor(card);
			securePay.debitPayor(card, "INV: 1234", SecurePay.asMoney("25.08"));
			
		}
		catch (MalformedURLException | SecurePayException e)
		{
			
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
			fail("Exception thrown");
		}

	}

}
