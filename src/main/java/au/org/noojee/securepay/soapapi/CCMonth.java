package au.org.noojee.securepay.soapapi;

public enum CCMonth
{

	JAN("01"), FEB("02"), MAR("03"), APR("04"), MAY("05"), JUN("06"), JUL("07"), AUG("08"), SEP("09"), OCT("10"), NOV("11"), DEC("12");
	
	
	private String month;
	
	CCMonth(String month)
	{
		this.setMonth(month);
	}

	public String getMonth()
	{
		return month;
	}

	public void setMonth(String month)
	{
		this.month = month;
	}

	public int toInt()
	{
		return Integer.valueOf(month);
	}
	
	public String toString()
	{
		return name() + " (" +  month + ")";
	}
}
