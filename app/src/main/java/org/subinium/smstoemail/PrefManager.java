package org.subinium.smstoemail;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefManager {

    private static final PrefManager INSTANCE = new PrefManager();

    private SharedPreferences mPreferences = null;

    private static final String PREF_SERVER_NAME = "saved-smtp-server";
    private static final String PREF_USER_NAME = "saved-username";
    private static final String PREF_PASSWORD = "saved-password";
    private static final String PREF_PORT = "saved-port";
    private static final String PREF_API = "saved-api";

    private static final String DEFAULT_SERVER_NAME = "smtp.gmail.com";
    private static final String DEFAULT_USER_NAME = "";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_PORT = "465";
    private static final String DEFAULT_API = "https://edk.univera.com.tr:8443/mobile/";

    private PrefManager() {

    }

    private void init(Context ctx) {

        if (mPreferences != null) {

            return;
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext());
    }

    public static PrefManager getInstance(Context ctx) {

        INSTANCE.init(ctx);
        return INSTANCE;
    }

    public void setServer(String server) {

        mPreferences.edit().putString(PREF_SERVER_NAME, server).apply();
    }

    public String getServer() {

        return mPreferences.getString(PREF_SERVER_NAME, DEFAULT_SERVER_NAME);
    }

    public void setUsername(String server) {

        mPreferences.edit().putString(PREF_USER_NAME, server).apply();
    }

    public void setPort(String server) {

        mPreferences.edit().putString(PREF_PORT, server).apply();
    }

    public String getUsername() {

        return mPreferences.getString(PREF_USER_NAME, DEFAULT_USER_NAME);
    }

    public void setPassword(String server) {

        mPreferences.edit().putString(PREF_PASSWORD, server).apply();
    }

    public String getPassword() {

        return mPreferences.getString(PREF_PASSWORD, DEFAULT_PASSWORD);
    }

    public String getPort() {

        return mPreferences.getString(PREF_PORT, DEFAULT_PORT);
    }



    public void setAPI(String server) {

        mPreferences.edit().putString(PREF_API, server).apply();
    }

    public String getAPI() {

        return mPreferences.getString(PREF_API, DEFAULT_API);
    }

}
