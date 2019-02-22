package com.gnetop.ltgameqq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.gnetop.ltgamecommon.impl.OnLoginSuccessListener;
import com.gnetop.ltgamecommon.login.LoginBackManager;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.WeakHashMap;

public class QQLoginManager {


    public static void initQQ(String APP_ID, Context context) {
        Tencent.createInstance(APP_ID, context);
    }

    /**
     * QQ登录
     *
     * @param context   上下文
     * @param APP_ID    qq申请的APPID
     * @param LTAppID   乐推APPID
     * @param LTAppKey  乐推appKey
     * @param mListener 接口回调
     */
    public static void qqLogin(Context context, String APP_ID,  String LTAppID,
                               String LTAppKey, OnLoginSuccessListener mListener) {
        Tencent mTencent = Tencent.createInstance(APP_ID, context);
        boolean isValid = mTencent.checkSessionValid(APP_ID);
        if (!isValid) {
            startLogin(context, mTencent, LTAppID, LTAppKey, mListener);
        } else {
            JSONObject jsonObject = mTencent.loadSession(APP_ID);
            mTencent.initSessionCache(jsonObject);
            try {
                String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
                String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
                String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
                Log.e("Tencent", token + "====" + openId);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 登录
     *
     * @param context 上下文
     */
    private static void startLogin(final Context context, final Tencent mTencent,
                                   final String LTAppID,
                                   final String LTAppKey,
                                   final OnLoginSuccessListener mListener) {
        mTencent.login((Activity) context, "all", new IUiListener() {
            @Override
            public void onComplete(Object response) {
                if (null == response) {
                    Log.e("Tencent", "返回为空登录失败");
                    return;
                }
                JSONObject jsonResponse = (JSONObject) response;
                if (jsonResponse.length() == 0) {
                    Log.e("Tencent", "返回为空登录失败");
                    return;
                } else {
                    try {
                        String token = jsonResponse.getString(Constants.PARAM_ACCESS_TOKEN);
                        String expires = jsonResponse.getString(Constants.PARAM_EXPIRES_IN);
                        String openId = jsonResponse.getString(Constants.PARAM_OPEN_ID);
                        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires)
                                && !TextUtils.isEmpty(openId)) {
                            mTencent.setAccessToken(token, expires);
                            mTencent.setOpenId(openId);
                            Log.e("Tencent", "登录成功" + token + "===" + openId);
                            Map<String,Object>map=new WeakHashMap<>();
                            map.put("access_token",token);
                            map.put("open_id",openId);
                            //登录
                            LoginBackManager.qqLogin(context, LTAppID, LTAppKey,map
                                    , mListener);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.e("Tencent", "登录成功");
            }

            @Override
            public void onError(UiError uiError) {
                Log.e("Tencent", uiError.errorMessage);
            }

            @Override
            public void onCancel() {
                Log.e("Tencent", "登录取消");
            }
        });
    }

    /**
     * 回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public static void onActivityResultData(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, new IUiListener() {
                @Override
                public void onComplete(Object response) {
                    if (null == response) {
                        Log.e("Tencent2", "==返回为空登录失败");
                        return;
                    }
                    JSONObject jsonResponse = (JSONObject) response;
                    if (jsonResponse.length() == 0) {
                        Log.e("Tencent2", "==返回为空登录失败");
                        return;
                    }
                    Log.e("Tencent2", "==登录成功");
                }

                @Override
                public void onError(UiError uiError) {
                    Log.e("Tencent", uiError.errorMessage);
                }

                @Override
                public void onCancel() {
                    Log.e("Tencent", "登录取消");
                }
            });
        }
    }

}
