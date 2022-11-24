package com.example.seyoung.findtaste.views.ChatView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.FriendArrayAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.AddFriendItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by seyoung on 2017-12-23.
 * 사용자가 채팅하고 싶은 유저를 찾는 액티비티입니다.
 * 검색창에 검색값을 넣고 찾기 버튼을 누르면
 * 검색값에 포함되는 친구리스트를 서버에서 받아옵니다.
 * 친구 추가 버튼을 누르면 친구 목록에 추가되며
 * 삭제를 누르면 서버에서 삭제됩니다.
 */

public class ChatAddFriendActivity extends AppCompatActivity {
    String userId;
    List<AddFriendItem> friendList;
    FriendArrayAdapter friendArrayAdapter;
    RecyclerView recyclerView;
    EditText finduser_et;
    Button search_userbt,x;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriend);
        //현재 유저의 아이디를 가지고,
        // 서버에서 받아온 친구리스트에서, 현재 친구가 추가된 친구인지, 아닌지를 구분해줍니다.
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);//검색한 값을 리스트에 추가하기 전에,
        userId= pref.getString("ing", "");
        //친구이름을 적을 에딧창입니다.
        finduser_et = findViewById(R.id.finduser);
        //친구찾기 버튼입니다.
        search_userbt = findViewById(R.id.search_userbt);
        //검색창을 비울 엑스버튼.
        x = findViewById(R.id.x);
        //서버에서 받아온 친구데이터를 담을 리스트
        friendList = new ArrayList<AddFriendItem>();
        //리싸이클뷰와 어댑터
        recyclerView = findViewById(R.id.recycler_view);
        friendArrayAdapter = new FriendArrayAdapter(friendList, this);                              //받아온 맛집의 리뷰데이터를 리싸이클뷰의 아이템과 연결시켜준다.

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);          //리싸이클뷰를 리니어레이아웃으로 지정해준다.
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);                         //리니어 매니저는 세로로 리싸이클뷰를 나열시킨다.

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(friendArrayAdapter);

        //친구 찾기 버튼을 누르면
        search_userbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID= finduser_et.getText().toString();
                //검색한 결과값이 있을 때, 검색결과를 가져오며, 검색창을 비워준 뒤 키보드를 내린다.
                if(!userID.equals("")||!userID.equals(" ")||!userID.equals("\n")) {
                    getallfriend(userID);
                    finduser_et.setText("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(search_userbt.getWindowToken(), 0);
                }
            }
        });
        //엑스버튼을 누르면 검색창을 비움.
        x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finduser_et.setText("");
            }
        });
    }

    //검색값에 포함되는 친구들을 불러옵니다.
    public void getallfriend(String friend){

        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        Call<List<AddFriendItem>> call = apiInterface.getfindfriend(userId,friend);        //서버에 를 보낸다
        call.enqueue(new Callback<List<AddFriendItem>>() {                                        //서버와 연결하고 나서 받아온 결과
            @Override
            public void onResponse(Call<List<AddFriendItem>> call, Response<List<AddFriendItem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    friendList.clear();
                    friendArrayAdapter.clear();
                    for (AddFriendItem Friend : response.body()) { // 맛집 검색한 값을 리싸이클뷰로 뿌려준다.
                        friendList.add(Friend);
                        friendArrayAdapter.notifyDataSetChanged();
                        Log.i("RESPONSE: ", "" + Friend.toString());
                    }
                }
                friendArrayAdapter.notifyDataSetChanged();       //추가한 리싸이클뷰를 새로고침해서 유저에게 보여준다
            }

            @Override
            public void onFailure(Call<List<AddFriendItem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getApplicationContext(), "결과값이 없습니다. " , Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });
    }

}
