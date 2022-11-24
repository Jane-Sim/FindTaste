package com.example.seyoung.findtaste.Adapter;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.databinding.ItemChatBinding;
import com.example.seyoung.findtaste.item.Chat;

import java.util.Vector;

/**
 * Created by seyoung on 2017-12-05.
 * 사용자가 채팅을 할 때, 채팅방 화면에 메세지가 나열되도록 만들어주는 채팅어댑터입니다.
 * 사용자가 받은 메세지가 자신 것인지, 다른 사용자의 메세지인지를 구별하며,
 * 사용자의 닉네임과 프로필사진, 채팅 내용, 시간을 레이아웃에 지정해줍니다.
 */

public class ChatAdapter extends RecyclerView.Adapter {

    private Vector<Chat> chats;
    private Context context;

    public ChatAdapter(Vector<Chat> chats, Context context) {
        this.chats = chats;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        ItemChatBinding binding = ItemChatBinding.inflate(LayoutInflater.from(context), parent, false);
        holder = new ChatHolder(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatHolder itemViewHolder = (ChatHolder) holder;
        final ItemChatBinding binding = itemViewHolder.binding;
        //채팅 액티비티에서 사용자의 아이디와 받아온 아이디가 다르면 1을 보내줍니다.
        String who = chats.get(position).getWho();
        //받은 메세지의 내용입니다.
        String msg = chats.get(position).getChat();
        switch (who) {
            //만약 메세지가 내 것이라면 상대방의 레이아웃을 숨기고, 자신의 레이아웃에 채팅내용과 시간을 넣어줍니다.
            case "0":
                binding.friendChatLayout.setVisibility(View.GONE);
                binding.myChatLayout.setVisibility(View.VISIBLE);
                binding.myMsgTxtView.setText(msg);
                binding.timemy.setText(chats.get(position).getTime());
                break;
                //상대방의 메세지일 경우, 내 레이아웃을 숨기고
                //상대방의 레이아웃에 사진과 닉네임, 시간, 내용을 넣습니다.
            case "1":
                binding.myChatLayout.setVisibility(View.GONE);
                binding.friendChatLayout.setVisibility(View.VISIBLE);
                binding.friendMsgTxtView.setText(msg);
                binding.timefriend.setText(chats.get(position).getTime());

                binding.friendname.setText(chats.get(position).getFriend());
                Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                        .load(chats.get(position).getProfile())
                        .apply(new RequestOptions()
                                .error(R.drawable.fbnull)
                                .fitCenter()
                                .override(200,200)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .circleCrop())
                        .into(binding.friendImgView);
                binding.friendImgView.setBackground(new ShapeDrawable(new OvalShape()));
                if(Build.VERSION.SDK_INT >= 21) {
                    binding.friendImgView.setClipToOutline(true);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    private class ChatHolder extends RecyclerView.ViewHolder {
        ItemChatBinding binding;

        ChatHolder(ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
