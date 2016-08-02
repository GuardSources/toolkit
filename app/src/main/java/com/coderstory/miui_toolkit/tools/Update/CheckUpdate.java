package com.coderstory.miui_toolkit.tools.Update;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.coderstory.miui_toolkit.R;
import com.coderstory.miui_toolkit.UpdateService;

import org.json.JSONException;
import org.json.JSONObject;



public class CheckUpdate {

  private  Context mcontext =null;

    public  CheckUpdate(Context context){
        this.mcontext=context;
    }


    /**
     * 检测app的更新信息并保存到UpdateConfig中
     * @throws JSONException
     */
    private static void hasNew() throws JSONException {
        HttpHelper HH=new  HttpHelper();
        String result = HH.RequestUrl("http://coderstory.picp.io/info");
        JSONObject JsonString = new JSONObject(result);//转换为JSONObject
        UpdateConfig.URL = JsonString.getString("URL");
        UpdateConfig.Version = JsonString.getString("Version");
        UpdateConfig.Info = JsonString.getString("info");
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("mylog", "请求结果为-->" + val);
            if (!UpdateConfig.Msg.equals("")) {
                Toast.makeText(mcontext, UpdateConfig.Msg, Toast.LENGTH_LONG).show();
            }


            try {
                UpdateConfig.localVersion =mcontext. getPackageManager().getPackageInfo(
                        mcontext.getPackageName(), 0).versionCode; // 设置本地版本号
                UpdateConfig.serverVersion = Integer.parseInt(UpdateConfig.Version);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (UpdateConfig.localVersion < UpdateConfig.serverVersion) {

                // 发现新版本，提示用户更新
                AlertDialog.Builder alert = new AlertDialog.Builder(mcontext);
                alert.setTitle(R.string.App_Update)
                        .setMessage(UpdateConfig.Info)
                        .setPositiveButton(R.string.Update,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // 开启更新服务UpdateService
                                        // 这里为了把update更好模块化，可以传一些updateService依赖的值
                                        // 如布局ID，资源ID，动态获取的标题,这里以app_name为例
                                        Intent updateIntent = new Intent(
                                                mcontext,
                                                UpdateService.class);
                                        updateIntent.putExtra("titleId",
                                                R.string.app_name);
                                        mcontext.startService(updateIntent);
                                    }
                                })
                        .setNegativeButton(R.string.Btn_Cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.dismiss();
                                    }
                                });
                alert.create().show();
            }
        }

    };

    /**
     * 网络操作相关的子线程
     */
   public   Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            // TODO
            // 在这里进行 http request.网络请求相关操作
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                hasNew();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", "OK");
            handler.sendMessage(msg);
        }
    };

}