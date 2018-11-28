package org.mozilla.gecko.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import org.mozilla.gecko.GeckoSharedPrefs;
import org.mozilla.gecko.R;

import static org.mozilla.gecko.preferences.GeckoPreferences.PREFS_BOND_THEME;

/**
 * Copyright © Cliqz 2018
 */
public class PreferenceManager {

    private static final String VPN_SELECTED_COUNTRY = "vpn.selected.country";
    private static final String VPN_START_TIME = "vpn.start.time";
    private static final String PREF_EMAIL_ID = "preferences.email.id";
    private static final String VPN_US_PASSWORD = "vpn.us.password";
    private static final String VPN_DE_PASSWORD = "vpn.de.password";

    private final SharedPreferences mAppSharedPreferences;
    private static PreferenceManager preferenceManager = null;
    private Context mContext;

    private PreferenceManager(Context context) {
        mAppSharedPreferences = GeckoSharedPrefs.forApp(context);
        mContext = context;
    }

    public static PreferenceManager getInstance(Context context) {
        if (preferenceManager == null) {
            preferenceManager = new PreferenceManager(context);
        }
        return preferenceManager;
    }

    public void setMyOffrzOnboardingEnabled(boolean value) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putBoolean(GeckoPreferences.PREFS_MYOFFRZ_ONBOARDING_ENABLED, value).apply();
    }

    public void setTelemetryEnabled(boolean value) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putBoolean(GeckoPreferences.CLIQZ_TELEMETRY_ENABLED, value).apply();
    }

    public void setGhosteryAutoUpdate(boolean value) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putBoolean(GeckoPreferences.PREFS_GHOSTERY_AUTO_UPDATE, value).apply();
    }

    public void setAllowFirstPartyTrackers(boolean value) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putBoolean(GeckoPreferences.PREFS_GHOSTERY_ALLOW_FIRST_PARTY, value).apply();
    }

    public void setBlockNewTrackers(boolean value) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putBoolean(GeckoPreferences.PREFS_GHOSTERY_BLOCK_NEW_TRACKERS, value).apply();
    }

    public void setVpnCountry(String country) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putString(VPN_SELECTED_COUNTRY, country).apply();
    }

    public void setVpnStartTime(long time) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putLong(VPN_START_TIME, time).apply();
    }

    public void setEmailId(String emailId) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putString(PREF_EMAIL_ID, emailId).apply();
    }

    public void setVpnPasswordUs(String password) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putString(VPN_US_PASSWORD, password).apply();
    }

    public void setVpnPasswordDe(String password) {
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putString(VPN_DE_PASSWORD, password).apply();
    }

    public boolean isTelemetryEnabled() {
        return mAppSharedPreferences.getBoolean(GeckoPreferences.CLIQZ_TELEMETRY_ENABLED, false);
    }

    public boolean isGhosteryAutoUpdateEnabled() {
        return mAppSharedPreferences.getBoolean(GeckoPreferences.PREFS_GHOSTERY_AUTO_UPDATE, true);
    }

    public boolean areFirstPartyTrackersAllowed() {
        return mAppSharedPreferences.getBoolean(GeckoPreferences.PREFS_GHOSTERY_ALLOW_FIRST_PARTY, true);
    }

    public boolean areNewTrackersBlocked() {
        return  mAppSharedPreferences.getBoolean(GeckoPreferences.PREFS_GHOSTERY_BLOCK_NEW_TRACKERS, false);
    }

    public boolean isQuickSearchEnabled(){
        return mAppSharedPreferences.getBoolean(GeckoPreferences.PREFS_SEARCH_QUICKSEARCH_ENABLED,true);
    }

    public boolean isHumanWebEnabled(){
        return mAppSharedPreferences.getBoolean(GeckoPreferences.PREFS_ENABLE_HUMAN_WEB,true);
    }

    public void setHumanWebEnabled(boolean value){
        final SharedPreferences.Editor editor = mAppSharedPreferences.edit();
        editor.putBoolean(GeckoPreferences.PREFS_ENABLE_HUMAN_WEB, value).apply();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mAppSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mAppSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public boolean isQuerySuggestionsEnabled(){
        return mAppSharedPreferences.getBoolean(GeckoPreferences.PREFS_SEARCH_QUERY_SUGGESTIONS,
                true);
    }

    public String getSearchRegional() {
        return mAppSharedPreferences.getString(GeckoPreferences.PREFS_SEARCH_REGIONAL,new
                Countries(mContext).getDefaultCountryCode());
    }

    public boolean isAutocompleteEnabled() {
        return mAppSharedPreferences.getBoolean(GeckoPreferences.PREFS_AUTO_COMPLETE, true);
    }

    public boolean isBackgroundEnabled() {
        return  mAppSharedPreferences.getBoolean(GeckoPreferences.PREFS_CLIQZ_TAB_BACKGROUND_ENABLED,true);
    }

    public String getVpnSelectedCountry() {
        return mAppSharedPreferences.getString(VPN_SELECTED_COUNTRY, mContext.getString(R.string.country_germany));
    }

    public long getVpnStartTime() {
        return mAppSharedPreferences.getLong(VPN_START_TIME, System.currentTimeMillis());
	}

    public String getEmailId() {
        return mAppSharedPreferences.getString(PREF_EMAIL_ID, "");
    }

    public String getVpnUsPassword() {
        return mAppSharedPreferences.getString(VPN_US_PASSWORD, "");
    }

    public String getVpnDePassword() {
        return mAppSharedPreferences.getString(VPN_DE_PASSWORD, "");
    }

    public boolean getIsBondThemeWhite() {
        return mAppSharedPreferences.getBoolean(PREFS_BOND_THEME,false);
    }
}
