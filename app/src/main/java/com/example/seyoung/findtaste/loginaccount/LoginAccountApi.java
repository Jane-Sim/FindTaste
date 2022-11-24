package com.example.seyoung.findtaste.loginaccount;

import com.example.seyoung.findtaste.config.Constant;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by seyoung on 2017-10-13.
 * 서버에 현재 사용자의 아이디를 보내서 중복처리 됬는지 알아온다.
 */

public interface LoginAccountApi {
    @FormUrlEncoded
    @POST(Constant.URL_LOGIN)
    Call<ResponseBody> loginAccount (@Field("username") String username);
}
