package com.example.seyoung.findtaste.views.FiveFgChilds;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.favorAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.Tasteitem;
import com.example.seyoung.findtaste.views.SeeTasteInfo.seeTasteActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by seyoung on 2017-11-04.
 * 마이페이지에서 사용자가 즐겨찾기한 맛집데이터를 모아볼 수 있게 만들었습니다.
 * 현재 즐겨찾기 목록에서 맛집 상세페이지로 넘어갈 수 있으며, 즐겨찾기를 해제할 수 있습니다.
 *
 */

public class ChildFragment extends Fragment {
    //public static ChildFragment newInstance() {
    //    return new ChildFragment();
   // }
    public static ChildFragment newInstance() {
        return new ChildFragment();
    }
    favorAdapter favoradapter;                                  //즐겨찾기한 맛집데이터를 뷰를 연결시켜주는 어댑터. 사용자에게 즐겨찾기한 맛집 정보를 줄 수 있습니다.
    RecyclerView recyclerView;                                  //즐겨찾기한 맛집데이터를 리스트 형식으로 보여줄 수 있는 리싸이클뷰입니다. 세로로 보여주거나 가로로 보여줄 수 있습니다.
    ArrayList<Tasteitem> favorList = new ArrayList<Tasteitem>();        //서버에서 받아온 데이터를 담을 리스트입니다. 사용자가 즐겨찾기한 맛집들을 저장하는 리스트.
    Parcelable recyclerViewState;                               //현재 화면에서 벗어났을 경우, 리싸이클 뷰의 상태를 저장해줍니다. 유저의 스크롤 위치를 다시 찾아줍니다.


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview2);
        getfoodList();  //서버에 현재사용자의 즐겨찾기된 맛집데이터를 가져옵니다.

        favoradapter = new favorAdapter(favorList, getActivity());  //가져온 데이터들을 어댑터에 넣습니다.
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);    //맛집을 한 줄에 하나가 아닌, 한줄에 두 칸씩으로 보여줍니다.
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new GridSpacingdecoration(2, dpToPx(10), true));         //뷰들의 여백과, 가장자리 패딩을 만들어줍니다.
        recyclerView.setNestedScrollingEnabled(false);      //스크롤을 사용하지 않게해서, 마이페이지에서 스크롤할 때 데이터만큼 스크롤을 길게 만들어줍니다.

        recyclerView.setAdapter(favoradapter);      //맛집데이터를 담은 어댑터를 현재 그리드로 만든 뷰와 연결시킵니다.
        //즐겨찾기 된 해당 맛집을 누를 경우, 누른 맛집의 이름을 알림창으로 띄운 뒤,
        favoradapter.setOnItemClickListener(new favorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Tasteitem item) {
                Toast.makeText(getActivity(), item.getFood_name(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(),seeTasteActivity.class);
                intent.putExtra("맛집이름", item.getFood_name());
                intent.putExtra("사진이름", item.getImage_name());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);      //상세페이지에 맛집이름과 사진이름을 보내줍니다.
            }
        });
        return view;
    }

    //현재 사용자가 즐겨찾기에 추가한 맛집데이터만 서버에서 가져옵니다.
    public void getfoodList() {
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);
        //서버와 연결을 시킨다.
        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        String userId= pref.getString("ing", "s");
        //사용자의 아이디를 서버에 보내서 즐겨찾기 목록을 받아옵니다.
        Call<List<Tasteitem>> call = apiInterface.getfavor(userId);
        call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과
            @Override
            public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getActivity(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    favorList.clear();
                    favoradapter.clear();
                    for (Tasteitem taste : response.body()) {
                        favorList.add(taste);   //맛집의 갯수만큼 리스트에 추가해줍니다.
                        Log.i("RESPONSE: ", "" + taste.toString());
                    }
                }
                favoradapter.notifyDataSetChanged();                     // 결과값을 보여주기 위해 화면 새로고침
            }

            @Override
            public void onFailure(Call<List<Tasteitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getActivity(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

    //다시 화면으로 돌아왔을 때 저장된 리싸이클뷰의 스크롤 위치로 화면을 이동시켜줍니다.
    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewState != null)
            getfoodList();
        Log.e("멈췄나요","?");
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        // feedadapter.notifyDataSetChanged();
    }

    //화면이 멈추거나 중지될 때 현재 리싸이클뷰의 스크롤위치를 저장해줍니다.
    @Override
    public void onStop() {
        super.onStop();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

    }

    //화면이 멈추거나 중지될 때 현재 리싸이클뷰의 스크롤위치를 저장해줍니다.
    @Override
    public void onPause() {
        super.onPause();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
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