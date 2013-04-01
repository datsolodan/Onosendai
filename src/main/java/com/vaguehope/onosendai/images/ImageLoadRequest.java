package com.vaguehope.onosendai.images;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.vaguehope.onosendai.R;

public class ImageLoadRequest {

	private final String url;
	private final ImageView imageView;
	private final ImageLoadListener listener;

	public ImageLoadRequest (final String url, final ImageView imageView) {
		this(url, imageView, null);
	}

	public ImageLoadRequest (final String url, final ImageView imageView, final ImageLoadListener listener) {
		if (url == null) throw new IllegalArgumentException("Missing arg: url.");
		if (imageView == null) throw new IllegalArgumentException("Missing arg: imageView.");
		this.url = url;
		this.imageView = imageView;
		this.listener = listener;
	}

	public String getUrl () {
		return this.url;
	}

	public void setImagePending (final String forUrl) {
		this.imageView.setImageResource(R.drawable.question_blue);
		this.imageView.setTag(forUrl);
	}

	public void setImageUnavailable (final String url) {
		if (!url.equals(this.imageView.getTag())) return;
		this.imageView.setImageResource(R.drawable.exclamation_red);
	}

	public void setImageBitmap (final Bitmap bmp) {
		this.imageView.setImageBitmap(bmp);
		this.imageView.setTag(null);
		if (this.listener != null) this.listener.imageLoaded(this);
	}

	public void setImageBitmap (final String url, final Bitmap bmp) {
		if (!url.equals(this.imageView.getTag())) return;
		this.imageView.setImageBitmap(bmp);
		if (this.listener != null) this.listener.imageLoaded(this);
	}

	public interface ImageLoadListener {
		/**
		 * Called only after the image has been successfully loaded.
		 */
		void imageLoaded (ImageLoadRequest req);
	}

}
