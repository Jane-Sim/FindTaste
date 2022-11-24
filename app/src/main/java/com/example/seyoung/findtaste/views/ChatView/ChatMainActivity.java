package com.example.seyoung.findtaste.views.ChatView;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.seyoung.findtaste.Adapter.MainFragmentAdapter;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.databinding.ActivityChatMainBinding;

// 채팅방 메인 액티비티입니다.
//사용하는 채팅 프래그먼트를 추가해 지정합니다.
public class ChatMainActivity extends AppCompatActivity {

    private static final String TAG = "ChatMainActivity";

    private static final int LAYOUT = R.layout.activity_chat_main;

    ActivityChatMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, LAYOUT);
        Log.i(TAG,"onCreate()");
        int position = getIntent().getIntExtra("position", 0);
        MainFragmentAdapter adapter = new MainFragmentAdapter(getSupportFragmentManager());
        MainViewModel mainViewModel = new MainViewModel(binding.addFab, binding.tabLayout, binding.viewPager, adapter, position,getApplicationContext());
        mainViewModel.onCreate();
    }
}
