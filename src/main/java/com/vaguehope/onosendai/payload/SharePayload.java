package com.vaguehope.onosendai.payload;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.vaguehope.onosendai.R;
import com.vaguehope.onosendai.images.ImageLoader;
import com.vaguehope.onosendai.model.Tweet;
import com.vaguehope.onosendai.provider.NetworkType;

public class SharePayload extends Payload {

	private final NetworkType networkType;

	public SharePayload (final Tweet ownerTweet) {
		this(ownerTweet, null);
	}

	public SharePayload (final Tweet ownerTweet, final NetworkType networkType) {
		super(ownerTweet, PayloadType.SHARE);
		this.networkType = networkType;
	}

	public NetworkType getNetworkType () {
		return this.networkType;
	}

	@Override
	public String getTitle () {
		return "Share";
	}

	@Override
	public PayloadLayout getLayout () {
		return PayloadLayout.SHARE;
	}

	@Override
	public PayloadRowView makeRowView (final View view) {
		return new PayloadRowView(new Button[] {
				(Button) view.findViewById(R.id.btnShareRt),
				(Button) view.findViewById(R.id.btnShareQuote)
		});
	}

	@Override
	public void applyTo (final PayloadRowView rowView, final ImageLoader imageLoader, final PayloadClickListener clickListener) {
		final Button[] btns = rowView.getButtons();
		final Button btnRt = btns[0];

		if (this.networkType != null) {
			switch (this.networkType) {
				case TWITTER:
					btnRt.setText(R.string.btn_share_rt);
					btnRt.setVisibility(View.VISIBLE);
					break;
				case FACEBOOK:
					btnRt.setText(R.string.btn_share_like);
					btnRt.setVisibility(View.VISIBLE);
					break;
				default:
					btnRt.setVisibility(View.GONE);
			}
		}
		else {
			btnRt.setVisibility(View.GONE);
		}

		for (int i = 0; i < btns.length; i++) {
			btns[i].setOnClickListener(new BtnListener(this, clickListener, i));
		}
	}

	private static class BtnListener implements OnClickListener {

		private final Payload payload;
		private final PayloadClickListener clickListener;
		private final int index;

		public BtnListener (final Payload payload, final PayloadClickListener clickListener, final int index) {
			this.payload = payload;
			this.clickListener = clickListener;
			this.index = index;
		}

		@Override
		public void onClick (final View v) {
			this.clickListener.subviewClicked(this.payload, this.index);
		}

	}

}
