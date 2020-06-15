package io.github.intransientmc.abstracter.util;

public class Util {
	public static String camelCase(String prefix, String string) {
		return prefix + Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}
}
