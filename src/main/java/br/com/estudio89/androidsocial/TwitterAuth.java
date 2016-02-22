package br.com.estudio89.androidsocial;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import io.fabric.sdk.android.Fabric;

/**
 * Created by luccascorrea on 2/15/16.
 *
 */
public class TwitterAuth extends AbstractSocialAuth implements View.OnClickListener {
    private TwitterLoginButton button;
    public static String TWITTER_CONSUMER_KEY_STRING = "twitter_consumer_key";
    public static String TWITTER_CONSUMER_SECRET_STRING = "twitter_consumer_secret";

    public TwitterAuth(@NonNull Context context, @Nullable SocialAuthListener listener) {
        super(context, listener);
    }

    @Override
    public void initializeSDK(AppCompatActivity activity) {
        int keyResId = activity.getResources().getIdentifier(TWITTER_CONSUMER_KEY_STRING, "string", activity.getApplication().getPackageName());
        int secretResId = activity.getResources().getIdentifier(TWITTER_CONSUMER_SECRET_STRING, "string", activity.getApplication().getPackageName());
        TwitterAuthConfig authConfig = new TwitterAuthConfig(activity.getString(keyResId), activity.getString(secretResId));
        Fabric.with(activity, new Twitter(authConfig));
    }

    @Override
    public void setupLogin(AppCompatActivity activity, int loginBtnId) {
        button = (TwitterLoginButton) activity.findViewById(loginBtnId);

        button.setOnClickListener(this);
        button.setCallback(new Callback<TwitterSession>() {

            // Logging in
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = Twitter.getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                final String token = authToken.token + "|" + authToken.secret;
                final String userId = String.valueOf(result.data.getUserId());

                TwitterAuthClient authClient = new TwitterAuthClient();

                // Requesting e-mail
                authClient.requestEmail(session, new Callback<String>() {

                    @Override
                    public void success(Result<String> result) {

                        final String email = result.data;
                        requestUserName(token, email, userId);

                    }

                    @Override
                    public void failure(TwitterException e) {
                        // Getting the user's e-mail may fail if the user denied access
                        requestUserName(token, null, userId);
                    }
                });

            }

            @Override
            public void failure(TwitterException e) {
                if (listener != null) {
                    listener.onSocialAuthFailed(button, getSocialAuthIdentifier(), e.getMessage());
                }
            }
        });
    }

    private void requestUserName(final String token, final String email, final String userId) {
        Twitter.getApiClient().getAccountService().verifyCredentials(true, false, new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                String name = result.data.name;
                if (listener != null) {
                    setLoginStatus(true);
                    storeAuthData(name, email, token, userId);
                    listener.onSocialAuthSuccess(button, getSocialAuthIdentifier(), token, email, name, userId);
                }
            }

            @Override
            public void failure(TwitterException e) {
                twitterLogout();
                if (listener != null) {
                    listener.onSocialAuthFailed(button, getSocialAuthIdentifier(), e.getMessage());
                }
            }
        });
    }
    @NonNull
    @Override
    public String getSocialAuthIdentifier() {
        return "twitter";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass the activity result to the login button.
        button.onActivityResult(requestCode, resultCode, data);
    }

    private void twitterLogout() {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();
        Twitter.getSessionManager().clearActiveSession();
        Twitter.logOut();
    }

    @Override
    public void logout() {
        if (!isLoggedIn()) {
            return;
        }

        twitterLogout();
        super.logout();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == button.getId()) {
            if (this.listener != null) {
                this.listener.onSocialLoginButtonClicked(v);
            }
        }
    }
}
