package com.example.seyoung.findtaste.Base;

import com.example.seyoung.findtaste.config.Constant;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by seyoung on 2017-11-01.
 */

public class RetroFitApiClient{

        private static Retrofit retrofit = null;
        public static Retrofit getClient(){
            if(retrofit==null){
                retrofit = new Retrofit.Builder().baseUrl(Constant.URL_BASE)
                                                  .addConverterFactory(GsonConverterFactory
                                                  .create())
                                                  .build();
            }
            return retrofit;
        }
}