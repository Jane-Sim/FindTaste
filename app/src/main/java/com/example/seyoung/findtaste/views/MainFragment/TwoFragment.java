package com.example.seyoung.findtaste.views.MainFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.TasteArrayAdapter;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.model.Tasteitem;
import com.example.seyoung.findtaste.views.SearcheTaste.SearchmapsActivity;
import com.example.seyoung.findtaste.views.SearcheTaste.TasteResponse;

/**
 * 맛집을 검색할 수 있는 화면입니다.
 * 사용자가 맛집리스트에서 봤던 맛집이나 찾는 맛집이 생겼을 경우를 대비해
 * 현재 창에서 검색창에 맛집의 이름을 넣어서 찾고자 하는 맛집을 찾을 수 있습니다.
 * 검색한 이름이 포함된 맛집들을 리스트로 보여줍니다.
 * 지도 이미지버튼을 누르면 지도맵으로 맛집을 찾을 수 있습니다.
 */

public class TwoFragment extends Fragment {
    TasteArrayAdapter tasteadapter;                         // 서버에서 불러 온 맛집값을 넣은 리스트와 리싸이클뷰를 연결할 어댑터.
    RecyclerView recyclerView;                              // 맛집데이터를 보여줄 리싸이클뷰 입니다.
    //ArrayList<Tasteitem> foodList = new ArrayList<Tasteitem>();     //
    EditText search;                                        //유저가 맛집을 검색할 때 쓰는 에딧입니다.
   ImageView maps;                                          //지도에서 맛집을  찾고자 하는 경우에 쓰는 이미지뷰입니다.
    String filterText;                                      //유저가 맛집을 검색할 값을 필터로 보내 줄 필터스트링입니다.
    TextView nulltext;                                      //유저가 아무값도 안넣었을 때 보여줄 텍스트입니다.
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.twofragment,container,false);
        recyclerView = view.findViewById(R.id.recycler_view);            //리싸이클뷰를 현재 창에 넣어주고,
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());          //리싸이클뷰를 리니어형식으로 만들어줍니다.
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);                         //검색을 하면 텍스트 값을 세로로 나열하기 위해 세로방향으로 설정합니다.
        //layoutManager.scrollToPosition(currPos);
        recyclerView.setLayoutManager(layoutManager);                                       // 리싸이클뷰에 리니어로 설정한 값을 넣어줍니다.

        maps = view.findViewById(R.id.maps);                                                //지도로 맛집을 찾을 수 있도록 이동해주는 이미지뷰입니다.
        maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                    // 누를 경우
                Intent intent = new Intent(getActivity(),SearchmapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);  // 지도화면으로 넘어갑니다.
            }
        });
        nulltext =view.findViewById(R.id.nulltext);
        search = view.findViewById(R.id.autoCompleteTextView);                // 서버에 보내 줄 사용자 에딧입니다.
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);                   // 엔터키를 검색으로 바꿔서 검색 엔터키를 누르면 리스너가 발동하게 만듭니다.

        search.addTextChangedListener(new TextWatcher() {                     // 텍스트를 입력할 때마다
            @Override
            public void afterTextChanged(Editable edit) {
                filterText = edit.toString();                                 //입력한 값을 계속 서버에 보내서 스트링 값이 들어있는
                if(filterText.length()>0){                                  // 입력한 값이 포함되는 맛집의 이름들을 가져옵니다.
                   // listview.setFilterText(filterText);
                    nulltext.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }else {
                    recyclerView.setVisibility(View.GONE);
                    nulltext.setVisibility(View.VISIBLE);
                }
                ((TasteArrayAdapter) recyclerView.getAdapter()).getFilter().filter(filterText);         // 리싸이클뷰의 필터안에 들어갈 값을 사용자가 적은 에딧으로 넣어줍니다.
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length()==0){
                    recyclerView.setVisibility(View.GONE);
                    nulltext.setVisibility(View.VISIBLE);
                }
                else{
                    nulltext.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int beore, int count) {
            }
        });
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                switch(i){
                    case EditorInfo.IME_ACTION_SEARCH:              // 사용자가 검색 엔터키를 누를 때 생기는 리스너입니다.
                        Intent intent = new Intent(getActivity(),TasteResponse.class);          // 사용자가 입력한 에디트텍스트값에 포함되는 맛집들을
                        intent.putExtra("맛집이름", search.getText().toString());          //다음 결과값 확인 화면에 보여줍니다.
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        break;

                    default:

                        return false;
                }

                return true;


            }
        });

        tasteadapter = new TasteArrayAdapter(getActivity());               //리싸이클뷰에 넣을 어댑터를 부릅니다.

        recyclerView.setAdapter(tasteadapter);                            //연결 후 사용자가 입력한 값에 해당되는 맛집들을 검색화면 리스트에 나열시킵니다.

        tasteadapter.setOnItemClickListener(new TasteArrayAdapter.OnItemClickListener() {              //서버에서 가져온 리스트를 클릭하면
            @Override
            public void onItemClick(View view, int position) {          //리스트중 사용자가 한 아이템을  클릭했을 때, 클릭한 포지션의 값을 가져옵니다.
                Tasteitem item = tasteadapter.getItem(position);
                Toast.makeText(getActivity(), item.getFood_name(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(),TasteResponse.class);                          // 클릭한 맛집의 이름이 들어가있는 리스트 화면을 띄워줍니다.
                intent.putExtra("맛집이름", item.getFood_name());
                intent.putExtra("사진이름", item.getImage_name());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });


        return view;
    }

}
