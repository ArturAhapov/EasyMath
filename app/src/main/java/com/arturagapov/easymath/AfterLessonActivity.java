package com.arturagapov.easymath;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.arturagapov.easymath.Lessons.Lesson1Activity;
import com.facebook.ads.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.*;

import static com.google.android.gms.internal.zzs.TAG;

public class AfterLessonActivity extends Activity implements com.facebook.ads.AdListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    //Serializable
    private static String fileNameData = "EasyMathData.ser";
    //Задержка времени
    private Handler mHandler = new Handler();

    //Firebase EventLog
    private FirebaseAnalytics mFirebaseAnalytics;

    private char unit;
    private int keyNumber;

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;

    //Данные интента
    private int rightQuantity;
    private int wrongQuantity;
    private int score;

    private int leaderboardScore;
    private int countWithoutWrong = 0;

    //Подключаем звуки
    private SoundPool wwSoundPool;
    private int wwSoundId = 1;
    private int wwStreamId;

    //NativeAd by Admob
    NativeExpressAdView nativeAdmob;
    AdRequest requestNativeAdmob;
    //NativeAd by Facebook
    private NativeAd nativeAd;
    private NativeAdViewAttributes viewAttributes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_lesson);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Восстанавливаем данные из сохраненного файла
        Data.userData = readFromFileData();
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setAds();

        //Получаем интент
        Intent intent = getIntent();
        unit = intent.getCharExtra("Unit", '+');// + - * /
        keyNumber = intent.getIntExtra("keyNumber", 0);//0 1 2 3 4 5 6 7 8 9 10
        rightQuantity = intent.getIntExtra("rightQuantity", -1);
        wrongQuantity = intent.getIntExtra("wrongQuantity", -1);
        score = rightQuantity * 5;

        //Подключаем звуки
        try {
            wwSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
            wwSoundPool.load(this, R.raw.notif2, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        playCongrats();
        setData();
        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();
        mGoogleApiClient.connect();
        setCurrentScore();
        setAchievements(countWithoutWrong);
        if (!Data.userData.isPremium()) {
            waiting();
        } else {
            RelativeLayout fon = (RelativeLayout) findViewById(R.id.after_lesson_fon);
            fon.setVisibility(View.INVISIBLE);
        }
    }

    private void waiting() {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                RelativeLayout fon = (RelativeLayout) findViewById(R.id.after_lesson_fon);
                fon.setVisibility(View.INVISIBLE);
            }
        }, 1600);
    }

    private void setAds() {
        if (!Data.userData.isPremium()) {
            //Подключаем NativeAd by Admob
            nativeAdmob = (NativeExpressAdView) findViewById(R.id.nativeAdmobAds_after_lesson_activity);
            requestNativeAdmob = new AdRequest.Builder().build();//можно добавить фильтр для детей AdRequest request = new AdRequest.Builder().setLocation(location).setGender(AdRequest.GENDER_FEMALE).setBirthday(new GregorianCalendar(1985, 1, 1).getTime()).tagForChildDirectedTreatment(true).build();
            nativeAdmob.loadAd(requestNativeAdmob);

            //NativeAd by Facebook
            nativeAd = new NativeAd(this, "1089755054476898_1090594284392975");
            nativeAd.setAdListener(this);
            //Initiate a request to load an ad.
            nativeAd.loadAd();
        }
    }

    //NativeAd by Facebook
    @Override
    public void onError(Ad ad, AdError adError) {

    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (ad != nativeAd) {
            nativeAdmob.loadAd(requestNativeAdmob);
        }
        viewAttributes = new NativeAdViewAttributes()
                .setBackgroundColor(getResources().getColor(R.color.colorDashBoardEmpty))
                .setDescriptionTextColor(getResources().getColor(R.color.colorWhite))
                .setTitleTextColor(getResources().getColor(R.color.colorWhite))
                .setButtonTextColor(getResources().getColor(R.color.colorWhite))
                .setButtonColor(getResources().getColor(R.color.colorMagenta))
                .setButtonBorderColor(getResources().getColor(R.color.colorWhite));
        // Render the View with the next native ad,
        // and define the Native Ad Template
        View adView = NativeAdView.render(this, nativeAd, NativeAdView.Type.HEIGHT_300, viewAttributes);

        // Add adView to the layout
        LinearLayout templateContainer = (LinearLayout) findViewById(R.id.nativeAdByFacebook_after_lesson_activity);
        templateContainer.addView(adView);
    }

    @Override
    public void onAdClicked(Ad ad) {

    }
    //NativeAd by Facebook end

    private void setData() {

        TextView youScore = (TextView) findViewById(R.id.after_score);
        youScore.setText("" + score);
        TextView weRecommend = (TextView) findViewById(R.id.we_recommend);

        ImageView smile = (ImageView) findViewById(R.id.after_image_smile);
        int sm = (int) (Math.random() * 10);

        if (wrongQuantity == 0) {
            Data.userData.setUnitsComplete(Data.userData.getUnitsComplete() + 1);
            countWithoutWrong = Data.userData.getUnitsComplete();
            playCongrats();
            weRecommend.setTextSize(2.0f);
            weRecommend.setVisibility(View.INVISIBLE);

            if (sm < 3) {
                smile.setImageResource(R.drawable.smile_happy_2);
            } else if (sm < 6) {
                smile.setImageResource(R.drawable.smile_happy_3);
            } else {
                smile.setImageResource(R.drawable.smile_happy_1);
            }

        } else {
            if (sm < 5) {
                smile.setImageResource(R.drawable.smile_sad_1);
            } else {
                smile.setImageResource(R.drawable.smile_sad_2);
            }

            TextView withWrong = (TextView) findViewById(R.id.you_complete_this_lesson);
            withWrong.setTextSize(2.0f);
            withWrong.setText("");
            weRecommend.setText(getResources().getString(R.string.you_complete_lesson_with_wrong));
        }
        if (!Data.userData.isPremium()) {
            Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            Point p = new Point();
            display.getSize(p);
            int screenWidth = p.x;
            int screenHeight = p.y;
            int imageWidth = (int) (screenWidth / 6);
            int imageHeight = (int) (screenHeight / 6);

            ViewGroup.LayoutParams smileParams = smile.getLayoutParams();
            smileParams.height = imageHeight;
            smileParams.width = imageWidth;
        }
    }

    private int setCurrentScore() {
        int currentScore = Data.userData.getScore();
        currentScore = currentScore + score;
        Data.userData.setScore(currentScore);
        leaderboardScore = currentScore;
        return currentScore;
    }

    private void rateDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.dialog_rate, null);
        //AlertDialog
        String positiveButtonText = getResources().getString(R.string.rate);
        String negativeButtonText = getResources().getString(R.string.rate_later);
        AlertDialog.Builder builder = new AlertDialog.Builder(AfterLessonActivity.this);
        builder.setCancelable(false)
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        eventLogsRate("Rate the App", "Rate the App_NOT NOW");
                        dialog.cancel();
                        continueAfterAds();
                    }
                })
                .setPositiveButton(positiveButtonText,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                eventLogsRate("Rate the App", "Rate the App_YES");
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=com.arturagapov.easymath"));
                                Data.userData.setReallyRate(true);
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=com.arturagapov.easymath")));
                                }

                            }
                        });
        builder.setView(dialoglayout);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void playCongrats() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float leftVolume = curVolume / maxVolume;
        float rightVolume = curVolume / maxVolume;
        int priority = 1;
        int no_loop = 0;
        float normal_playback_rate = 1f;
        wwStreamId = wwSoundPool.play(wwSoundId, leftVolume, rightVolume, priority, no_loop,
                normal_playback_rate);
    }

    public void onClickContinueAfterLesson(View view) {
        if ((Data.userData.getSessionCount() % 7) == 0 && !Data.userData.isReallyRate()) {
            rateDialog();
        } else if ((Data.userData.getSessionCount() % 20) == 0 && !Data.userData.isPremium()) {
            continueToPurchase();
        } else {
            continueAfterAds();
        }
    }

    private void continueToPurchase() {
        saveToFileData();
        Intent intent = new Intent(this, BuyPremiumActivity.class);
        intent.putExtra("level", "learn");
        intent.putExtra("Unit", unit);
        startActivity(intent);
    }

    public void onClickTryAgain(View view) {
        saveToFileData();
        Intent intent;
        if (Data.userData.isPremium()) {
            intent = new Intent(this, Lesson1Activity.class);
            intent.putExtra("Unit", unit);
            intent.putExtra("keyNumber", keyNumber);
        } else {
            intent = new Intent(this, UnitActivity.class);
            intent.putExtra("Level","learn");
        }
        startActivity(intent);
    }

    private void continueAfterAds() {
        saveToFileData();
        Intent intent = new Intent(this, LevelActivity.class);
        startActivity(intent);
    }

    private void eventLogsRate(String id, String name) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, name);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.GENERATE_LEAD, bundle);
    }

    public void onClickLeaderboard(View view) {
        if (checkConnection()) {
            setLeaderboard(leaderboardScore);
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    getResources().getString(R.string.leaderboard_leaderboard)), 1);
        } else {
            //mGoogleApiClient.connect();
        }
    }

    public void onClickAchievements(View view) {
        if (checkConnection()) {
            setAchievements(countWithoutWrong);
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
        } else {
            //mGoogleApiClient.connect();
        }
    }

    private void setLeaderboard(final Integer value) {
        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_leaderboard), value);//1337
        Bundle bundle = new Bundle();
        bundle.putLong(FirebaseAnalytics.Param.SCORE, value);
        bundle.putString("leaderboard_id", getResources().getString(R.string.leaderboard_leaderboard));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle);
    }

    private void setAchievements(int count) {
        if (isSignedIn()) {
            if (count >= 5) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_beginner));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_beginner));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (count >= 25) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_intermediate));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_intermediate));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (count >= 50) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_advanced));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_advanced));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (count == 100) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_professional));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_professional));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (count == 200) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_master));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_master));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (count == 500) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_euclide));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_euclide));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (leaderboardScore >= 1000) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_johnny_raw));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_johnny_raw));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (leaderboardScore >= 4000) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_mediume_math_skilled));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_mediume_math_skilled));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (leaderboardScore >= 8000) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_hi_skilled_in_math));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_hi_skilled_in_math));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (leaderboardScore >= 15000) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_lord_of_math));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_lord_of_math));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (leaderboardScore >= 30000) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_king_of_math));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_king_of_math));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
            if (leaderboardScore >= 50000) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_god_of_math));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ACHIEVEMENT_ID, getResources().getString(R.string.achievement_god_of_math));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
                //startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 1);
            }
        }
    }

    private boolean checkConnection() {
        boolean isConnect = false;
        if (mGoogleApiClient.isConnected()) {
            isConnect = true;
        } else {
            isConnect = false;
        }
        return isConnect;
    }

    // Serializes an object and saves it to a file
    private void saveToFileData() {//Context context) {
        try {
            FileOutputStream fileOutputStream = openFileOutput(fileNameData, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(Data.userData);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Creates an object by reading it from a file
    private Data readFromFileData() {//Context context) {
        try {
            FileInputStream fileInputStream = openFileInput(fileNameData);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Data.userData = (Data) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Data.userData;
    }

    //Отключаем кнопку Назад
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
        //return super.onKeyDown(keyCode, event);
    }

    //Google Game Center
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        setAchievements(countWithoutWrong);
        setLeaderboard(leaderboardScore);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.signin_other_error);
            }
        }
    }

    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): connecting");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveToFileData();
        Log.d(TAG, "onStop(): disconnecting");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
