package com.example.mycloudalbum.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.mycloudalbum.Bean.DeleteBean;
import com.example.mycloudalbum.Bean.FinalDeleteBean;
import com.example.mycloudalbum.Bean.RecoverBean;
import com.example.mycloudalbum.ImageLoader;
import com.example.mycloudalbum.Manager.UpdateManager;
import com.example.mycloudalbum.R;
import com.example.mycloudalbum.Util.OkhttpUtil;
import com.google.gson.Gson;
import com.komi.slider.SliderConfig;
import com.komi.slider.SliderUtils;
import com.komi.slider.position.SliderPosition;
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageDetailsActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener
{
    /**
     * 用于管理图片的滑动
     */
    private ViewPager viewPager;

    /**
     * 显示当前图片的页数
     */
    private TextView pageText;

    /**
     * 本地技术的核心类
     */
    private ImageLoader imageLoader;

    private UpdateManager updateManager;

    private int currentPosition;

    private int index;

    private ViewPagerAdapter adapter;

    private Button download_btn;

    public static final int PERMISSIONS_REQUEST_SD = 4;

    private Context context;

    private SliderConfig mConfig;

    private String userID = "12345";

    private String updateUrl;
    private String imgDelete;
    private String imgRecovery;
    private String imgFinalDelete;


    /**
     * 判断当前是云相册（0）还是回收站（1）
     */
    private int status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        QMUISwipeBackActivityManager.init(getApplication());


        final ActionBar ab = getSupportActionBar();
        if(ab != null)
        {
            ab.setDisplayHomeAsUpEnabled(true);//显示按钮
        }
        setContentView(R.layout.module_image_details);
        updateManager = UpdateManager.getInstance();
        imageLoader = ImageLoader.getInstance();

        userID = updateManager.getmUserID();
        /**
         * 刷新云相册
         */
        updateUrl = "http://203.195.217.253/cloudimg.php?userID="+userID;
        /**
         * 删除云相册图片并放入回收站
         */
        imgDelete = "http://203.195.217.253/delete.php?userID="+userID;
        /**
         * 恢复回收站图片并放入云相册
         */
        imgRecovery = "http://203.195.217.253/recovery.php?userID="+userID;
        /**
         * 彻底删除回收站中的图片
         */
        imgFinalDelete = "http://203.195.217.253/final_Delete.php?userID="+userID;

        currentPosition = getIntent().getIntExtra("image_position", 0);
        index = getIntent().getIntExtra("image_index", 0);
        status = getIntent().getIntExtra("status", 0);

        pageText = findViewById(R.id.ViewPager_text);
        viewPager = findViewById(R.id.View_Pager);
        adapter = new ViewPagerAdapter();
        context = this;

        Log.e("T","index="+index+"  current="+currentPosition);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition);
        viewPager.addOnPageChangeListener(this);

        if (status == 0)
        {
            pageText.setText((currentPosition + 1) + "/" + updateManager.getmRecyclerViewAdapter()
                    .getmAlbumBeanList().getResult().get(index).getImgUrl().size());
        }
        else
        {
            pageText.setText((currentPosition + 1) + "/" + updateManager
                    .getmRecycleRecycleViewAdapter().getmImgUrls().size());
        }


        download_btn = findViewById(R.id.detail_download_btn);
        if (status == 1)
        {
            download_btn.setBackgroundResource(R.drawable.recover);
        }
        download_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (status == 0)
                {
                    //云相册的下载
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(ImageDetailsActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_SD);
                    }
                    else
                    {
                        downLoadImg(updateManager.getmRecyclerViewAdapter()
                                .getmAlbumBeanList().getResult().get(index).getImgUrl().get(currentPosition));
                    }
                }
                else
                {
                    //TODO 回收站的恢复(需要通过网络请求获取图片原本的位置)
                    final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(context)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                            .setTipWord("正在恢复").create();
                    waitTipDialog.show();
                    final String deleteUrl = updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().get(currentPosition);
                    List<String> urls = new ArrayList<>();
                    urls.add(deleteUrl);

                    OkhttpUtil.handleSelectImgs(imgRecovery, urls, new Callback()
                    {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e)
                        {
                            waitTipDialog.dismiss();
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                            .setTipWord("恢复图片失败！请检查网络后重试").create();
                                    errorTipDialog.show();
                                    pageText.postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            errorTipDialog.dismiss();
                                        }
                                    }, 1500);
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException
                        {
                            String result = response.body().string();

                            Gson gson = new Gson();
                            final RecoverBean recoverBean = gson.fromJson(result, RecoverBean.class);
                            if (recoverBean.getResult() == -1)
                            {
                                waitTipDialog.dismiss();
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                .setTipWord("图片恢复失败！无法连接数据库").create();
                                        errorTipDialog.show();
                                        pageText.postDelayed(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                errorTipDialog.dismiss();
                                            }
                                        }, 1500);
                                    }
                                });
                            }
                            else if (recoverBean.getResult() == 802)
                            {
                                waitTipDialog.dismiss();
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                .setTipWord("图片恢复失败！").create();
                                        errorTipDialog.show();
                                        pageText.postDelayed(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                errorTipDialog.dismiss();
                                            }
                                        }, 1500);
                                    }
                                });
                            }
                            else
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //成功
                                        final int mResult = updateManager.recoverSingleImgForRecycle(recoverBean.getSuccessUrl().get(0).getImgUrl()
                                                , recoverBean.getSuccessUrl().get(0).getDate());
                                        if (mResult == 0)
                                        {
                                            finish();
                                        }
                                        else
                                        {
                                            waitTipDialog.dismiss();
                                            adapter.notifyDataSetChanged();
                                            final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                                    .setTipWord("恢复成功！").create();
                                            qmuiTipDialog.show();
                                            pageText.postDelayed(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    qmuiTipDialog.dismiss();
                                                }
                                            }, 500);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }

            }
        });

        //因为滑动退出与图片手势操作存在冲突bug，故将其暂时注释
        //initSilder();
    }

    /**
     * 下载图片到本地相册
     * @param imgUrl 需要下载的图片url
     */
    private void downLoadImg(final String imgUrl)
    {
        //网络请求,将对应图片从其url地址中下载到本地相册中
        final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在保存中").create();
        waitTipDialog.show();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Bitmap mBitmap = null;
                    if (!TextUtils.isEmpty(imgUrl))
                    { //网络图片
                        // 对资源链接
                        URL url = new URL(imgUrl);
                        //打开输入流
                        InputStream inputStream = url.openStream();
                        //对网上资源进行下载转换位图图片
                        mBitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                    }
                    saveFile(mBitmap);
                    imageLoader.addBitmapToMemoryCache(imgUrl, mBitmap);
                    //成功
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            waitTipDialog.dismiss();
                            final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                    .setTipWord("图片保存成功！").create();
                            qmuiTipDialog.show();
                            pageText.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    qmuiTipDialog.dismiss();
                                }
                            }, 500);
                        }
                    });
                }
                catch (IOException e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("图片保存失败！").create();
                            errorTipDialog.show();
                            pageText.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    errorTipDialog.dismiss();
                                }
                            }, 1500);
                        }
                    });
                    e.printStackTrace();
                    Log.e("error",e.toString());
                }
                catch (Exception e)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("图片保存失败！").create();
                            errorTipDialog.show();
                            pageText.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    errorTipDialog.dismiss();
                                }
                            }, 1500);
                        }
                    });
                    e.printStackTrace();
                    Log.e("error",e.toString());
                }
                finally
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            waitTipDialog.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 保存图片
     * @param bm 需要保存的bitmap
     * @throws IOException
     */
    private void saveFile(Bitmap bm) throws IOException
    {
        File dirFile = new File(Environment.getExternalStorageDirectory().getPath());
        if (!dirFile.exists())
        {
            dirFile.mkdir();
        }
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File myCaptureFile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        //把图片保存后声明这个广播事件通知系统相册有新图片到来
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(myCaptureFile);
        intent.setData(uri);
        this.sendBroadcast(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.image_details_bar, menu);//加载menu文件到布局
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;

            case R.id.delete_select:
                if (status == 1)
                {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(ImageDetailsActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_SD);
                    }
                    else
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(context);
                        messageDialogBuilder.setMessage("确定要彻底删除吗？")
                                .setTitle("（严肃脸）")
                                .addAction("取消", new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index)
                                    {
                                        dialog.dismiss();
                                    }
                                })

                                .addAction(0, "删除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int Index)
                                    {
                                        dialog.dismiss();

                                        final String deleteUrl = updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().get(currentPosition);
                                        //网络请求 回收站图片删除by进辉

                                        final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(context)
                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                                .setTipWord("正在删除").create();
                                        waitTipDialog.show();

                                        List<String> urls = new ArrayList<>();
                                        urls.add(deleteUrl);
                                        OkhttpUtil.handleSelectImgs(imgFinalDelete, urls, new Callback()
                                        {
                                            @Override
                                            public void onFailure(@NotNull Call call, @NotNull IOException e)
                                            {
                                                waitTipDialog.dismiss();
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                .setTipWord("删除图片失败！请检查网络后重试").create();
                                                        errorTipDialog.show();
                                                        pageText.postDelayed(new Runnable()
                                                        {
                                                            @Override
                                                            public void run()
                                                            {
                                                                errorTipDialog.dismiss();
                                                            }
                                                        }, 1500);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                                            {

                                                String result = response.body().string();
//                                                Log.e("TestFinalDelete", result);
                                                waitTipDialog.dismiss();

                                                Gson gson = new Gson();
                                                FinalDeleteBean finalDeleteBean = gson.fromJson(result, FinalDeleteBean.class);
                                                if (finalDeleteBean.getResult() == -1)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                    .setTipWord("图片删除失败！无法连接数据库").create();
                                                            errorTipDialog.show();
                                                            pageText.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1500);
                                                        }
                                                    });
                                                }
                                                else if (finalDeleteBean.getResult() == 902)
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                    .setTipWord("图片删除失败！").create();
                                                            errorTipDialog.show();
                                                            pageText.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    errorTipDialog.dismiss();
                                                                }
                                                            }, 1500);
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    runOnUiThread(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            //成功
                                                            if (updateManager.deleteSingleImgForRecycle(deleteUrl) == 0)
                                                            {
                                                                finish();
                                                            }
                                                            else
                                                            {
                                                                adapter.notifyDataSetChanged();
                                                                final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                                                        .setTipWord("删除成功！").create();
                                                                qmuiTipDialog.show();
                                                                pageText.postDelayed(new Runnable()
                                                                {
                                                                    @Override
                                                                    public void run()
                                                                    {
                                                                        qmuiTipDialog.dismiss();
                                                                    }
                                                                }, 500);
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                        QMUIDialog qmuiDialog = messageDialogBuilder.create();
                        qmuiDialog.show();
                    }
                }
                else
                {
                    QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(context);
                    messageDialogBuilder.setMessage("确定要删除吗？")
                            .setTitle("（严肃脸）")
                            .addAction("取消", new QMUIDialogAction.ActionListener()
                            {
                                @Override
                                public void onClick(QMUIDialog dialog, int index)
                                {
                                    dialog.dismiss();
                                }
                            })

                            .addAction(0, "删除", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener()
                            {
                                @Override
                                public void onClick(QMUIDialog dialog, int Index)
                                {
                                    dialog.dismiss();
                                    final String deleteUrl = updateManager.getmRecyclerViewAdapter()
                                                .getmAlbumBeanList().getResult().get(index).getImgUrl().get(currentPosition);

                                    //网络请求 云相册图片删除by进辉
                                    final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(context)
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                            .setTipWord("正在删除").create();
                                    waitTipDialog.show();


                                    List<String> urls = new ArrayList<>();
                                    urls.add(deleteUrl);
                                    OkhttpUtil.handleSelectImgs(imgDelete, urls, new Callback()
                                    {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e)
                                        {
                                            waitTipDialog.dismiss();
//                                            Log.e("Upload", e.toString());
                                            runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                            .setTipWord("删除图片失败！请检查网络后重试").create();
                                                    errorTipDialog.show();
                                                    pageText.postDelayed(new Runnable()
                                                    {
                                                        @Override
                                                        public void run()
                                                        {
                                                            errorTipDialog.dismiss();
                                                        }
                                                    }, 1500);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                                        {
                                            String result = response.body().string();
//                                                Log.e("TestDelete", result);
                                            waitTipDialog.dismiss();

                                            Gson gson = new Gson();
                                            DeleteBean deleteBean = gson.fromJson(result, DeleteBean.class);
                                            if (deleteBean.getResult() == -1)
                                            {
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                .setTipWord("图片删除失败！无法连接数据库").create();
                                                        errorTipDialog.show();
                                                        pageText.postDelayed(new Runnable()
                                                        {
                                                            @Override
                                                            public void run()
                                                            {
                                                                errorTipDialog.dismiss();
                                                            }
                                                        }, 1500);
                                                    }
                                                });
                                            }
                                            else if (deleteBean.getResult() == 702)
                                            {
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(context)
                                                                .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                                .setTipWord("图片删除失败！").create();
                                                        errorTipDialog.show();
                                                        pageText.postDelayed(new Runnable()
                                                        {
                                                            @Override
                                                            public void run()
                                                            {
                                                                errorTipDialog.dismiss();
                                                            }
                                                        }, 1500);
                                                    }
                                                });
                                            }
                                            else
                                            {
                                                runOnUiThread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        //成功
                                                        if (updateManager.deleteSingleImg(deleteUrl) == 0)
                                                        {
                                                            finish();
                                                        }
                                                        else
                                                        {
                                                            adapter.notifyDataSetChanged();
                                                            final QMUITipDialog qmuiTipDialog = new QMUITipDialog.Builder(context)
                                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                                                    .setTipWord("删除成功！").create();
                                                            qmuiTipDialog.show();
                                                            pageText.postDelayed(new Runnable()
                                                            {
                                                                @Override
                                                                public void run()
                                                                {
                                                                    qmuiTipDialog.dismiss();
                                                                }
                                                            }, 500);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                    QMUIDialog qmuiDialog = messageDialogBuilder.create();
                    qmuiDialog.show();
                }
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ViewPager的适配器
     *
     */
    class ViewPagerAdapter extends PagerAdapter
    {
        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
//            Log.e("Test", "index= "+index+"position= "+position+"  current= "+currentPosition);
            String url;
            if (status == 0)
            {
                url = updateManager.getmRecyclerViewAdapter()
                        .getmAlbumBeanList().getResult().get(index).getImgUrl().get(position);
            }
            else
            {
                url = updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().get(position);
            }

            Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(url);
            if (bitmap == null)
            {
                bitmap = imageLoader.getBitmapFromLocal(url);
                if (bitmap == null)
                {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_img);
                }
            }

            View view = LayoutInflater.from(ImageDetailsActivity.this).inflate(R.layout.module_zoom_image_layout, null);
            PhotoView mPhotoView = view.findViewById(R.id.zoom_photo_view);
            PhotoViewAttacher mAttacher = new PhotoViewAttacher(mPhotoView);
            mPhotoView.setImageBitmap(bitmap);
            mAttacher.update();
            container.addView(view);
            return view;
        }

        @Override
        public void notifyDataSetChanged()
        {
            super.notifyDataSetChanged();
            if (status == 0)
            {
                if (!updateManager.getmRecyclerViewAdapter()
                        .getmAlbumBeanList().getResult().isEmpty())
                {
                    pageText.setText((currentPosition + 1) + "/" + updateManager.getmRecyclerViewAdapter()
                            .getmAlbumBeanList().getResult().get(index).getImgUrl().size());
                }
            }
            else
            {
                if (!updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().isEmpty())
                {
                    pageText.setText((currentPosition + 1) + "/" + updateManager
                            .getmRecycleRecycleViewAdapter().getmImgUrls().size());
                }
            }
        }

        @Override
        public int getCount()
        {
            if (status == 0)
            {
                if (!updateManager.getmRecyclerViewAdapter()
                        .getmAlbumBeanList().getResult().isEmpty())
                {
                    if (index >= updateManager.getmRecyclerViewAdapter()
                            .getmAlbumBeanList().getResult().size())
                    {
                        index = updateManager.getmRecyclerViewAdapter()
                                .getmAlbumBeanList().getResult().size()-1;

                    }
                    return updateManager.getmRecyclerViewAdapter()
                            .getmAlbumBeanList().getResult().get(index).getImgUrl().size();
                }
                else
                {
                    return 0;
                }

            }
            else
            {
                if (!updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().isEmpty())
                {
                    return updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().size();
                }
                else
                {
                    return 0;
                }

            }

        }

        /**
         * Called when the host view is attempting to determine if an item's position
         * has changed. Returns {@link #POSITION_UNCHANGED} if the position of the given
         * item has not changed or {@link #POSITION_NONE} if the item is no longer present
         * in the adapter.
         *
         * <p>The default implementation assumes that items will never
         * change position and always returns {@link #POSITION_UNCHANGED}.
         *
         * @param object Object representing an item, previously returned by a call to
         *               {@link #instantiateItem(View, int)}.
         * @return object's new position index from [0, {@link #getCount()}),
         * {@link #POSITION_UNCHANGED} if the object's position has not changed,
         * or {@link #POSITION_NONE} if the item is no longer present.
         */
        @Override
        public int getItemPosition(@NonNull Object object)
        {
            if (status == 0)
            {
                return POSITION_NONE;
            }
            else
            {
                if (updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().contains(object))
                {
                    return updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().indexOf(object);
                }
                else
                {
                    return POSITION_NONE;
                }
            }

        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            View view = (View) object;
            container.removeView(view);
        }

    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    /**
     * 每当页数发生改变时重新设定一遍当前的页数和总页数
     * @param currentPage 当前的页数
     */
    @Override
    public void onPageSelected(int currentPage)
    {
        if (status == 0)
        {
            pageText.setText((currentPage + 1) + "/" + updateManager.getmRecyclerViewAdapter()
                    .getmAlbumBeanList().getResult().get(index).getImgUrl().size());
        }
        else
        {
            pageText.setText((currentPage + 1) + "/" + updateManager.getmRecycleRecycleViewAdapter().getmImgUrls().size());
        }
        currentPosition = currentPage;

    }

    /**
     * 实现界面上下滑动退出界面效果
     */
    private void initSilder()
    {
        mConfig = new SliderConfig.Builder()
                .secondaryColor(Color.TRANSPARENT)
                .position(SliderPosition.VERTICAL)  //设置上下滑动
                .edge(false)  //是否允许有滑动边界值,默认是有的true
                .build();
        SliderUtils.attachActivity(this, mConfig);
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_SD:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    downLoadImg(updateManager.getmRecyclerViewAdapter()
                            .getmAlbumBeanList().getResult().get(index).getImgUrl().get(currentPosition));
                } else
                {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(this);
                        messageDialogBuilder.setMessage("我们需要访问SD卡的读取权限,不然我们就无法读取相册啦!")
                                .setTitle("提示")
                                .addAction(0, "好的", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index)
                                    {
                                        dialog.dismiss();
                                    }
                                });
                        QMUIDialog qmuiDialog = messageDialogBuilder.create();
                        qmuiDialog.show();
                    } else
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(this);
                        messageDialogBuilder.setMessage("如果没有权限的话我们就无法调用相册啦!")
                                .setTitle("提示")
                                .addAction(0, "好的", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener()
                                {
                                    @Override
                                    public void onClick(QMUIDialog dialog, int index)
                                    {
                                        dialog.dismiss();
                                    }
                                });
                        QMUIDialog qmuiDialog = messageDialogBuilder.create();
                        qmuiDialog.show();
                    }
                }
                break;

                default:
                    break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
