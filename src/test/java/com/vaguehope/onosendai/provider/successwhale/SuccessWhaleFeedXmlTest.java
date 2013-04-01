package com.vaguehope.onosendai.provider.successwhale;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaguehope.onosendai.model.Meta;
import com.vaguehope.onosendai.model.MetaType;
import com.vaguehope.onosendai.model.Tweet;
import com.vaguehope.onosendai.model.TweetList;

public class SuccessWhaleFeedXmlTest {

	@Test
	public void itParsesAllTweets () throws Exception {
		SuccessWhaleFeedXml feed = new SuccessWhaleFeedXml(getClass().getResourceAsStream("/successwhale_tweets.xml"));
		TweetList tweets = feed.getTweets();
		assertEquals(3, tweets.count());
	}

	@Test
	public void itParsesASimpleTweets () throws Exception {
		SuccessWhaleFeedXml feed = new SuccessWhaleFeedXml(getClass().getResourceAsStream("/successwhale_tweets.xml"));
		TweetList tweets = feed.getTweets();

		Tweet t = tweets.getTweet(0);
		assertEquals("first tweets message", t.getBody());
		assertEquals("313781345934852800", t.getSid());
		assertEquals("some_user_name", t.getUsername());
		assertEquals(1363646281L, t.getTime());
		assertEquals("http://si0.twimg.com/profile_images/2344890405/af462a16c498087e4372cb6ac6aff134_normal.jpeg", t.getAvatarUrl());

		assertEquals(2, t.getMetas().size());

		Meta m0 = t.getMetas().get(0);
		assertEquals(MetaType.INREPLYTO, m0.getType());
		assertEquals("313783459384590224", m0.getData());

		Meta m1 = t.getMetas().get(1);
		assertEquals(MetaType.SERVICE, m1.getType());
		assertEquals("twitter:09823422", m1.getData());
	}

	@Test
	public void itPareseATweetWithLinkAndPreview () throws Exception {
		SuccessWhaleFeedXml feed = new SuccessWhaleFeedXml(getClass().getResourceAsStream("/successwhale_tweets.xml"));
		TweetList tweets = feed.getTweets();

		Tweet t = tweets.getTweet(1);
		assertEquals("DreamWorks tops compute-cycle record with 'The Croods' http://t.co/zG68KbAFDX", t.getBody());
		assertEquals("339045390394853920", t.getSid());
		assertEquals("some_other_user", t.getUsername());
		assertEquals(1364374692L, t.getTime());
		assertEquals("http://si0.twimg.com/profile_images/0983453383/e987609785a770b7ceeb476525435a1f_normal.jpeg", t.getAvatarUrl());

		assertEquals(3, t.getMetas().size());

		Meta m0 = t.getMetas().get(0);
		assertEquals(MetaType.MEDIA, m0.getType());
		assertEquals("http://pbs.twimg.com/media/SDFLKsdflkjdfmG.jpg", m0.getData());

		Meta m1 = t.getMetas().get(1);
		assertEquals(MetaType.URL, m1.getType());
		assertEquals("http://www.computerworld.com/s/article/9237880/DreamWorks_tops_compute_cycle_record_with_The_Croods_", m1.getData());
		assertEquals("computerworld.com/s/article/9237…", m1.getTitle());

		Meta m2 = t.getMetas().get(2);
		assertEquals(MetaType.SERVICE, m2.getType());
		assertEquals("twitter:09823422", m2.getData());
	}

	@Test
	public void itParsesARetweet () throws Exception {
		SuccessWhaleFeedXml feed = new SuccessWhaleFeedXml(getClass().getResourceAsStream("/successwhale_tweets.xml"));
		TweetList tweets = feed.getTweets();

		Tweet t = tweets.getTweet(2);

		assertEquals(3, t.getMetas().size());

		Meta m0 = t.getMetas().get(0);
		assertEquals(MetaType.MENTION, m0.getType());
		assertEquals("johndoe", m0.getData());
		assertEquals("RT by @johndoe", m0.getTitle());

		Meta m1 = t.getMetas().get(1);
		assertEquals(MetaType.URL, m1.getType());
		assertEquals("http://example.com/cool", m1.getData());
		assertEquals("Link Title Goes Here", m1.getTitle());

		Meta m2 = t.getMetas().get(2);
		assertEquals(MetaType.SERVICE, m2.getType());
		assertEquals("twitter:09823422", m2.getData());
	}

	@Test
	public void itParsesFacebookHomeFeedEntryWithUrl () throws Exception {
		SuccessWhaleFeedXml feed = new SuccessWhaleFeedXml(getClass().getResourceAsStream("/successwhale_fb_home.xml"));
		TweetList tweets = feed.getTweets();
		assertEquals(3, tweets.count());

		Tweet t = tweets.getTweet(0);
		assertEquals("Some User shared a link.", t.getBody());
		assertEquals("557897893_497789789789963", t.getSid());
		assertEquals(null, t.getUsername());
		assertEquals("Some User", t.getFullname());
		assertEquals(1364295194L, t.getTime());

		assertEquals(3, t.getMetas().size());

		Meta m0 = t.getMetas().get(0);
		assertEquals(MetaType.MEDIA, m0.getType());
		assertEquals("https://s-platform.ak.fbcdn.net/www/app_full_proxy.php?app=350685531728&v=1&size=z&cksum=1e87fdc3550bd207da99c7d631a82ae0&src=http%3A%2F%2Fnews.bbcimg.co.uk%2Fmedia%2Fimages%2F66629000%2Fjpg%2F_66629196_66629188.jpg", m0.getData());

		Meta m1 = t.getMetas().get(1);
		assertEquals(MetaType.URL, m1.getType());
		assertEquals("http://m.bbc.co.uk/news/world-asia-21950139", m1.getData());
		assertEquals("North Korea warns South president", m1.getTitle());

		Meta m2 = t.getMetas().get(2);
		assertEquals(MetaType.SERVICE, m2.getType());
		assertEquals("facebook:532423349", m2.getData());
	}

	// TODO conversation entry.

	@Test
	public void itParsesFacebookHomeFeedEntryWithUrlAndNoText () throws Exception {
		SuccessWhaleFeedXml feed = new SuccessWhaleFeedXml(getClass().getResourceAsStream("/successwhale_fb_home.xml"));
		TweetList tweets = feed.getTweets();
		assertEquals(3, tweets.count());

		Tweet t = tweets.getTweet(2);
		assertEquals("A Link with a Page Title", t.getBody());
		assertEquals("534234215_10923840922345316", t.getSid());
		assertEquals(null, t.getUsername());
		assertEquals("Jon Doe", t.getFullname());
		assertEquals(1364270468L, t.getTime());

		assertEquals(2, t.getMetas().size());

		Meta m0 = t.getMetas().get(0);
		assertEquals(MetaType.URL, m0.getType());
		assertEquals("http://apps.facebook.com/daily_photos_plus/y/the&other&args", m0.getData());
		assertEquals("A Link with a Page Title", m0.getTitle());

		Meta m1 = t.getMetas().get(1);
		assertEquals(MetaType.SERVICE, m1.getType());
		assertEquals("facebook:532423349", m1.getData());
	}

	@Test
	public void itParsesFacebookCommentsThreadInsteadOfPost () throws Exception {
		SuccessWhaleFeedXml feed = new SuccessWhaleFeedXml(getClass().getResourceAsStream("/successwhale_fb_comments.xml"));
		TweetList tweets = feed.getTweets();
		assertEquals(2, tweets.count());

		Tweet t = tweets.getTweet(0);
		assertEquals("The first comment.", t.getBody());
		assertEquals("523049849_10152161726966490_26840788", t.getSid());
		assertEquals(null, t.getUsername());
		assertEquals("The commenter", t.getFullname());
		assertEquals(1364767585L, t.getTime());

		Tweet t1 = tweets.getTweet(1);
		assertEquals("The second comment.", t1.getBody());
		assertEquals("523049849_10152161726966490_26841688", t1.getSid());
		assertEquals(null, t1.getUsername());
		assertEquals("The Poster", t1.getFullname());
		assertEquals(1364774559L, t1.getTime());
	}

}
