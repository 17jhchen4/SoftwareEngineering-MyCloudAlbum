package com.example.mycloudalbum.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mycloudalbum.Manager.UpdateManager;
import com.example.mycloudalbum.View.MyGridView;
import com.example.mycloudalbum.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 回收站RecyclerViewAdapter
 */
public class RecycleRecycleViewAdapter extends RecyclerView.Adapter
{
    private Context mContext;
    private List<String> mImgUrls;
    private static MyGridView myGridView;
    private UpdateManager updateManager;
    private final int STATUS_RECYCLE = 1;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //此处进行控件初始化
        public final View mView;
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;
            myGridView = itemView.findViewById(R.id.grid_view_image);
        }
    }

    public RecycleRecycleViewAdapter(Context context, List<String> ImgUrls)
    {
        mContext = context;
        if (ImgUrls == null)
        {
            mImgUrls = new ArrayList<>();
        }
        else
        {
            mImgUrls = ImgUrls;
        }
        updateManager = UpdateManager.getInstance();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.module_recycle_recycle_item,parent,false);
        return new RecycleRecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        final View view = holder.itemView;

//        String imgUrl = mImgUrls.get(position);
        PhotoWallAdapter adapter = new PhotoWallAdapter(mContext, 0, mImgUrls, myGridView, STATUS_RECYCLE);
        myGridView.setAdapter(adapter);

        updateManager.setmRecycleWallAdapter(adapter);
    }

    @Override
    public int getItemCount()
    {
        return 1;
    }

    public List<String> getmImgUrls()
    {
        return mImgUrls;
    }

    public void setmImgUrls(List<String> mImgUrls)
    {
        this.mImgUrls = mImgUrls;
    }
}
