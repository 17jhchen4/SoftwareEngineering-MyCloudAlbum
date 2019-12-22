package com.example.mycloudalbum.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.example.mycloudalbum.MD5Encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LocalCacheUtil
{
    private static String cachePath;

    private Context mContext;

    private static LocalCacheUtil localCacheUtil;

    public LocalCacheUtil(Context context, String uniqueName)
    {
        cachePath = getCacheDirString(context, uniqueName);
        mContext = context;
    }

    public static String getCachePath()
    {
        return cachePath;
    }

    public static LocalCacheUtil getInstance()
    {
        if (localCacheUtil != null)
        {
            return localCacheUtil;
        }
        return null;
    }

    /**
     * 设置Bitmap数据到本地
     *
     * @param url
     * @param bitmap
     */
    public void setBitmapToLocal(String url, Bitmap bitmap)
    {
        FileOutputStream fos = null;
        try {
            String fileName = MD5Encoder.encode(url);
            File file = new File(cachePath, fileName);

            File parentFile = file.getParentFile();
            //获取上级所有目录

            if (!parentFile.exists())
            {
                // 如果文件不存在，则创建文件夹
                parentFile.mkdirs();
            }
            fos = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            // 将图片压缩到本地

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                    //关闭流
                    fos = null;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 通过url获取Bitmap
     *
     * @param url
     */
    public Bitmap getBitmapFromLocal(String url)
    {
        try {
            File file = new File(cachePath, MD5Encoder.encode(url));
            if (file.exists())
            {
                // 如果文件存在
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                //Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                return bitmap;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void setUrlToLocal(String content)
    {
        FileWriter fwriter = null;
        try
        {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            File file = new File(cachePath, "BingUrl.txt");
            fwriter = new FileWriter(file);
            fwriter.write(content);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                fwriter.flush();
                fwriter.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public static String getUrlFromLocal()
    {
        String content = null;
        try
        {
            File file = new File(cachePath, "BingUrl.txt");
            BufferedReader bf = new BufferedReader(new FileReader(file));
            content = bf.readLine();
            bf.close();
            Log.e("Read", content);
            return content;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return null;
}

    /*获取缓存目录的路径：String类型*/
    private String getCacheDirString(Context context, String uniqueName)
    {
        File file = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable())
        {
            file = new File(context.getExternalCacheDir(), uniqueName);
            //file = new File(Environment.getExternalStorageDirectory(), uniqueName);
        }
        else
        {
            file = new File(context.getCacheDir(), uniqueName);
        }

        if (!file.exists())
        {
            file.mkdirs();
        }

        return file.getAbsolutePath();
    }

}
