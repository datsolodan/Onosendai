package com.vaguehope.onosendai.provider;

import twitter4j.TwitterException;

public final class TaskUtils {

	private TaskUtils () {
		throw new AssertionError();
	}

	public static String getEmsg(final Exception e) {
		if (e instanceof TwitterException) {
			final TwitterException te = (TwitterException) e;
			if (te.getErrorCode() >= 0 && te.getErrorMessage() != null) {
				return String.format("%s %s", te.getErrorCode(), te.getErrorMessage());
			}
		}
		return e.getMessage();
	}

}
