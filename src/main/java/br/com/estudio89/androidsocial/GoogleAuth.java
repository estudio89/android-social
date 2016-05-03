package br.com.estudio89.androidsocial;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Created by luccascorrea on 1/7/16.
 *
 */
public class GoogleAuth extends AbstractSocialAuth implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    GoogleApiClient googleApiClient;
    public static int RC_SIGN_IN = 101;
    public static String GOOGLE_AUTH_SERVER_ID_STRING = "google_auth_server_id";

    private AppCompatActivity activity;

    @Nullable
    private SignInButton googleLoginButton;

    public GoogleAuth(@NonNull Context context, @Nullable SocialAuthListener listener) {
        super(context, listener);
    }

    @Override
    public void initializeSDK(AppCompatActivity activity) {
        int serverIdRes = activity.getResources().getIdentifier(GOOGLE_AUTH_SERVER_ID_STRING, "string", activity.getApplication().getPackageName());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(serverIdRes))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void setupLogin(@NonNull AppCompatActivity activity, View loginBtn) {
        googleLoginButton = (SignInButton) loginBtn;
        googleLoginButton.setOnClickListener(this);
        this.activity = activity;
    }

    @NonNull
    @Override
    public String getSocialAuthIdentifier() {
        return "google";
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (this.listener != null) {
            this.listener.onSocialAuthFailed(googleLoginButton, getSocialAuthIdentifier(), "Não foi possível efetuar login, por favor tente novamente.");
        }
    }

    @Override
    public void onClick(View view) {
        if (googleLoginButton != null && view.getId() == googleLoginButton.getId()) {

            if (this.listener != null) {
                this.listener.onSocialLoginButtonClicked(view);
            }
            signIn();
        }
    }

    private void signIn() {
        if (activity != null) {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        boolean wasLoggedOut = isLoggedIn();

        if (result != null && result.isSuccess() && result.getSignInAccount() != null) {
            GoogleSignInAccount acct = result.getSignInAccount();
            // Signed in successfully, show authenticated UI.
            String email = acct.getEmail();
            String name = acct.getDisplayName();
            String token = acct.getIdToken();
            String id = acct.getId();

            this.setLoginStatus(true);
            this.storeAuthData(name, email, token, id);

            if (this.listener != null) {
                this.listener.onSocialAuthSuccess(googleLoginButton, getSocialAuthIdentifier(), token, email, name, id);
            }
        } else {
            if (this.listener != null) {
                if (wasLoggedOut) {
                    this.listener.onSocialLogout(getSocialAuthIdentifier());
                } else {
                    this.listener.onSocialAuthCanceled(googleLoginButton, getSocialAuthIdentifier());
                }

            }
        }
    }

    private void googleLogout() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess() && listener != null) {
                            listener.onSocialLogout(getSocialAuthIdentifier());
                        }
                        GoogleAuth.super.logout();
                    }
                });
    }

    @Override
    public void logout() {
        if (!isLoggedIn()) {
            return;
        }
        if (!googleApiClient.isConnected()) {
            googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    googleLogout();
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
        } else {
            googleLogout();
        }
        super.logout();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

}
