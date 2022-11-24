package com.example.seyoung.findtaste.Adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.Constant;
import com.example.seyoung.findtaste.model.FeedItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by seyoung on 2017-11-24.
 * 사용자들이 적은 리뷰들을 나열해주는 어댑터입니다.
 * 사용들의 맛집 정보를 원할하게 할 수 있도록 도와줍니다.
 * 최근순으로 리뷰를 보여주며, 리뷰를 적은 사용자의 닉네임과 사진,시간
 * 리뷰의 내용과 사진들을 보여줍니다. 또한 뎃글의 갯수와 좋아요의 갯수도 표시합니다.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHolder> {
    private List<FeedItem> FeedList;            // 사용자들이 적은 리뷰의 데이터가 들어갈 리스트
    Context context;
    private String likey;                       //해당 사용자가 리뷰에 좋아요를 했는 지 판단해주는 스트링. (0이면 좋아요를 안함)

    public FeedAdapter(List<FeedItem> FeedList, Context context) {
        this.FeedList = FeedList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final FeedItem item = FeedList.get(position);
        //이름과 등록한 리뷰의 맛집이름, 등록한 이후로 얼마나 시간이 지났는 지 알려줍니다.
        holder.name.setText(item.getUsername());
        holder.foodname.setText(item.getFoodname());
        // Converting timestamp into x ago format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(item.getTimestamp()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestamp.setText(timeAgo);

        //리뷰의 좋아요갯수와 댓글 갯수를 지정합니다.
        String like = item.getGood() + "개";
        holder.likenum.setText(like);
        final String comment = item.getComment() + "개";
        holder.cmnum.setText(comment);
        // Chcek for empty status message
        //만약 사용자의 상태창이 비었으면 나타내지 않는다.
        if (!TextUtils.isEmpty(item.getStatus())) {
            holder.statusMsg.setText(item.getStatus());
            holder.statusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            holder.statusMsg.setVisibility(View.GONE);
        }
        //리뷰 작성자의 사진을 넣는다
        Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                .load(item.getProfilepic())
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .override(200, 200)
                        .centerCrop()
                        .circleCrop())
                .into(holder.profilePic);

        // Feed ratingbar
        //작성자의 평점을 지정
        holder.ratingBar.setRating(item.getRating());

        //리뷰를 적은 작성자가 사진을 추가했다면 그 갯수만큼 동적으로 추가해준다.
        if(item.getReviewpic().size()>0) {
            holder.scrollView.setVisibility(View.VISIBLE);
        }
        if(item.getReviewpic().size()==0) {
            holder.scrollView.setVisibility(View.GONE);
        }

        View.OnClickListener onClickListener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(item);
            }
        };
        //이미지뷰들을 추가시킬 리니어 레이아웃을 만들어준 뒤, 이미지뷰를 갯수만큼 추가시킨다.
        LinearLayout topLinearLayout = new LinearLayout(getApplicationContext());          //호리즌스크롤뷰에 리니어 레이아웃 추가
        // topLinearLayout.setLayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT,android.widget.LinearLayout.LayoutParams.FILL_PARENT);
        topLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < item.getReviewpic().size(); i++) {
            final ImageView imageView = new ImageView(getApplicationContext());
            Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                    .load(String.valueOf(item.getReviewpic().get(i).getPath()))
                    .apply(new RequestOptions()
                            .override(400, 400)
                            .centerCrop())
                    .into(imageView);
            imageView.setPadding(0, 10, 10, 0);
            Log.e("사진경로", String.valueOf(item.getReviewpic().get(i).getPath()));
            imageView.setAdjustViewBounds(true);       //사진이 마음대로 크기가 커지지 않도록 부모의 레이아웃에 크기를 맞춘다.
            topLinearLayout.addView(imageView);         // 리니어 레이아웃에 사진 갯수만큼 이미지뷰 추가

            final int finalI = i;
            imageView.setOnClickListener(onClickListener2);
            topLinearLayout.setOnClickListener(onClickListener2);
        }

        holder.scrollView.removeAllViews();
        //이미지뷰들을 담은 리니어레이아웃을 가로 스크롤뷰에 추가시킨다.
        //사용자가 옆으로 스크롤 해 사진들을 볼 수 있다.
        holder.scrollView.addView(topLinearLayout);

        //리뷰를 적은 사용자가 수정이나 삭제를 원할 때 사용하는 클릭리스너.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onreviewItemClickListener.onreviewItemClick(item);
            }
        };
        // 점점점 세개의 이미지를 누르면 발동한다.
        holder.junm.setOnClickListener(listener);

        //현재 앱을 사용하는 유저가 해당 리뷰를 좋아요 했을 경우 나타내는 하트표시
        if(item.getLikey().equals("0")){
            holder.heart.setImageResource(R.drawable.nullheart);
        }
        else{
            holder.heart.setImageResource(R.drawable.fullheart);
        }

        //하트를 누를 경우의 리스너입니다.
        // 좋아요를 안한 경우, 서버에 현재 리뷰를 좋아요해주며, 해제한 경우 지워달라고 요청합니다.
        //또한 하트의 그림도 바꿔줍니다.
        holder.heart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
                String userId= pref.getString("ing", "");
                //현재 사용자의 이름을 보내서 서버에 추가, 삭제
                likey=item.getLikey();
                if(likey.equals("0")){
                    likey ="1";
                    LikeData FD = new LikeData();
                    FD.execute(userId,item.getFoodname(),item.getUsername2(),item.getTimestamp());
                    item.setLikey("1");
                    //좋아요 갯수를 하나 늘려줍니다.
                    item.setGood(String.valueOf(Integer.parseInt(item.getGood())+1));
                    holder.likenum.setText(item.getGood()+"개");
                    holder.heart.setImageResource(R.drawable.fullheart);
                }else {
                    likey="0";
                    UnLikeData FD = new UnLikeData();
                    FD.execute(userId,item.getFoodname(),item.getUsername2(),item.getTimestamp());
                    item.setLikey("0");
                    //좋아요 갯수를 하나 뺍니다
                    item.setGood(String.valueOf(Integer.parseInt(item.getGood())-1));
                    holder.likenum.setText(item.getGood()+"개");
                    holder.heart.setImageResource(R.drawable.nullheart);
                }
            }

        });



        holder.name.setOnClickListener(onClickListener2);
        holder.foodname.setOnClickListener(onClickListener2);
        holder.timestamp.setOnClickListener(onClickListener2);
        holder.statusMsg.setOnClickListener(onClickListener2);
        holder.like.setOnClickListener(onClickListener2);
        holder.likenum.setOnClickListener(onClickListener2);
        holder.comment.setOnClickListener(onClickListener2);
        holder.cmnum.setOnClickListener(onClickListener2);
        holder.ratingBar.setOnClickListener(onClickListener2);
        holder.scrollView.setOnClickListener(onClickListener2);
        holder.talk.setOnClickListener(onClickListener2);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;      //작성자 이름
        TextView foodname;  //맛집 이름
        TextView timestamp; //작성자가 등록한 시간
        TextView statusMsg; //작성내용
        TextView like;      //좋아요 텍스트
        TextView likenum;   //좋아요 갯수
        TextView comment;   //댓글 텍스트
        TextView cmnum;     //댓글 갯수
        ImageView profilePic;   //작성자의 사진
        ImageView junm;         //수정, 삭제를 돕는 점그림
        ImageView heart;        //좋아요 그림
        ImageView talk;         //댓글 그림
        RatingBar ratingBar;    //평점을 내타내는 레이팅바
        HorizontalScrollView scrollView;    // 이미지뷰를 가로로 볼 수 있게해주는 스크롤 뷰

        public MyViewHolder(View convertView) {
            super(convertView);

            scrollView = (HorizontalScrollView) convertView.findViewById(R.id.hori);
            name = (TextView) convertView.findViewById(R.id.name);
            foodname = (TextView) convertView.findViewById(R.id.foodName);
            timestamp = (TextView) convertView
                    .findViewById(R.id.timestamp);
            statusMsg = (TextView) convertView
                    .findViewById(R.id.txtStatusMsg);
            profilePic = (ImageView) convertView
                    .findViewById(R.id.profilePic);
            ratingBar = (RatingBar) convertView
                    .findViewById(R.id.ratingBar);
            like = (TextView) convertView
                    .findViewById(R.id.like);
            likenum = (TextView) convertView
                    .findViewById(R.id.likenum);
            comment = (TextView) convertView
                    .findViewById(R.id.comment);
            cmnum = (TextView) convertView
                    .findViewById(R.id.cmnum);
            junm = (ImageView) convertView
                    .findViewById(R.id.junm);
            heart = (ImageView) convertView
                    .findViewById(R.id.heart);
            talk = (ImageView) convertView
                    .findViewById(R.id.talk);
        }
    }

    @Override
    public int getItemCount() {
        return FeedList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        FeedList.clear();
    }

    private OnreviewItemClickListener onreviewItemClickListener;

    public OnreviewItemClickListener getOnreviewItemClickListener() {
        return onreviewItemClickListener;
    }

    public void setOnItemClickListener(OnreviewItemClickListener onreviewItemClickListener) {
        this.onreviewItemClickListener = onreviewItemClickListener;
    }

    public interface OnreviewItemClickListener {
        void onreviewItemClick(FeedItem item);
    }

    private OnItemClickListener onItemClickListener;
    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(FeedItem item);
    }

    // 서버에 현재 유저가 좋아요한 리뷰를 추가해달라고 요청합니다. 서버와 안드로이드 연결
    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    class LikeData extends AsyncTask<String, Void, String> {
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

            String user_name = (String)params[0];
            String food_name= (String)params[1];
            String write_user = (String)params[2];
            String timestamp = (String)params[3];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"likey.php";
            String postParameters = "user_name=" + user_name +"&food_name=" + food_name+"&write_user=" + write_user+"&timestamp=" + timestamp ;

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
        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    class UnLikeData extends AsyncTask<String, Void, String> {
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

            String user_name = (String)params[0];
            String food_name= (String)params[1];
            String write_user = (String)params[2];
            String timestamp = (String)params[3];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"unlikey.php";
            String postParameters = "user_name=" + user_name +"&food_name=" + food_name+"&write_user=" + write_user+"&timestamp=" + timestamp ;

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