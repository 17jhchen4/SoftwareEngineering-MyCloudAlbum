package com.example.mycloudalbum.Bean;

import java.util.List;

public class UploadBean
{

    /**
     * result_code : 301
     * successNumber : 1
     * result : ["http://203.195.217.253/img/2019-12-07 22:42:39_(4)(3)(2)(1)mmexport1575537911163.jpg"]
     */

    private int result_code;
    private int successNumber;
    private List<String> result;

    public int getResult_code()
    {
        return result_code;
    }

    public void setResult_code(int result_code)
    {
        this.result_code = result_code;
    }

    public int getSuccessNumber()
    {
        return successNumber;
    }

    public void setSuccessNumber(int successNumber)
    {
        this.successNumber = successNumber;
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
