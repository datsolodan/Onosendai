package com.vaguehope.onosendai.storage;

import java.util.List;

import com.vaguehope.onosendai.config.Column;
import com.vaguehope.onosendai.model.Tweet;

public interface DbInterface {

	void storeTweets(Column column, List<Tweet> tweets);
	List<Tweet> getTweets(int columnId, int numberOf);

	void addTwUpdateListener (Runnable action);
	void removeTwUpdateListener (Runnable action);

}
