package com.arturagapov.easymath;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.arturagapov.easymath.Lessons.Lesson1Activity;

public class ChooseLessonActivity extends Activity {

    private char unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lesson);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        String action = intent.getStringExtra("Unit");// + - * /
        setUnitChar(action);
        setButtonListener();
    }

    private void setUnitChar(String a) {
        if (a.equals("Plus")) {
            unit = '+';
        } else if (a.equals("Minus")) {
            unit = '-';
        } else if (a.equals("Multiply")) {
            unit = 'x';
        }else if (a.equals("Division")){
            unit ='/';
            Button btn0=(Button)findViewById(R.id.btn0);
            btn0.setVisibility(View.INVISIBLE);
        }
    }

    private void setButtonListener() {
        final Intent intent = new Intent(this, Lesson1Activity.class);
        Button[] button = {
                (Button) findViewById(R.id.btn0),
                (Button) findViewById(R.id.btn1),
                (Button) findViewById(R.id.btn2),
                (Button) findViewById(R.id.btn3),
                (Button) findViewById(R.id.btn4),
                (Button) findViewById(R.id.btn5),
                (Button) findViewById(R.id.btn6),
                (Button) findViewById(R.id.btn7),
                (Button) findViewById(R.id.btn8),
                (Button) findViewById(R.id.btn9),
                (Button) findViewById(R.id.btn10),
        };
        for (int i = 0; i < button.length; i++) {
            String buttonText = ("" + unit + " " + button[i].getText());
            final int keyNumber = i;
            //Test
            button[i].setWidth(R.dimen.choice_button_height);
            button[i].setHeight(R.dimen.choice_button_height);

            button[i].setText(buttonText);
            button[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent.putExtra("Unit", unit);
                    intent.putExtra("keyNumber", keyNumber);
                    startActivity(intent);
                }
            });
        }
    }

    public void onClickMoreBtn(View view) {
        LinearLayout getMoreBtn = (LinearLayout) findViewById(R.id.get_more_btn);
        TextView moreBtn = (TextView) findViewById(R.id.more);
        moreBtn.setVisibility(View.INVISIBLE);
        getMoreBtn.setVisibility(View.VISIBLE);
    }
}
