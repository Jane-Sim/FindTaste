package com.example.seyoung.findtaste.registeraccount;

import android.util.Log;

import com.example.seyoung.findtaste.Base.BaseRetrofitIml;
import com.example.seyoung.findtaste.listener.RegisterListener;
import com.example.seyoung.findtaste.model.Account;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by seyoung on 2017-10-13.
 * 사용자가 회원가입을 해서 서버에 잘 저장이 되면 유저의 닉네임과 이메일을 서버에서 받아옵니다.
 */

public class RegisterAccountApiIml extends BaseRetrofitIml {
    String TAG = RegisterAccountApiIml.class.getSimpleName();
    RegisterAccountApi registerAccountApi;
    Retrofit retrofit = getRetrofit();

    //Đăng ký tài khoản
    public void registerAccount(String userName, String Name, String email, String image,final RegisterListener listener) {
        registerAccountApi = retrofit.create(RegisterAccountApi.class);
        Call<ResponseBody> call = registerAccountApi.registerAccount(userName,Name,email,image);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        int status = jsonObject.getInt("success");
                        if (status == 1) {
                            Account account = new Account();
                            account.setUserName(jsonObject.getString("user_name"));
                            account.setUserName(jsonObject.getString("email"));
                            listener.getDataSuccess(account);
                        } else {
                            Log.d(TAG, "onResponse: " + jsonObject.toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.getMessageError(new Exception(t));
            }
        });
    }
}