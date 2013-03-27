package com.vaguehope.onosendai.payload;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class PayloadRowView {

	private final TextView main;
	private final TextView secondary;
	private final ImageView image;

	public PayloadRowView (final TextView main) {
		this(main, null);
	}

	public PayloadRowView (final TextView main, final ImageView image) {
		this(main, image, null);
	}

	public PayloadRowView (final TextView main, final ImageView image, final TextView secondary) {
		this.main = main;
		this.secondary = secondary;
		this.image = image;
	}

	public void setText (final String text) {
		if (this.main == null) return;
		this.main.setText(text);
		this.main.setVisibility(View.VISIBLE);
	}

	public void hideText () {
		this.main.setVisibility(View.GONE);
	}

	public void setSecondaryText(final String text) {
		if (this.secondary == null) return;
		this.secondary.setText(text);
	}

	public ImageView getImage () {
		return this.image;
	}

}