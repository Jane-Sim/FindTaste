package com.example.seyoung.findtaste.views.ChatView;

/**
 * Created by seyoung on 2017-11-29.
 * 채팅방 메인액티비티에서 채팅에 해당되는 프래그먼트를 찾아주며
 * 스와이프시켜주고 해딩 프래그먼트로 이동시켜줍니다.
 *
 */

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.example.seyoung.findtaste.Adapter.MainFragmentAdapter;
import com.example.seyoung.findtaste.R;


public class MainViewModel implements ViewModel {

    private FloatingActionButton fab;
    private TabLayout tab;
    private ViewPager pager;
    private MainFragmentAdapter adapter;
    private int position;
    private Context context;

    public MainViewModel(FloatingActionButton fab, TabLayout tab, ViewPager pager, MainFragmentAdapter adapter, int position,Context context) {
        this.fab = fab;
        this.tab = tab;
        this.pager = pager;
        this.adapter = adapter;
        this.position = position;
        this.context = context;
    }

    @Override
    public void onCreate() {
        bindingViewPager();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPause() {

    }
    private void bindingViewPager() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ChatAddFriendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);
        tab.addTab(tab.newTab().setIcon(R.drawable.ic_people_black_24dp),0,true);
        tab.addTab(tab.newTab().setIcon(R.drawable.ic_chat_bubble_black_24dp),1);
        tab.addTab(tab.newTab().setIcon(R.drawable.ic_more_horiz_black_24dp),2);
        tab.addOnTabSelectedListener(pagerListener);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
        pager.setCurrentItem(position);
    }
    private TabLayout.OnTabSelectedListener pagerListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            pager.setCurrentItem(tab.getPosition());
            switch (tab.getPosition()) {
                case 0:
                    fab.setImageResource(R.drawable.ic_person_add_black_24dp);
                    fab.setVisibility(View.VISIBLE);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        Intent intent = new Intent(context,ChatAddFriendActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    });
                    break;
                case 1:
                    fab.setImageResource(R.drawable.ic_message_black_24dp);
                    fab.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    fab.setVisibility(View.GONE);
                    break;
            }
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}
        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    };
}
