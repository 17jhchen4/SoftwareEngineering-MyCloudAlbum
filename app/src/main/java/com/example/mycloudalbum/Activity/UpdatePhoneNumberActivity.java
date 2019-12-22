package com.example.mycloudalbum.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mycloudalbum.Bean.UpdatePhoneNumberBean;
import com.example.mycloudalbum.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import org.litepal.crud.DataSupport;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdatePhoneNumberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_phonenumber);

        Intent it = getIntent();
        final String phoneNumber = it.getStringExtra("phoneNumber");
        final String userID = it.getStringExtra("userID");

        Button submitBn = (Button) findViewById(R.id.submit);
        submitBn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                EditText oldPhoneNumber_ev = (EditText) findViewById(R.id.oldPhoneNumber);
                String oldPhoneNumber = oldPhoneNumber_ev.getText().toString();

                EditText newPhoneNumber_ev = (EditText) findViewById(R.id.newPhoneNumber);
                final String newPhoneNumber = newPhoneNumber_ev.getText().toString();

                if(newPhoneNumber.length()!=11){
                    Toast toast=Toast.makeText(UpdatePhoneNumberActivity.this,"原手机号码格式错误",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
//                else if(!newPhoneNumber.equals(phoneNumber)){
//                    Toast toast=Toast.makeText(UpdatePhoneNumberActivity.this,"原手机号码与绑定号码不一致",Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER,0,0);
//                    toast.show();
//                }
                else if(!newPhoneNumber.equals(newPhoneNumber)){
                    Toast toast=Toast.makeText(UpdatePhoneNumberActivity.this,"两次手机号码不一致",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else{
                    OkHttpClient okHttpClient = new OkHttpClient();

                    final Request request = new Request.Builder()
                            // TODO 更改网址
                            .url("http://203.195.217.253/change_phone.php" + "?" + "userID=" + userID + "&old_phone=" + oldPhoneNumber + "&new_phone=" + newPhoneNumber)//请求的url
                            .get()//设置请求方式，get()/post()  查看Builder()方法知，在构建时默认设置请求方式为GET
                            .build(); //构建一个请求Request对象

                    //创建/Call
                    Call call = okHttpClient.newCall(request);
                    //加入队列 异步操作
                    call.enqueue(new Callback() {

                        //请求错误回调方法
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Looper.prepare();
                            Toast.makeText(UpdatePhoneNumberActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        //异步请求(非主线程)
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String result = response.body().string();;
                            Log.i("change_phone", result);
                            Gson gson = new Gson();
                            UpdatePhoneNumberBean updatePhoneNumberBean = gson.fromJson(result, UpdatePhoneNumberBean.class);
                            if (updatePhoneNumberBean.getResult() == -1) {
                                Looper.prepare();
                                Toast.makeText(UpdatePhoneNumberActivity.this, "连接数据库失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (updatePhoneNumberBean.getResult() == 1) {
                                Looper.prepare();
                                Toast.makeText(UpdatePhoneNumberActivity.this, "更新成功", Toast.LENGTH_SHORT).show();

                                List<LitePalActivity> data = DataSupport.where("userID=?", userID).find(LitePalActivity.class);
                                for (LitePalActivity data0:data) {//实际只有一条
                                    data0.setPhoneNumber(newPhoneNumber);
                                    data0.updateAll("userID=?", userID);
                                    data0.save();
                                }
                                //更新之后重新登录
                                Intent intent=new Intent(UpdatePhoneNumberActivity.this, LoginActivity.class);
                                startActivity(intent);
                                Looper.loop();
                            } else if (updatePhoneNumberBean.getResult() == 2) {
                                Looper.prepare();
                                Toast.makeText(UpdatePhoneNumberActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if(updatePhoneNumberBean.getResult() == 0){
                                Looper.prepare();
                                Toast.makeText(UpdatePhoneNumberActivity.this, "旧手机号错误", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if(updatePhoneNumberBean.getResult() == 3){
                                Looper.prepare();
                                Toast.makeText(UpdatePhoneNumberActivity.this, newPhoneNumber + "已绑定账号", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    });
                }
            }
        });
    }
}
