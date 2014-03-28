package com.vaguehope.onosendai.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.vaguehope.onosendai.model.TweetListCursorAdapter;

public final class ScrollIndicator {

	private static final int ACCEL_STOP_1 = 5;
	private static final int ACCEL_STOP_2 = 15;
	private static final int ACCEL_STOP_3 = 100;
	private static final int BAR_WIDTH_DIP = 7;
	private static final int COLOUR_BAR = Color.GRAY;
	private static final int COLOUR_UNREAD = Color.parseColor("#268bd2"); // Solarized Blue.

	private final BarMover barMover;

	private ScrollIndicator (final BarMover barMover) {
		this.barMover = barMover;
	}

	public void setUnreadTime (final long unreadTime) {
		this.barMover.setUnreadTime(unreadTime);
	}

	public long getUnreadTime () {
		return this.barMover.getUnreadTime();
	}

	public static ScrollIndicator attach (final Context context, final ViewGroup rootView, final ListView list, final OnScrollListener onScrollListener) {
		final AbsoluteShape bar = new AbsoluteShape(context);
		bar.layoutForce(0, 0, 0, 0);
		bar.setBackgroundColor(COLOUR_BAR);
		bar.bringToFront();
		rootView.addView(bar);

		final float pxPerUnit = dipToPixels(context, 1); // Perhaps this should be a % of list height?
		final int barWidth = (int) dipToPixels(context, BAR_WIDTH_DIP);

		final BarMover barMovingScrollListener = new BarMover(bar, list, pxPerUnit, barWidth, onScrollListener);
		list.setOnScrollListener(barMovingScrollListener);

		return new ScrollIndicator(barMovingScrollListener);
	}

	private static float dipToPixels (final Context context, final int a) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, a, context.getResources().getDisplayMetrics());
	}

	private static class AbsoluteShape extends View {

		private final Rect unreadRect;
		private final Paint unreadPaint;

		public AbsoluteShape (final Context context) {
			super(context);
			this.unreadRect = new Rect(0, 0, 1, 1);
			this.unreadPaint = new Paint(0);
			this.unreadPaint.setColor(COLOUR_UNREAD);
		}

		public void setUnreadSize (final int unreadHeight) {
			this.unreadRect.bottom = unreadHeight;
			invalidate();
		}

		@Override
		public void layout (final int l, final int t, final int r, final int b) {
			/* block calls so no layout manager can not mess with us. */
		}

		public void layoutForce (final int l, final int t, final int r, final int b) {
			super.layout(l, t, r, b);
		}

		@Override
		protected void onSizeChanged (final int w, final int h, final int oldw, final int oldh) {
			this.unreadRect.right = w;
		}

		@Override
		protected void onDraw (final Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawRect(this.unreadRect, this.unreadPaint);
		}

	}

	private static class BarMover implements OnScrollListener {

		private final AbsoluteShape bar;
		private final ListView list;
		private final TweetListCursorAdapter listAdaptor;
		private final float pxPerUnit;
		private final int barWidth;
		private final OnScrollListener delagate;

		private int lastPosition = -1;
		private long unreadTime = -1;

		public BarMover (final AbsoluteShape bar, final ListView list, final float pxPerUnit, final int barWidth, final OnScrollListener delagate) {
			this.bar = bar;
			this.list = list;
			this.listAdaptor = (TweetListCursorAdapter) list.getAdapter();
			this.pxPerUnit = pxPerUnit;
			this.barWidth = barWidth;
			this.delagate = delagate;
		}

		public void setUnreadTime (final long unreadTime) {
			this.unreadTime = unreadTime;
			for (int i = 0; i < this.listAdaptor.getCount(); i++) {
				if (this.listAdaptor.getItemTime(i) <= this.unreadTime) {
					drawUnread(i);
					return;
				}
			}
		}

		public long getUnreadTime () {
			return this.unreadTime;
		}

		private void updateBar () {
			final int position = this.list.getFirstVisiblePosition();
			if (position != this.lastPosition) {
				this.bar.layoutForce(this.list.getRight() - this.barWidth, this.list.getTop(), this.list.getRight(), this.list.getTop() + barHeight(position));
				this.lastPosition = position;

				final long time = this.listAdaptor.getItemTime(position);
				if (time > this.unreadTime) {
					this.unreadTime = time;
					drawUnread(position);
				}
			}
		}

		private void drawUnread (final int position) {
			this.bar.setUnreadSize(barHeight(position));
		}

		private int barHeight (final int position) {
			final int h = (int) ((position
					+ Math.min(position, ACCEL_STOP_1)
					+ Math.min(position, ACCEL_STOP_2)
					+ Math.min(position, ACCEL_STOP_3)) * this.pxPerUnit);
			return h;
		}

		@Override
		public void onScrollStateChanged (final AbsListView view, final int scrollStateFlag) {
			updateBar();
			this.delagate.onScrollStateChanged(view, scrollStateFlag);
		}

		@Override
		public void onScroll (final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
			updateBar();
			this.delagate.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}

	}

}
