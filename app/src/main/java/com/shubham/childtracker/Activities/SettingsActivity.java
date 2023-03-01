package com.shubham.childtracker.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.shubham.childtracker.DialogFragments.AccountDeleteDialogFragment;
import com.shubham.childtracker.DialogFragments.ConfirmationDialogFragment;
import com.shubham.childtracker.DialogFragments.LanguageSelectionDialogFragment;
import com.shubham.childtracker.DialogFragments.PasswordChangingDialogFragment;
import com.shubham.childtracker.R;
import com.shubham.childtracker.databinding.ActivitySettingsBinding;
import com.shubham.childtracker.interfaces.OnConfirmationListener;
import com.shubham.childtracker.interfaces.OnDeleteAccountListener;
import com.shubham.childtracker.interfaces.OnLanguageSelectionListener;
import com.shubham.childtracker.interfaces.OnPasswordChangeListener;
import com.shubham.childtracker.utils.AccountUtils;
import com.shubham.childtracker.utils.Constant;
import com.shubham.childtracker.utils.LocaleUtils;
import com.shubham.childtracker.utils.SharedPrefsUtils;

public class SettingsActivity extends AppCompatActivity implements OnLanguageSelectionListener, OnConfirmationListener, OnPasswordChangeListener, OnDeleteAccountListener {

	ActivitySettingsBinding binding;

	@SuppressLint("UseCompatLoadingForDrawables")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivitySettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		ImageButton btnBack = findViewById(R.id.btnBack);
		btnBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));

		btnBack.setOnClickListener(v -> onBackPressed());

		ImageButton btnSettings = findViewById(R.id.btnSettings);

		btnSettings.setVisibility(View.GONE);

		TextView txtTitle = findViewById(R.id.txtTitle);
		txtTitle.setText(getString(R.string.settings));

		binding.btnLanguageSelection.setOnClickListener(view -> selectLanguage());

		binding.btnLogout.setOnClickListener(view -> logout());

		binding.btnChangePassword.setOnClickListener(view -> changePassword());

		binding.btnDeleteAccount.setOnClickListener(view -> deleteAccount());

		binding.btnAbout.setOnClickListener(view -> showAbout());

		binding.btnSendFeedBack.setOnClickListener(view -> SendFeedBack());

		binding.btnVisitWebsite.setOnClickListener(view -> visitWebsite());
		
	}
	
	private void showAbout() {
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	private void SendFeedBack() {
		String body = null;
		try {
			body = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " + Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND + "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"telphatech@gmail.com"});
		intent.putExtra(Intent.EXTRA_SUBJECT, "Child Tracker Feedback");
		intent.putExtra(Intent.EXTRA_TEXT, body);
		startActivity(Intent.createChooser(intent, getString(R.string.choose_email_client)));
	}
	
	private void visitWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://telphatech.in/"));
		startActivity(intent);
	}
	
	private void deleteAccount() {
		AccountDeleteDialogFragment accountDeleteDialogFragment = new AccountDeleteDialogFragment();
		accountDeleteDialogFragment.setCancelable(false);
		accountDeleteDialogFragment.show(getSupportFragmentManager(), Constant.ACCOUNT_DELETE_DIALOG_FRAGMENT_TAG);
	}
	
	private void changePassword() {
		PasswordChangingDialogFragment passwordChangingDialogFragment = new PasswordChangingDialogFragment();
		passwordChangingDialogFragment.setCancelable(false);
		passwordChangingDialogFragment.show(getSupportFragmentManager(), Constant.PASSWORD_CHANGING_DIALOG_FRAGMENT_TAG);
	}
	
	private void logout() {
		ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constant.CONFIRMATION_MESSAGE, getString(R.string.do_you_want_to_logout));
		confirmationDialogFragment.setArguments(bundle);
		confirmationDialogFragment.setCancelable(false);
		confirmationDialogFragment.show(getSupportFragmentManager(), Constant.CONFIRMATION_FRAGMENT_TAG);
	}
	
	private void selectLanguage() {
		LanguageSelectionDialogFragment languageSelectionDialogFragment = new LanguageSelectionDialogFragment();
		languageSelectionDialogFragment.setCancelable(false);
		languageSelectionDialogFragment.show(getSupportFragmentManager(), Constant.LANGUAGE_SELECTION_DIALOG_FRAGMENT_TAG);
	}
	
	@Override
	public void onLanguageSelection(String language) {
		String appLanguage = SharedPrefsUtils.getStringPreference(this, Constant.APP_LANGUAGE, "en");
		if (language.equals("English") && !appLanguage.equals("en")) {
			LocaleUtils.setLocale(this, "en");
		} else if (language.equals("Arabic") && !appLanguage.equals("ar")) {
			LocaleUtils.setLocale(this, "ar");
			
		}
		
		restartApp();
		
	}
	
	private void restartApp() {
		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		
	}
	
	@Override
	public void onConfirm() {
		AccountUtils.logout(this);
	}
	
	@Override
	public void onConfirmationCancel() {
		//DO NOTHING
	}

	@Override
	public void onModeSelected(String parentEmail) {

	}

	@Override
	public void onPasswordChange(String newPassword) {
		AccountUtils.changePassword(this, newPassword);
		
	}
	
	@Override
	public void onDeleteAccount(String password) {
		AccountUtils.deleteAccount(this, password);
		
	}
}
