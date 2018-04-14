package au.org.noojee.securepay.soapapi;

import org.javamoney.moneta.Money;

public interface MerchantRate 
{
	public Money getRate();
	
	public Money addMargin(Money baseAmount);

	public Money calculateMerchantFee(Money unpaid);
	
}
