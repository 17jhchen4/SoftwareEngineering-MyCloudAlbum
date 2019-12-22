package com.example.mycloudalbum.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mycloudalbum.Bean.AlbumBean;
import com.example.mycloudalbum.Manager.UpdateManager;
import com.example.mycloudalbum.R;
import com.example.mycloudalbum.View.MyGridView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 云相册RecyclerViewAdapter
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter
{
    private Context mContext;
    private AlbumBean mAlbumBeanList;
    private List<String> ImgUrls;
    private static TextView imageDate;
    private static MyGridView myGridView;
    private UpdateManager updateManager;

    private final int STATUS_ALBUM = 0;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //此处进行控件初始化
        public final View mView;


        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;
            imageDate = itemView.findViewById(R.id.image_date);
            myGridView = itemView.findViewById(R.id.grid_view_image);
        }
    }

    public RecyclerViewAdapter(Context context, AlbumBean albumBeans)
    {
        mContext = context;
        mAlbumBeanList = albumBeans;
        updateManager = UpdateManager.getInstance();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.module_album_recycle_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position)
    {
        final View view = holder.itemView;

        AlbumBean.ResultBean resultBean = mAlbumBeanList.getResult().get(position);
        imageDate.setText(resultBean.getDate());
        PhotoWallAdapter adapter = new PhotoWallAdapter(mContext, 0, resultBean.getImgUrl(), myGridView, STATUS_ALBUM);
        myGridView.setAdapter(adapter);

        updateManager.setmPhotoWallAdapter(adapter);

    }

    @Override
    public int getItemCount()
    {
        return mAlbumBeanList.getResult().size();
    }

    public AlbumBean getmAlbumBeanList()
    {
        return mAlbumBeanList;
    }

    public void setmAlbumBeanList(AlbumBean mAlbumBeanList)
    {
        this.mAlbumBeanList = mAlbumBeanList;
    }


}
