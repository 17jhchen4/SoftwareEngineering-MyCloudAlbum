package com.example.mycloudalbum.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mycloudalbum.R;

import androidx.appcompat.app.AppCompatActivity;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import java.util.List;

public class SettingActivity extends AppCompatActivity {

    /*
     *增加intent获取userID
     */
    private static final String TAG = "litePalData" ;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        LitePal.initialize(this);

        Intent it = getIntent();
        String userID = it.getStringExtra("userID");

        TextView phoneNumber = (TextView)findViewById(R.id.phoneNumber);

        ImageButton back_btn = (ImageButton)findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new back_btn_onclick());

        Button changeCode_btn=(Button)findViewById(R.id.changeCode_btn);
        changeCode_btn.setOnClickListener(new updatePassword_onclick());

        Button logout_btn=(Button)findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(new logout_onclick());

        Button changPhone_btn=(Button)findViewById(R.id.changePhone_btn);
        changPhone_btn.setOnClickListener(new changePhone_onclick());

        /*
         *展示信息
         */
        //显示信息
        List<LitePalActivity> litePalData = DataSupport.findAll(LitePalActivity.class);
        for(LitePalActivity litePal:litePalData){
            Log.d(TAG,"userID is " + litePal.userID);
            Log.d(TAG,"userName is " + litePal.userName);
            Log.d(TAG,"name is " + litePal.name);
            Log.d(TAG,"sex is " + litePal.sex);
            Log.d(TAG,"personalized_signature is " + litePal.personalized_signature);
            Log.d(TAG,"phoneNumber is " + litePal.phoneNumber);
        }

        List<LitePalActivity> data = DataSupport.where("userID=?", userID).find(LitePalActivity.class);
        for (LitePalActivity data0:data) {//实际只有一条
            Log.d(TAG, "data userID is " + data0.userID);
            Log.d(TAG, "data userName is " + data0.userName);
            Log.d(TAG, "data name is " + data0.name);
            Log.d(TAG, "data sex is " + data0.sex);
            Log.d(TAG, "data personalized_signature is " + data0.personalized_signature);
            Log.d(TAG, "data phoneNumber is " + data0.phoneNumber);

            /*
             *暗码显示手机号码
             */
            char[] phoneNumber_charSet = data0.phoneNumber.toCharArray();
            String str = "";
            int length = phoneNumber_charSet.length;
            for (int i = 0; i < length; i++) {
                if (i < 3 || i >= (length - 4)) {
                    //phoneNumber.setText(phoneNumber_charSet[i]-48);
                    // TODO 改这个方法
                    str += Integer.valueOf(phoneNumber_charSet[i])-48;
                } else {
                    str += "*";
                }
            }
            phoneNumber.setText(str);
        }
    }

    class back_btn_onclick implements View.OnClickListener{
        public void onClick(View v){
//            Intent it = getIntent();
//            String userID = it.getStringExtra("userID");
//            Intent intent;
//            intent = new Intent(SettingActivity.this, MainActivity.class);
//            intent.putExtra("userID",userID);
//            startActivity(intent);
            finish();
        }
    }

    class updatePassword_onclick implements View.OnClickListener{
        public void onClick(View v){
            Intent it = getIntent();
            String userID = it.getStringExtra("userID");
            Intent intent;
            intent = new Intent(SettingActivity.this, ChangePasswordActivity.class);
            intent.putExtra("userID",userID);
            startActivity(intent);
            finish();
        }
    }

    class logout_onclick implements View.OnClickListener{
        public void onClick(View v){
            Intent intent;
            intent = new Intent(SettingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    class changePhone_onclick implements View.OnClickListener{
        public void onClick(View v){
            Intent it = getIntent();
            String userID = it.getStringExtra("userID");
            Intent intent = new Intent(SettingActivity.this, UpdatePhoneNumberActivity.class);
            intent.putExtra("userID",userID);
            startActivity(intent);
            finish();
        }
    }
}
