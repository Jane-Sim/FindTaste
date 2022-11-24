package com.example.seyoung.findtaste.views.WriteReview;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.ReviewArrayAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.Tasteitem;
import com.example.seyoung.findtaste.views.AddTasteInfor.AddtasteActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by seyoung on 2017-11-21.
 * 유저가 메인화면에서 리뷰를 쓰기 원할 때 나타나는 프래그먼트입니다.
 * 사용자의 가장 가까운 거리 순으로 맛집의 리스트를 넣어줍니다.
 * 아니면 사용자가 직접 맛집 이름을 검색해서 어디 식당의 리뷰를
 * 적을 것인 지 아니면 가까운 맛집의 리스트를 눌러서 사용할 건지를 도와줍니다.
 */


public class ReviewFind extends Fragment {
    double lati,logi;           // 사용자의 위도와 경도를 담을 변수입니다.
    EditText search;            // 사용자가 검색할 맛집의 에딧입니다.
    Button addtaste;            // 만약 사용자가 원하는 맛집이 없을 경우에, 맛집을 등록하러 가는 버튼입니다.
    ReviewArrayAdapter reviewadapter; //서버에서 불러온 맛집의 정보를 리싸이클뷰와 연결시킬 어댑터입니다.
    RecyclerView recyclerView;      // 맛집의 이름을 보여 줄 리싸이클뷰입니다.
    List<Tasteitem> reviewlist = new ArrayList<Tasteitem>();  // 받아온 맛집의 데이터를 넣을 list입니다.
    String filterText;                                //유저가 맛집을 검색할 값을 담을 String.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {                       //사용자의 위도와 경도를 받았을 경우
            lati = getArguments().getDouble("lati");
            logi = getArguments().getDouble("logi");
        }
        else {                                              //못 받았을 경우에는 기본 값을 지정해줍니다.
                lati =37.484876;
                logi =126.970673;
        }
        getresponse();                      //서버에서 데이터를 불러오는 메소드를 실행합니다.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_reviewfind, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addtaste = view.findViewById(R.id.addtaste);
        addtaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {            //맛집을 등록하러 가는 버튼과 리스너입니다.
                Intent intent = new Intent(getActivity(), AddtasteActivity.class);
                startActivity(intent);
            }
        });

        search = view.findViewById(R.id.autoCompleteTextView);   //유저가 찾고자 하는 맛집 검색값입니다.

        recyclerView = view.findViewById(R.id.recycler_view);    //맛집의 리스트를 보여줄 리싸이클뷰를 불러옵니다.

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());          //리싸이클뷰에 필요한 매니저도 찾아준다.
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);                         //리니어 매니저를 볼러서 세로로 만들어준다.
        recyclerView.setLayoutManager(layoutManager);                                       //리싸이클뷰에 리니어 매니저 값을 넣는다.

        search.addTextChangedListener(new TextWatcher() {                                  // 맛집 검색창에 텍스트를 입력할 때마다 나오는 리스너입니다.
            @Override
            public void afterTextChanged(Editable edit) {
                filterText = edit.toString();                                               //입력한 값을 계속 서버에 보내서 검색한 값이 들어있는 맛집 리스트를 가져옵니다.
               if(filterText.length()<0){   // 만약 아무 값도 않넣었을 때는 가까운 순으로
                    filterText=" ";         //맛집 리스트를 넣기 위해 검색 창을 널값으로 만듭니다. ex)'가' 이런 게 아닌 'ㄱ' 만 들어간 경우 'ㄱ'을 없애준다.
                }
                ((ReviewArrayAdapter) recyclerView.getAdapter()).getFilter().filter(filterText);         // 리싸이클뷰는 필터링된 값만 나열한다.
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

            @Override
            public void onTextChanged(CharSequence s, int start, int beore, int count) {

            }
        });

        reviewadapter = new ReviewArrayAdapter(reviewlist,getActivity());                            // 필터링 된 맛집 이름 값을 어댑터에 넣고
        recyclerView.setAdapter(reviewadapter);                                                      //리싸이클뷰에 어댑터를 연결해준다.

        reviewadapter.setOnItemClickListener(new ReviewArrayAdapter.OnItemClickListener() {          //리싸이클뷰에 나타난 아이템을 클릭하면,
            @Override
            public void onItemClick(View view, int position) {                                       //선택한 맛집의 이름을 가져와서
                Tasteitem item = reviewadapter.getItem(position);                                    //리뷰를 적는 프래그먼트로 넣어준 뒤
                String foodname=item.getFood_name();                                                 //현재 프래그먼트에서 리뷰를 적는 프래그먼트로 바꿔준다.
                Toast.makeText(getActivity(), item.getFood_name(), Toast.LENGTH_LONG).show();        // 선택한 맛집을 정확하게 알게 하기 위해서
                FragmentManager fragmentManager = getFragmentManager();                              //알림창에 맛집 이름을 띄워준다
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_main, newInstance(foodname))
                        .commit();
            }
        });


    }

    //검색한 맛집의 이름을 가져오는 메소드입니다.
    public void getresponse(){
        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);//검색한 값을 리스트에 추가하기 전에,
        String userId= pref.getString("ing", "");                                       //유저의 가까운 순으로 먼저 맛집의 리스트를 가져와야 하기에
                                                                                              //유저의 아이디와 위도 경도를 서버에 보내준다

        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        Call<List<Tasteitem>> call = apiInterface.getreviewtaste(userId,lati,logi);        //서버에 를 보낸다
        call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과
            @Override
            public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    for (Tasteitem taste : response.body()) { // 맛집 검색한 값을 리싸이클뷰로 뿌려준다.
                         reviewlist.add(taste);
                        reviewadapter.notifyDataSetChanged();
                        Log.i("RESPONSE: ", "" + taste.toString());
                    }
                }
                reviewadapter.notifyDataSetChanged();       //추가한 리싸이클뷰를 새로고침해서 유저에게 보여준다
            }

            @Override
            public void onFailure(Call<List<Tasteitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getApplicationContext(), "결과값이 없습니다. " , Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });
    }


    // 다음 프래그먼트로 이동시키는 메소드이다. 선택한 맛집 이름을 프래그먼트에 보내준다
    public static ReviewInfo newInstance(String name) {
        ReviewInfo myFragment = new ReviewInfo();

        Bundle args = new Bundle();
        args.putString("foodname", name);
        myFragment.setArguments(args);

        return myFragment;
    }

}
