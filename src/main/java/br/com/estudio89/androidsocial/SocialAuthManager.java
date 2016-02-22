package br.com.estudio89.androidsocial;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by luccascorrea on 1/7/16.
 */
public class SocialAuthManager extends AbstractSocialAuth {


    public static class Builder {
        @NonNull private Context context;
        @Nullable private SocialAuthListener listener;
        private HashMap<String, Integer> buttonIds = new HashMap<>();

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setListener(@Nullable SocialAuthListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setLoginButtonId(String socialNetworkIdentifier, int resId) {
            buttonIds.put(socialNetworkIdentifier, resId);
            return this;
        }

        public SocialAuthManager build() {
            return new SocialAuthManager(context, listener, buttonIds);
        }
    }

    @NonNull
    private List<SocialAuth> socialAuths = new ArrayList<>();
    private HashMap<String, Integer> buttonIds = new HashMap<>();

    protected SocialAuthManager(@NonNull Context context, @Nullable SocialAuthListener listener, HashMap<String, Integer> buttonIds) {
        super(context, listener);
        this.buttonIds = buttonIds;

        if (buttonIds.containsKey("facebook")) {
            socialAuths.add(new FacebookAuth(context, listener));
        }

        if (buttonIds.containsKey("google")) {
            socialAuths.add(new GoogleAuth(context, listener));
        }

        if (buttonIds.containsKey("twitter")) {
            socialAuths.add(new TwitterAuth(context, listener));
        }

        if (buttonIds.containsKey("linkedin")) {
            socialAuths.add(new LinkedinAuth(context, listener));
        }

    }

    @Override
    public void initializeSDK(AppCompatActivity activity) {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.initializeSDK(activity);
        }
    }

    public void setupLogin(AppCompatActivity activity) {
        setupLogin(activity, 0);
    }

    @Override
    public void setupLogin(AppCompatActivity activity, int loginBtnId) {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.setupLogin(activity, buttonIds.get(socialAuth.getSocialAuthIdentifier()));
        }
    }

    @Override
    public boolean isLoggedIn() {
        for (SocialAuth socialAuth:socialAuths) {
            if (socialAuth.isLoggedIn()) {
                return true;
            }
        }
        return false;
    }

    public String getLoggedInIdentifier() {
        for (SocialAuth socialAuth:socialAuths) {
            if (socialAuth.isLoggedIn()) {
                return socialAuth.getSocialAuthIdentifier();
            }
        }

        return "";
    }

    @Override
    public void logout() {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.logout();
        }
    }

    @NonNull
    @Override
    public String getSocialAuthIdentifier() {
        return "";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.onDestroy();
        }
    }
}
