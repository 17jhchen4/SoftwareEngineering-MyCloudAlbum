package com.example.mycloudalbum.Bean;

import java.util.List;

public class AlbumBean
{

    /**
     * result_code : 501
     * result : [{"date":"2019-12-07","imgUrl":["http://203.195.217.253/localhost1","http://203.195.217.253/localhost2","http://203.195.217.253/localhost3"]},{"date":"2019-12-08","imgUrl":["http://203.195.217.253/localhost4"]},{"date":"2019-12-09","imgUrl":["http://203.195.217.253/localhost5","http://203.195.217.253/localhost6"]}]
     */

    private int result_code;
    private List<ResultBean> result;

    public int getResult_code()
    {
        return result_code;
    }

    public void setResult_code(int result_code)
    {
        this.result_code = result_code;
    }

    public List<ResultBean> getResult()
    {
        return result;
    }

    public void setResult(List<ResultBean> result)
    {
        this.result = result;
    }

    public static class ResultBean
    {
        /**
         * date : 2019-12-07
         * imgUrl : ["http://203.195.217.253/localhost1","http://203.195.217.253/localhost2","http://203.195.217.253/localhost3"]
         */

        private String date;
        private List<String> imgUrl;

        public String getDate()
        {
            return date;
        }

        public void setDate(String date)
        {
            this.date = date;
        }

        public List<String> getImgUrl()
        {
            return imgUrl;
        }

        public void setImgUrl(List<String> imgUrl)
        {
            this.imgUrl = imgUrl;
        }
    }
}
