package com.example.seyoung.findtaste.views.SearcheTaste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.foodAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.GeoItem;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.Tasteitem;
import com.example.seyoung.findtaste.views.SeeTasteInfo.seeTasteActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by seyoung on 2017-11-08.
 * 사용자가 맛집을 검색해서 나온, 맛집들의 리스트를 확인하는 화면입니다.
 * 만약 사용자가 검색창에 적은 맛집이름을 토대로 나온, 맛집리스트에서 하나를 누를 경우나,
 * 검색창에 맛집이름을 적고 키보드의 검색버튼을 누를 때, 현재 화면을 불러옵니다
 */

public class TasteResponse extends AppCompatActivity {
    ArrayList<Tasteitem> itemList = new ArrayList<Tasteitem>(); //검색한 값에 해당되는 맛집데이터를 담을 리스트
    foodAdapter foodadapter;            //맛집데이터와 재활용뷰를 연결시켜주는 어댑터
    RecyclerView recyclerView;          //맛집데이터를 리스트로 보여주며 뷰를 계속 만들지 않고 재활용하게 해주는 뷰
    TextView tastename;         //사용자가 적은 글씨나 클릭한 리스트의 이름을 보여줄 텍스트뷰
    TextView response;          //만약 사용자가 원한 데이터가 없을 경우, 결과값이 없음을 나타낼 텍스트뷰
    String userId;              //현재 사용자의 아이디
    ImageView x;                //결과화면을 종료해주는 이미지
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasteresponse);
        x = findViewById(R.id.imageView6);
        tastename = findViewById(R.id.tastename);
        response = findViewById(R.id.response);
        x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        userId= pref.getString("ing", "");

        Intent intent = getIntent();            //사용자가 검색한 값을 가져옵니다.(맛집의 이름이거나 주소)
        String taste = intent.getStringExtra("맛집이름");

        tastename.setText(taste);               //검색값을 넣어줍니다.

        getresponse(taste);                     //서버에 결과값을 보내줘서 해당되는 맛집데이터를 불러옵니다

        recyclerView =  findViewById(R.id.recycler_view);
        foodadapter = new foodAdapter(itemList, this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);     //화면에 가로로 2개씩 이미지가 나타나도록 설정

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new GridSpacingdecoration(2, dpToPx(10), true));         //뷰의 중간 사이에 여백을 만들어주며 뷰 자체에 패딩을 만들어줍니다.
        recyclerView.setAdapter(foodadapter);           // 맛집 데이터와 뷰를 연결시켜줍니다.

        foodadapter.setOnItemClickListener(new foodAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Tasteitem item) {
               // Toast.makeText(getApplicationContext(), item.getFood_name(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(),seeTasteActivity.class);
                intent.putExtra("맛집이름", item.getFood_name());
                intent.putExtra("사진이름", item.getImage_name());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);           // 해당 맛집을 누를 경우 맛집 이름과 사진을 보낸 뒤 맛집상세페이지로 넘어가게 해줍니다.
                startActivity(intent);
            }
        });

    }

    //검색한 맛집값을 서버에서 불러오게 요청합니다.
    public void getresponse(String taste){                                                  //맛집 검색한 결과를 리싸이클뷰로 뿌려준다.
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        //검색 값과 현재 사용자의 이름, 위도, 경도를 보내서 즐겨찾기 값과 거리를 보여주게 만듭니다.
        Call<List<Tasteitem>> call = apiInterface.gettasteresponse(taste,userId, GeoItem.knownLatitude,GeoItem.knownLongitude);
        call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과
            @Override
            public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    itemList.clear(); //데이터가 중복되지 않게 리스트를 비운 다음,
                    for (Tasteitem taste : response.body()) {
                        itemList.add(taste);    //받아온 맛집 데이터를 리스트에 차례대로 넣습니다.
                        Log.i("RESPONSE: ", "" + taste.toString());
                    }
                }
                foodadapter.notifyDataSetChanged();     //받아온 결과를 보여주기 위해 새로고침을 해줍니다.
            }

            @Override
            public void onFailure(Call<List<Tasteitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getApplicationContext(), "결과값이 없습니다. " , Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
                response.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }



    public class GridSpacingdecoration extends RecyclerView.ItemDecoration {
        private int span;
        private int space;
        private boolean include;

        public GridSpacingdecoration(int span,int space, boolean include){
            this.span = span;
            this.space = space;
            this.include = include;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int posion =parent.getChildAdapterPosition(view);
            int column = posion % span;

            if(include){
                outRect.left = space -column * space / span;
                outRect.right = (column + 1)* space / span;

                if(posion<span){
                    outRect.top = space;
                }
                outRect.bottom = space;
            } else {
                outRect.left = column * space / span;
                outRect.right = space - (column + 1) * space / span;
                if(posion>=span){
                    outRect.top = space;
                }
            }
        }
    }

    private int dpToPx(int dp){
        Resources r = getResources();
        return  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics()));
    }

}
