package com.vaguehope.onosendai.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaguehope.onosendai.config.Account;
import com.vaguehope.onosendai.config.Config;
import com.vaguehope.onosendai.model.Meta;
import com.vaguehope.onosendai.model.MetaType;
import com.vaguehope.onosendai.model.MetaUtils;
import com.vaguehope.onosendai.model.Tweet;
import com.vaguehope.onosendai.provider.NetworkType;
import com.vaguehope.onosendai.provider.successwhale.ServiceRef;
import com.vaguehope.onosendai.provider.successwhale.SuccessWhaleProvider;
import com.vaguehope.onosendai.util.LogWrapper;

public final class PayloadUtils {

	// http://www.regular-expressions.info/unicode.html

	private static final Pattern URL_PATTERN = Pattern.compile("\\(?\\b(https?://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
	private static final Pattern HASHTAG_PATTERN = Pattern.compile(
			"\\B([#|\uFF03][a-z0-9_\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff\\u3040-\\u309F\\u30A0-\\u30FF]+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MENTIONS_PATTERN = Pattern.compile("\\B@([a-zA-Z0-9_]{1,15})");

	private static final LogWrapper LOG = new LogWrapper("PU");

	private PayloadUtils () {
		throw new AssertionError();
	}

	public static PayloadList extractPayload (final Config conf, final Tweet tweet) {
		final Account account = MetaUtils.accountFromMeta(tweet, conf);

		final Set<Payload> set = new LinkedHashSet<Payload>();
		if (account != null) convertMeta(account, tweet, set);
		extractUrls(tweet, set);
		extractHashTags(tweet, set);
		if (account != null) {
			extractMentions(account, tweet, set);
			addShareOptions(account, tweet, set);
		}

		final List<Payload> sorted = new ArrayList<Payload>(set);
		Collections.sort(sorted, Payload.TYPE_COMP);
		return new PayloadList(sorted);
	}

	private static void addShareOptions (final Account account, final Tweet tweet, final Set<Payload> set) {
		if (account.getProvider() == null) return;
		switch (account.getProvider()) {
			case TWITTER:
				set.add(new SharePayload(tweet, NetworkType.TWITTER));
				break;
			case SUCCESSWHALE:
				final Meta svcMeta = tweet.getFirstMetaOfType(MetaType.SERVICE);
				final ServiceRef serviceRef = svcMeta != null ? SuccessWhaleProvider.parseServiceMeta(svcMeta) : null;
				final NetworkType networkType = serviceRef != null ? serviceRef.getType() : null;
				if (networkType == NetworkType.FACEBOOK) set.add(new CommentPayload(account, tweet));
				set.add(new SharePayload(tweet, networkType));
				break;
			default:
				set.add(new SharePayload(tweet));
				break;
		}
	}

	private static void convertMeta (final Account account, final Tweet tweet, final Set<Payload> ret) {
		List<Meta> metas = tweet.getMetas();
		if (metas == null) return;
		for (Meta meta : metas) {
			Payload payload = metaToPayload(account, tweet, meta);
			if (payload != null) ret.add(payload);
		}
	}

	private static Payload metaToPayload (final Account account, final Tweet tweet, final Meta meta) {
		switch (meta.getType()) {
			case MEDIA:
				return new MediaPayload(tweet, meta);
			case HASHTAG:
				return new HashTagPayload(tweet, meta);
			case MENTION:
				return new MentionPayload(account, tweet, meta);
			case URL:
				return new LinkPayload(tweet, meta);
			case INREPLYTO:
			case SERVICE:
			case ACCOUNT:
				return null;
			default:
				LOG.e("Unknown meta type: %s", meta.getType());
				return null;
		}
	}

	private static void extractUrls (final Tweet tweet, final Set<Payload> ret) {
		String text = tweet.getBody();
		if (text == null || text.isEmpty()) return;
		Matcher m = URL_PATTERN.matcher(text);
		while (m.find()) {
			String g = m.group();
			if (g.startsWith("(") && g.endsWith(")")) g = g.substring(1, g.length() - 1);
			ret.add(new LinkPayload(tweet, g));
		}
	}

	private static void extractHashTags (final Tweet tweet, final Set<Payload> set) {
		String text = tweet.getBody();
		if (text == null || text.isEmpty()) return;
		Matcher m = HASHTAG_PATTERN.matcher(text);
		while (m.find()) {
			String g = m.group();
			set.add(new HashTagPayload(tweet, g));
		}
	}

	private static void extractMentions (final Account account, final Tweet tweet, final Set<Payload> set) {
		if (tweet.getUsername() != null) set.add(new MentionPayload(account, tweet, tweet.getUsername()));
		List<String> allMentions = null;
		String text = tweet.getBody();
		if (text == null || text.isEmpty()) return;
		Matcher m = MENTIONS_PATTERN.matcher(text);
		while (m.find()) {
			String g = m.group(1);
			set.add(new MentionPayload(account, tweet, g));
			if (allMentions == null) allMentions = new ArrayList<String>();
			allMentions.add(g);
		}
		if (allMentions != null && tweet.getUsername() != null) set.add(new MentionPayload(account, tweet, tweet.getUsername(), allMentions.toArray(new String[allMentions.size()])));
	}

}
