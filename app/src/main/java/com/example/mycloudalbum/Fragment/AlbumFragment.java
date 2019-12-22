package com.example.mycloudalbum.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.mycloudalbum.Adapter.RecyclerViewAdapter;
import com.example.mycloudalbum.Bean.AlbumBean;
import com.example.mycloudalbum.Manager.UpdateManager;
import com.example.mycloudalbum.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumFragment extends Fragment
{
    private RecyclerView mRecyclerView;
    private LinearLayout mLinearLayout;
    private AlbumBean mAlbumBeanList;
    private RecyclerViewAdapter recyclerViewAdapter;
    private UpdateManager updateManager;

    public void setmAlbumBeanList(AlbumBean mAlbumBeanList)
    {
        this.mAlbumBeanList = mAlbumBeanList;
    }

    public AlbumBean getmAlbumBeanList()
    {
        return mAlbumBeanList;
    }

    public AlbumFragment()
    {
        updateManager = UpdateManager.getInstance();
        updateManager.setmAlbumFragment(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.module_fragment_list,container,false);
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mLinearLayout = view.findViewById(R.id.empty_layout);
        mLinearLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Init();
    }

    public void Init()
    {
        if (mAlbumBeanList != null && !mAlbumBeanList.getResult().isEmpty())
        {
            mLinearLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
            recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), mAlbumBeanList);
            mRecyclerView.setAdapter(recyclerViewAdapter);
            updateManager.setmRecyclerViewAdapter(recyclerViewAdapter);
        }
        else
        {
            mLinearLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
    }
}
