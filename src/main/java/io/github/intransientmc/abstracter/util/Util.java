package io.github.intransientmc.abstracter.util;

public class Util {
	public static String capitalizeFirstCharacter(String string) {
		if(string.isEmpty())
			return string;
		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}
}
