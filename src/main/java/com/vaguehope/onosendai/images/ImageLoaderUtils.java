package com.vaguehope.onosendai.images;

import android.app.Activity;
import android.graphics.Bitmap;

public final class ImageLoaderUtils {

	private ImageLoaderUtils () {
		throw new AssertionError();
	}

	public static ImageLoader fromActivity (final Activity activity) {
		if (!(activity instanceof ImageLoader)) throw new IllegalArgumentException("Activity is not an ImageLoader: " + activity);
		return (ImageLoader) activity;
	}

	public static void loadImage (final HybridBitmapCache cache, final ImageLoadRequest req) {
		final Bitmap bmp = cache.quickGet(req.getUrl());
		if (bmp != null) {
			req.setImageBitmap(bmp);
		}
		else {
			req.setImagePending();
			// TODO if this becomes multi-threaded, need to lock in each unique URL to avoid duplicate downloads.
			new ImageFetcherTask(cache).execute(req);
		}
	}

}
