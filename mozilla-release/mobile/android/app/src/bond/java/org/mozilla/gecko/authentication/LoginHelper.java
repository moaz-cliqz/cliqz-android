package org.mozilla.gecko.authentication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.protobuf.GeneratedMessageLite;

import org.mozilla.gecko.Error;
import org.mozilla.gecko.ErrorCode;
import org.mozilla.gecko.IsDeviceActivatedResponse;
import org.mozilla.gecko.OVPNResponse;
import org.mozilla.gecko.R;
import org.mozilla.gecko.Response;
import org.mozilla.gecko.Utils;
import org.mozilla.gecko.preferences.PreferenceManager;
import org.mozilla.gecko.vpn.ConfigConverter;
import org.mozilla.gecko.vpn.core.ProfileManager;

import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.mozilla.gecko.InputMethods.getInputMethodManager;

/**
 * Copyright © Cliqz 2018
 */
public class LoginHelper implements View.OnClickListener, TalkToServer.ServerCallbacks {

    private static final String LOGTAG = LoginHelper.class.getSimpleName();
    private static final long POLL_INTERVAL = 10 * 1000; //10 seconds

    private Activity mActivity;
    private ViewStub mLoginScreenStub;
    private ImageView mLoginIcon;
    private TextView mLoginTitle;
    private TextView mLoginDescription;
    private TextView mErrorMessageTextView;
    private EditText mLoginInputField;
    private Button mContinueButton;
    private Button mResendButton;
    private TextView mFooterTextView;

    private String mSecretKey;
    private PreferenceManager mPreferenceManager;
    private Timer mTimer;
    private String mEmailId;
    private LoginStatus mLoginState = LoginStatus.REGISTRATION;
    private enum LoginStatus {REGISTRATION, ACTIVATION, WELCOME}

    @SuppressLint("HardwareIds")
    public LoginHelper(AppCompatActivity activity) {
        mActivity = activity;
        mSecretKey = Settings.Secure.getString(mActivity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        mPreferenceManager =
            PreferenceManager.getInstance(activity.getBaseContext());
    }

    public void start() {
        if (!autoLogin()) {
            initViews();
            showLoginScreen();
        }
    }

    private boolean autoLogin() {
        importVpnProfiles();
        mEmailId = mPreferenceManager.getEmailId();
        if (!mEmailId.isEmpty()) {
            checkDeviceActivated();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_continue_button) {
            switch (mLoginState) {
                case REGISTRATION:
                    mEmailId = mLoginInputField.getText().toString();
                    if (!Utils.validateEmail(mEmailId)) {
                        mErrorMessageTextView
                            .setText(mActivity
                                    .getString(R.string.error_invalid_email));
                    } else {
                        mErrorMessageTextView.setText("");
                        registerDevice();
                    }
                    break;
                case ACTIVATION:
                    openMailApp();
                    break;
                case WELCOME:
                    hideLoginScreen();
                    break;
                default:
                    break;
            }
            getInputMethodManager(mActivity)
                .hideSoftInputFromWindow(mLoginInputField.getWindowToken(), 0);
        } else if (view.getId() == R.id.login_resend_button) {
            mErrorMessageTextView.setText("");
            showResendActivationLinkDialog();
            resendActivation();
        }
    }

    private void openMailApp() {
        try {
            final Intent emailIntent = new Intent(Intent.ACTION_VIEW);
            emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
            mActivity.startActivity(emailIntent);
        } catch (Exception e) {
            Log.e(LOGTAG, "no email app exist to be opened");
        }
    }

    private void showResendActivationLinkDialog() {
        new AlertDialog.Builder(mActivity)
            .setTitle(mActivity.getString(R.string.login_resend_activation_dialog_title))
            .setMessage(R.string.login_resend_activation_dialog_description)
            .setPositiveButton(mActivity.getString(R.string.open_mail_app),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openMailApp();
                    }
                })
            .setNegativeButton(mActivity.getString(R.string.default_browser_dialog_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onServerReplied(GeneratedMessageLite serverResponse,
                                TalkToServer.Cases whichCase) {
        switch (whichCase) {
            case IS_DEVICE_ACTIVATED:
                final IsDeviceActivatedResponse activatedRes =
                    (IsDeviceActivatedResponse) serverResponse;
                if (activatedRes.getErrorCount() > 0) {
                    if (activatedRes.getErrorList().get(0).getCode() ==
                            ErrorCode.SERVER_NOT_REACHED) {
                        initViews();
                        showLoginScreen();
                        mErrorMessageTextView
                            .setText(mActivity.getString(R.string.error_server_not_reached));
                    } else {
                        Log.e(LOGTAG, "Device not activated yet");
                        initViews();
                        showActivationScreen();
                    }
                    getVpnCreds(mPreferenceManager.getEmailId());
                }
                break;
            case REGISTER_DEVICE:
                final Response registerRes = (Response) serverResponse;
                if (registerRes.getErrorCount() > 0) {
                    Log.e(LOGTAG, "Error registering device.");
                    final Error error = registerRes.getErrorList().get(0);
                    Log.e(LOGTAG, error.getMsg());

                    if (error.getCode() == ErrorCode.DEVICE_EXISTS) {
                        showActivationScreen();
                    } else if (error.getCode() == ErrorCode.SERVER_NOT_REACHED) {
                        mErrorMessageTextView
                            .setText(mActivity.getString(R.string.error_server_not_reached));
                    }
                } else {
                    showActivationScreen();
                }
                break;
            case RESEND_ACTIVATION:
                final Response resendRes = (Response) serverResponse;
                if (resendRes.getErrorCount() > 0) {
                    Log.e(LOGTAG, "can't resend the activation again");
                    if (resendRes.getErrorList().get(0).getCode() ==
                            ErrorCode .SERVER_NOT_REACHED) {
                        mErrorMessageTextView
                            .setText(mActivity.getString(R.string .error_server_not_reached));
                    } else {
                        mErrorMessageTextView
                            .setText(mActivity.getString(R.string .error_resend_activation));
                    }
                }
                break;
            case WAIT_FOR_ACTIVATION:
                final IsDeviceActivatedResponse waitActivationRes =
                        (IsDeviceActivatedResponse) serverResponse;
                if (waitActivationRes.getErrorCount() > 0) {
                    Log.e(LOGTAG, "device is still not active");
                } else {
                    mTimer.cancel();
                    mPreferenceManager.setEmailId(mEmailId);
                    showWelcomeScreen();
                    getVpnCreds(mPreferenceManager.getEmailId());
                }
                break;
            case GET_VPN_CREDS:
                if (((OVPNResponse)serverResponse).getErrorCount() > 0) {
                    Log.e(LOGTAG, "error getting vpn creds");
                } else {
                    mPreferenceManager.setVpnPasswordUs(((OVPNResponse)serverResponse).getConfigMap().get("us").getPassword());
                    mPreferenceManager.setVpnPasswordDe(((OVPNResponse)serverResponse).getConfigMap().get("de").getPassword());
                }
                break;
        }
    }

    private void checkDeviceActivated() {
        new TalkToServer(this, TalkToServer.Cases.IS_DEVICE_ACTIVATED,
                         mEmailId, mSecretKey).execute();
    }

    private void registerDevice() {
        new TalkToServer(this, TalkToServer.Cases.REGISTER_DEVICE,
                         mEmailId, mSecretKey).execute();
    }

    private void resendActivation() {
        new TalkToServer(LoginHelper.this, TalkToServer.Cases.RESEND_ACTIVATION,
                mEmailId, mSecretKey) .execute();
	}

    private void getVpnCreds(String emailId) {
        new TalkToServer(this, TalkToServer.Cases.GET_VPN_CREDS, emailId, mSecretKey).execute();
    }

    private void pollServerForActivation() {
        final Handler handler = new Handler();
        mTimer = new Timer();
        final TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        new TalkToServer(LoginHelper.this,
                                TalkToServer.Cases.WAIT_FOR_ACTIVATION,
                                mEmailId, mSecretKey).execute();
                    }
                });
            }
        };
        //we should define a limit for how long we keep polling
        mTimer.schedule(doAsynchronousTask, 0, POLL_INTERVAL); //execute in every 10secs
    }

    private void initViews() {
        mLoginScreenStub = mActivity.findViewById(R.id.bond_login_screen_stub);
        mLoginScreenStub.setLayoutResource(R.layout.bond_login_screen);
        final ScrollView loginScreenView = (ScrollView) mLoginScreenStub.inflate();
        mLoginIcon = (ImageView) loginScreenView.findViewById(R.id.login_icon);
        mLoginTitle = (TextView) loginScreenView.findViewById(R.id.login_title);
        mLoginDescription = (TextView) loginScreenView.findViewById(R.id.login_description);
        mErrorMessageTextView = (TextView) loginScreenView.findViewById(R.id.login_error_message);
        mLoginInputField = loginScreenView.findViewById(R.id.login_input_field);
        mLoginInputField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mContinueButton.performClick();
                }
                return true;
            }
        });
        mContinueButton = loginScreenView.findViewById(R.id.login_continue_button);
        mContinueButton.setOnClickListener(this);
        mResendButton = loginScreenView.findViewById(R.id.login_resend_button);
        mResendButton.setOnClickListener(this);
        mFooterTextView = (TextView) loginScreenView.findViewById(R.id.login_footer_text);
    }

    private void setViewsValues(LoginStatus loginState, int iconId,
                                int titleId, int descriptionId,
                                int inputFieldHintId, int inputFieldVisibility,
                                int resendVisibility, int continueButtonTextId,
                                int footerTextId, int footerTextVisibility) {
        mLoginState = loginState;
        final Drawable drawable = VectorDrawableCompat
            .create(mActivity.getResources(), iconId, null);
        mLoginIcon.setImageDrawable(drawable);
        mLoginTitle.setText(mActivity.getString(titleId));
        mLoginDescription.setText(mActivity.getString(descriptionId));
        mLoginInputField.setVisibility(inputFieldVisibility);
        if (inputFieldVisibility == VISIBLE) {
            mLoginInputField.setHint(mActivity.getString(inputFieldHintId));
        }
        mContinueButton.setText(mActivity.getString(continueButtonTextId));
        mResendButton.setVisibility(resendVisibility);

        mFooterTextView.setVisibility(footerTextVisibility);
    }

    private void showLoginScreen() {
        setViewsValues(LoginStatus.REGISTRATION, R.mipmap.lumen_logo,
                       R.string.browserName, R.string.login_description,
                       R.string.login_input_hint, VISIBLE, GONE,
                       R.string.login_continue_button, R.string.login_footer,
                       VISIBLE);
    }

    private void showActivationScreen() {
        setViewsValues(LoginStatus.ACTIVATION, R.drawable.login_circle,
                       R.string.login_activation_title,
                       R.string.login_activation_description, -1, GONE,
                       VISIBLE, R.string.open_mail_app, -1, GONE);
        pollServerForActivation();
    }

    private void showWelcomeScreen() {
        setViewsValues(LoginStatus.WELCOME, R.drawable.login_welcome,
                       R.string.login_welcome_title,
                       R.string.login_welcome_description, -1, GONE, GONE,
                       R.string.login_continue_button, -1, GONE);
    }

    private void hideLoginScreen() {
        mLoginScreenStub.setVisibility(View.GONE);
    }

    public boolean backPressed() {
        if (mLoginState == LoginStatus.ACTIVATION) {
            showLoginScreen();
            return true;
        }
        return false;
    }

    private void importVpnProfiles() {
        final ProfileManager profileManager = ProfileManager.getInstance(mActivity);
        if (profileManager.getProfileByName("us-vpn") == null) {
            final Uri usVpnUri = Uri.parse("android.resource://" + mActivity.getPackageName() + "/raw/us");
            final ConfigConverter usConvertor = new ConfigConverter(mActivity);
            usConvertor.startImportTask(usVpnUri, "us-vpn");
		}
		if (profileManager.getProfileByName("germany-vpn") == null) {
            final Uri germanyVpnUri = Uri.parse("android.resource://" + mActivity.getPackageName() + "/raw/germany");
            final ConfigConverter deConvertor = new ConfigConverter(mActivity);
            deConvertor.startImportTask(germanyVpnUri, "germany-vpn");
        }
    }
}
