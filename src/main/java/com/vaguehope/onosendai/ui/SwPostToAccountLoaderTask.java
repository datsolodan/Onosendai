package com.vaguehope.onosendai.ui;

import java.util.List;

import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.vaguehope.onosendai.R;
import com.vaguehope.onosendai.config.Account;
import com.vaguehope.onosendai.provider.TaskUtils;
import com.vaguehope.onosendai.provider.successwhale.EnabledServiceRefs;
import com.vaguehope.onosendai.provider.successwhale.PostToAccount;
import com.vaguehope.onosendai.provider.successwhale.ServiceRef;
import com.vaguehope.onosendai.provider.successwhale.SuccessWhaleException;
import com.vaguehope.onosendai.provider.successwhale.SuccessWhaleProvider;
import com.vaguehope.onosendai.storage.KvStore;
import com.vaguehope.onosendai.ui.SwPostToAccountLoaderTask.AccountLoaderResult;
import com.vaguehope.onosendai.util.LogWrapper;

class SwPostToAccountLoaderTask extends AsyncTask<Account, AccountLoaderResult, AccountLoaderResult> {

	private static final LogWrapper LOG = new LogWrapper("AL");

	private final KvStore kvStore;
	private final ViewGroup llSubAccounts;
	private final EnabledServiceRefs enabledSubAccounts;

	private ProgressBar progressBar;

	public SwPostToAccountLoaderTask (final KvStore kvStore, final ViewGroup llSubAccounts, final EnabledServiceRefs enabledSubAccounts) {
		super();
		this.kvStore = kvStore;
		this.llSubAccounts = llSubAccounts;
		this.enabledSubAccounts = enabledSubAccounts;
	}

	@Override
	protected void onPreExecute () {
		this.llSubAccounts.removeAllViews();
		showInProgress();
		this.llSubAccounts.setVisibility(View.VISIBLE);
	}

	@Override
	protected AccountLoaderResult doInBackground (final Account... params) {
		if (params.length != 1) throw new IllegalArgumentException("Only one account per task.");
		final Account account = params[0];

		SuccessWhaleProvider swProv = new SuccessWhaleProvider(this.kvStore);
		try {
			final List<PostToAccount> cached = swProv.getPostToAccountsCached(account);
			if (cached != null) publishProgress(new AccountLoaderResult(cached));
			return new AccountLoaderResult(swProv.getPostToAccounts(account));
		}
		catch (SuccessWhaleException e) {
			return new AccountLoaderResult(e);
		}
		finally {
			swProv.shutdown();
		}
	}

	@Override
	protected void onProgressUpdate (final AccountLoaderResult... results) {
		if (results == null || results.length < 1) return;
		final AccountLoaderResult result = results[0];
		if (result != null && result.isSuccess()) displayAccounts(result);
	}

	@Override
	protected void onPostExecute (final AccountLoaderResult result) {
		hideInProgress();
		if (result == null) return;
		if (result.isSuccess()) {
			displayAccounts(result);
		}
		else {
			LOG.w("Failed to update SW post to accounts: %s", result.getE());
			Toast.makeText(this.llSubAccounts.getContext(),
					"Failed to update sub accounts: " + result.getEmsg(),
					Toast.LENGTH_LONG).show();
		}
	}

	private void showInProgress () {
		this.progressBar = new ProgressBar(this.llSubAccounts.getContext());
		this.progressBar.setIndeterminate(true);
		this.llSubAccounts.addView(this.progressBar);
	}

	private void hideInProgress () {
		this.llSubAccounts.removeView(this.progressBar);
	}

	private void displayAccounts (final AccountLoaderResult result) {
		for (final PostToAccount pta : result.getAccounts()) {
			View existingView = this.llSubAccounts.findViewWithTag(pta.getUid());
			if (existingView == null) {
				final View view = View.inflate(this.llSubAccounts.getContext(), R.layout.subaccountitem, null);
				view.setTag(pta.getUid());
				configureAccountBtn((ToggleButton) view.findViewById(R.id.btnEnableAccount), pta);
				this.llSubAccounts.addView(view);
			}
		}
	}

	private void configureAccountBtn (final ToggleButton btn, final PostToAccount pta) {
		final String displayName = pta.getDisplayName();
		btn.setTextOn(displayName);
		btn.setTextOff(displayName);

		final ServiceRef svc = pta.toSeviceRef();
		boolean checked = false;
		if (this.enabledSubAccounts.isEnabled(svc)) {
			checked = true;
		}
		else if (!this.enabledSubAccounts.isServicesPreSpecified()) {
			checked = pta.isEnabled();
			if (checked) this.enabledSubAccounts.enable(svc);
		}
		btn.setChecked(checked);

		btn.setOnClickListener(new SubAccountToggleListener(svc, this.enabledSubAccounts));
	}

	public static void setExclusiveSelectedAccountBtn (final ViewGroup llSubAccounts, final ServiceRef svc) {
		final int childCount = llSubAccounts.getChildCount();
		if (childCount < 1) return;
		for (int i = 0; i < childCount; i++) {
			final View child = llSubAccounts.getChildAt(i);
			final ToggleButton btn = (ToggleButton) child.findViewById(R.id.btnEnableAccount);
			if (btn == null) continue;
			btn.setChecked(svc.getUid().equals(child.getTag()));
		}
	}

	protected static class AccountLoaderResult {

		private final boolean success;
		private final List<PostToAccount> accounts;
		private final Exception e;

		public AccountLoaderResult (final List<PostToAccount> accounts) {
			if (accounts == null) throw new IllegalArgumentException("Missing arg: accounts.");
			this.success = true;
			this.accounts = accounts;
			this.e = null;
		}

		public AccountLoaderResult (final Exception e) {
			if (e == null) throw new IllegalArgumentException("Missing arg: e.");
			this.success = false;
			this.accounts = null;
			this.e = e;
		}

		public boolean isSuccess () {
			return this.success;
		}

		public List<PostToAccount> getAccounts () {
			return this.accounts;
		}

		public Exception getE () {
			return this.e;
		}

		public String getEmsg () {
			return TaskUtils.getEmsg(this.e);
		}

	}

	private static class SubAccountToggleListener implements OnClickListener {

		private final ServiceRef svc;
		private final EnabledServiceRefs enabledSubAccounts;

		public SubAccountToggleListener (final ServiceRef svc, final EnabledServiceRefs enabledSubAccounts) {
			this.svc = svc;
			this.enabledSubAccounts = enabledSubAccounts;
		}

		@Override
		public void onClick (final View v) {
			if (!(v instanceof ToggleButton)) return;
			ToggleButton btn = (ToggleButton) v;
			if (btn.isChecked()) {
				this.enabledSubAccounts.enable(this.svc);
			}
			else {
				this.enabledSubAccounts.disable(this.svc);
			}
		}
	}

}
