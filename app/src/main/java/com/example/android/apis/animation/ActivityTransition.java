/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.android.apis.R;

import java.util.List;
import java.util.Map;

/**
 * 兩個Activity間, 如何以相同的View element來進行場景的切換
 * EX: Activity有8張圖, 在Activity1時按下鴨子(View), 此時以鴨子為動畫變化起始點, 並變化到 Activity2 仍有這個鴨子(View)
 *     Activity1 的鴨子(View)之所以能延續到 Activity2 就是因為透過 sharedElements 分享
 *     注意觀察 onMapSharedElements callback API 裡面將更新此參數 sharedElements.put(KEY_ID, View)
 */
public class ActivityTransition extends Activity {

    private static final String TAG = "ActivityTransition";

    private static final String KEY_ID = "ViewTransitionValues:id";

    private ImageView mHero;

    public static final int[] DRAWABLES = {
            R.drawable.ball,
            R.drawable.block,
            R.drawable.ducky,
            R.drawable.jellies,
            R.drawable.mug,
            R.drawable.pencil,
            R.drawable.scissors,
            R.drawable.woot,
    };

    public static final int[] IDS = {
            R.id.ball,
            R.id.block,
            R.id.ducky,
            R.id.jellies,
            R.id.mug,
            R.id.pencil,
            R.id.scissors,
            R.id.woot,
    };

    public static final String[] NAMES = {
            "ball",
            "block",
            "ducky",
            "jellies",
            "mug",
            "pencil",
            "scissors",
            "woot",
    };

    public static int getIdForKey(String id) {
        return IDS[getIndexForKey(id)];
    }

    public static int getDrawableIdForKey(String id) {
        return DRAWABLES[getIndexForKey(id)];
    }

    public static int getIndexForKey(String id) {
        for (int i = 0; i < NAMES.length; i++) {
            String name = NAMES[i];
            if (name.equals(id)) {
                return i;
            }
        }
        return 2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(randomColor()));
        setContentView(R.layout.image_block);
        setupHero();
    }

    private void setupHero() {
        String name = getIntent().getStringExtra(KEY_ID);
        mHero = null;
        if (name != null) {
            mHero = (ImageView) findViewById(getIdForKey(name));
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names,
                        Map<String, View> sharedElements) { // 不知道何時輝呼叫到此處? 因BreakPoint與Log都不會出現
                    Log.v("Hawk","onMapSharedElements() callback");
                    sharedElements.put("hero", mHero); // key, View
                }
            });
        }
    }

    public void clicked(View v) {
        mHero = (ImageView) v;
        Intent intent = new Intent(this, ActivityTransitionDetails.class);
        intent.putExtra(KEY_ID, v.getTransitionName());
        ActivityOptions activityOptions
                = ActivityOptions.makeSceneTransitionAnimation(this, mHero, "hero");
        startActivity(intent, activityOptions.toBundle());
    }

    private static int randomColor() {
        int red = (int)(Math.random() * 128);
        int green = (int)(Math.random() * 128);
        int blue = (int)(Math.random() * 128);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
