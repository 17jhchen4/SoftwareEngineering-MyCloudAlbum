package com.example.mycloudalbum.Bean;

import java.util.ArrayList;
import java.util.List;

public class AlbumWallBean
{
    private String date;
    private List<String> ImgUrl;
    private List<Boolean>isShow = new ArrayList<>();
    private List<Boolean>isCheck = new ArrayList<>();

    public String getDate()
    {
        return date;
    }

    public List<String> getImgUrl()
    {
        return ImgUrl;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public void setImgUrl(List<String> imgUrl)
    {
        ImgUrl = imgUrl;
//        for (int i = 0; i < imgUrl.size(); i++)
//        {
//            isShow.add(false);
//            isCheck.add(false);
//        }
    }

    public List<Boolean> getIsCheck()
    {
        return isCheck;
    }

    public List<Boolean> getIsShow()
    {
        return isShow;
    }
}
