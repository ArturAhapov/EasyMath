package com.arturagapov.easymath.Lessons;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.arturagapov.easymath.R;

public class Lesson1Activity extends Activity {
    private char unit;
    private int keyNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson1);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        unit = intent.getCharExtra("Unit", '+');// + - * /
        keyNumber = intent.getIntExtra("keyNumber", 0);//0 1 2 3 4 5 6 7 8 9 10
        setTextForLearning();

        //Scroll down to bottom
        final ScrollView scrollview = ((ScrollView) findViewById(R.id.scroll_lesson1));
        scrollview.setSmoothScrollingEnabled(true);
    }

    private void setTextForLearning() {
        TextView[] textViews = {
                (TextView) findViewById(R.id.lesson_1_text_01),
                (TextView) findViewById(R.id.lesson_1_text_02),
                (TextView) findViewById(R.id.lesson_1_text_03),
                (TextView) findViewById(R.id.lesson_1_text_04),
                (TextView) findViewById(R.id.lesson_1_text_05),
                (TextView) findViewById(R.id.lesson_1_text_06),
                (TextView) findViewById(R.id.lesson_1_text_07),
                (TextView) findViewById(R.id.lesson_1_text_08),
                (TextView) findViewById(R.id.lesson_1_text_09),
                (TextView) findViewById(R.id.lesson_1_text_10),
                (TextView) findViewById(R.id.lesson_1_text_11),
        };
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setText(doMathAction(i, keyNumber, unit));

        }
    }

    private String doMathAction(int i, int j, char action) {
        int result;
        String text = "null";
        switch (action) {
            case '+':
                result = i + j;
                text = ("" + i + " + " + j + " = " + result);
                break;
            case '-':
                result = i + j;
                text = ("" + result + " - " + j + " = " + i);
                break;
            case 'x':
                result = i * j;
                text = ("" + i + " x " + j + " = " + result);
                break;
            case '/':
                result = i * j;
                text = ("" + result + " / " + j + " = " + i);
                break;
        }
        return text;
    }

    public void onClickNext(View view) {
        Intent intent = new Intent(this, Lesson2Activity.class);
        intent.putExtra("Unit", unit);
        intent.putExtra("keyNumber", keyNumber);
        startActivity(intent);
    }
}
