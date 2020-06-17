package com.heaven7.android.sticker.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.heaven7.android.sticker.StickerView;


public class TestStickerViewActivity extends AppCompatActivity {

    StickerView mStickerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_test_sticker);

        mStickerView = findViewById(R.id.sticker_view);

        mStickerView.setSticker(R.mipmap.ic_launcher);
        mStickerView.setOnClickListener(new StickerView.OnClickListener() {
            @Override
            public void onClickTextArea(StickerView view) {
                view.rotateSticker(90);
            }
            @Override
            public void onClickSticker(StickerView view) {
                Toast.makeText(view.getContext(), "Sticker is clicked.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
