# securepay4J
Java client library for the Australia Post Secure Pay Periodic XML api.

The client is fairly basic but allows three core operations

storeCard -- essentially stores the a credit card in the secure pay system for later use.

updateStoredCard-- update the stored credit card details for the credit card - to replace a new card.

debitStoredCard  - bill a credit card that was previously stored.

Note: we use a bcrypt salted hash of the card as the card id, so every time you call CreditCard.generateCardID
you will get a different ID even if its for the same credit card.

SecurePay use the term PayorId and ClientID interchangeably. Both terms are incorrect as it is actually an
id for the card not a client or payor. This is why this api uses the term 'card id'.

SecurePay does not use the CVV so you don't need to capture it.

Make certain you clear the CVV and CardNo from the CreditCard object after calling addPayor or updatePayor.

Example usage:

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


