package com.example.mycloudalbum.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mycloudalbum.Bean.SuggestionBean;
import com.example.mycloudalbum.R;
import com.google.gson.Gson;

import org.litepal.LitePal;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SuggestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestion);
        LitePal.initialize(this);

        Button submitBn = (Button) findViewById(R.id.submit);
        submitBn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                ImageButton back_btn = (ImageButton)findViewById(R.id.back_btn);
                back_btn.setOnClickListener(new SuggestionActivity.back_btn_onclick());

                EditText suggestion_ev = findViewById(R.id.suggestion_ev);
                String suggestions = suggestion_ev.getText().toString();

                if(suggestions.isEmpty()) {
                    Looper.prepare();
                    Toast.makeText(SuggestionActivity.this, "建议不能为空", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else{
                    Intent it = getIntent();
                    String userID = it.getStringExtra("userID");
                    OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            // TODO 更改网址
                            .url("http://203.195.217.253/suggest.php" + "?" + "userID=" + userID + "&message=" + suggestions)//请求的url
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
                            Toast.makeText(SuggestionActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        //异步请求(非主线程)
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String result = response.body().string();;
                            Log.i("SuggestionActivity", result);
                            Gson gson = new Gson();
                            SuggestionBean suggestionBean = gson.fromJson(result, SuggestionBean.class);
                            if (suggestionBean.getResult() == 0) {
                                Looper.prepare();
                                Toast.makeText(SuggestionActivity.this, "反馈失败", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (suggestionBean.getResult() == 1) {
                                Looper.prepare();
                                Toast.makeText(SuggestionActivity.this, "反馈成功", Toast.LENGTH_SHORT).show();

                                //更新之后跳回原来界面
                                finish();
                                Looper.loop();
                            } else{
                                Looper.prepare();
                                Toast.makeText(SuggestionActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        }
                    });
                }
            }
        });
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
}
