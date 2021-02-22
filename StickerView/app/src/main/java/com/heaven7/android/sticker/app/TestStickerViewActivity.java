package com.heaven7.android.sticker.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.heaven7.android.sticker.StickerView;


public class TestStickerViewActivity extends AppCompatActivity {

    StickerView mStickerView;
    ImageView mIv1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_test_sticker);

        mStickerView = findViewById(R.id.sticker_view);
        mIv1 = findViewById(R.id.iv1);

        mStickerView.setSticker(getStickerBitmap());
        mStickerView.setCallback(new StickerView.Callback() {
            @Override
            public void onClickTextArea(StickerView view) {
                //only work 90.
                view.rotateSticker(90);
            }
            @Override
            public void onClickSticker(StickerView view) {
                Toast.makeText(view.getContext(), "Sticker is clicked.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickReset(View view) {
        mStickerView.reset(true);
        mStickerView.setSticker(getStickerBitmap());
       // mStickerView.invalidate();
    }
    private Bitmap getStickerBitmap(){
        int w = 300;
        int h = 200;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        return Bitmap.createScaledBitmap(bitmap, w, h, false);
    }

    public void onClickGetResult(View view) {
        mIv1.setImageBitmap(mStickerView.getResultBitmap());
    }
}
