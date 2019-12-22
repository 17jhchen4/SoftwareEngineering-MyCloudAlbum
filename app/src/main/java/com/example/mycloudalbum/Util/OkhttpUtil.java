package com.example.mycloudalbum.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkhttpUtil
{
    public static void sengOkhttpGetRequest(String address, okhttp3.Callback callback)
    {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static Response sengOkhttpGetRequestForSyn(String address) throws IOException
    {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(address)
                .build();
        return client.newCall(request).execute();
    }

    private static RequestBody getRequestBody(List<String> fileNames)
    {
        //创建MultipartBody.Builder，用于添加请求的数据
        MultipartBody.Builder builder = new MultipartBody.Builder()
            .setType(MultipartBody.FORM);
        MediaType MutilPart_Form_Data = MediaType.parse("multipart/form-data; charset=utf-8");
        for (int i = 0; i < fileNames.size(); i++)
        { //对文件进行遍历
            File file = new File(fileNames.get(i)); //生成文件
            //根据文件的后缀名，获得文件类型
//            String fileType = getMimeType(file.getName());
            builder.addFormDataPart( //给Builder添加上传的文件
                    "image"+i,  //请求的名字
                    file.getName(), //文件的文字，服务器端用来解析的
                    RequestBody.create(MutilPart_Form_Data, file) //创建RequestBody，把上传的文件放入
            );
        }
        return builder.build(); //根据Builder创建请求
    }

    private static Request getRequest(String url, List<String> fileNames)
    {
        Request.Builder builder = new Request.Builder();
        builder.url(url)
                .post(getRequestBody(fileNames));
        return builder.build();
    }

    public static void upLoadFile(String url, List<String> fileNames, Callback callback)
    {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
        Call call = okHttpClient.newCall(getRequest(url,fileNames)) ;
        call.enqueue(callback);
    }

    public static void handleSelectImgs(String url, List<String> imgUrls, Callback callback)
    {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        FormBody.Builder builder = new FormBody.Builder();
        for (int i = 0; i < imgUrls.size(); i++)
        {
            builder.add("handleSelectImgs"+i,imgUrls.get(i)).build();
        }

        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

}
