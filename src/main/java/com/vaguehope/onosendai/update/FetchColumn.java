package com.vaguehope.onosendai.update;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import twitter4j.TwitterException;

import com.vaguehope.onosendai.config.Account;
import com.vaguehope.onosendai.config.Column;
import com.vaguehope.onosendai.model.Tweet;
import com.vaguehope.onosendai.model.TweetList;
import com.vaguehope.onosendai.provider.ProviderMgr;
import com.vaguehope.onosendai.provider.successwhale.SuccessWhaleException;
import com.vaguehope.onosendai.provider.successwhale.SuccessWhaleFeed;
import com.vaguehope.onosendai.provider.successwhale.SuccessWhaleProvider;
import com.vaguehope.onosendai.provider.twitter.TwitterFeed;
import com.vaguehope.onosendai.provider.twitter.TwitterFeeds;
import com.vaguehope.onosendai.provider.twitter.TwitterProvider;
import com.vaguehope.onosendai.storage.DbInterface;
import com.vaguehope.onosendai.util.LogWrapper;

class FetchColumn implements Callable<Void> {

	protected static final LogWrapper LOG = new LogWrapper("FC");

	private final DbInterface db;
	private final Account account;
	private final Column column;
	private final ProviderMgr providerMgr;

	public FetchColumn (final DbInterface db, final Account account, final Column column, final ProviderMgr providerMgr) {
		if (db == null) throw new IllegalArgumentException("db can not be null.");
		if (account == null) throw new IllegalArgumentException("account can not be null.");
		if (column == null) throw new IllegalArgumentException("column can not be null.");
		if (providerMgr == null) throw new IllegalArgumentException("providerMgr can not be null.");
		this.db = db;
		this.account = account;
		this.column = column;
		this.providerMgr = providerMgr;
	}

	@Override
	public Void call () {
		fetchColumn(this.db, this.account, this.column, this.providerMgr);
		return null;
	}

	public static void fetchColumn (final DbInterface db, final Account account, final Column column, final ProviderMgr providerMgr) {
		final long startTime = System.nanoTime();
		switch (account.getProvider()) {
			case TWITTER:
				try {
					final TwitterProvider twitterProvider = providerMgr.getTwitterProvider();
					twitterProvider.addAccount(account);
					TwitterFeed feed = TwitterFeeds.parse(column.getResource());

					long sinceId = -1;
					List<Tweet> existingTweets = db.getTweets(column.getId(), 1);
					if (existingTweets.size() > 0) sinceId = Long.parseLong(existingTweets.get(existingTweets.size() - 1).getSid());

					TweetList tweets = twitterProvider.getTweets(feed, account, sinceId);
					db.storeTweets(column, tweets.getTweets());

					long durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
					LOG.i("Fetched %d items for '%s' in %d millis.", tweets.count(), column.getTitle(), durationMillis);
				}
				catch (TwitterException e) {
					LOG.w("Failed to fetch from Twitter: %s", e.toString());
				}
				break;
			case SUCCESSWHALE:
				try {
					final SuccessWhaleProvider successWhaleProvider = providerMgr.getSuccessWhaleProvider();
					successWhaleProvider.addAccount(account);
					SuccessWhaleFeed feed = new SuccessWhaleFeed(column);

					TweetList tweets = successWhaleProvider.getTweets(feed, account);
					db.storeTweets(column, tweets.getTweets());

					long durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
					LOG.i("Fetched %d items for '%s' in %d millis.", tweets.count(), column.getTitle(), durationMillis);
				}
				catch (SuccessWhaleException e) {
					LOG.w("Failed to fetch from SuccessWhale: %s", e.toString());
				}
				break;
			default:
				LOG.e("Unknown account type: %s", account.getProvider());
		}
	}

}
