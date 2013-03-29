package com.vaguehope.onosendai.provider.successwhale;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.vaguehope.onosendai.config.Account;
import com.vaguehope.onosendai.model.TweetList;

public class SuccessWhaleProvider {

	private final ConcurrentMap<String, SuccessWhale> accounts;
	private final HttpClientFactory httpClientFactory;

	public SuccessWhaleProvider () {
		this.accounts = new ConcurrentHashMap<String, SuccessWhale>();
		this.httpClientFactory = new HttpClientFactory();
	}

	public void addAccount (final Account account) {
		if (this.accounts.containsKey(account.getId())) return;
		SuccessWhale s = new SuccessWhale(account, this.httpClientFactory);
		this.accounts.putIfAbsent(account.getId(), s);
	}

	private SuccessWhale getAccount (final Account account) {
		final SuccessWhale a = this.accounts.get(account.getId());
		if (a != null) return a;
		addAccount(account);
		return this.accounts.get(account.getId());
	}

	public TweetList getTweets (final SuccessWhaleFeed feed, final Account account) throws SuccessWhaleException {
		return fetchSuccessWhaleFeed(getAccount(account), feed);
	}

	public List<PostToAccount> getPostToAccounts (final Account account) throws SuccessWhaleException {
		return getAccount(account).getPostToAccounts();
	}

	public void post (final Account account, final Set<PostToAccount> postToAccounts, final String body, final String inReplyToSid) throws SuccessWhaleException {
		getAccount(account).post(postToAccounts, body, inReplyToSid);
	}

	public void shutdown () {
		this.httpClientFactory.shutdown();
	}

	private static TweetList fetchSuccessWhaleFeed (final SuccessWhale sw, final SuccessWhaleFeed feed) throws SuccessWhaleException {
		// TODO paging, etc.
		return sw.getFeed(feed);
	}

}
