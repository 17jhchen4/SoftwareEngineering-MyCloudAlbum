package com.example.mycloudalbum.Manager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mycloudalbum.Bean.UploadBean;
import com.example.mycloudalbum.MyGlideEngine;
import com.example.mycloudalbum.Util.FileUtilcll;
import com.example.mycloudalbum.Util.OkhttpUtil;
import com.google.gson.Gson;
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UploadManager
{
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int CROP_PHOTO = 3;

    public static final int PERMISSIONS_REQUEST_SD = 4;
    public static final int PERMISSIONS_REQUEST_CAMERA = 5;

    private static final int REQUEST_CODE_CHOOSE = 6;


    private Context mContext;
    private AppCompatActivity mActivty;
    private Uri imageUri;
    private ImageView mImageView;
//    private LocalCacheUtil localCacheUtil;
    private Handler mHandle;
    private UpdateManager updateManager;
    private String mUserID = "12345";

    public UploadManager(Context context, ImageView imageView, Handler handler)
    {
        mContext = context;
        mActivty = (AppCompatActivity)mContext;
        mImageView = imageView;
        mHandle = handler;
//        localCacheUtil = LocalCacheUtil.getInstance();
        updateManager = UpdateManager.getInstance();
        QMUISwipeBackActivityManager.init(mActivty.getApplication());
    }

    public void setmUserID(String mUserID)
    {
        this.mUserID = mUserID;
    }

    /**
     * 通过结合参数type调用getImg()方法获取并上传图片
     */
    public void uploadImg()
    {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(mActivty,
                    new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        }
        else if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(mActivty,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_SD);
        }
        else
        {
            openSysAlbum();
        }
    }

    /**
     * 打开图片选择界面
     */
    public void openSysAlbum()
    {
        Matisse.from(mActivty)
                .choose(MimeType.ofImage())//图片类型
                .showSingleMediaType(true)//是否只显示选择的类型的缩略图，就不会把所有图片视频都放在一起，而是需要什么展示什么
                .countable(true)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(9)//可选的最大数
                .capture(true)//选择照片时，是否显示拍照
                .captureStrategy(new CaptureStrategy(true, "com.example.mycloudalbum.fileprovider"))//参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.87f)
                .imageEngine(new MyGlideEngine())//图片加载引擎
                .forResult(REQUEST_CODE_CHOOSE);
    }

    private void displayImage(String imagePath)
    {
        if (imagePath != null)
        {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mImageView.setImageBitmap(bitmap);
        }
        else
        {
            Toast.makeText(mContext, "获取相册图片失败", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 对带有返回值的Intent进行处理
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data Intent对象
     */
    public void activityResult(int requestCode, int resultCode, final Intent data)
    {
        switch (requestCode)
        {
            case CROP_PHOTO:
                if (data != null && resultCode == -1)
                {
                    Bundle bundle = data.getExtras();
                    if (bundle != null)
                    {
                        Bitmap bitmap = bundle.getParcelable("data");
                        mImageView.setImageBitmap(bitmap);

                        // 把裁剪后的图片保存至本地 返回路径
                        String urlpath = FileUtilcll.saveFile(mContext, "crop.jpg", bitmap);
                    }
                }
                break;

            case REQUEST_CODE_CHOOSE:

                if (data != null && resultCode == -1)
                {
                    final QMUITipDialog waitTipDialog = new QMUITipDialog.Builder(mContext)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                            .setTipWord("正在上传").create();
                    waitTipDialog.show();

                    final List<String> imgPaths= new ArrayList<>();
                    for (int i = 0; i < Matisse.obtainPathResult(data).size(); i++)
                    {
                        String imgPath = Matisse.obtainPathResult(data).get(i);
                        //Toast.makeText(mContext, "imgPath = "+imgPath, Toast.LENGTH_SHORT).show();
                        Log.i("URL",imgPath);
                        if (imgPath != null)
                        {
                            imgPaths.add(imgPath);
                        }
                        else
                        {
                            Toast.makeText(mContext, "获取选择上传图片路径失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    Log.e("UploadCount", imgPaths.size()+"");
                   // final List<String> finalImgPaths = imgPaths;
                    OkhttpUtil.upLoadFile("http://203.195.217.253/upload.php?userID="+mUserID
                            , imgPaths, new Callback()
                            {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e)
                                {
                                    waitTipDialog.dismiss();
                                    Log.e("Upload", e.toString());
                                    mActivty.runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(mContext)
                                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                    .setTipWord("上传图片失败！请检查网络后重试").create();
                                            errorTipDialog.show();
                                            mImageView.postDelayed(new Runnable()
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
                                    Log.e("Upload", result);
                                    waitTipDialog.dismiss();

                                    Gson gson = new Gson();
                                    final UploadBean uploadBean = gson.fromJson(result, UploadBean.class);
                                    if (uploadBean.getResult_code() == -1)
                                    {
                                        mActivty.runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(mContext)
                                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                        .setTipWord("图片上传失败！无法连接数据库").create();
                                                errorTipDialog.show();
                                                mImageView.postDelayed(new Runnable()
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
                                    else if (uploadBean.getResult_code() == 0)
                                    {
                                        mActivty.runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                final QMUITipDialog errorTipDialog = new QMUITipDialog.Builder(mContext)
                                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                                        .setTipWord("图片上传失败！无法接受该文件").create();
                                                errorTipDialog.show();
                                                mImageView.postDelayed(new Runnable()
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
                                        mActivty.runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                final QMUITipDialog successTipDialog = new QMUITipDialog.Builder(mContext)
                                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                                        .setTipWord("上传图片成功！").create();
                                                successTipDialog.show();
                                                mImageView.postDelayed(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        successTipDialog.dismiss();
                                                    }
                                                }, 1500);

                                                Message message = new Message();
                                                message.what = 1;
                                                mHandle.sendMessage(message);
                                            }
                                        });
                                        //保存到本地
//                                        for (int i = 0; i < uploadBean.getResult().size(); i++)
//                                        {
//                                            localCacheUtil.setBitmapToLocal(uploadBean.getResult().get(i)
//                                                    , BitmapFactory.decodeFile(imgPaths.get(i)));
//                                        }
                                    }
                                }
                            });
                }

                 break;

            default:
                break;
        }
    }


    /**
     * 权限请求处理
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 请求结果
     */
    public void requestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_SD:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    openSysAlbum();
                }
                else
                {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivty, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    {

                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(mContext);
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
                    }
                    else
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(mContext);
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

            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    uploadImg();
                }
                else
                {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivty, Manifest.permission.CAMERA))
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(mContext);
                        messageDialogBuilder.setMessage("如果没有权限的话我们就无法调用手机拍照啦!")
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
                    else
                    {
                        QMUIDialog.MessageDialogBuilder messageDialogBuilder = new QMUIDialog.MessageDialogBuilder(mContext);
                        messageDialogBuilder.setMessage("如果没有权限的话我们就无法调用相机啦!")
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
    }

    /**
     * 裁剪图片
     *
     * @param data
     */
    private  void cropPic(Uri data)
    {
        if (data == null)
        {
            return;
        }
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(data, "image/*");

        // 开启裁剪：打开的Intent所显示的View可裁剪
        cropIntent.putExtra("crop", "true");
        // 裁剪宽高比
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        // 裁剪输出大小
        cropIntent.putExtra("outputX", 320);
        cropIntent.putExtra("outputY", 320);
        cropIntent.putExtra("scale", true);

        /**
         * return-data
         * 这个属性决定我们在 onActivityResult 中接收到的是什么数据，
         * 如果设置为true 那么data将会返回一个bitmap
         * 如果设置为false，则会将图片保存到本地并将对应的uri返回，当然这个uri得自己设定。
         * 系统裁剪完成后将会将裁剪完成的图片保存在我们所这设定这个uri地址上。我们只需要在裁剪完成后直接调用该uri来设置图片，就可以了。
         */
        cropIntent.putExtra("return-data", true);
        // 当 return-data 为 false 的时候需要设置这句
//        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // 图片输出格式
//        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 头像识别 会启动系统的拍照时人脸识别
//        cropIntent.putExtra("noFaceDetection", true);
        mActivty.startActivityForResult(cropIntent, CROP_PHOTO);
    }



}
