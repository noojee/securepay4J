package au.org.noojee.securepay.soapapi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TestMerchantImp implements Merchant
{

	@Override
	public String getID()
	{
		return "ABC0001";
	}

	@Override
	public String getPassword()
	{
		return "abc123";
	}

	@Override
	public String getPeriodicBaseURL()
	{
		return "https://test.api.securepay.com.au/xmlapi/periodic";
	}

	@Override
	public String getPaymentBaseURL()
	{
		return "https://test.api.securepay.com.au/xmlapi/payment";
	}

	@Override
	public int getZoneOffsetMinutes()
	{
		ZoneId zone = ZoneId.of("Australia/Victoria");
		ZonedDateTime zdt = LocalDateTime.now().atZone(zone);
		int offsetMinutes = zdt.getOffset().getTotalSeconds() / 60;
		return offsetMinutes;
	}

}
