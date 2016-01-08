package br.com.estudio89.androidsocial;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luccascorrea on 1/7/16.
 */
public class SocialAuthManager extends AbstractSocialAuth {

    @NonNull
    private List<SocialAuth> socialAuths = new ArrayList<>();
    private int[] loginButtonIds;

    public SocialAuthManager(@NonNull Context context, @Nullable SocialAuthListener listener, int[] loginButtonIds) {
        super(context, listener);
        this.loginButtonIds = loginButtonIds;
        socialAuths.add(new FacebookAuth(context, listener));
        socialAuths.add(new GoogleAuth(context, listener));

    }

    @Override
    public void initializeSDK(FragmentActivity activity) {
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.initializeSDK(activity);
        }
    }

    public void setupLogin(FragmentActivity activity) {
        setupLogin(activity, 0);
    }

    @Override
    public void setupLogin(FragmentActivity activity, int loginBtnId) {
        int idx = 0;
        for (SocialAuth socialAuth:socialAuths) {
            socialAuth.setupLogin(activity, loginButtonIds[idx]);
            idx++;
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
