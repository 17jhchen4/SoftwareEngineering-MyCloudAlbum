package com.example.mycloudalbum.Bean;

import java.util.List;

public class DeleteBean
{

    /**
     * successUrl : ["http://203.195.217.253/img/2019-12-14 20:27:05_09d7015b-dc6a-40be-8d86-ab2452679f1f.jpg","http://203.195.217.253/img/2019-12-14 20:27:05_b19edaef-b273-484c-9644-05b1120179b5.jpg","http://203.195.217.253/img/2019-12-14 20:12:46_Screenshot_20191213-200513.png"]
     * result : 701
     */

    private int result;
    private List<String> successUrl;

    public int getResult()
    {
        return result;
    }

    public void setResult(int result)
    {
        this.result = result;
    }

    public List<String> getSuccessUrl()
    {
        return successUrl;
    }

    public void setSuccessUrl(List<String> successUrl)
    {
        this.successUrl = successUrl;
    }
}
