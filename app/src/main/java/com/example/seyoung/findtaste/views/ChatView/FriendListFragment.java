package com.example.seyoung.findtaste.views.ChatView;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.ProfileAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.databinding.FragmentFriendlistBinding;
import com.example.seyoung.findtaste.item.Profile;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.ProfileMe;

import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;



/**
 * 현재 유저의 친구들 목록을 가져오는 프래그먼트입니다.
 * 바로 채팅을 할 수 있도록 버튼을 추가했다.
 */

public class FriendListFragment extends Fragment {

    FragmentFriendlistBinding binding;
    Vector<Profile> profiles;
    String g=null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_friendlist, container, false);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        g= pref.getString("ing", "s");

        binding.myListView.setLayoutManager(manager);
        profiles = new Vector<>();
        getMe();
        binding.myListView.setAdapter(new ProfileAdapter(profiles, getContext()));
        View view = binding.getRoot();
        //here data must be an instance of the class MarsDataProvider
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getMe();
    }
    //내 정보와 친구목록에 추가된 친구데이터를 서버에서 가져와 리스트에 추가시킵니다.
    public void getMe() {

        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        Call<List<ProfileMe>> call = apiInterface.getPFme(g,1);
        call.enqueue(new Callback<List<ProfileMe>>() {
            @Override
            public void onResponse(Call<List<ProfileMe>> call, Response<List<ProfileMe>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getContext(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    profiles.clear();
                    profiles.add(new Profile("","내 프로필","",""));
                    int o=0;
                    for (ProfileMe pfme : response.body()) {
                        profiles.add(new Profile(pfme.getProfilepic(),pfme.getUsername(),"",pfme.getUserid()));
                        o++;
                        if(o==1)
                            profiles.add(new Profile("","친구목록","",""));

                    }
                    binding.myListView.getAdapter().notifyDataSetChanged();
                    // 리스트에 계속 json 데이터를 축적시키며 추가한다.
                }
            }

            @Override
            public void onFailure(Call<List<ProfileMe>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getContext(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

}
