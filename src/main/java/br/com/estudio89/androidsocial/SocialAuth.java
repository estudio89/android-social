package br.com.estudio89.androidsocial;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by luccascorrea on 1/7/16.
 *
 */
public interface SocialAuth {

    /**
     * Preinitializes the authentication SDK - sets up sdk tokens, etc.
     * This method should not depend on UI elements.
     * @param activity
     */
    void initializeSDK(AppCompatActivity activity);

    /**
     * Sets up the necessary steps for login.
     * This method may depend on UI elements.
     * @param activity
     * @param loginBtn
     */
    void setupLogin(AppCompatActivity activity, View loginBtn);

    /**
     * Must return a string used as the identifier for this social network. Ex: "facebook", "google", etc.
     * @return identifier
     */
    @NonNull
    String getSocialAuthIdentifier();

    /**
     * Method that must be called inside the activity's onActivityResult method.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Method that must be called inside the activity's onDestroy method.
     */
    void onDestroy();
    /**
     * @return true if the user is logged in with this social network or false otherwise.
     */
    boolean isLoggedIn();

    void logout();
}
