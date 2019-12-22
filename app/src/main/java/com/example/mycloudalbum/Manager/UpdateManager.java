package com.example.mycloudalbum.Manager;

import android.content.Context;

import com.example.mycloudalbum.Adapter.PhotoWallAdapter;
import com.example.mycloudalbum.Adapter.RecycleRecycleViewAdapter;
import com.example.mycloudalbum.Adapter.RecyclerViewAdapter;
import com.example.mycloudalbum.Bean.AlbumBean;
import com.example.mycloudalbum.Bean.RecoverBean;
import com.example.mycloudalbum.Fragment.AlbumFragment;
import com.example.mycloudalbum.Fragment.RecycleFragment;
import com.example.mycloudalbum.ImageLoader;
import com.example.mycloudalbum.MD5Encoder;
import com.example.mycloudalbum.Util.OkhttpUtil;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class UpdateManager
{
    private static UpdateManager mUpdateManager;
    private static Context mContext;

    private static RecyclerViewAdapter  mRecyclerViewAdapter;

    private static RecycleRecycleViewAdapter mRecycleRecycleViewAdapter;
    /**
     * 存放云相册每一个PhotoWallAdapter
     */
    private List<PhotoWallAdapter> mPhotoWallAdapters;
    /**
     * 存放回收站的PhotoWallAdapter
     */
    private  PhotoWallAdapter mRecycleWallAdapter;

    private AlbumFragment mAlbumFragment;

    private RecycleFragment mRecycleFragment;

//    private LocalCacheUtil localCacheUtil;

    /**
     * 用于表示是否为多选删除模式
     */
    private Boolean MULTI_SELECT_MODULE = false;

    private List<String> selectList;

    private ImageLoader imageLoader;

    private String mUserID = "12345";
    /**
     * 刷新云相册
     */
    private String updateUrl = "http://203.195.217.253/cloudimg.php?userID="+mUserID;

    public UpdateManager()
    {
        mUpdateManager = null;
        mContext = null;
        mRecyclerViewAdapter = null;
        mRecycleRecycleViewAdapter = null;
        mRecycleWallAdapter = null;
        mPhotoWallAdapters = new ArrayList<>();
        imageLoader = ImageLoader.getInstance();
    }

    public void setmUserID(String mUserID)
    {
        this.mUserID = mUserID;
    }

    public String getmUserID()
    {
        return mUserID;
    }

    public  PhotoWallAdapter getmRecycleWallAdapter()
    {
        return mRecycleWallAdapter;
    }

    public RecycleFragment getmRecycleFragment()
    {
        return mRecycleFragment;
    }

    public List<String> getSelectList()
    {
        return selectList;
    }

    public void setMULTI_SELECT_MODULE(Boolean MULTI_SELECT_MODULE)
    {
        this.MULTI_SELECT_MODULE = MULTI_SELECT_MODULE;
        if (MULTI_SELECT_MODULE)
        {
            selectList = new ArrayList<>();
        }
    }

    public Boolean getMULTI_SELECT_MODULE()
    {
        return MULTI_SELECT_MODULE;
    }

    public void setmAlbumFragment(AlbumFragment mAlbumFragment)
    {
        this.mAlbumFragment = mAlbumFragment;
    }

    public void setmRecycleFragment(RecycleFragment mRecycleFragment)
    {
        this.mRecycleFragment = mRecycleFragment;
    }

    /**
     * 获取UpdateManager的实例。
     *
     * @return UpdateManager的实例。
     */
    public static UpdateManager getInstance()
    {
        if (mUpdateManager == null)
        {
            mUpdateManager = new UpdateManager();
        }
        return mUpdateManager;
    }

    public static void setmContext(Context mContext)
    {
        UpdateManager.mContext = mContext;
    }

    /**
     * 与云相册RecyclerViewAdapter进行绑定
     * @param mRecyclerViewAdapter 云相册
     */
    public static void setmRecyclerViewAdapter(RecyclerViewAdapter mRecyclerViewAdapter)
    {
        UpdateManager.mRecyclerViewAdapter = mRecyclerViewAdapter;
    }

    /**
     * 与回收站RecyclerViewAdapter进行绑定
     * @param mRecycleRecycleViewAdapter 回收站
     */
    public static void setmRecycleRecycleViewAdapter(RecycleRecycleViewAdapter mRecycleRecycleViewAdapter)
    {
        UpdateManager.mRecycleRecycleViewAdapter = mRecycleRecycleViewAdapter;
    }

    /**
     * 与云相册的每一个PhotoWallAdapter进行绑定
     * @param mPhotoWallAdapter 云相册PhotoWallAdapter
     */
    public void setmPhotoWallAdapter(PhotoWallAdapter mPhotoWallAdapter)
    {
        mPhotoWallAdapters.add(mPhotoWallAdapter);
    }

    public List<PhotoWallAdapter> getmPhotoWallAdapters()
    {
        return mPhotoWallAdapters;
    }



    /**
     * 获取云相册RecyclerViewAdapter
     * @return 云相册RecyclerViewAdapter
     */
    public static RecyclerViewAdapter getmRecyclerViewAdapter()
    {
        return mRecyclerViewAdapter;
    }

    /**
     * 获取回收站RecyclerViewAdapter
     * @return 回收站RecyclerViewAdapter
     */
    public static RecycleRecycleViewAdapter getmRecycleRecycleViewAdapter()
    {
        return mRecycleRecycleViewAdapter;
    }

    /**
     * 删除云相册的单个图片,并将其加入到回收站中
     * @param imageUrl 需要删除的图片Url
     * @return 若是删除后绑定的数据为空，则返回0，否则返回1
     */
    public int deleteSingleImg(String imageUrl)
    {
        if (mRecyclerViewAdapter != null && mPhotoWallAdapters != null)
        {
            for (int i=0; i<mRecyclerViewAdapter.getmAlbumBeanList().getResult().size(); i++)
            {
                if (mRecyclerViewAdapter.getmAlbumBeanList().getResult().get(i).getImgUrl().contains(imageUrl))
                {
                    mPhotoWallAdapters.get(i).getmImageLists().remove(imageUrl);
                    mPhotoWallAdapters.get(i).notifyDataSetChanged();
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    if (mRecyclerViewAdapter.getmAlbumBeanList().getResult().get(i).getImgUrl().isEmpty())
                    {
                        mRecyclerViewAdapter.getmAlbumBeanList().getResult().remove(i);//当对应url为空时直接移除该项
                        mRecyclerViewAdapter.notifyItemRemoved(i);
                    }
                    break;
                }
            }
        }

        if (mRecycleWallAdapter == null)
        {
            List<String> data = new ArrayList<>();
            data.add(imageUrl);
            getmRecycleFragment().setmImgUrlList(data);
            getmRecycleFragment().Init();
        }
        else
        {
            mRecycleWallAdapter.add(imageUrl);
        }

        if (mRecyclerViewAdapter.getmAlbumBeanList().getResult().isEmpty())
        {
            mAlbumFragment.Init();
            return 0;
        }
        return 1;
    }

    /**
     * 恢复回收站的单个图片
     * @param imageUrl 需要恢复的图片Url
     * @param date 恢复图片对应的日期
     * @return 若是删除后绑定的数据为空，则返回0;正常返回1
     */
    public int recoverSingleImgForRecycle(final String imageUrl, String date)
    {
        Boolean isFind = false;
        mRecycleWallAdapter.remove(imageUrl);
        if (mRecycleRecycleViewAdapter.getmImgUrls().isEmpty())
        {
            mRecycleRecycleViewAdapter.notifyItemRemoved(0);
        }

        if (mRecyclerViewAdapter != null && mPhotoWallAdapters != null)
        {
            for (int i=0; i<mRecyclerViewAdapter.getmAlbumBeanList().getResult().size(); i++)
            {
                if (mRecyclerViewAdapter.getmAlbumBeanList().getResult().get(i).getDate().equals(date))
                {
                    mPhotoWallAdapters.get(i).getmImageLists().add(imageUrl);
                    mPhotoWallAdapters.get(i).notifyDataSetChanged();
                    isFind = true;
                    break;
                }
            }
        }

        if (mRecycleRecycleViewAdapter.getmImgUrls().isEmpty())
        {
            mRecycleFragment.Init();
//            if (!isFind)
//            {
//                Response response = null;
//                try
//                {
//                    response = OkhttpUtil.sengOkhttpGetRequestForSyn(updateUrl);
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//                String json = null;
//                try
//                {
//                    json = response.body().string();
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//                Gson gson = new Gson();
//                AlbumBean albumBean = gson.fromJson(json, AlbumBean.class);
//                int resultCode = albumBean.getResult_code();
//                if (resultCode != 501)
//                {
//                    albumBean = null;
//                }
//                updateAlbum(albumBean);
//            }
            return 0;
        }
        if (!isFind)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Response response = OkhttpUtil.sengOkhttpGetRequestForSyn(updateUrl);
                        String json = response.body().string();
                        Gson gson = new Gson();
                        AlbumBean albumBean = gson.fromJson(json, AlbumBean.class);
                        updateAlbum(albumBean);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
        return 1;
    }

    /**
     * 删除回收站的单个图片
     * @param imageUrl 需要删除的图片Url
     * @return 若是删除后绑定的数据为空，则返回0，否则返回1
     */
    public int deleteSingleImgForRecycle(String imageUrl)
    {
        //本地删除(测试的时候将这个部分注释掉)
        File file = new File(imageLoader.getCachePath(), MD5Encoder.encode(imageUrl));
        if (file != null)
        {
            file.delete();
        }
        mRecycleWallAdapter.remove(imageUrl);
        if (mRecycleRecycleViewAdapter.getmImgUrls().isEmpty())
        {
            mRecycleRecycleViewAdapter.notifyItemRemoved(0);
        }

        if (mRecycleRecycleViewAdapter.getmImgUrls().isEmpty())
        {
            mRecycleFragment.Init();
            return 0;
        }
        return 1;
    }

    /**
     * 与回收站的PhotoWallAdapter进行绑定
     * @param mRecycleWallAdapter 回收站PhotoWallAdapter
     */
    public void setmRecycleWallAdapter(PhotoWallAdapter mRecycleWallAdapter)
    {
        this.mRecycleWallAdapter = mRecycleWallAdapter;
    }

    /**
     * 删除回收站的多个图片(包括本地)
     */
    public void deleteMultiImgForRecycle(List<String> mSelectList)
    {
        for (int j = 0; j < mSelectList.size(); j++)
        {
            String deleteImgUrl = mSelectList.get(j);
            mRecycleWallAdapter.remove(deleteImgUrl);

            //本地删除(测试的时候将这个部分注释掉)
            File file = new File(imageLoader.getCachePath(), MD5Encoder.encode(deleteImgUrl));
            if (file != null)
            {
                file.delete();
            }

            if (mRecycleRecycleViewAdapter.getmImgUrls().isEmpty())
            {
                mRecycleRecycleViewAdapter.notifyItemRemoved(0);
                mRecycleFragment.Init();
            }
        }
    }

    /**
     * 恢复回收站的多个图片
     * @param successUrlList 对应的图片url和所属时间
     * @return 如果没有找到就返回0提示刷新数据,否则返回1
     */
    public void recoverMultiImgForRecycle(List<RecoverBean.SuccessUrlBean> successUrlList)
    {
        for (int i = 0; i < successUrlList.size(); i++)
        {
            final String imageUrl = successUrlList.get(i).getImgUrl();
            String date = successUrlList.get(i).getDate();
            Boolean find = false;

            mRecycleWallAdapter.remove(imageUrl);
            if (mRecycleRecycleViewAdapter.getmImgUrls().isEmpty())
            {
                mRecycleRecycleViewAdapter.notifyItemRemoved(0);
                mRecycleFragment.Init();
            }

            if (mRecyclerViewAdapter != null && mPhotoWallAdapters != null)
            {
                for (int j = 0; j < mRecyclerViewAdapter.getmAlbumBeanList().getResult().size(); j++)
                {
                    if (mRecyclerViewAdapter.getmAlbumBeanList().getResult().get(j).getDate().equals(date))
                    {
                        mPhotoWallAdapters.get(j).getmImageLists().add(imageUrl);
                        mPhotoWallAdapters.get(j).notifyDataSetChanged();
                        find = true;
                        break;
                    }
                }
                if (!find)
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                Response response = OkhttpUtil.sengOkhttpGetRequestForSyn(updateUrl);
                                String json = response.body().string();
                                Gson gson = new Gson();
                                AlbumBean albumBean = gson.fromJson(json, AlbumBean.class);
                                updateAlbum(albumBean);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }
        }
    }

    /**
     * 删除云相册的多个图片,并将其加入到回收站中
     */
    public void deleteMultiImg (List<String> mSelectList)
    {
        for (int j = 0; j < mSelectList.size(); j++)
        {
            String deleteImgUrl = mSelectList.get(j);
            if (mRecyclerViewAdapter != null && mPhotoWallAdapters != null)
            {
                int i;

                for (i=0; i<mRecyclerViewAdapter.getmAlbumBeanList().getResult().size(); i++)
                {
                    if (mRecyclerViewAdapter.getmAlbumBeanList().getResult().get(i).getImgUrl().contains(deleteImgUrl))
                    {
                        mRecyclerViewAdapter.getmAlbumBeanList().getResult().get(i).getImgUrl().remove(deleteImgUrl);
                        mPhotoWallAdapters.get(i).notifyDataSetChanged();
                        if (mRecyclerViewAdapter.getmAlbumBeanList().getResult().get(i).getImgUrl().isEmpty())
                        {
                            mRecyclerViewAdapter.getmAlbumBeanList().getResult().remove(i);//当对应url为空时直接移除该项
                            mRecyclerViewAdapter.notifyItemRemoved(i);
                        }
                        break;
                    }
                }
            }
            if (mRecycleWallAdapter == null || getmRecycleFragment().getmImgUrlList().isEmpty())
            {
                List<String> data = new ArrayList<>();
                data.add(deleteImgUrl);
                getmRecycleFragment().setmImgUrlList(data);
                getmRecycleFragment().Init();
            }
            else
            {
                mRecycleWallAdapter.add(deleteImgUrl);
            }
        }
        if (mRecyclerViewAdapter.getmAlbumBeanList().getResult().isEmpty())
        {
            mAlbumFragment.Init();
        }
    }

    /**
     * 刷新云相册
     * @param albumBean
     */
    public void updateAlbum(AlbumBean albumBean)
    {
//        if (mRecyclerViewAdapter.getmAlbumBeanList() == null || mRecyclerViewAdapter.getmAlbumBeanList().getResult().isEmpty())
//        {
//            mAlbumFragment.setmAlbumBeanList(albumBean);
//            mAlbumFragment.Init();
//        }
//        else
//        {
//            if (mRecyclerViewAdapter != null)
//            {
//                mRecyclerViewAdapter.setmAlbumBeanList(albumBean);
//                mRecyclerViewAdapter.notifyDataSetChanged();
//            }
//        }

        mAlbumFragment.setmAlbumBeanList(albumBean);
        mAlbumFragment.Init();
    }

    /**
     * 刷新回收站
     * @param imgUrls
     */
    public void updateRecycle(List<String> imgUrls)
    {
        mRecycleWallAdapter.setmImageLists(imgUrls);
        mRecycleWallAdapter.notifyDataSetChanged();
        if (!mRecycleRecycleViewAdapter.getmImgUrls().isEmpty())
        {
            mRecycleFragment.Init();
        }
    }
}
