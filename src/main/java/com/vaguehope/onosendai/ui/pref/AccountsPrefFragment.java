package com.vaguehope.onosendai.ui.pref;

import java.util.List;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.vaguehope.onosendai.config.Account;
import com.vaguehope.onosendai.config.AccountProvider;
import com.vaguehope.onosendai.config.Prefs;
import com.vaguehope.onosendai.ui.pref.TwitterOauthWizard.TwitterOauthCallback;
import com.vaguehope.onosendai.util.DialogHelper;
import com.vaguehope.onosendai.util.LogWrapper;

public class AccountsPrefFragment extends PreferenceFragment {

	// TODO merge these lists.
	protected static final AccountProvider[] NEW_ACCOUNT_PROVIDERS = new AccountProvider[] {
			AccountProvider.TWITTER,
			AccountProvider.SUCCESSWHALE
	};
	private static final String[] NEW_ACCOUNT_LABELS = new String[] {
			AccountProvider.TWITTER.toHumanString(),
			AccountProvider.SUCCESSWHALE.toHumanString()
	};

	private static final LogWrapper LOG = new LogWrapper("APF");

	private Prefs prefs;

	@Override
	public void onCreate (final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
		this.prefs = new Prefs(getPreferenceManager());
		refreshAccountsList();
	}

	protected Prefs getPrefs () {
		if (this.prefs == null) throw new IllegalStateException("Prefs has not been initialised.");
		return this.prefs;
	}

	protected void refreshAccountsList () {
		getPreferenceScreen().removeAll();

		final Preference pref = new Preference(getActivity());
		pref.setTitle("Add Account");
		pref.setSummary("Add a new Twitter or SuccessWhale account");
		pref.setOnPreferenceClickListener(new AddAcountClickListener(this));
		getPreferenceScreen().addPreference(pref);

		final List<String> accountIds = getPrefs().readAccountIds();
		for (final String accountId : accountIds) {
			try {
				final Account account = getPrefs().readAccount(accountId);
				getPreferenceScreen().addPreference(new AccountDialogPreference(getActivity(), account, this));
			}
			catch (final JSONException e) {
				DialogHelper.alert(getActivity(), "Failed to read account: ", e);
			}
		}
	}

	protected void promptNewAccountType () {
		final AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
		bld.setTitle("Account Type");
		bld.setNegativeButton("Cancel", DialogHelper.DLG_CANCEL_CLICK_LISTENER);
		bld.setItems(NEW_ACCOUNT_LABELS, new DialogInterface.OnClickListener() {
			@Override
			public void onClick (final DialogInterface dialog, final int item) {
				dialog.dismiss();
				promptAddAccount(NEW_ACCOUNT_PROVIDERS[item]);
			}
		});
		bld.show();
	}

	protected void promptAddAccount (final AccountProvider accountProvider) {
		switch (accountProvider) {
			case TWITTER:
				promptAddTwitterAccount();
				break;
			case SUCCESSWHALE:
				promptAddSuccessWhaleAccount();
				break;
			default:
				DialogHelper.alert(getActivity(), "Do not know how to add account of type: " + accountProvider);
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private TwitterOauthWizard twitterOauthWizard;

	private void initTwitterOauthWizard () {
		if (this.twitterOauthWizard != null) return;
		this.twitterOauthWizard = new TwitterOauthWizard(getActivity(), getPrefs(), new TwitterOauthCallback() {
			@Override
			public void deligateStartActivityForResult (final Intent intent, final int requestCode) {
				startActivityForResult(intent, requestCode);
			}
			@Override
			public void onAccountAdded (final Account account) {
				refreshAccountsList();
			}
		});
	}

	private void promptAddTwitterAccount () {
		initTwitterOauthWizard();
		this.twitterOauthWizard.start();
	}

	@Override
	public void onActivityResult (final int requestCode, final int resultCode, final Intent intent) {
		LOG.d("onActivityResult(%d, %d, %s)", requestCode, resultCode, intent);
		super.onActivityResult(requestCode, resultCode, intent);
		this.twitterOauthWizard.onActivityResult(requestCode, resultCode, intent);
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private void promptAddSuccessWhaleAccount () {
		final String id = getPrefs().getNextAccountId();
		final AccountDialog dlg = new AccountDialog(getActivity(), id, AccountProvider.SUCCESSWHALE);

		final AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getActivity());
		dlgBuilder.setTitle("New Account (" + id + ")");
		dlgBuilder.setView(dlg.getRootView());
		dlgBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick (final DialogInterface dialog, final int which) {
				dialog.dismiss();
				try {
					getPrefs().writeNewAccount(dlg.getValue());
				}
				catch (final JSONException e) {
					DialogHelper.alert(getActivity(), "Failed to write new account: ", e);
				}
				refreshAccountsList();
			}
		});
		dlgBuilder.setNegativeButton("Cancel", DialogHelper.DLG_CANCEL_CLICK_LISTENER);
		dlgBuilder.create().show();
	}

	private static class AddAcountClickListener implements OnPreferenceClickListener {

		private final AccountsPrefFragment accountsPrefFragment;

		public AddAcountClickListener (final AccountsPrefFragment accountsPrefFragment) {
			this.accountsPrefFragment = accountsPrefFragment;
		}

		@Override
		public boolean onPreferenceClick (final Preference preference) {
			this.accountsPrefFragment.promptNewAccountType();
			return true;
		}
	}

}
