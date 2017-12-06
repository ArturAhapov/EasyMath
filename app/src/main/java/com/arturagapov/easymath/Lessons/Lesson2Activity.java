package com.arturagapov.easymath.Lessons;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.arturagapov.easymath.Data;
import com.arturagapov.easymath.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import java.io.*;

public class Lesson2Activity extends Activity {
    //Serializable
    private static String fileNameData = "EasyMathData.ser";

    private int[] x = new int[10];
    private int k = 0;
    private char unit;
    private int keyNumber;
    private int color;//цвет кнопки
    //ProgressBar
    private int progress = 0;
    private int maxProgress = x.length;
    private ProgressBar pbHorizontal;
    //Подключаем звуки
    private SoundPool rSoundPool;

    //Native by Admob
    private boolean isNativeAdShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson2);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //Восстанавливаем данные из сохраненного файла
        Data.userData = readFromFileData();
        //ProgressBar
        pbHorizontal = (ProgressBar) findViewById(R.id.lesson2progressBar);
        pbHorizontal.setMax(maxProgress);

        //Подключаем звуки
        try {
            rSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
            rSoundPool.load(this, R.raw.rightanswer5, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        unit = intent.getCharExtra("Unit", '+');// + - * /
        keyNumber = intent.getIntExtra("keyNumber", 0);//0 1 2 3 4 5 6 7 8 9 10
        setX();
        setText();
        questionAnimationIn();
        buttonAnination();
    }

    //Подключаем NativeAd by Admob
    private void requestNativeAd() {
        if(!Data.userData.isPremium()) {
            NativeExpressAdView adView = (NativeExpressAdView) findViewById(R.id.lesson2_nativeAdmobAds);
            AdRequest requestNative = new AdRequest.Builder().build();//можно добавить фильтр для детей AdRequest request = new AdRequest.Builder().setLocation(location).setGender(AdRequest.GENDER_FEMALE).setBirthday(new GregorianCalendar(1985, 1, 1).getTime()).tagForChildDirectedTreatment(true).build();
            adView.loadAd(requestNative);
            isNativeAdShow = true;
        }
    }

    private void setX() {
        boolean sum;
        do {
            sum = false;
            for (int i = 0; i < x.length; i++) {
                x[i] = (int) (Math.random() * x.length);
            }
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x.length; j++) {
                    if (j != i && x[j] == x[i]) {
                        sum = true;
                    }
                }
            }
        } while (sum);
    }

    private void setText() {
        int b = x[k];
        TextView questionText = (TextView) findViewById(R.id.lesson_2_question);
        String text = doMathAction(b, keyNumber, unit);
        questionText.setText(text);
        k = k + 1;
        progress = k;
        postProgress(progress);
    }

    private void playRight() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float leftVolume = curVolume / maxVolume;
        float rightVolume = curVolume / maxVolume;
        int priority = 1;
        int no_loop = 0;
        float normal_playback_rate = 1f;
        //rStreamId = rSoundPool.play(rSoundId, leftVolume, rightVolume, priority, no_loop,normal_playback_rate);
    }

    //ProgressBar
    private void postProgress(int progress) {
        pbHorizontal.setProgress(progress);
        pbHorizontal.setSecondaryProgress(progress + 1);
    }

    private String doMathAction(int i, int j, char action) {
        Button answer = (Button) findViewById(R.id.lesson_2_answer);
        int result;
        String text = "null";
        switch (action) {
            case '+':
                result = i + j;
                text = ("" + i + " + " + j + " = ");
                answer.setText("" + result);
                break;
            case '-':
                result = i + j;
                text = ("" + result + " - " + j + " = ");
                answer.setText("" + i);
                break;
            case 'x':
                result = i * j;
                text = ("" + i + " x " + j + " = ");
                answer.setText("" + result);
                break;
            case '/':
                result = i * j;
                text = ("" + result + " / " + j + " = ");
                answer.setText("" + i);
                break;
        }
        return text;
    }

    public void onClickNextAnswer(View view) {
        if (k >= (x.length - 6) & !isNativeAdShow) {
            requestNativeAd();
        }
        if (k < x.length) {
            playRight();
            setButtonColor();
            setText();
            questionAnimationIn();
            buttonAnination();
        } else {
            Button nextButton = (Button) findViewById(R.id.next_lesson2);
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonColor() {
        Button answerButton = (Button) findViewById(R.id.lesson_2_answer);
        int preColor = color;//предыдущий цвет кнопки
        do {
            int z = (int) (Math.random() * 100);
            if (z < 20) {
                color = R.drawable.buttonroundedblue;
                answerButton.setBackground(getResources().getDrawable(color));
            } else if (z < 40) {
                color = R.drawable.buttonroundedgreen;
                answerButton.setBackground(getResources().getDrawable(color));
            } else if (z < 60) {
                color = R.drawable.buttonroundedmagenta;
                answerButton.setBackground(getResources().getDrawable(color));
            } else if (z < 80) {
                color = R.drawable.buttonroundedred;
                answerButton.setBackground(getResources().getDrawable(color));
            } else if (z < 100) {
                color = R.drawable.buttonroundedyellow;
                answerButton.setBackground(getResources().getDrawable(color));
            }
        } while (color == preColor);
    }

    //Animation
    private void questionAnimationIn() {
        TextView questionImageIn = (TextView) findViewById(R.id.lesson_2_question);
        //Animation
        Animation anim = null;
        anim = AnimationUtils.loadAnimation(this, R.anim.level_activity_righttoleft1);
        questionImageIn.startAnimation(anim);
    }

    private void buttonAnination() {
        Button button1 = (Button) findViewById(R.id.lesson_2_answer);
        //Animation
        Animation anim = null;
        anim = AnimationUtils.loadAnimation(this, R.anim.level_activity_righttoleft2);
        button1.startAnimation(anim);
    }

    public void onClickNext(View view) {
        Intent intent = new Intent(this, Lesson3Activity.class);
        intent.putExtra("Unit", unit);
        intent.putExtra("keyNumber", keyNumber);
        startActivity(intent);
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

    @Override
    protected void onStop() {
        super.onStop();
        saveToFileData();
    }
}
