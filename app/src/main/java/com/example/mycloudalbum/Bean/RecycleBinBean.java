package com.example.mycloudalbum.Bean;

import java.util.List;

public class RecycleBinBean
{

    /**
     * result_code : 601
     * result : ["http//203.195.217.253/localhost1","http//203.195.217.253/localhost2","http//203.195.217.253/localhost4","http//203.195.217.253/localhost6"]
     */

    private int result_code;
    private List<String> result;

    public int getResult_code()
    {
        return result_code;
    }

    public void setResult_code(int result_code)
    {
        this.result_code = result_code;
    }

    public List<String> getResult()
    {
        return result;
    }

    public void setResult(List<String> result)
    {
        this.result = result;
    }
}
