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

import androidx.appcompat.app.AppCompatActivity;

import com.example.mycloudalbum.Bean.ChangePasswordBean;
import com.example.mycloudalbum.Bean.UpdatePasswordBean;
import com.example.mycloudalbum.MD5Encoder;
import com.example.mycloudalbum.R;
import com.google.gson.Gson;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);
        LitePal.initialize(this);
        Intent it = getIntent();
        final String userID = it.getStringExtra("userID");

        Button submitBn = (Button) findViewById(R.id.submit);
        submitBn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                EditText oldPassword_ev = (EditText) findViewById(R.id.old_password);
                String oldpassword = oldPassword_ev.getText().toString();

                EditText newPassword_ev = (EditText) findViewById(R.id.new_password);
                String newpassword = newPassword_ev.getText().toString();
                
                EditText newPasswordSure_ev = (EditText) findViewById(R.id.new_password_Sure);
                String newpasswordsure = newPasswordSure_ev.getText().toString();

                if(oldpassword.length()<6||oldpassword.length()>10){
                    Toast toast=Toast.makeText(ChangePasswordActivity.this,"原密码格式错误",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else if(newpassword.length()<6||newpassword.length()>10){
                    Toast toast=Toast.makeText(ChangePasswordActivity.this,"新密码格式错误",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else if(!newpasswordsure.equals(newpassword)){
                    Toast toast=Toast.makeText(ChangePasswordActivity.this,"两次新密码不一致",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else{
                    OkHttpClient okHttpClient = new OkHttpClient();

                    final Request request = new Request.Builder()
                            // TODO 更改网址
                            .url("http://203.195.217.253/change_password.php" + "?" + "userID=" + userID + "&old_password=" + MD5Encoder.encode(oldpassword) + "&new_password=" + MD5Encoder.encode(newpassword))//请求的url
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
                            Toast.makeText(ChangePasswordActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        //异步请求(非主线程)
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String result = response.body().string();;
                            Log.i("ChangePasswordActivity", result);
                            Gson gson = new Gson();
                            ChangePasswordBean changePasswordBean = gson.fromJson(result, ChangePasswordBean.class);
                            if (changePasswordBean.getResult() == -1) {
                                Looper.prepare();
                                Toast.makeText(ChangePasswordActivity.this, "连接数据库失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (changePasswordBean.getResult() == 1) {
                                Looper.prepare();
                                Toast.makeText(ChangePasswordActivity.this, "更新成功", Toast.LENGTH_SHORT).show();

                                //更新之后重新登录
                                Intent intent=new Intent(ChangePasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                Looper.loop();
                            } else if (changePasswordBean.getResult() == 0) {
                                Looper.prepare();
                                Toast.makeText(ChangePasswordActivity.this, "原密码错误", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (changePasswordBean.getResult() == 2){
                                Looper.prepare();
                                Toast.makeText(ChangePasswordActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    });
                }
            }
        });
    }
}