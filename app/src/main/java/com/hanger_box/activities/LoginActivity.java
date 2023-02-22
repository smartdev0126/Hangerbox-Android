package com.hanger_box.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.hanger_box.R;
import com.hanger_box.common.Common;
import com.hanger_box.common.Config;
import com.hanger_box.common.LocalStorageManager;
import com.hanger_box.models.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;

import static android.content.ContentValues.TAG;
import static com.hanger_box.common.Common.cm;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static int RC_SIGN_IN = 10002;
    public static String mylocale;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private LoginManager loginManager;

    private TextView loginTitle;
    private LinearLayout forgotPwdLayout;
    private TextView forgotPwdBtn;
    private TextView registerBtn;
    private TextView privacyBtn;
//    private TextView skipBtn;
    private EditText emailExt;
    private EditText pwdExt;

    private LinearLayout loadingLayout;

    private String loginState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mylocale = Resources.getSystem().getConfiguration().locale.getDisplayCountry();

        Common.currentActivity = this;

        loginState = "login";
        loginTitle = findViewById(R.id.login_title);

        forgotPwdLayout = findViewById(R.id.forgot_password_layout);
        forgotPwdBtn = findViewById(R.id.forgot_btn);
        SpannableString forgotPwdContent = new SpannableString(getText(R.string.login_forgot_password_btn));
        forgotPwdContent.setSpan(new UnderlineSpan(), 0, forgotPwdContent.length(), 0);
        forgotPwdBtn.setText(forgotPwdContent);
        forgotPwdBtn.setOnClickListener(this);

        registerBtn = findViewById(R.id.register_btn);
        SpannableString registerBtnContent = new SpannableString(getText(R.string.register_to));
        registerBtnContent.setSpan(new UnderlineSpan(), 0, registerBtnContent.length(), 0);
        registerBtn.setText(registerBtnContent);
        registerBtn.setOnClickListener(this);

        privacyBtn = findViewById(R.id.privacy_btn);
        SpannableString privacyBtnContent = new SpannableString(getText(R.string.login_next2));
        privacyBtnContent.setSpan(new UnderlineSpan(), 0, privacyBtnContent.length(), 0);
        privacyBtn.setText(privacyBtnContent);
        privacyBtn.setOnClickListener(this);

//        skipBtn = findViewById(R.id.skip_btn);
//        SpannableString skipBtnContent = new SpannableString(getText(R.string.login_skip));
//        skipBtnContent.setSpan(new UnderlineSpan(), 0, skipBtnContent.length(), 0);
//        skipBtn.setText(skipBtnContent);
//        skipBtn.setOnClickListener(this);

        emailExt = findViewById(R.id.email_txt);
        pwdExt = findViewById(R.id.pwd_txt);

        findViewById(R.id.facebook_btn).setOnClickListener(this);
        findViewById(R.id.google_btn).setOnClickListener(this);
        findViewById(R.id.next_btn).setOnClickListener(this);

        loadingLayout = findViewById(R.id.loading_layout);
        loadingLayout.setVisibility(View.GONE);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Facebook Login button
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        Log.d(TAG, "signInWithCredential:"+currentUser.getEmail());
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.currentActivity = this;
    }

    private void login(String email, String pwd) {
        loadingLayout.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.LOGIN_URL;
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        // Add form parameter
        formBodyBuilder.add("email", email);
        formBodyBuilder.add("password", pwd);
        formBodyBuilder.add("lang", Common.lang);
        // Build form body.
        FormBody formBody = formBodyBuilder.build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                    }
                });
                String mMessage = e.getMessage().toString();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String mMessage = response.body().string();
                confirmResult(mMessage);
            }
        });
    }

    private void snsLogin(String email, String token, String type, String locale) {
        loadingLayout.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.SNS_LOGIN_URL;
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        // Add form parameter
        formBodyBuilder.add("email", email);
        formBodyBuilder.add("token", "1");
        formBodyBuilder.add("type", type);
        formBodyBuilder.add("lang", Common.lang);
        formBodyBuilder.add("country_name", locale);
        // Build form body.
        FormBody formBody = formBodyBuilder.build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                    }
                });
                String mMessage = e.getMessage().toString();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String mMessage = response.body().string();
                confirmResult(mMessage);
            }
        });
    }

    private void facebookByLogin(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                snsLogin(user.getEmail(), token.getToken(), "FACEBOOK", mylocale);
                                Log.i(TAG, "email" + user.getEmail());
                            }else {
                                cm.showAlertDlg(getString(R.string.error_title), "user is null", null, null);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            cm.showAlertDlg(getString(R.string.error_title), "signInWithCredential:failure", null, null);
                        }
                    }
                });
    }

    private void googleByLogin(final String idToken) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(idToken)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInOptions googleOptions = new GoogleSignInOptions.Builder().requestId().requestEmail().requestProfile().build();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String email = "";
                                for (UserInfo profile : user.getProviderData()) {
                                    if(profile.getEmail() != null)
                                        email = profile.getEmail();
                                }
                                snsLogin(email, idToken, "GOOGLE", mylocale);
                                Log.i("hangerbox", "email" + user.getEmail());
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void register(String email, String pwd) {
        loadingLayout.setVisibility(View.VISIBLE);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        final String path = Config.SERVER_URL + Config.SINGUP_URL;
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        // Add form parameter
        formBodyBuilder.add("email", email);
        formBodyBuilder.add("password", pwd);
        formBodyBuilder.add("password_confirmation", pwd);
        formBodyBuilder.add("country_name", mylocale);
        formBodyBuilder.add("lang", Common.lang);
        // Build form body.
        FormBody formBody = formBodyBuilder.build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(path)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingLayout.setVisibility(View.GONE);
                        cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_access), null, null);
                    }
                });
                String mMessage = e.getMessage().toString();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String mMessage = response.body().string();
                confirmResult(mMessage);
            }
        });
    }

    private void confirmResult (final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingLayout.setVisibility(View.GONE);
                try {
                    JSONObject result = new JSONObject(message);
                    try {
                        cm.showAlertDlg(getString(R.string.error_title), result.getString("message"), null, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        LocalStorageManager.saveObjectToLocal(result.getString("token"), "login_token");
                        JSONObject userInfo = result.getJSONObject("data");
                        Common.me = new UserModel(userInfo);
                        Common.addFavorite = true;
                        Common.initMyItem = true;
                        Common.initLibrary = true;
                        LocalStorageManager.saveObjectToLocal(Common.cm.convertToStringFromHashMap(Common.me.getMap()), "account");
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("back", "top");
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId() /*to get clicked view id**/) {
            case R.id.facebook_btn:
                loginManager = LoginManager.getInstance();
                loginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
                loginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        if (AccessToken.getCurrentAccessToken() != null) {

                            GraphRequest request = GraphRequest.newMeRequest(
                                    loginResult.getAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(
                                                JSONObject object,
                                                GraphResponse response) {

                                            if (object != null) {
                                                try {
                                                    snsLogin(object.getString("email"), loginResult.getAccessToken().getToken(), "FACEBOOK", mylocale);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }else {
                                                cm.showAlertDlg(getString(R.string.error_title), "fb login failed", null, null);

                                            }

                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email,gender, birthday, about");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }else {
                        }
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                    }
                });
                break;
            case R.id.google_btn:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.forgot_btn:
                intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(intent);
                break;
            case R.id.privacy_btn:
                intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                startActivity(intent);

                break;
//            case R.id.skip_btn:
//                Common.me = null;
//                intent = new Intent(getApplicationContext(), MainActivity.class);
//                intent.putExtra("back", "top");
//                startActivity(intent);
//
//                break;
            case R.id.register_btn:
                if (loginState.equals("login")) {
                    loginState = "register";
                    loginTitle.setText(R.string.register);
                    forgotPwdLayout.setVisibility(View.GONE);
                    SpannableString registerBtnContent = new SpannableString(getText(R.string.login));
                    registerBtnContent.setSpan(new UnderlineSpan(), 0, registerBtnContent.length(), 0);
                    registerBtn.setText(registerBtnContent);
                }else {
                    loginState = "login";
                    loginTitle.setText(R.string.login);
                    forgotPwdLayout.setVisibility(View.VISIBLE);
                    SpannableString registerBtnContent = new SpannableString(getText(R.string.register_to));
                    registerBtnContent.setSpan(new UnderlineSpan(), 0, registerBtnContent.length(), 0);
                    registerBtn.setText(registerBtnContent);
                }

                break;
            case R.id.next_btn:
                if (emailExt.getText().toString().equals("")) {
                    cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_input_email), null, null);
                    return;
                }
                if (pwdExt.getText().toString().equals("")) {
                    cm.showAlertDlg(getString(R.string.error_title), getString(R.string.error_input_password), null, null);
                    return;
                }

                if (loginState.equals("login")) {
                    login(emailExt.getText().toString(), pwdExt.getText().toString());
                }else {
                    register(emailExt.getText().toString(), pwdExt.getText().toString());
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getEmail());

                snsLogin(account.getEmail(), account.getIdToken(), "GOOGLE", mylocale);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

//    FirebaseAuth.getInstance().signOut();
}