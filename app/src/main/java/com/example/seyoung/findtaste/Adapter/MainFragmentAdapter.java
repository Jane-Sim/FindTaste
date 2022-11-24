package com.example.seyoung.findtaste.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.seyoung.findtaste.views.ChatView.ChatListFragment;
import com.example.seyoung.findtaste.views.ChatView.FriendListFragment;
import com.example.seyoung.findtaste.views.ChatView.SetupFragment;



/**
 * Created by seyoung on 2017-11-29.
 * 친구목록과 채팅방목록을 보여주는 프래그먼트들을 추가시켜 스와아프로 연동시킵니다.
 */

public class MainFragmentAdapter extends FragmentStatePagerAdapter {

    private static final int PAGE_COUNT = 3;

    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FriendListFragment();    //친구 목록을 보여주는 프래그먼트
            case 1:
                return new ChatListFragment();      //채팅방 목록을 보여주는 프래그먼트
            case 2:
                return new SetupFragment();         //아직 아무것도 추가안함
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
