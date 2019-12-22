package com.example.mycloudalbum.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.example.mycloudalbum.R;
import com.example.mycloudalbum.Util.AliyunSmsUtils;
import com.example.mycloudalbum.Util.CountDownTimerUtils;

import androidx.appcompat.app.AppCompatActivity;

import org.litepal.crud.DataSupport;

import java.util.List;

import static com.example.mycloudalbum.Util.AliyunSmsUtils.querySendDetails;

public class FindPasswordActivity extends AppCompatActivity {

    private String final_vCode=null;
    private static int newcode;
    String phone = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findpassword);

//        EditText phoneText =(EditText)findViewById(R.id.phoneNumber);
//        final String phoneNumber=phoneText.getText().toString();
        Button vCodeBn=(Button)findViewById(R.id.vCodeBn);
        vCodeBn.setOnClickListener(new View.OnClickListener(){

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

            }
        });

        Button submit=(Button)findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText phoneText =(EditText)findViewById(R.id.phoneNumber);
                String phoneNumber=phoneText.getText().toString();
                EditText vCodeText=(EditText)findViewById(R.id.vCode);
                String vcode=vCodeText.getText().toString();
//                if(final_vCode.equals(vcode)&&phoneNumber.equals(phone)) {//检测验证码和手机号码是否匹配
                if(true){
                    List<LitePalActivity> data = DataSupport.where("phoneNumber=?",phoneNumber).find(LitePalActivity.class);
                    String userID = null;
                    for (LitePalActivity data0:data) {//实际只有一条
                        userID = data0.getUserID();
                        Log.d("userID","data userID is " + data0.userID);
                    }
                    Intent intent = new Intent(FindPasswordActivity.this, UpdatePasswordActivity.class);
                    intent.putExtra("userID",userID);
                    startActivity(intent);
                }
                else {
                    Toast toast = Toast.makeText(FindPasswordActivity.this, "验证码错误或手机号码已修改", Toast.LENGTH_SHORT);
                }
            }
        });

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
}