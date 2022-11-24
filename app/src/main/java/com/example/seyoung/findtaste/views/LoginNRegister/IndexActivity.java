package com.example.seyoung.findtaste.views.LoginNRegister;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.views.MainFragment.MainActivity;
import com.kakao.util.helper.Utility;

/**
 * Created by seyoung on 2017-10-13.
 * 처음에 어플을 키면 보이는 스플릿 화면입니다.
 * 유저가 어플을 편리하게 쓸 수 있도록 했습니다.
 * 스플릿 화면에서는, 유저가 로그인한 적이 있으면 메인 화면으로 이동시켜주며,
 * 로그아웃을 하거나 로그인한 적이 없으면 로그인창으로 이동시킵니다.
 * 또한 인터넷 비연결이면 인터넷을 확인하라면서 알람과 함꼐 종료시킵니다.
 * (인터넷이 연결 안 되어있으면 어플에서 데이터를 못 가져옴)
 */

public class IndexActivity extends Activity {
    ConnectivityManager manager;            // 인터넷 연결유무를 학인해주는 매니저를 가져옵니다.
    NetworkInfo phone ;                     // 3,4g 유무
    NetworkInfo wifi ;                      //Wifi 유무
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
    /*    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } */          // 현재 위치를 사용할것이기에 꼭 퍼미션 확인을 해주었는 지 확인.

        manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); //현재 기기의 상태를 가져옵니다.
        phone = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);        // 3,4g가 연결 되었는지의 상태와
        wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);           //wifi 연결상태를 매니저를 통해 읽어옵니다.
        if (phone.isConnected() || wifi.isConnected()) { //무선 데이터 네트워크 또는 Wifi연결이 되어있는 상태면
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTask();
                }
            },2000);                            //2초 뒤에 화면 이동을 시킵니다.
        } else { //연결되어 있지 않을 때 다이얼로그를 띄워서 확인을 누르면 어플이 종료되게 했습니다.
            AlertDialog.Builder alert = new AlertDialog.Builder(IndexActivity.this);
            alert.setMessage("인터넷을 확인해주세요").
                    setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();     //닫기
                    finish();
                }
            }).show();
        }
    }

    // 화면이동 메서드입니다.
    public void startTask(){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String g= pref.getString("ing", "");        //쉐어드에 저장된 아이디값을 불러옵니다.

        Log.e("자동로그인값", g);

        if (g.equals("")){                                // 아이디 값이 비었으면
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);                        //로그인 화면으로 이동시켜줍니다.
        }
        if(!g.equals("")){                                 //아이디 값이 있으면
            Log.e("카톡해시키",Utility.getKeyHash(this));
         //   GeoLib.getInstance().setLastKnownLocation(this);    // 사용자 위치정보를 마지막에 업데이트한 곳으로 현재 위치를 지정합니다.
            Intent intent = new Intent(this,MainActivity.class);    //사용자가 위치 정보를 안 켰을 경우를 대비.
            startActivity(intent);                          //어플 메인으로 이동.

            finish();
        }
        finish();
    }                                                       //스플릿을 닫습니다.
}
