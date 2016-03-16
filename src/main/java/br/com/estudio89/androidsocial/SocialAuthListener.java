package br.com.estudio89.androidsocial;

import android.view.View;

/**
 * Created by luccascorrea on 1/7/16.
 */
public interface SocialAuthListener {

    void onSocialLoginButtonClicked(View button);

    void onSocialAuthCanceled(View button, String socialAuthIdentifier);

    void onSocialAuthFailed(View button, String socialAuthIdentifier, String message);

    void onSocialAuthSuccess(View button, String socialAuthIdentifier, String socialAuthToken, String email, String name, String userId);

    void onSocialLogout(String socialAuthIdentifier);
}
