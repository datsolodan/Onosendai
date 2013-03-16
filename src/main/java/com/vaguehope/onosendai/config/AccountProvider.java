package com.vaguehope.onosendai.config;

import java.util.Locale;

public enum AccountProvider {
	TWITTER,
	SUCCESSWHALE;

	public static AccountProvider parse (final String s) {
		return valueOf(s.toUpperCase(Locale.UK));
	}

}