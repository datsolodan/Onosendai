package com.vaguehope.onosendai.payload;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

class PayloadRowView {

	private final TextView main;
	private final TextView secondary;
	private final ImageView image;
	private final Button[] buttons;

	public PayloadRowView (final TextView main) {
		this(main, null, null);
	}

	public PayloadRowView (final TextView main, final ImageView image) {
		this(main, image, null);
	}

	public PayloadRowView (final TextView main, final TextView secondary) {
		this(main, null, secondary);
	}

	public PayloadRowView (final TextView main, final ImageView image, final TextView secondary) {
		this.main = main;
		this.secondary = secondary;
		this.image = image;
		this.buttons = null;
	}

	public PayloadRowView (final Button[] buttons) {
		this.main = null;
		this.secondary = null;
		this.image = null;
		this.buttons = buttons;
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
		this.secondary.setVisibility(View.VISIBLE);
	}

	public void hideSecondary () {
		this.secondary.setVisibility(View.GONE);
	}

	public ImageView getImage () {
		return this.image;
	}

	public Button[] getButtons () {
		return this.buttons;
	}

}