package com.example.mycloudalbum.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.mycloudalbum.Adapter.RecycleRecycleViewAdapter;
import com.example.mycloudalbum.Manager.UpdateManager;
import com.example.mycloudalbum.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecycleFragment extends Fragment
{
    private RecyclerView mRecyclerView;
    private LinearLayout mLinearLayout;
    private List<String> mImgUrlList;
    private RecycleRecycleViewAdapter recycleRecycleViewAdapter;
    private UpdateManager updateManager;
    public RecycleFragment()
    {
        updateManager = UpdateManager.getInstance();
        updateManager.setmRecycleFragment(this);
    }

    public List<String> getmImgUrlList()
    {
        return mImgUrlList;
    }

    public void setmImgUrlList(List<String> mImgUrlList)
    {
        this.mImgUrlList = mImgUrlList;
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
//        return mRecyclerView;
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
        if (mImgUrlList != null && !mImgUrlList.isEmpty())
        {
            mLinearLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
            recycleRecycleViewAdapter = new RecycleRecycleViewAdapter(getActivity(), mImgUrlList);
            mRecyclerView.setAdapter(recycleRecycleViewAdapter);
            updateManager.setmRecycleRecycleViewAdapter(recycleRecycleViewAdapter);
        }
        else
        {
            mLinearLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

}
