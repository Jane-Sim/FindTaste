package com.example.seyoung.findtaste.views.WriteReview;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.GeoItem;

import java.util.ArrayList;

/**
 * Created by seyoung on 2017-11-21.
 * 사용자가 맛집리뷰를 쓸 수 있도록 만든 맛집리뷰 액티비티입니다.
 * 맛집 어플의 가장 중요한 핵심인, 사용자들의 솔직한 후기를 쓸 수 있는 기능입니다.
 * 사용자가 메인 화면에서 리뷰등록을 하면, 맛집이름을 선택하는 화면으로 바꿔줍니다.
 * 만약 사용자가 맛집의 상세페이지에서 리뷰등록을 하면 바로
 * 리뷰를 수정할 수 있는 화면으로 보내줍니다.
 *
 * 사용자가 리뷰수정을 눌렀을 경우에는 저장되었던 맛집데이터를
 * 리뷰 쓰는 화면에 같이 보내줍니다
 */

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewmain);

        Intent intent = getIntent();                                    //만약 사용자가 등록했던 리뷰를 수정할 경우,
        String food = intent.getStringExtra("음식 값");            // 음식 값과 리뷰에 적은 메세지, 평점과 시간과 사진값과 갯수를 받습니다.
        String status = intent.getStringExtra("리뷰 값");
        int rating = intent.getIntExtra("평점 값",0);  //만약 원하는 맛집의 상세화면에서 리뷰쓰기를 눌렀다면
        String time = intent.getStringExtra("시간 값");            // 리뷰의 맛집이름 값을 받아서 리뷰쓰는 창으로 넘어갑니다.
        ArrayList<String> pic;
        pic = intent.getStringArrayListExtra("사진 값");
        int piccount = intent.getIntExtra("사진 갯수",0);

        FragmentManager fragmentManager = getFragmentManager();         //프래그먼트를 추가 수정 삭제할 수 있는 프래그먼트 매니저를 호출합니다.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (rating==0&&food==null) {                                    //평점이 0이고 맛집 이름이 정해지지 않은 경우는 사용자가 메인화면에서 리뷰쓰기를 눌렀을 경우입니다.
            fragmentTransaction.replace(R.id.content_main, newInstance(GeoItem.getKnownLocation().latitude, GeoItem.getKnownLocation().longitude))
                    .commit();                                          // 맛집들의 이름을 검색할 수 있는 프래그먼트를 불러옵니다
        }

        else if(!food.equals(null)&&status==null){                     // 맛집 이름 값만 있는 경우에는 바로 리뷰 내용과 사진을 넣을 수 있는 프래그먼트로
            fragmentTransaction.replace(R.id.content_main, newInstance(food))   //맛집 이름을 프래그먼트에 보내준 뒤 화면을 바꿔줍니다.
                    .commit();
        }
        else {                                                          //적었던 리뷰를 수정할 때 입니다. 적었었던 맛집데이터들을 가지고 리뷰 적는 프래그먼트에
            fragmentTransaction.replace(R.id.content_main, newInstance(food,status,time,rating,pic,piccount))   //데이터들을 보내준 뒤 화면을 바꿔줍니다.
                    .commit();
        }

    }

    // 메인화면에서 -> 맛집이름부터 찾는 화면으로 넘어가고 싶을 때,(메인에서 바로 리뷰등록 클릭 시,)
    public static ReviewFind newInstance(double laticle, double logia) {
        ReviewFind myFragment = new ReviewFind();

        Bundle args = new Bundle();
        args.putDouble("lati", laticle);    //맨 처음에 사용자의 현재위치에서 가까운 맛집순으로 리스트를 보여주기 위해
        args.putDouble("logi", logia);      //번들에 사용자의 위도와 경도를 담아서 프래그먼트에 전달 합니다.
        myFragment.setArguments(args);

        return myFragment;
    }

    //사용자가 리뷰를 수정하기를 선택했을 때 나오는 메소드입니다.
    public static ReviewInfo newInstance(String foodname, String status, String time, int rating, ArrayList<String> pic, int picnum) {
        ReviewInfo myFragment = new ReviewInfo();               //이름과 리뷰 내용, 적었던 시간과 평점, 사진들과 사진 갯수를 받아서 실행합니다.
        //리뷰 적는 프래그먼트로 이동
        Bundle args = new Bundle();
        args.putString("foodname", foodname);   //맛집이름
        args.putString("status", status);       //상세내용
        args.putString("time", time);           //적었던 시간
        args.putInt("rating", rating);          //평점
        args.putStringArrayList("pic",pic);     //사진들의 경로
        args.putInt("picnum", picnum);          //사진갯수
        myFragment.setArguments(args);          // 리뷰를 수정하기 위해 사용자가 저장했던 리뷰 값을 프래그먼트에 보내 줍니다

        return myFragment;
    }

    //사용자가 맛집상세페이지에서 리뷰를 적을 때 호출되는 메소드입니다.
    public static ReviewInfo newInstance(String foodname) { //맛집 상세정보 -> 리뷰 적는 값으로
        ReviewInfo myFragment = new ReviewInfo();

        Bundle args = new Bundle();                         //맛집 이름만 보내줍니다.
        args.putString("foodname", foodname);               //리뷰를 적을 수 있는 프래그먼트를 가져옵니다.
        myFragment.setArguments(args);

        return myFragment;
    }

}