package com.example.seyoung.findtaste.registeraccount;

/**
 * Created by seyoung on 2017-10-13.
 * 사용자가 회원가입을 할 때 서버에 사용자가 적은 값들을 보내줍니다.
 * 아이디와 닉네임 이메일, 사진 등
 */


import com.example.seyoung.findtaste.config.Constant;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RegisterAccountApi {
    @FormUrlEncoded
    @POST(Constant.URL_REGISTER)
    Call<ResponseBody> registerAccount (@Field("userid") String username, @Field("username") String password, @Field("email") String email, @Field("image") String image);
}