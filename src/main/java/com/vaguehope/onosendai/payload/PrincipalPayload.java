package com.vaguehope.onosendai.payload;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vaguehope.onosendai.R;
import com.vaguehope.onosendai.images.ImageLoadRequest;
import com.vaguehope.onosendai.images.ImageLoader;
import com.vaguehope.onosendai.model.Tweet;

public class PrincipalPayload extends Payload {

	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

	public PrincipalPayload (final Tweet tweet) {
		super(tweet, PayloadType.PRINCIPAL);
	}

	@Override
	public String getTitle () {
		return String.format("tweet[%s]", getOwnerTweet().getSid());
	}

	@Override
	public PayloadLayout getLayout () {
		return PayloadLayout.PRINCIPAL_TWEET;
	}

	@Override
	public PayloadRowView makeRowView (final View view) {
		return new PayloadRowView(
				(TextView) view.findViewById(R.id.tweetDetailBody),
				(ImageView) view.findViewById(R.id.tweetDetailAvatar),
				(TextView) view.findViewById(R.id.tweetDetailName),
				(TextView) view.findViewById(R.id.tweetDetailDate));
	}

	@Override
	public void applyTo (final PayloadRowView rowView, final ImageLoader imageLoader, final PayloadClickListener clickListener) {
		final Tweet tweet = getOwnerTweet();

		rowView.setText(tweet.getBody());
		rowView.setSecondaryText(tweet.getFullname());
		rowView.setTertiaryText(DATE_FORMAT.format(new Date(TimeUnit.SECONDS.toMillis(tweet.getTime()))));

		final String avatarUrl = tweet.getAvatarUrl();
		if (avatarUrl != null) {
			imageLoader.loadImage(new ImageLoadRequest(avatarUrl, rowView.getImage()));
		}
		else {
			rowView.getImage().setImageResource(R.drawable.question_blue);
		}
	}

}
