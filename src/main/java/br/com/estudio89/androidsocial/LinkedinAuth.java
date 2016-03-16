package br.com.estudio89.androidsocial;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by luccascorrea on 2/22/16.
 * The Linkedin SDK jar file was generated after downloading the SDK Android Library
 * and running the gradle task specified in this link: http://stackoverflow.com/a/19037807/2343626
 * in order to create the file. Afterwards, the generated file was combiined with the files in the sdk's
 * lib diretory, as explained in this link: http://stackoverflow.com/questions/5080220/how-to-combine-two-jar-files
 *
 */
public class LinkedinAuth extends AbstractSocialAuth {


    LISessionManager sessionManager;
    private AppCompatActivity activity;
    private Button button;

    private static final String host = "api.linkedin.com";
    private static final String profileUrl = "https://" + host + "/v1/people/~:(first-name,last-name,email-address,id)";

    public LinkedinAuth(@NonNull Context context, @Nullable SocialAuthListener listener) {
        super(context, listener);
    }

    @Override
    public void initializeSDK(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void setupLogin(final AppCompatActivity activity, View loginBtn) {
        sessionManager = LISessionManager.getInstance(context);
        button = (Button) loginBtn;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSocialLoginButtonClicked(v);
                }

                Scope scope = Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS);

                sessionManager.init(activity, scope, new AuthListener() {

                    @Override
                    public void onAuthSuccess() {
                        LISession session = sessionManager.getSession();

                        final String token = session.getAccessToken().getValue();
                        APIHelper.getInstance(context).getRequest(context, profileUrl, new ApiListener() {
                            @Override
                            public void onApiSuccess(ApiResponse apiResponse) {
                                JSONObject response = apiResponse.getResponseDataAsJson();
                                String name;
                                try {
                                    name = response.getString("firstName") + " " + response.getString("lastName");
                                    String email = response.getString("emailAddress");
                                    String userId = response.getString("id");

                                    if (listener != null) {
                                        setLoginStatus(true);
                                        storeAuthData(name, email, token, userId);
                                        listener.onSocialAuthSuccess(button, getSocialAuthIdentifier(), token, email, name, userId);
                                    }
                                } catch (JSONException e) {
                                    if (listener != null) {
                                        listener.onSocialAuthFailed(button, getSocialAuthIdentifier(), e.getMessage());
                                    }
                                    logoutLinkedin();
                                }

                            }

                            @Override
                            public void onApiError(LIApiError liApiError) {
                                if (listener != null) {
                                    listener.onSocialAuthFailed(button, getSocialAuthIdentifier(), liApiError.getMessage());
                                }
                                logoutLinkedin();
                            }
                        });

                    }

                    @Override
                    public void onAuthError(LIAuthError error) {
                        if (listener != null) {
                            listener.onSocialAuthFailed(button, getSocialAuthIdentifier(), error.toString());
                        }
                    }
                }, true);

            }
        });
    }

    @NonNull
    @Override
    public String getSocialAuthIdentifier() {
        return "linkedin";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        sessionManager.onActivityResult(activity, requestCode, resultCode, data);
    }

    private void logoutLinkedin() {
        sessionManager.clearSession();
    }

    @Override
    public void logout() {
        if (!isLoggedIn()) {
            return;
        }

        logoutLinkedin();
        super.logout();
    }
}
