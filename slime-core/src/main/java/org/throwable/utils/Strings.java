package org.throwable.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author throwable
 * @version 2017/1/14 18:41
 * @function String工具类,命名是为了区别
 */
public final class Strings {

	public static String firstCharToLowerCase(String input) {
		if (null == input || 0 == input.length()){
			return null;
		}
		char[] ch = input.toCharArray();
		if (ch[0] >= 'A' && ch[0] <= 'Z') {
			ch[0] ^= 32;
		}
		return String.valueOf(ch);
	}

	public static String firstCharToUpperCase(String input) {
		if (null == input || 0 == input.length()){
			return null;
		}
		char[] ch = input.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] ^= 32;
		}
		return String.valueOf(ch);
	}

	public static boolean isFirstCharUpperCase(String input) {
		if (null == input || 0 == input.length()){
			return false;
		}
		char[] ch = input.toCharArray();
		return Character.isUpperCase(ch[0]);
	}

	public static boolean isFirstCharLowerCase(String input) {
		if (null == input || 0 == input.length()){
			return false;
		}
		char[] ch = input.toCharArray();
		return Character.isLowerCase(ch[0]);
	}


	public static String camelhumpToUnderline(String str) {
		final int size;
		final char[] chars;
		final StringBuilder sb = new StringBuilder(
				(size = (chars = str.toCharArray()).length) * 3 / 2 + 1);
		char c;
		for (int i = 0; i < size; i++) {
			c = chars[i];
			if (isUppercaseAlpha(c)) {
				sb.append('_').append(toLowerAscii(c));
			} else {
				sb.append(c);
			}
		}
		return sb.charAt(0) == '_' ? sb.substring(1) : sb.toString();
	}


	public static String underlineToCamelhump(String str) {
		Matcher matcher = Pattern.compile("_[a-z]").matcher(str);
		StringBuilder builder = new StringBuilder(str);
		for (int i = 0; matcher.find(); i++) {
			builder.replace(matcher.start() - i, matcher.end() - i, matcher.group().substring(1).toUpperCase());
		}
		if (Character.isUpperCase(builder.charAt(0))) {
			builder.replace(0, 1, String.valueOf(Character.toLowerCase(builder.charAt(0))));
		}
		return builder.toString();
	}

	public static boolean isUppercaseAlpha(char c) {
		return (c >= 'A') && (c <= 'Z');
	}

	public static boolean isLowercaseAlpha(char c) {
		return (c >= 'a') && (c <= 'z');
	}

	public static char toUpperAscii(char c) {
		if (isLowercaseAlpha(c)) {
			c -= (char) 0x20;
		}
		return c;
	}

	public static char toLowerAscii(char c) {
		if (isUppercaseAlpha(c)) {
			c += (char) 0x20;
		}
		return c;
	}

	public static String trimAndToLowerCase(String s) {
		return s == null ? null : s.trim().toLowerCase();
	}


}
