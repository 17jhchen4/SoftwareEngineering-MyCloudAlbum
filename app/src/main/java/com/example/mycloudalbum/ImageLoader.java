package com.example.mycloudalbum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ImageLoader
{

        /**
         * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。
         */
        private static LruCache<String, Bitmap> mMemoryCache;

        /**
         * ImageLoader的实例。
         */
        private static ImageLoader mImageLoader;

    private static String cachePath;

    private Context context;

        private ImageLoader()
        {
            // 获取应用程序最大可用内存
            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            int cacheSize = maxMemory / 8;
            // 设置图片缓存大小为程序最大可用内存的1/8
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize)
            {
                @Override
                protected int sizeOf(String key, Bitmap bitmap)
                {
                    return bitmap.getByteCount();
                }
            };
        }

    public void setContext(Context context)
    {
        this.context = context;
        cachePath = getCacheDirString(context, "myCloudAlbum");
    }

    /**
         * 获取ImageLoader的实例。
         *
         * @return ImageLoader的实例。
         */
        public static ImageLoader getInstance()
        {
            if (mImageLoader == null)
            {
                mImageLoader = new ImageLoader();
            }
            return mImageLoader;
        }

        /**
         * 将一张图片存储到LruCache中。
         *
         * @param key LruCache的键，这里传入图片的URL地址。
         * @param bitmap LruCache的键，这里传入从网络上下载的Bitmap对象。
         */
        public void addBitmapToMemoryCache(String key, Bitmap bitmap)
        {
            if (getBitmapFromMemoryCache(key) == null)
            {
                mMemoryCache.put(key, bitmap);
            }
        }

        /**
         * 从LruCache中获取一张图片，如果不存在就返回null。
         *
         * @param key
         *            LruCache的键，这里传入图片的URL地址。
         * @return 对应传入键的Bitmap对象，或者null。
         */
        public Bitmap getBitmapFromMemoryCache(String key)
        {
            return mMemoryCache.get(key);
        }

        public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth)
        {
            // 源图片的宽度
            final int width = options.outWidth;
            int inSampleSize = 1;
            if (width > reqWidth)
            {
                // 计算出实际宽度和目标宽度的比率
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = widthRatio;
            }
            return inSampleSize;
        }

        public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth)
        {
            // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, options);
            // 调用上面定义的方法计算inSampleSize值
            options.inSampleSize = calculateInSampleSize(options, reqWidth);
            // 使用获取到的inSampleSize值再次解析图片
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(pathName, options);
        }

    public static String getCachePath()
    {
        return cachePath;
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
        try
        {
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

            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
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
     * 通过url获取Bitmap并保存到缓存中
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
                if (getBitmapFromMemoryCache(url) == null)
                {
                    mMemoryCache.put(url, bitmap);
                }
                return mMemoryCache.get(url);
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
