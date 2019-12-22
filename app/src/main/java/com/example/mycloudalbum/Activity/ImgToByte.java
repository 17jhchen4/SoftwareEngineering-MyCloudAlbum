package com.example.mycloudalbum.Activity;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class ImgToByte {
    public static byte[]img(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
