package com.example.seyoung.findtaste.loginaccount;

import android.util.Log;

import com.example.seyoung.findtaste.Base.BaseRetrofitIml;
import com.example.seyoung.findtaste.listener.LoginListener;
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
 * 사용자가 정상적으로 로그인을 했을 경우, 사용자의 닉네임과 이메일을 서버에서 받아옵니다.
 */

public class LoginAccountApiIml extends BaseRetrofitIml {
    String TAG = LoginAccountApiIml.class.getSimpleName();
    LoginAccountApi loginAccountApi;
    Retrofit retrofit = getRetrofit();

    //Xác thực tài khoản
    public void authAccount (String userName, final LoginListener listener) {
        loginAccountApi = retrofit.create(LoginAccountApi.class);
        Call<ResponseBody> call = loginAccountApi.loginAccount(userName);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Kết quả trả về dạng String nên cần chuyển về dạng Json
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        int status = jsonObject.getInt("success");
                        if (status == 1) {
                            Account account = new Account();
                            account.setUserName(jsonObject.getString("user_name"));
                            account.setEmail(jsonObject.getString("email"));
                            listener.getDataSuccess(account);
                        } else {
                            Log.d(TAG, "onResponse: "+jsonObject.toString());
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