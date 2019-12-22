package com.example.mycloudalbum.Activity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mycloudalbum.Bean.LoginBean;
import com.example.mycloudalbum.MD5Encoder;
import com.example.mycloudalbum.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {

    private static int ERROR;//用于更新UI
    private TextInputLayout username_tl, password_tl;
    private Button test_btn;//用于直接进入主页面的测试btn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        LitePal.initialize(this);

        username_tl = findViewById(R.id.phoneNumber_til);
        password_tl = findViewById(R.id.password_til);

        //不用作测试时应该将其注释
//        test_btn = findViewById(R.id.test_btn);
//        test_btn.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                Intent intent=new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });

        Button login_btn=(Button) findViewById(R.id.login);//检测登陆信息
        login_btn.setOnClickListener(new login_btn_onclick());

        Button forget_btn= (Button)findViewById((R.id.forget));
        forget_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View source) {
                Intent intent=new Intent(LoginActivity.this, FindPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button register =(Button) findViewById(R.id.register);//注册跳转
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View source) {
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
//               startActivityForResult(intent, 1);
//                onActivityResult(1,RESULT_OK,intent);
            }
        });
    }

    @SuppressLint("WrongConstant")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    String str = data.getStringExtra("phone");
                    TextInputLayout username_tl = findViewById(R.id.phoneNumber_til);
                    username_tl.getEditText().setText(str);
                    //EditText phoneNumber =(EditText) findViewById(R.id.phoneNumber);
                    //phoneNumber.setText(str);
                }
                break;
        }
    }

    private void delay(int ms){
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class login_btn_onclick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
//            Intent intent=new Intent(login.this,personal_homepage_activity.class);
//            startActivity(intent);
            EditText phoneNumber = (EditText) findViewById(R.id.phoneNumber);//账户id
            //final String number = phoneNumber.getText().toString();
            final String number = username_tl.getEditText().getText().toString();

            final EditText password = (EditText) findViewById(R.id.password);
            //final String pw = password.getText().toString();
            final String pw = password_tl.getEditText().getText().toString();

            if (number.isEmpty()) {
//                Toast toast = Toast.makeText(LoginActivity.this, "账户不能为空", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//                username_tl.setErrorEnabled(true);
//                username_tl.setError("账户不能为空");
                Message msg = new Message();
                msg.what = 100;
                handler.sendMessage(msg);
            } else if (pw.isEmpty()) {
                Message msg = new Message();
                msg.what = 200;
                handler.sendMessage(msg);
//                password_tl.setErrorEnabled(true);
//                password_tl.setError("密码不能为空");
//                Toast toast = Toast.makeText(LoginActivity.this, "密码不能为空", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
            } else {
                //创建OkHttpClient对象
                OkHttpClient okHttpClient = new OkHttpClient();

                /*
                 *密码MD5加密后再请求
                 */
                final Request request = new Request.Builder()
                        .url("http://203.195.217.253/login2.php" + "?" + "phoneNumber=" + number + "&password=" + MD5Encoder.encode(pw))//请求的url
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
                        Toast.makeText(LoginActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    //异步请求(非主线程)
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String result = response.body().string();
//                        Looper.prepare();
//                        Toast.makeText(login.this, "code="+code, Toast.LENGTH_SHORT).show();
//                        Looper.loop();
//                        delay(2000);
//                        final int code1 = response.code();
                        Log.i("LoginActivity", result);
                        Gson gson = new Gson();
                        LoginBean loginBean = gson.fromJson(result, LoginBean.class);
                        if (loginBean.getResult() == -1) {
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, "连接数据库失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        } else if (loginBean.getResult() == 101) {
//                            username_tl.setErrorEnabled(false);
//                            password_tl.setErrorEnabled(false);
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            /*
                             *增加返回UserId
                             */
                            String userID;
                            userID = loginBean.getUserID();
                            //此处添加或更新userID
                            if(userID.equals("-1")){
                                List<LitePalActivity> data = DataSupport.where("phoneNumber=?",number).find(LitePalActivity.class);
                                for (LitePalActivity data0:data)//实际只有一条
                                userID = data0.userID;//使用上次的userID
                            }
                            else if(userID.equals("0")){
                                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                //更新本地库的userID
                                List<LitePalActivity> data = DataSupport.where("phoneNumber=?",number).find(LitePalActivity.class);
                                for (LitePalActivity data0:data) {//实际只有一条
                                    data0.setUserID(userID);
                                    data0.updateAll("phoneNumber=?", number);
                                    data0.save();
                                }
                                Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                                // TODO 此处还应该将登录得到的UserId通过Intent传给MainActivity by金盛
                                intent.putExtra("userID", userID);
                                startActivity(intent);
                                finish();
                                Looper.loop();
                            }
                        /*
                         *由于不能在子线程调用view，改用handle
                         */
                        } else if (loginBean.getResult() == 102) {
                            Message msg = new Message();
                            msg.what = 102;
                            handler.sendMessage(msg);
//                            Looper.prepare();
//                            Toast.makeText(LoginActivity.this, "密码或账号错误", Toast.LENGTH_SHORT).show();
//                            Looper.loop();
                        } else if(loginBean.getResult() == 200){
                            Looper.prepare();
                            Toast.makeText(LoginActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                });
            }
        }
    }

    /**
     * 重写输入框，实现点击空白处隐藏效果
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 窗口的父类事件分发处理，如果不写所有组件的点击事件都没有了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    //工作线程
    // @Override
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 102) {
                username_tl.setErrorEnabled(true);
                username_tl.setError("账户或密码错误");
                password_tl.setErrorEnabled(true);
                password_tl.setError("账户或密码错误");
            }
            else if(msg.what == 100){
                username_tl.setErrorEnabled(true);
                username_tl.setError("账户不能为空");
            }
            else if(msg.what == 200){
                password_tl.setErrorEnabled(true);
                password_tl.setError("密码不能为空");
            }
        }  
    };
}