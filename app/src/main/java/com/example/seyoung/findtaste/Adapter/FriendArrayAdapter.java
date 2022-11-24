package com.example.seyoung.findtaste.Adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.Constant;
import com.example.seyoung.findtaste.model.AddFriendItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by seyoung on 2017-11-04.
 * 사용자가 채팅하고 싶은 친구를 추가, 삭제할 수 있도록 하며
 * 사용자가 검색한 유저들을 나열시켜주는 어댑터입니다.
 */

public class FriendArrayAdapter extends RecyclerView.Adapter<FriendArrayAdapter.MyViewHolder> {
    private List<AddFriendItem> friendItemList = new ArrayList<AddFriendItem>();
    Context context;                                        //리싸이클뷰가 가질 뷰값.
    private AddFriendItem friendItem;                                        // 서버에서 받아올 값을 고정시킨다.
    private int friendst;
    public FriendArrayAdapter(List<AddFriendItem> friendItemList, Context context) {
        this.friendItemList = friendItemList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_addfriend, parent, false);          //미리 지정한 커스텀 뷰를 팽창시킨다
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        friendItem = friendItemList.get(position);
        //검색한 사용자의 닉네임, 사진을 지정해줍니다.
        holder.userNameTv.setText(friendItem.getFriendname());
        Log.e("친구 이름",friendItem.getFriend());

        Glide.with(context)                         //글라이드로 빠르게 사용자의 사진을 넣는다.
                .load(friendItem.getFriendImage())
                .apply(new RequestOptions()
                        .override(100, 100)
                        .error(R.drawable.fbnull)
                        .centerCrop()
                        .circleCrop())
                .into(holder.profileImgView);

        //만약 사용자가 해당 유저를 추가한 경우 친구삭제버튼으로 만들어주며
        // 친구추가를 안했으면 추가할 수 있도록 버튼을 만들어준다.
        if(friendItem.getFbadd()==0){
            holder.addfriend.setText("친구추가");
        }else{
            holder.addfriend.setText("친구삭제");
        }

        //친구삭제를 누르면 해당 사용자의 친구추가 목록에서 해당 유저를 삭제시킨다.
        holder.addfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
                String userId= pref.getString("ing", "");
                friendst=friendItem.getFbadd();
                if(friendItem.getFbadd()==0){
                    friendst =1;
                    friendADD FA = new friendADD();
                    FA.execute(userId,friendItem.getFriendname(),friendItem.getFriend());
                    friendItem.setFbadd(1);
                    holder.addfriend.setText("친구삭제");
                }else {
                    friendst=0;
                    friendDEL FD = new friendDEL();
                    FD.execute(userId,friendItem.getFriendname(),friendItem.getFriend());
                    friendItem.setFbadd(0);
                    holder.addfriend.setText("친구추가");
                }
            }
        });

        View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v)  {
                int posi = position;
                onItemClickListener.onItemClick(v,posi);
                return true;
            }
        };
        holder.userNameTv.setOnLongClickListener(listener);
        holder.profileImgView.setOnLongClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return friendItemList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    public AddFriendItem getItem(int position){                             //이게 중요하다. 필터의 결과인 favorlist의 포지션을 줘야 하는 것이다.
        return friendItemList.get(position);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTv;                   //맛집 이름
         ImageView profileImgView;              //사용자 사진
         Button addfriend;                      //친구추가시키거나 삭제시키는 버튼
         MyViewHolder(View view) {
            super(view);
             userNameTv = (TextView) view.findViewById(R.id.userNameTv);
             profileImgView = (ImageView) view.findViewById(R.id.profileImgView);
             addfriend = view.findViewById(R.id.addfriend);
        }
    }

    public void clear(){
        friendItemList.clear();
    }

    private OnItemLongClickListener onItemClickListener;                        //아이템을 클릭하면 설정되는 리스너

    public OnItemLongClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemLongClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemClick(View view, int position);
    }

    //서버에 해당 친구를 추가시켜달라고 요청한다
    @SuppressLint("StaticFieldLeak")
    class friendADD extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected String doInBackground(String... params) {

            String name = (String)params[0];
            String friend = (String)params[1];
            String friendname = (String)params[2];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"friendAdd.php";
            String postParameters = "name=" + name +"&friend=" + friend +"&friendname=" +friendname;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
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

    @SuppressLint("StaticFieldLeak")
    class friendDEL extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected String doInBackground(String... params) {

            String name = (String)params[0];
            String friend = (String)params[1];
            String friendname = (String)params[2];

            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"friend_del.php";
            String postParameters = "name=" + name +"&friend=" + friend +"&friendname=" +friendname;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
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
