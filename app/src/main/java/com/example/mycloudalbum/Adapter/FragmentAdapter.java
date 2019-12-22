package com.example.mycloudalbum.Adapter;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class FragmentAdapter extends FragmentStatePagerAdapter
{
    private List<Fragment> mFragments;
    private List<String> mTitles;

    public FragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles)
    {
        super(fm);
        mFragments = fragments;
        mTitles = titles;
    }

    public void setmFragments(List<Fragment> mFragments)
    {
        this.mFragments = mFragments;
    }



    @Override
    public Fragment getItem(int position)
    {
        return mFragments.get(position);
    }

    @Override
    public int getCount()
    {
        return mFragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        return mTitles.get(position);
    }
}
