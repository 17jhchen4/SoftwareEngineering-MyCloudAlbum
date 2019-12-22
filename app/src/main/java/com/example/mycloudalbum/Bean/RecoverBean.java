package com.example.mycloudalbum.Bean;

import java.util.List;

public class RecoverBean
{

    /**
     * result : 801
     * successUrl : [{"date":"2019-12-07","imgUrl":"http://203.195.217.253/localhost1"},{"date":"2019-12-08","imgUrl":"http://203.195.217.253/localhost4"}]
     */

    private int result;
    private List<SuccessUrlBean> successUrl;

    public int getResult()
    {
        return result;
    }

    public void setResult(int result)
    {
        this.result = result;
    }

    public List<SuccessUrlBean> getSuccessUrl()
    {
        return successUrl;
    }

    public void setSuccessUrl(List<SuccessUrlBean> successUrl)
    {
        this.successUrl = successUrl;
    }

    public static class SuccessUrlBean
    {
        /**
         * date : 2019-12-07
         * imgUrl : http://203.195.217.253/localhost1
         */

        private String date;
        private String imgUrl;

        public String getDate()
        {
            return date;
        }

        public void setDate(String date)
        {
            this.date = date;
        }

        public String getImgUrl()
        {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl)
        {
            this.imgUrl = imgUrl;
        }
    }
}
