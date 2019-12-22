package com.example.mycloudalbum.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mycloudalbum.Activity.ImageDetailsActivity;
import com.example.mycloudalbum.Bean.AlbumBean;
import com.example.mycloudalbum.ImageLoader;
import com.example.mycloudalbum.Manager.UpdateManager;
import com.example.mycloudalbum.R;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 照片墙的适配器类，这里对照片墙中的照片进行了优化，利用LRUCache类进行优化我们的照片
 * 在调用的时候注意这个类
 */

public class PhotoWallAdapter extends ArrayAdapter<String> implements AbsListView.OnScrollListener {
    /**
     * 记录所有正在下载或等待下载的任务。
     */
    private Set<BitmapWorkerTask> taskCollection;

    /**
     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。
     */
    private ImageLoader imageLoader;

    /**
     * GridView的实例
     */
    private GridView mPhotoWall;

    /**
     * 第一张可见图片的下标
     */
    private int mFirstVisibleItem;

    /**
     * 一屏有多少张图片可见
     */
    private int mVisibleItemCount;

    /**
     * 每一列的宽度
     */
    private int columnWidth;


    /**
     * 记录是否刚打开程序，用于解决进入程序不滚动屏幕，不会下载图片的问题。
     */
    private boolean isFirstEnter = true;

    /**
     * 把图片路径的集合传递进来
     *
     * @param context
     * @param textViewResourceId
     * @param objects
     * @param photoWall
     */
    private List<String> mImageLists;

    private UpdateManager updateManager;

    private int index = 0;

    private int mStatus;


    public PhotoWallAdapter(Context context, int textViewResourceId, List<String> objects, GridView photoWall,int status)
    {
        super(context, textViewResourceId, objects);
        mImageLists = objects;
        mPhotoWall = photoWall;
        taskCollection = new HashSet<>();
        imageLoader = ImageLoader.getInstance();
        mStatus = status;

        mPhotoWall.setOnScrollListener(this);
        updateManager = UpdateManager.getInstance();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        final String url = mImageLists.get(position);
        View view;
        if (convertView == null)
        {
            //子项布局
            view = LayoutInflater.from(getContext()).inflate(R.layout.module_gridview_item, null);
        }
        else
        {
            view = convertView;
        }
        final ImageView photo = view.findViewById(R.id.wall_Image_item);
        final CheckBox checkBox = view.findViewById(R.id.wall_Image_check);
        checkBox.setChecked(false);
        // 给ImageView设置一个Tag，保证异步加载图片时不会乱序
        photo.setTag(url);
        setImageView(url, photo);
        columnWidth = photo.getWidth();
        photo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (updateManager.getMULTI_SELECT_MODULE())
                {
                    if (checkBox.isChecked())
                    {
                        checkBox.setChecked(false);
                    }
                    else
                    {
                        checkBox.setChecked(true);
                    }
                }
                else
                {
                    Intent intent = new Intent(getContext(), ImageDetailsActivity.class);
                    if (mStatus == 0)
                    {
                        for (int i=0; i<updateManager.getmRecyclerViewAdapter().getmAlbumBeanList().getResult().size(); i++)
                        {
                            AlbumBean.ResultBean resultBean = updateManager.getmRecyclerViewAdapter().getmAlbumBeanList().getResult().get(i);
                            if (resultBean.getImgUrl().contains(url))
                            {
                                index = i;
                                break;
                            }
                        }
                    }
                    else
                    {
                        for (int i = 0; i < updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().size(); i++)
                        {
                            if (url.equals(updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().get(i)))
                            {
                                index = i;
                                break;
                            }
                        }
                    }
                    intent.putExtra("status", mStatus);
                    intent.putExtra("image_position", position);
                    intent.putExtra("image_index", index);
//                    Log.e("T","url="+url);
//                    Log.e("T","position="+position);
                    getContext().startActivity(intent);
                }

            }
        });

        //实现多选删除by进辉
        if (updateManager.getMULTI_SELECT_MODULE())
        {
            checkBox.setVisibility(View.VISIBLE);
        }
        else
        {
            checkBox.setVisibility(View.GONE);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    if (!updateManager.getSelectList().contains(url))
                    {
                        updateManager.getSelectList().add(url);
                    }
                }
                else
                {
                    if (updateManager.getSelectList().contains(url))
                    {
                        updateManager.getSelectList().remove(url);
                    }
                }
            }
        });

        return view;
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    @Override
    public void add(@Nullable String object)
    {
        if (mImageLists != null)
        {
            mImageLists.add(object);
            notifyDataSetChanged();
        }
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    @Override
    public void remove(@Nullable String object)
    {
        if (mImageLists != null && !mImageLists.isEmpty())
        {
            mImageLists.remove(object);
            notifyDataSetChanged();
        }
    }

    public List<String> getmImageLists()
    {
        return mImageLists;
    }

    public void setmImageLists(List<String> mImageLists)
    {
        this.mImageLists = mImageLists;
    }

    /**
     * 给ImageView设置图片。首先从LruCache中取出图片的缓存，设置到ImageView上。如果LruCache中没有该图片的缓存，
     * 就给ImageView设置一张默认图片。
     *
     * @param imageUrl  图片的URL地址，用于作为LruCache的键。
     * @param imageView 用于显示图片的控件。
     */
    private void setImageView(String imageUrl, ImageView imageView)
    {
        Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(imageUrl);
        if (bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            bitmap = imageLoader.getBitmapFromLocal(imageUrl);
            if (bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
            else
            {
                imageView.setImageResource(R.drawable.empty_img);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        // 仅当GridView静止时才去下载图片，GridView滑动时取消所有正在下载的任务
        if (scrollState == SCROLL_STATE_IDLE)
        {
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
        }
        else
        {
            cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        // 下载的任务应该由onScrollStateChanged里调用，但首次进入程序时onScrollStateChanged并不会调用，
        // 因此在这里为首次进入程序开启下载任务。
        if (isFirstEnter && visibleItemCount > 0)
        {
            loadBitmaps(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;
        }
    }

    /**
     * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象，
     * 如果发现任何一个ImageView的Bitmap对象不在缓存中，就会去本地sd卡寻找
     * 若都没有对应缓存，则开启异步线程去下载图片。
     *
     * @param firstVisibleItem 第一个可见的ImageView的下标
     * @param visibleItemCount 屏幕中总共可见的元素数
     */
    private void loadBitmaps(int firstVisibleItem, int visibleItemCount)
    {
        try
        {
            if (mImageLists != null)
            {
                for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++)
                {
                    /**
                     * 按照 三级缓存(本地缓存/本地sd卡/网络) 的顺序加载对应图片
                     */
                    final String imageUrl = mImageLists.get(i);
                    final ImageView imageView = mPhotoWall.findViewWithTag(imageUrl);
                    Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(imageUrl);

                    if (bitmap != null)
                    {
                        if (imageView != null)
                        {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                    else
                    {
                        bitmap = imageLoader.getBitmapFromLocal(imageUrl);
                        if (imageView != null && bitmap != null)
                        {
                            imageView.setImageBitmap(bitmap);
                        }
                        else
                        {
                            Glide.with(getContext()).asBitmap().load(imageUrl).into(new CustomTarget<Bitmap>()
                            {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition)
                                {
                                    if (imageView != null)
                                    {
                                        imageView.setImageBitmap(resource);
                                    }
                                    imageLoader.setBitmapToLocal(imageUrl, resource);
                                    imageLoader.addBitmapToMemoryCache(imageUrl, resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder)
                                {

                                }
                            });
//                            downloadImage(imageUrl);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 启动后台线程进行下载
     * @param imageUrl 对应图片的url地址
     */
    public void downloadImage(String imageUrl)
    {
        BitmapWorkerTask task = new BitmapWorkerTask();
        taskCollection.add(task);
        task.execute(imageUrl);
    }


    /**
     * 取消所有正在下载或等待下载的任务。
     */
    public void cancelAllTasks()
    {
        if (taskCollection != null)
        {
            for (BitmapWorkerTask task : taskCollection)
            {
                task.cancel(false);
            }
        }
    }


    /**
     * 获取图片的本地存储路径。
     *
     * @param imageUrl
     *            图片的URL地址。
     * @return 图片的本地存储路径。
     */
    private String getImagePath(String imageUrl)
    {
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        String imageName = imageUrl.substring(lastSlashIndex + 1);
        String imageDir = Environment.getExternalStorageDirectory().getPath() + "/myCloudAlbum/";
        File file = new File(imageDir);

        if (!file.exists())
        {
            file.mkdirs();
        }

        String imagePath = imageDir + imageName;
        return imagePath;
    }

    /**
     * 异步下载图片的任务
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>
    {

        /**
         * 图片的URL地址
         */
        private String imageUrl;

        @Override
        protected Bitmap doInBackground(String... params)
        {
            imageUrl = params[0];
            // 在后台开始下载图片
            final Bitmap bitmap = downloadBitmap(params[0]);

            if (bitmap != null)
            {
                // 图片下载完成后缓存到LrcCache以及本地
                imageLoader.setBitmapToLocal(params[0], bitmap);
                imageLoader.addBitmapToMemoryCache(params[0], bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            super.onPostExecute(bitmap);
            // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。
            ImageView imageView = mPhotoWall.findViewWithTag(imageUrl);
            if (imageView != null && bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this);
        }

        /**
         * 建立HTTP请求，获取Bitmap对象，并保存到本地sd卡中。
         *
         * @param imageUrl 图片的URL地址
         * @return 解析后的Bitmap对象
         */
        private Bitmap downloadBitmap(String imageUrl)
        {

            HttpURLConnection con = null;
            Bitmap bitmap = null;
            try
            {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(10 * 1000);
                con.setReadTimeout(10 * 1000);
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (con != null)
                {
                    con.disconnect();
                }
            }
            return bitmap;
        }
    }
}
