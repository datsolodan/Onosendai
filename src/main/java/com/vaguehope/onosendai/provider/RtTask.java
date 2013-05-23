package com.vaguehope.onosendai.provider;

import twitter4j.TwitterException;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.vaguehope.onosendai.R;
import com.vaguehope.onosendai.Ui;
import com.vaguehope.onosendai.config.Account;
import com.vaguehope.onosendai.model.Meta;
import com.vaguehope.onosendai.model.MetaType;
import com.vaguehope.onosendai.model.Tweet;
import com.vaguehope.onosendai.provider.RtTask.RtResult;
import com.vaguehope.onosendai.provider.successwhale.ItemAction;
import com.vaguehope.onosendai.provider.successwhale.SuccessWhaleException;
import com.vaguehope.onosendai.provider.successwhale.SuccessWhaleProvider;
import com.vaguehope.onosendai.provider.twitter.TwitterProvider;
import com.vaguehope.onosendai.storage.DbBindingAsyncTask;
import com.vaguehope.onosendai.storage.DbInterface;
import com.vaguehope.onosendai.util.LogWrapper;

public class RtTask extends DbBindingAsyncTask<Void, Void, RtResult> {

	private static final LogWrapper LOG = new LogWrapper("RT");

	private final Context context;
	private final RtRequest req;
	private final int notificationId;

	private NotificationManager notificationMgr;

	public RtTask (final Context context, final RtRequest req) {
		super(context);
		this.context = context;
		this.req = req;
		this.notificationId = (int) System.currentTimeMillis(); // Probably unique.
	}

	@Override
	protected LogWrapper getLog () {
		return LOG;
	}

	@Override
	protected void onPreExecute () {
		this.notificationMgr = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new NotificationCompat.Builder(this.context)
				.setSmallIcon(Ui.notificationIcon())
				.setContentTitle(String.format("RTing via %s...", this.req.getAccount().humanTitle()))
				.setOngoing(true)
				.setUsesChronometer(true)
				.build();
		this.notificationMgr.notify(this.notificationId, n);
	}

	@Override
	protected RtResult doInBackgroundWithDb (final DbInterface db, final Void... params) {
		LOG.i("RTing: %s", this.req);
		switch (this.req.getAccount().getProvider()) {
			case TWITTER:
				return rtViaTwitter();
			case SUCCESSWHALE:
				return rtViaSuccessWhale(db);
			default:
				return new RtResult(this.req, new UnsupportedOperationException("Do not know how to RT via account type: " + this.req.getAccount().humanTitle()));
		}
	}

	private RtResult rtViaTwitter () {
		final TwitterProvider p = new TwitterProvider();
		try {
			p.rt(this.req.getAccount(), Long.parseLong(this.req.getTweet().getSid()));
			return new RtResult(this.req);
		}
		catch (TwitterException e) {
			return new RtResult(this.req, e);
		}
		finally {
			p.shutdown();
		}
	}

	private RtResult rtViaSuccessWhale (final DbInterface db) {
		final SuccessWhaleProvider p = new SuccessWhaleProvider(db);
		try {
			final Meta svcMeta = this.req.getTweet().getFirstMetaOfType(MetaType.SERVICE);
			if(svcMeta != null) {
				final ServiceRef svc = ServiceRef.parseServiceMeta(svcMeta);
				if (svc != null) {
					final NetworkType networkType = svc.getType();
					if (networkType != null) {
						switch (networkType) {
							case TWITTER:
								p.itemAction(this.req.getAccount(), svc, this.req.getTweet().getSid(), ItemAction.RETWEET);
								return new RtResult(this.req);
							case FACEBOOK:
								p.itemAction(this.req.getAccount(), svc, this.req.getTweet().getSid(), ItemAction.LIKE);
								return new RtResult(this.req);
							default:
								return new RtResult(this.req, new SuccessWhaleException("Unknown network type: " + networkType));
						}
					}
					return new RtResult(this.req, new SuccessWhaleException("Service metadata missing network type: " + svc));
				}
				return new RtResult(this.req, new SuccessWhaleException("Invalid service metadata: " + svcMeta.getData()));
			}
			return new RtResult(this.req, new SuccessWhaleException("Service metadata missing from message."));
		}
		catch (SuccessWhaleException e) {
			return new RtResult(this.req, e);
		}
		finally {
			p.shutdown();
		}
	}

	@Override
	protected void onPostExecute (final RtResult res) {
		if (!res.isSuccess()) {
			LOG.w("RT failed: %s", res.getE());
			Notification n = new NotificationCompat.Builder(this.context)
					.setSmallIcon(R.drawable.exclamation_red) // TODO better icon.
					.setContentTitle(String.format("Failed to RT via %s.", this.req.getAccount().humanTitle()))
					.setContentText(res.getEmsg())
					.setAutoCancel(true)
					.setUsesChronometer(false)
					.setWhen(System.currentTimeMillis())
					.build();
			this.notificationMgr.notify(this.notificationId, n);
		}
		else {
			this.notificationMgr.cancel(this.notificationId);
		}
	}

	public static class RtRequest {

		private final Account account;
		private final Tweet tweet;

		public RtRequest (final Account account, final Tweet tweet) {
			this.account = account;
			this.tweet = tweet;
		}

		public Account getAccount () {
			return this.account;
		}

		public Tweet getTweet () {
			return this.tweet;
		}

		@Override
		public String toString () {
			return new StringBuilder()
					.append("RtRequest{").append(this.account)
					.append(",").append(this.tweet)
					.append("}").toString();
		}

	}

	protected static class RtResult {

		private final boolean success;
		private final RtRequest request;
		private final Exception e;

		public RtResult (final RtRequest request) {
			this.success = true;
			this.request = request;
			this.e = null;
		}

		public RtResult (final RtRequest request, final Exception e) {
			this.success = false;
			this.request = request;
			this.e = e;
		}

		public boolean isSuccess () {
			return this.success;
		}

		public RtRequest getRequest () {
			return this.request;
		}

		public Exception getE () {
			return this.e;
		}

		public String getEmsg() {
			return TaskUtils.getEmsg(this.e);
		}

	}

}
