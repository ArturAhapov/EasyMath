package com.arturagapov.easymath;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.analytics.FirebaseAnalytics;


public class StartActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;


    //Firebase EventLog
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setScreenSize();//Усранавливаем размер иконки
        //animation();

        try {
            FacebookSdk.sdkInitialize(getApplicationContext());
            AppEventsLogger.activateApp(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        setButtonListeners();
        NotificationPollReceiver.scheduleAlarms(this);
    }

    private void eventLogs(){
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    private void setScreenSize(){
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        int screenWidth = p.x;
        int screenHeight = p.y;
        int imageSize = (int)(screenWidth/2);
        int cornerImg = (int)(screenWidth/3);

        ImageView logo =(ImageView)findViewById(R.id.logo);
        ImageView leftTop =(ImageView)findViewById(R.id.image_left_top);
        ImageView rightTop =(ImageView)findViewById(R.id.image_right_top);
        ImageView leftBottom =(ImageView)findViewById(R.id.image_left_bottom);
        ImageView rightBottom =(ImageView)findViewById(R.id.image_right_bottom);
        ImageView center =(ImageView)findViewById(R.id.image_center);

        ViewGroup.LayoutParams logoParams = logo.getLayoutParams();
        logoParams.height = imageSize;
        logoParams.width = imageSize;

        ViewGroup.LayoutParams leftTopParams = leftTop.getLayoutParams();
        leftTopParams.height = cornerImg;
        leftTopParams.width = cornerImg;

        ViewGroup.LayoutParams rightTopParams = rightTop.getLayoutParams();
        rightTopParams.height = (int)(cornerImg*0.66f);
        rightTopParams.width = cornerImg;

        ViewGroup.LayoutParams leftBottomParams = leftBottom.getLayoutParams();
        leftBottomParams.height = (int)(cornerImg*0.66f);
        leftBottomParams.width = cornerImg;

        ViewGroup.LayoutParams rightBottomParams = rightBottom.getLayoutParams();
        rightBottomParams.height = (int)(cornerImg*0.66f);
        rightBottomParams.width = cornerImg;

        ViewGroup.LayoutParams centerParams = center.getLayoutParams();
        centerParams.height = (int)(screenHeight*0.55f);
        centerParams.width = (int)(screenWidth*0.75f);
    }

    private void setButtonListeners(){
        Button sign = (Button)findViewById(R.id.login);
        TextView achievements = (TextView)findViewById(R.id.start_achievements);
        TextView leaderboard = (TextView)findViewById(R.id.start_leaderboard);
        if(mGoogleApiClient.isConnected()){
            sign.setText(getResources().getString(R.string.login_out));
            sign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signOutclicked();
                }
            });
            achievements.setVisibility(View.VISIBLE);
            leaderboard.setVisibility(View.VISIBLE);
        }else {
            sign.setText(getResources().getString(R.string.login_in));
            sign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signInClicked();
                }
            });
            achievements.setVisibility(View.INVISIBLE);
            leaderboard.setVisibility(View.INVISIBLE);
        }

    }

    public void onClickStart(View view) {
        Intent intent = new Intent(this, LevelActivity.class);
        startActivity(intent);
    }

    //GoogleApiClient
    @Override
    public void onConnected(Bundle bundle) {//(@Nullable Bundle bundle) {
        setButtonListeners();
        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {//(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // Already resolving
            return;
        }

        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInflow) {
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using com.arturagapov.easymathpro.BaseGameUtils.
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    // Call when the sign-in button is clicked
    private void signInClicked() {

        eventLogs();

        mSignInClicked = true;
        mGoogleApiClient.connect();
        setButtonListeners();
    }

    // Call when the sign-out button is clicked
    private void signOutclicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        setButtonListeners();
    }

    public void onClickStartLeaderboard(View view) {
        if(mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    getResources().getString(R.string.leaderboard_leaderboard)), 1);//1=REQUEST_LEADERBOARD
        }else{
            //dialog
        }
    }

    public void onClickStartAchievements(View view) {
        if(mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);//REQUEST_ACHIEVEMENTS
        }else{
            //dialog
        }
    }

    @Override
    public void onClick(View view) {

    }

}
