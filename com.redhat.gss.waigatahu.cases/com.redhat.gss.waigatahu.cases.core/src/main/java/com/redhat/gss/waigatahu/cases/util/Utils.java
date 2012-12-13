package com.redhat.gss.waigatahu.cases.util;

public class Utils {
	public static String padStringLeft(int n, char c, String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n - s.length(); i++) {
			sb.append(c);
		}
		sb.append(s);
		return sb.toString();
	}

	public static String padStringRight(int n, char c, String s) {
		StringBuilder sb = new StringBuilder();
		sb.append(s);
		for (int i = 0; i < n - s.length(); i++) {
			sb.append(c);
		}
		return sb.toString();
	}
}
