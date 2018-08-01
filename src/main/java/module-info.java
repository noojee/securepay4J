/**
 * 
 */
/**
 * @author bsutton
 *
 */
module securepay.soapapi
{
	exports au.org.noojee.securepay.soapapi;

	requires bcprov.jdk15on;
	requires transitive joda.money;
	requires junit;
	requires log4j.api;
}