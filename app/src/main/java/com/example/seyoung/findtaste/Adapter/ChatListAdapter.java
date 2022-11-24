package com.example.seyoung.findtaste.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.databinding.ItemChatlistBinding;
import com.example.seyoung.findtaste.item.Profile;
import com.example.seyoung.findtaste.views.ChatView.ChatRoomActivity;

import java.util.Vector;

/**
 * Created by seyoung on 2017-12-04.
 * 채팅방의 리스트 목록입니다.
 * 사용자가 채팅방 밖에서 대기화면에 있을 때,
 * 원하는 채팅방으로 들어갈 수 있도록 리스트에 뿌려주며,
 * 메세지를 받을 때, 보낸 유저와 채팅방이 있을 경우, 채팅방의 메세지 내용과 시간을 바꿔주며,
 * 없을 경우, 채팅방을 추가해줍니다.
 **/

public class ChatListAdapter extends RecyclerView.Adapter {

    private Vector<Profile> profiles;
    private Context context;
    private Activity activity;

    public ChatListAdapter(Vector<Profile> profiles, Context context, Activity activity) {
        this.profiles = profiles;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        ItemChatlistBinding binding  = ItemChatlistBinding.inflate(LayoutInflater.from(context), parent, false);
        holder = new ChatViewHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatViewHolder itemViewHolder = (ChatViewHolder) holder;
        final ItemChatlistBinding binding = itemViewHolder.binding;
        final int pos = position;

        //채팅하는 반대 사용자의 이미지를 넣어줍니다.
        Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                .load(profiles.get(pos).getUrl())
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .circleCrop())
                .into(binding.friendProfileImgView);

        //마지막으로 받은 메세지 내용과, 시간, 닉네임을 지정합니다.
        binding.friendNameTxtView.setText(profiles.get(pos).getName());
        binding.friendLastMsgTxtView.setText(profiles.get(pos).getMsg());
        binding.friendLastMsgTimeTxtView.setText(profiles.get(pos).getTime());
        //채팅방을 누르면 해당 방으로 들어가게 합니다.
        //이때, 사용자의 아이디와 닉네임, 사진, 친구아이디와 닉네임, 채팅방 이름을 채팅방 액티비티에 전달합니다.
        binding.chatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ChatRoomActivity.class);
                intent.putExtra("fImg", profiles.get(pos).getUserimage());
                intent.putExtra("name", profiles.get(pos).getUsername());
                intent.putExtra("friendid", profiles.get(pos).getUserid());
                intent.putExtra("friend", profiles.get(pos).getName());
                intent.putExtra("chatroom", profiles.get(pos).getRoomname());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }


    private class ChatViewHolder extends RecyclerView.ViewHolder {

        ItemChatlistBinding binding;

        ChatViewHolder(ItemChatlistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
