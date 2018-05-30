package au.org.noojee.securepay.soapapi;

public enum CCYear
{
	_2018, _2019, _2020, _2021, _2022, _2023, _2024, _2025, _2026, _2027, _2028, _2029, _2030, _2031, _2032, _2033, _2034, _2035;

	public int toInt()
	{
		return Integer.valueOf(this.name().substring(1)) - 2000;
	}
	
	public int getYear()
	{
		return Integer.valueOf(this.name().substring(1));
	}

	public String toString()
	{
		return name().substring(1);
	}
	
	
	
}
