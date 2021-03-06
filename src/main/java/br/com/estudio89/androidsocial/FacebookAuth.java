package br.com.estudio89.androidsocial;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;


/**
 * Created by luccascorrea on 1/7/16.
 */
public class FacebookAuth extends AbstractSocialAuth implements View.OnClickListener {
    @Nullable
    private CallbackManager callbackManager;

    @Nullable
    private ProfileTracker profileTracker;

    @Nullable
    private AccessTokenTracker accessTokenTracker;

    @Nullable
    private String email;

    @Nullable
    private String name;

    @Nullable
    private String fbToken;

    @Nullable
    private String userId;

    @Nullable
    LoginButton fbLoginButton;

    private boolean nameRequestFinished = false;
    private boolean emailRequestFinished = false;


    public FacebookAuth(@NonNull Context context, @Nullable SocialAuthListener listener) {
        super(context, listener);
    }

    @Override
    public void initializeSDK(AppCompatActivity activity) {
        FacebookSdk.sdkInitialize(activity.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void setupLogin(AppCompatActivity activity, View loginBtn) {
        fbLoginButton = (LoginButton) loginBtn;
        fbLoginButton.setReadPermissions("public_profile", "email");
        fbLoginButton.setOnClickListener(this);

        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                fbToken = accessToken.getToken();
                requestUserName();
                requestUserEmail(loginResult);

            }

            @Override
            public void onCancel() {
                if (listener != null) {
                    listener.onSocialAuthCanceled(fbLoginButton, getSocialAuthIdentifier());
                }
            }

            @Override
            public void onError(FacebookException error) {
                if (listener != null) {
                    listener.onSocialAuthFailed(fbLoginButton, getSocialAuthIdentifier(), error.getMessage());
                }
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    logout();
                }
            }
        };
        accessTokenTracker.startTracking();
    }

    private void requestUserName() {
        nameRequestFinished = false;
        Profile profile = Profile.getCurrentProfile();
        if (profile != null) {
            name = profile.getName();
            userId = profile.getId();
            nameRequestFinished = true;
            notifyListenerAuthSuccess();
        }  else {
            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                    if (profile2 != null) {
                        name = profile2.getName();
                        userId = profile2.getId();
                        nameRequestFinished = true;
                        notifyListenerAuthSuccess();
                    } else {
                        if (listener != null) {
                            listener.onSocialAuthFailed(fbLoginButton, getSocialAuthIdentifier(), "Erro de autenticação.");
                        }
                    }
                    if (profileTracker != null) {
                        profileTracker.stopTracking();
                    }
                }
            };
            profileTracker.startTracking();
        }
    }

    private void requestUserEmail(LoginResult loginResult) {
        emailRequestFinished = false;
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Optional because the user may not have granted the permission
                        final JSONObject jsonObject = response.getJSONObject();
                        if (jsonObject != null) {
                            email = jsonObject.optString("email");
                        }
                        emailRequestFinished = true;

                        notifyListenerAuthSuccess();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void notifyListenerAuthSuccess() {
        if (emailRequestFinished && nameRequestFinished && listener != null) {
            this.setLoginStatus(true);
            this.storeAuthData(name, email, fbToken, userId);
            listener.onSocialAuthSuccess(fbLoginButton, getSocialAuthIdentifier(), fbToken, email, name, userId);
        }
    }

    @Override
    public void logout() {
        if (!isLoggedIn()) {
            return;
        }

        LoginManager.getInstance().logOut();

        this.email = null;
        this.name = null;
        this.fbToken = null;

        this.nameRequestFinished = false;
        this.emailRequestFinished = false;

        super.logout();
    }

    @Override
    @NonNull
    public String getSocialAuthIdentifier() {
        return "facebook";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStop() {
        if (accessTokenTracker != null) {
            accessTokenTracker.stopTracking();
        }

    }

    @Override
    public void onClick(View view) {
        if (fbLoginButton != null && view.getId() == fbLoginButton.getId()) {
            if (this.listener != null) {
                this.listener.onSocialLoginButtonClicked(view);
            }
        }
    }
}
