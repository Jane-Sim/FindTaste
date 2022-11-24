package com.example.seyoung.findtaste.Base;

import com.example.seyoung.findtaste.config.Constant;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by seyoung on 2017-10-13.
 */

public abstract class BaseRetrofitIml {
    private Retrofit retrofit;

    public Retrofit getRetrofit() {
        if (retrofit == null)
            return new Retrofit.Builder()
                    .baseUrl(Constant.URL_BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        else return retrofit;
    }

}
