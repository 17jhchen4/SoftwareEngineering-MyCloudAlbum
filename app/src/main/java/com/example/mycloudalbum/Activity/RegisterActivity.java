package com.example.mycloudalbum.Activity;
/*
**此类修改过
 */
        //import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.example.mycloudalbum.Bean.LoginBean;
import com.example.mycloudalbum.MD5Encoder;
import com.example.mycloudalbum.R;
import com.example.mycloudalbum.Util.AliyunSmsUtils;
import com.example.mycloudalbum.Util.CountDownTimerUtils;
import com.google.gson.Gson;

import java.io.IOException;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.litepal.LitePal;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.mycloudalbum.Util.AliyunSmsUtils.querySendDetails;
/****
 *
 *修改过
 */
public class RegisterActivity extends AppCompatActivity {

    private String final_vCode = null;
    private static int newcode;
    String phone = "";

    LitePalActivity litePalActivity = new LitePalActivity();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        LitePal.initialize(this);

        Button vCodeBn = (Button)findViewById(R.id.vCodeBn);
        vCodeBn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                EditText phoneText =(EditText)findViewById(R.id.phoneNumber);
                phone = phoneText.getText().toString();

                TextView vCode_tv = (TextView)findViewById(R.id.vCodeBn);
                //验证码倒计时
                CountDownTimerUtils mCountDownTimerUtils = new CountDownTimerUtils(vCode_tv, 30000, 1000);
                mCountDownTimerUtils.start();

                // TODO AliyunSmsUtils 的 try catch有问题，还未处理

                Toast.makeText(getApplicationContext(),"正在发送验证码，请注意查收", Toast.LENGTH_SHORT).show();

               //String vCode = random_vCode();
               //final_vCode = vCode;
                setNewcode();
               final_vCode = Integer.toString(getNewcode());
                //发短信
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            //发短信
                            SendSmsResponse response = AliyunSmsUtils.sendSms(phone,final_vCode);
                            System.out.println("短信接口返回的数据----------------");
                            System.out.println("Code=" + response.getCode());
                            System.out.println("Message=" + response.getMessage());
                            System.out.println("RequestId=" + response.getRequestId());
                            System.out.println("BizId=" + response.getBizId());


                            //查明细
                            if(response.getCode() != null && response.getCode().equals("OK"))
                            {
                                QuerySendDetailsResponse querySendDetailsResponse = querySendDetails(response.getBizId());
                                System.out.println("短信明细查询接口返回数据----------------");
                                System.out.println("Code=" + querySendDetailsResponse.getCode());
                                System.out.println("Message=" + querySendDetailsResponse.getMessage());
                                int i = 0;
                                for(QuerySendDetailsResponse.SmsSendDetailDTO smsSendDetailDTO : querySendDetailsResponse.getSmsSendDetailDTOs())
                                {
                                    System.out.println("SmsSendDetailDTO["+i+"]:");
                                    System.out.println("Content=" + smsSendDetailDTO.getContent());
                                    System.out.println("ErrCode=" + smsSendDetailDTO.getErrCode());
                                    System.out.println("OutId=" + smsSendDetailDTO.getOutId());
                                    System.out.println("PhoneNum=" + smsSendDetailDTO.getPhoneNum());
                                    System.out.println("ReceiveDate=" + smsSendDetailDTO.getReceiveDate());
                                    System.out.println("SendDate=" + smsSendDetailDTO.getSendDate());
                                    System.out.println("SendStatus=" + smsSendDetailDTO.getSendStatus());
                                    System.out.println("Template=" + smsSendDetailDTO.getTemplateCode());
                                }
                                System.out.println("TotalCount=" + querySendDetailsResponse.getTotalCount());
                                System.out.println("RequestId=" + querySendDetailsResponse.getRequestId());
                            }
                        } catch (ClientException e)
                        {
                            e.printStackTrace();
                        }
                        //System.out.println(response.getData());
                        System.out.println("结束");


                    }
                }).start();

//               String vCode=random_vCode();
//               final_vCode=vCode;
//               try {
//                    SendSmsResponse sendSmsResponse=AliyunSmsUtils.sendSms(phone,vCode);
//                    if(sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {//请求成功
//                        Toast toast=Toast.makeText(RegisterActivity.this,"发送成功",Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER,0,0);
//                        toast.show();
//                    }
//                    else{
//                        Toast toast=Toast.makeText(RegisterActivity.this,"发送失败",Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER,0,0);
//                        toast.show();
//                    }
//                } catch (ClientException e) {
//                    Toast toast=Toast.makeText(RegisterActivity.this,"发送成功",Toast.LENGTH_SHORT);e.printStackTrace();
//                }


            }
        });

        Button register = (Button) findViewById(R.id.register);//检测注册信息
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText accountText =(EditText) findViewById(R.id.account);
                final String account = accountText.getText().toString();

                EditText phoneText =(EditText)findViewById(R.id.phoneNumber);
                String phoneNumber = phoneText.getText().toString();

                EditText vCode=(EditText)findViewById(R.id.vCode);
                String vcode = vCode.getText().toString();

                EditText pwd = (EditText)findViewById(R.id.password);
                String pw = pwd.getText().toString();

                EditText pwdSure = (EditText)findViewById(R.id.passwordSure);
                String pwSure = pwdSure.getText().toString();

                if(phone.isEmpty()||phone.length()!=11){
                    Toast toast = Toast.makeText(RegisterActivity.this,"错误的手机号码",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                //else if(!vcode.equals(final_vCode)||!phone.equals(phoneNumber)){//检测验证码和手机号码是否匹配
                else if(false){
                    Toast toast = Toast.makeText(RegisterActivity.this,"验证码错误或手机号码已修改",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else if(pw.isEmpty()||pw.length()<6||pw.length()>10){
                    Toast toast = Toast.makeText(RegisterActivity.this,"密码格式错误",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else if(!pwSure.equals(pw)||pwSure.length()<6||pwSure.length()>10){
                    Toast toast = Toast.makeText(RegisterActivity.this,"两次密码不一致",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                else{
                    OkHttpClient okHttpClient  = new OkHttpClient();
                    /*
                     *修改将密码经MD5加密后放入数据库
                     */

                    Request request = new Request.Builder()
                            .url("http://203.195.217.253/register2.php?"
                                    + "&account=" + account
                                    + "&password=" + MD5Encoder.encode(pw) //将密码经MD5加密后放入数据库
                                    + "&phoneNumber=" + phone)//请求的url
                            //.post(body)//设置请求方式，get()/post()  查看Builder()方法知，在构建时默认设置请求方式为GET
                            .get()
                            .build(); //构建一个请求Request对象

                    //创建/Call
                    Call call = okHttpClient.newCall(request);
                    //加入队列 异步操作
                    call.enqueue(new Callback() {
                        //请求错误回调方法
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Looper.prepare();//子线程无法生成looper
                            Toast.makeText(RegisterActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();// 进入loop中的循环，查看消息队列
                        }

                        //异步请求(非主线程)
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String result = response.body().string();
                            Log.i("RegisterActivity", result);
                            Gson gson = new Gson();
                            LoginBean registerBean = gson.fromJson(result, LoginBean.class);
//                            Looper.prepare();
//                            Toast.makeText(getApplicationContext(), loginBean.getResult()+"", Toast.LENGTH_SHORT).show();
                            //Looper.loop();
                            if (registerBean.getResult() == -1) {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(), "连接数据库失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (registerBean.getResult() == 204) {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(), "两次密码不一样", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (registerBean.getResult() == 201) {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(), "账户已存在", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (registerBean.getResult() == 203) {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(), "注册失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (registerBean.getResult() == 202) {
                                Looper.prepare();
                                Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_SHORT).show();

                                /*
                                **添加本地数据
                                 */
                                //添加手机号码、用户名信息到本地数据库
                                LitePal.getDatabase();
                                litePalActivity.setPhoneNumber(phone);
                                litePalActivity.setUserName(account);
                                litePalActivity.setName("null");
                                litePalActivity.setSex(1);//默认男，可修改
                                litePalActivity.setPersonalized_signature("null");
                                litePalActivity.save();

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("phone", phone);
                                startActivity(intent);
                                //setResult(RESULT_OK, intent);
                                finish();//结束activity
                                 Looper.loop();
                            }
                        }
                    });
                }
            }
        });
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

    public static int getNewcode()
    {
        return newcode;
    }
    public static void setNewcode()
    {
        /*
        **改了生成四位数的方法
        */
        //newcode = (int)(Math.random()*9999)+100;  //每次调用生成一位四位数的随机数
        newcode = (int)(Math.random()*9000)+1000;  //每次调用生成一位四位数的随机数
    }

    public String random_vCode(){
        String vCode=null;
        int vCodenumber;
        for(int i=0;i<6;i++){
            vCodenumber=(int)(1+Math.random()*(10-2));
            vCode+=vCodenumber;
        }
        return vCode;
    }
}
