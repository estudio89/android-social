package br.com.estudio89.androidsocial;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by luccascorrea on 1/7/16.
 *
 */
public abstract class AbstractSocialAuth implements SocialAuth {
    protected static final String SOCIAL_AUTH_PREFERENCES_FILE = "br.com.estudio89.social_auth";
    protected static final String LOGGED_IN_KEY = "logged_in";
    protected static final String NAME_KEY = "name";
    protected static final String EMAIL_KEY = "email";
    protected static final String TOKEN_KEY = "token";
    protected static final String USER_ID_KEY = "user_id";

    @NonNull
    protected Context context;

    @Nullable
    protected SocialAuthListener listener;

    public AbstractSocialAuth(@NonNull Context context, @Nullable SocialAuthListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * This method should be called whenever login is finished successfully, marking the user as logged in.
     *
     * @param loggedIn boolean
     */
    protected void setLoginStatus(boolean loggedIn) {

        SharedPreferences sharedPref = context.getSharedPreferences(SOCIAL_AUTH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getSocialAuthIdentifier() + "." + LOGGED_IN_KEY, loggedIn);
        editor.commit();
    }

    /**
     * Stores the authentication data. This method must be called by child classes right after
     * authentication was successful.
     *  @param name
     * @param email
     * @param token
     * @param userId
     */
    protected void storeAuthData(String name, String email, String token, String userId) {
        SharedPreferences sharedPref = context.getSharedPreferences(SOCIAL_AUTH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getSocialAuthIdentifier() + "." + NAME_KEY, name);
        editor.putString(getSocialAuthIdentifier() + "." + EMAIL_KEY, email);
        editor.putString(getSocialAuthIdentifier() + "." + TOKEN_KEY, token);
        editor.putString(getSocialAuthIdentifier() + "." + USER_ID_KEY, userId);
        editor.commit();
    }

    /**
     * Returns an array with the social authentication data.
     *
     * @return social authentication data where data[0] is the user's name, data[1] is the user's
     * email address, data[2] is the token and data[3] is the user's id.
     */
    public String[] getAuthData() {
        String[] authData = new String[3];
        SharedPreferences sharedPref = context.getSharedPreferences(SOCIAL_AUTH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        String name = sharedPref.getString(getSocialAuthIdentifier() + "." + NAME_KEY, null);
        String email = sharedPref.getString(getSocialAuthIdentifier() + "." + EMAIL_KEY, null);
        String token = sharedPref.getString(getSocialAuthIdentifier() + "." + TOKEN_KEY, null);
        String userId = sharedPref.getString(getSocialAuthIdentifier() + "." + USER_ID_KEY, null);
        if (name != null && email != null && token != null) {
            authData[0] = name;
            authData[1] = email;
            authData[2] = token;
            authData[3] = userId;
        }

        return authData;
    }

    protected void clearAuthData() {
        SharedPreferences sharedPref = context.getSharedPreferences(SOCIAL_AUTH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(getSocialAuthIdentifier() + "." + NAME_KEY);
        editor.remove(getSocialAuthIdentifier() + "." + EMAIL_KEY);
        editor.remove(getSocialAuthIdentifier() + "." + TOKEN_KEY);
        editor.commit();
    }

    @Override
    public void logout() {
        setLoginStatus(false);
        clearAuthData();
        if (this.listener != null) {
            this.listener.onSocialLogout(getSocialAuthIdentifier());
        }
    }

    @Override
    public boolean isLoggedIn() {
        SharedPreferences sharedPref = context.getSharedPreferences(SOCIAL_AUTH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(getSocialAuthIdentifier() + "." + LOGGED_IN_KEY, false);
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }
}
