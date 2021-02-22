package mtm68.util;

public class StringUtils {
	
	/**
	 * Converts a hex string of form "\xFFFF" to its corresponding char value
	 * Requires hex string is of form "\x[0-9A-F]{1,4}"
	 * 
	 * @param hex      a hex string
	 * @return         the char representation of hex value
	 */
	public static char convertHexToChar(String hex) {
		String digits = hex.substring(2); // chop off \x
		return (char)Integer.parseInt(digits, 16);
	}

	/**
	 * Converts a hex string of form "\xFFFF" to its corresponding long value
	 * Requires hex string is of form "\x[0-9A-F]{1,4}"
	 * 
	 * @param hex      a hex string
	 * @return         the long representation of hex value
	 */
	public static long convertHexToLong(String hex) {
		String digits = hex.substring(2); // chop off \x
		return Long.parseLong(digits, 16);
	}
}