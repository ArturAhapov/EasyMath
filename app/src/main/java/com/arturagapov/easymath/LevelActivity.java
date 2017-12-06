package com.arturagapov.easymath;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.*;

public class LevelActivity extends Activity {
    //Serializable
    private static String fileNameData = "EasyMathData.ser";

    //Firebase EventLog
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Восстанавливаем данные из сохраненного файла
        readFromFileData();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setSessionCount();
    }

    private void setSessionCount() {
        Data.userData.setSessionCount(Data.userData.getSessionCount()+1);
        saveToFileData();
    }

    private void eventLogs(String id, String name){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, name);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    private void eventLogsMoreApps(String id, String name){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, name);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.JOIN_GROUP, bundle);
    }

    //More Apps
    public void onClickGeoQuiz(View view) {
        if(Data.userData.isMoreAppFirst()){
            eventLogsMoreApps("More Apps", "GeoQuiz First");
            Data.userData.setMoreAppFirst(true);
        }else {
            eventLogsMoreApps("More Apps", "GeoQuiz OneMore Time");
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.arturagapov.geoquiz"));
        startActivity(intent);
    }
    public void onClickLearn(View view) {
        eventLogs("MathActivity", "Learn");
        Intent intent = new Intent(this, UnitActivity.class);
        intent.putExtra("Level","learn");
        startActivity(intent);
    }
    public void onClickPractice(View view) {
        eventLogs("MathActivity", "Practice");
        Intent intent = new Intent(this, UnitActivity.class);
        intent.putExtra("Level","practice");
        startActivity(intent);
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
}
