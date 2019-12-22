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

import com.example.mycloudalbum.Bean.UpdatePasswordBean;
import com.example.mycloudalbum.MD5Encoder;
import com.example.mycloudalbum.R;
import com.google.gson.Gson;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

import org.litepal.LitePal;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdatePasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_password);
        LitePal.initialize(this);

        Intent it =getIntent();
        final String userID = it.getStringExtra("userID");
        Button submitBn = (Button) findViewById(R.id.submit);
        submitBn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                EditText password = (EditText) findViewById(R.id.password);
                String newPassword = password.getText().toString();

                EditText passwordSure = (EditText) findViewById(R.id.passwordSure);
                String newPasswordSure = passwordSure.getText().toString();

                if(newPassword.length()<6||newPassword.length()>10){
                    Toast toast=Toast.makeText(UpdatePasswordActivity.this,"密码格式错误",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else if(!newPasswordSure.equals(newPassword)){
                    Toast toast=Toast.makeText(UpdatePasswordActivity.this,"两次密码不一致",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else{
                    OkHttpClient okHttpClient = new OkHttpClient();

                    final Request request = new Request.Builder()
                            .url("http://203.195.217.253/find_password.php" + "?" + "userID=" + userID + "&new_password=" + MD5Encoder.encode(newPassword))//请求的url
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
                            Toast.makeText(UpdatePasswordActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        //异步请求(非主线程)
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String result = response.body().string();;
                            Log.i("UpdatePasswordActivity", result);
                            Gson gson = new Gson();
                            UpdatePasswordBean updatePasswordBean = gson.fromJson(result, UpdatePasswordBean.class);
                            if (updatePasswordBean.getResult() == -1) {
                                Looper.prepare();
                                Toast.makeText(UpdatePasswordActivity.this, "连接数据库失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (updatePasswordBean.getResult() == 1) {
                                Looper.prepare();
                                Toast.makeText(UpdatePasswordActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                                //更新之后重新登录
                                Intent intent=new Intent(UpdatePasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                Looper.loop();
                            } else if (updatePasswordBean.getResult() == 2) {
                                Looper.prepare();
                                Toast.makeText(UpdatePasswordActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    });
                }
            }
        });
    }
}
