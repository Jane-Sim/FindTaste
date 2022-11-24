package com.example.seyoung.findtaste.Adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.Constant;
import com.example.seyoung.findtaste.databinding.ItemHeaderBinding;
import com.example.seyoung.findtaste.databinding.ItemProfileBinding;
import com.example.seyoung.findtaste.item.Profile;
import com.example.seyoung.findtaste.util.FacetimeActivity;
import com.example.seyoung.findtaste.views.ChatView.ChatRoomActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * 현재 사용자의 친구 목록을 가져와 나열시켜주는 어댑터입니다.
 * 친구의 닉네임과 사진을 보여줍니다.
 */

public class ProfileAdapter extends RecyclerView.Adapter {
    private String time,friendid,friendname;
    private static final int MY_HEADER = 0;

    private static final int MY_PROFILE = 1;

    private static final int FRIEND_HEADER = 2;

    private Vector<Profile> profiles;

    private Context context;

    public ProfileAdapter(Vector<Profile> profiles, Context context) {
        this.profiles = profiles;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;

        if( viewType == MY_HEADER || viewType == FRIEND_HEADER ) {
            ItemHeaderBinding headerBinding = ItemHeaderBinding.inflate(LayoutInflater.from(context), parent, false);
            holder = new HeaderHolder(headerBinding);
        } else {
            ItemProfileBinding profileBinding = ItemProfileBinding.inflate(LayoutInflater.from(context), parent, false);
            holder = new ProfileHolder(profileBinding);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(position == MY_HEADER && holder instanceof HeaderHolder) {
            HeaderHolder itemViewHolder = (HeaderHolder) holder;
            final ItemHeaderBinding binding = itemViewHolder.binding;
            binding.friendHeaderTv.setText(profiles.get(position).getName());
        } else if (position == FRIEND_HEADER && holder instanceof HeaderHolder) {
            HeaderHolder itemViewHolder = (HeaderHolder) holder;
            final ItemHeaderBinding binding = itemViewHolder.binding;
            binding.friendHeaderTv.setText(profiles.get(position).getName());
        } else if (position == MY_PROFILE && holder instanceof ProfileHolder) {
            ProfileHolder itemViewHolder = (ProfileHolder) holder;
            final ItemProfileBinding binding = itemViewHolder.binding;
            binding.userNameTv.setText(profiles.get(position).getName());
            binding.profileMsgTv.setText(profiles.get(position).getMsg());
            Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                    .load(profiles.get(position).getUrl())
                    .apply(new RequestOptions()
                            .error(R.drawable.fbnull)
                            .fitCenter()
                            .error(R.drawable.fbnull)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .circleCrop())
                    .into(binding.profileImgView);
            binding.connect.setVisibility(View.GONE);
        } else {
            ProfileHolder itemViewHolder = (ProfileHolder) holder;
            final ItemProfileBinding binding = itemViewHolder.binding;
            binding.userNameTv.setText(profiles.get(position).getName());
            binding.profileMsgTv.setText(profiles.get(position).getMsg());
            Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                    .load(profiles.get(position).getUrl())
                    .apply(new RequestOptions()
                            .error(R.drawable.fbnull)
                            .fitCenter()
                            .centerCrop()
                            .circleCrop())
                    .into(binding.profileImgView);
            binding.connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    time = String.valueOf(System.currentTimeMillis());
                    friendid=profiles.get(position).getUserid();
                    friendname=profiles.get(position).getName();
                    CheckChattingList CC = new CheckChattingList();
                    CC.execute(profiles.get(1).getUserid(),profiles.get(position).getUserid(),time);

                }
            });
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    time = String.valueOf(System.currentTimeMillis());
                    friendid=profiles.get(position).getUserid();
                    friendname=profiles.get(position).getName();
                    String friendpic = profiles.get(position).getUrl();
                    String userid = profiles.get(1).getUserid();
                    String username=profiles.get(1).getName();
                    Intent intent = new Intent(context , FacetimeActivity.class);
                    intent.putExtra("friendid",friendid);
                    intent.putExtra("friendname",friendname);
                    intent.putExtra("friendpic",profiles.get(position).getUrl());
                    intent.putExtra("userpic",profiles.get(1).getUrl());
                    intent.putExtra("userid",userid);
                    intent.putExtra("username",username);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return MY_HEADER;
        } else if (position == 2) {
            return FRIEND_HEADER;
        } else {
            return position;
        }
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }


    private class HeaderHolder extends RecyclerView.ViewHolder {

        ItemHeaderBinding binding;

        HeaderHolder(ItemHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private class ProfileHolder extends RecyclerView.ViewHolder {

        ItemProfileBinding binding;

        ProfileHolder(ItemProfileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

    }

    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    class CheckChattingList extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            if(result.equals("fail")){
                Toast.makeText(context,"채팅방이 생성되지 않았습니다.",Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(context, ChatRoomActivity.class);
                intent.putExtra("name", profiles.get(1).getName());
                intent.putExtra("friendid", friendid);
                intent.putExtra("friend", friendname);
                intent.putExtra("fImg", profiles.get(1).getUrl());
                intent.putExtra("chatroom", result);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
                super.onPostExecute(result);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String user_id = (String)params[0];
            String friend_id= (String)params[1];
            String time= (String)params[2];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"checkroom.php";
            String postParameters = "user_id=" + user_id +"&friend_id=" + friend_id +"&time=" + time ;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {

                return new String("Error: " + e.getMessage());
            }

        }
    }
}
