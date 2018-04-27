package au.org.noojee.securepay.soapapi;

import static org.junit.Assert.fail;

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
			System.out.println("CardID:" + card.getCardID());
			securePay.storeCard(card);
			
			// Change the expiry date.
			card.setExpiry(CCYear._2019, CCMonth.AUG);
			
			// update the stored card details.
			securePay.updateStoredCard(card);
			
			card.clearCardNo();
			card.clearCVV();
			
			SecurePayResponse response = securePay.debitStoredCard(card.getCardID(), "INV: 1234", SecurePay.asMoney("25.08"));
			
			if (response.isSuccessful())
				System.out.println("Success: transactionID=" + response.getTransactionID());
			else
				System.out.println(response.toString());
			
		}
		catch (SecurePayException e)
		{
			
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
			fail("Exception thrown");
		}

	}

}
