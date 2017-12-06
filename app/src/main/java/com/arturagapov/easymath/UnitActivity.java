package com.arturagapov.easymath;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.*;

public class UnitActivity extends Activity {
    //Serializable
    private static String fileNameData = "EasyMathData.ser";

    //Firebase EventLog
    private FirebaseAnalytics mFirebaseAnalytics;
    //Подключаем звуки
    private SoundPool wwSoundPool;
    private int wwSoundId = 1;
    private int wwStreamId;

    Intent intent;

    String level;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Восстанавливаем данные из сохраненного файла
        readFromFileData();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //Подключаем звуки
        try {
            wwSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
            wwSoundPool.load(this, R.raw.notif2, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Получаем интент
        Intent intent1 = getIntent();
        level = intent1.getStringExtra("Level");
        setIntentToGo(level);
        updateUi();
    }
    private void updateUi(){

        if(Data.userData.isPremium()){
            Button buttonDivision = (Button)findViewById(R.id.button4);
            buttonDivision.setBackground(getResources().getDrawable(R.drawable.buttonroundedmagenta));

            buttonDivision.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    eventLogs("MathAction", "DIVISION");
                    intent.putExtra("Unit", "Division");
                    startActivity(intent);
                }
            });
        }
    }

    private void eventLogs(String id, String name){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, name);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void setIntentToGo(String levelToGo){
        if(levelToGo.equals("learn")){
            intent = new Intent(this, ChooseLessonActivity.class);
        }else {
            intent = new Intent(this, DifficultyActivity.class);
            intent.putExtra("level", levelToGo);
        }
    }

    public void onClickPlus(View view) {
        eventLogs("MathAction", "ADDITION");
        intent.putExtra("Unit", "Plus");
        startActivity(intent);
    }
    public void onClickMinus(View view) {
        eventLogs("MathAction", "SUBTRACTION");
        intent.putExtra("Unit", "Minus");
        startActivity(intent);
    }
    public void onClickMultiply(View view) {
        eventLogs("MathAction", "MULTIPLICATION");
        intent.putExtra("Unit", "Multiply");
        startActivity(intent);
    }
    public void onClickDivision(View view) {
        continueToMarket();
    }

    /*
    private void showProDialog() {
        playCongrats();
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.activity_buypremium, null);
        //AlertDialog
        String positiveButtonText = getResources().getString(R.string.get_pro);
        String negativeButtonText = getResources().getString(R.string.rate_later);
        final AlertDialog.Builder builder = new AlertDialog.Builder(UnitActivity.this);
        builder.setCancelable(true)
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(positiveButtonText,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                continueToMarket();
                                updateUi();
                                dialog.cancel();
                            }
                        });
        builder.setView(dialoglayout);

        AlertDialog alert = builder.create();
        alert.show();
    }
    */

    private void eventLogsMoreApps(String id, String name){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, name);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.JOIN_GROUP, bundle);
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

    private void eventLogsGetRpo(){
        Bundle bundle = new Bundle();
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE_REFUND, bundle);
    }

    private void continueToMarket(){
        /*Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.arturagapov.easymathpr"));
        Data.userData.setRate(true);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.arturagapov.easymathpr")));
        }
        */
        playCongrats();
        eventLogsGetRpo();
        Intent intent = new Intent(this, BuyPremiumActivity.class);
        intent.putExtra("level", level);
        intent.putExtra("Unit", "Division");
        startActivity(intent);
    }

    //More Apps
    public void onClickGeoQuiz(View view) {
        if (Data.userData.isMoreAppFirst()) {
            eventLogsMoreApps("More Apps", "GeoQuiz First");
            Data.userData.setMoreAppFirst(true);
        } else {
            eventLogsMoreApps("More Apps", "GeoQuiz OneMore Time");
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.arturagapov.geoquiz"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.arturagapov.geoquiz")));
        }
    }
    // Serializes an object and saves it to a file
    private void saveToFileData(){//Context context) {
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
    private void readFromFileData(){//Context context) {
        try {
            FileInputStream fileInputStream = openFileInput(fileNameData);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Data.userData = (Data) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        readFromFileData();
    }
    @Override
    protected void onStop() {
        super.onStop();
        saveToFileData();
    }
}
