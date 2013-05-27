package com.vaguehope.onosendai.provider.successwhale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.vaguehope.onosendai.config.Account;
import com.vaguehope.onosendai.storage.VolatileKvStore;
import com.vaguehope.onosendai.util.DialogHelper;
import com.vaguehope.onosendai.util.DialogHelper.Listener;
import com.vaguehope.onosendai.util.LogWrapper;
import com.vaguehope.onosendai.util.Result;

public class SuccessWhaleSourcesFetcher extends AsyncTask<Void, Void, Result<SuccessWhaleSources>> {

	private static final LogWrapper LOG = new LogWrapper("SWSF");

	private final Activity activity;
	private final Account account;
	private final Listener<SuccessWhaleSources> onSources;

	private ProgressDialog dialog;

	public SuccessWhaleSourcesFetcher (final Activity activity, final Account account, final Listener<SuccessWhaleSources> onSources) {
		this.activity = activity;
		this.account = account;
		this.onSources = onSources;
	}

	@Override
	protected void onPreExecute () {
		this.dialog = ProgressDialog.show(this.activity, "SuccessWhale", "Fetching sources...", true);
	}

	@Override
	protected Result<SuccessWhaleSources> doInBackground (final Void... params) {
		final SuccessWhaleProvider swProv = new SuccessWhaleProvider(new VolatileKvStore());
		try {
			return new Result<SuccessWhaleSources>(swProv.getSources(this.account));
		}
		catch (SuccessWhaleException e) {
			return new Result<SuccessWhaleSources>(e);
		}
		finally {
			swProv.shutdown();
		}
	}

	@Override
	protected void onPostExecute (final Result<SuccessWhaleSources> result) {
		if (result.isSuccess()) {
			this.onSources.onAnswer(result.getData());
			this.dialog.dismiss();
		}
		else {
			this.dialog.dismiss();
			LOG.e("Failed fetch SuccessWhale sources.", result.getE());
			DialogHelper.alert(this.activity, result.getE());
		}
	}

}
