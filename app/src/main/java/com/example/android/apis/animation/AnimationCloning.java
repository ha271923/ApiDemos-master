/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.animation;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.android.apis.R;

import java.util.ArrayList;


public class AnimationCloning extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_cloning);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        final MyAnimationView animView = new MyAnimationView(this);
        container.addView(animView); // 在一個Layout上, 動態新增一個View

        Button starter = (Button) findViewById(R.id.startButton);
        starter.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                animView.startAnimation();
            }
        });
    }
    // 一個View要變成Animation時, 需要implement相關animation的interface
    public class MyAnimationView extends View implements ValueAnimator.AnimatorUpdateListener {

        public final ArrayList<ShapeHolder> balls = new ArrayList<ShapeHolder>();
        AnimatorSet animation = null;
        private float mDensity;

        public MyAnimationView(Context context) {
            super(context);

            mDensity = getContext().getResources().getDisplayMetrics().density;

            ShapeHolder ball0 = addBall(50f, 25f);
            ShapeHolder ball1 = addBall(150f, 25f);
            ShapeHolder ball2 = addBall(250f, 25f);
            ShapeHolder ball3 = addBall(350f, 25f);
        }

        private void createAnimation() { // STEP2: 時間軸要安排演員出場順序及行為與劇本
            // ball[0], ball[1], ball[2], ball[3]  == 演員
            //  get(0),  get(1),  get(2),  get(3)
            //    Down,    Down, Down+Up, Down+Up  == 表演內容
            //    Anim,    Anim, AnimSet, AnimSet  == 順序安排

            if (animation == null) {
                ObjectAnimator anim1 = ObjectAnimator.ofFloat(balls.get(0), "y",
                        0f, getHeight() - balls.get(0).getHeight()).setDuration(500);
                ObjectAnimator anim2 = anim1.clone(); // 為了ball[0],[1]有一樣的動畫, 所以先clone出一個anim2
                anim2.setTarget(balls.get(1)); // 動畫設定給哪個view
                anim1.addUpdateListener(this); // ** 每次運行anim時, 需要對View手動invalidate()來更新動畫{在callback onAnimationUpdate()中}

                ShapeHolder ball2 = balls.get(2); // ball[2]是一顆彈跳的球
                ObjectAnimator animDown = ObjectAnimator.ofFloat(ball2, "y",
                        0f, getHeight() - ball2.getHeight()).setDuration(500); // 設計落下動畫
                animDown.setInterpolator(new AccelerateInterpolator());
                ObjectAnimator animUp = ObjectAnimator.ofFloat(ball2, "y",
                        getHeight() - ball2.getHeight(), 0f).setDuration(500); // 設計回彈動畫
                animUp.setInterpolator(new DecelerateInterpolator()); // 回彈時, 速度感表現
                AnimatorSet s1 = new AnimatorSet();
                s1.playSequentially(animDown, animUp); // 動畫組合中設計animDown與animUp的順序
                animDown.addUpdateListener(this);
                animUp.addUpdateListener(this);
                AnimatorSet s2 = (AnimatorSet) s1.clone(); // 因為都是回彈動畫, 所以複製
                s2.setTarget(balls.get(3)); // 動畫設定給哪個view

                animation = new AnimatorSet();
                animation.playTogether(anim1, anim2, s1);
                animation.playSequentially(s1, s2);
            }
        }

        private ShapeHolder addBall(float x, float y) { // STEP1: 增加演員 View
            OvalShape circle = new OvalShape();
            circle.resize(50f * mDensity, 50f * mDensity);
            ShapeDrawable drawable = new ShapeDrawable(circle);
            ShapeHolder shapeHolder = new ShapeHolder(drawable);
            shapeHolder.setX(x - 25f);
            shapeHolder.setY(y - 25f);
            int red = (int)(100 + Math.random() * 155);
            int green = (int)(100 + Math.random() * 155);
            int blue = (int)(100 + Math.random() * 155);
            int color = 0xff000000 | red << 16 | green << 8 | blue;
            Paint paint = drawable.getPaint(); //new Paint(Paint.ANTI_ALIAS_FLAG);
            int darkColor = 0xff000000 | red/4 << 16 | green/4 << 8 | blue/4;
            RadialGradient gradient = new RadialGradient(37.5f, 12.5f,
                    50f, color, darkColor, Shader.TileMode.CLAMP);
            paint.setShader(gradient);
            shapeHolder.setPaint(paint);
            balls.add(shapeHolder); // 把產生的圖片放入List中
            return shapeHolder;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < balls.size(); ++i) {
                ShapeHolder shapeHolder = balls.get(i);
                canvas.save();
                canvas.translate(shapeHolder.getX(), shapeHolder.getY());
                shapeHolder.getShape().draw(canvas);
                canvas.restore();
            }
        }

        public void startAnimation() {
            createAnimation();
            animation.start(); // STEP3: 演員都設置好就開演了
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();  // ** 每次運行anim時, 需要對View手動invalidate()來更新動畫{在callback onAnimationUpdate()中}
        }

    }
}