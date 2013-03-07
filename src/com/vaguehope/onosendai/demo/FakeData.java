package com.vaguehope.onosendai.demo;

import java.util.ArrayList;
import java.util.Random;

import com.vaguehope.onosendai.Tweet;
import com.vaguehope.onosendai.TweetList;

public class FakeData {

	private static final String IPSUM = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad mi.";
	private static final Random RAND = new Random();

	private FakeData () {}

	public static TweetList makeFakeTweets () {
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		for (int i = 0; i < 500; i++) {
			tweets.add(new Tweet(makeUsername(), makeTweetBody()));
		}
		return new TweetList(tweets);
	}

	public static String makeTweetBody() {
		return IPSUM.substring(0, RAND.nextInt(140)) + ".";
	}

	public static String makeUsername() {
		return Long.toHexString(RAND.nextLong()).substring(0, RAND.nextInt(15));
	}

}