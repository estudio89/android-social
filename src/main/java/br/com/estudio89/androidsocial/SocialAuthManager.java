package br.com.estudio89.androidsocial;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
    private Fragment fragment;

    protected SocialAuthManager(@NonNull Context context, @Nullable SocialAuthListener listener, HashMap<String, Integer> buttonIds) {
        super(context, listener);
        this.buttonIds = buttonIds;

        if (buttonIds.containsKey("facebook")) {
            socialAuths.add(new FacebookAuth(context, listener));
        }

        if (buttonIds.containsKey("google")) {
            socialAuths.add(new GoogleAuth(context, listener));
        }

    }

    @Override
    public void initializeSDK(AppCompatActivity activity) {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.initializeSDK(activity);
        }
    }

    public void setupLogin(AppCompatActivity activity) {
        setupLogin(activity, null);
    }

    public void setupLogin(Fragment fragment) {
        this.fragment = fragment;
        setupLogin((AppCompatActivity) fragment.getActivity(), null);
    }

    @Override
    public void setupLogin(AppCompatActivity activity, View loginBtn) {
        View rootView = null;
        if (this.fragment != null) {
            rootView = fragment.getView();
        }

        for (SocialAuth socialAuth:socialAuths) {
            View view;
            if (rootView != null) {
                view = rootView.findViewById(buttonIds.get(socialAuth.getSocialAuthIdentifier()));
            } else {
                view = activity.findViewById(buttonIds.get(socialAuth.getSocialAuthIdentifier()));
            }
            socialAuth.setupLogin(activity, view);
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
    public void onStart() {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.onStart();
        }
    }

    @Override
    public void onStop() {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.onStop();
        }
    }
}
