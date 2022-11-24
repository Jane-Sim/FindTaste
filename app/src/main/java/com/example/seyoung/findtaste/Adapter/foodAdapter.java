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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.Constant;
import com.example.seyoung.findtaste.config.GeoItem;
import com.example.seyoung.findtaste.model.Tasteitem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by seyoung on 2017-11-01.
 * 사용자들이 추가한 맛집데이터를 어댑터에 추가해 사용자에게 맛집을 나열시켜보여줍니다.
 * 맛집의 이름, 주소, 사진, 평점, 거리, 리뷰 갯수를 받아옵니다.
 * 즐겨찾기를 할 경우와 맛집을 클릭하면 상세보기로 넘어가게 합니다.
 */

public class foodAdapter extends RecyclerView.Adapter<foodAdapter.MyViewHolder> {
    private List<Tasteitem> foodList;
    Context context;
    private String favorst;

    public foodAdapter(List<Tasteitem> foodList, Context context) {
        this.foodList = foodList;
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_row, parent, false);          //미리 지정한 커스텀 뷰를 팽창시킨다
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Tasteitem food = foodList.get(position);
        // 맛집이름, 주소 , 평점, 즐겨찾기를 지정합니다.
        holder.fname.setText((position+1)+". "+food.getFood_name());   //앞글자에 기호가 들어가서 값을 빼줌.
        holder.faddress.setText(food.getFood_address());
        if(food.getRating()==0.0)
            holder.Rating.setText("");
        else
        holder.Rating.setText(String.valueOf(food.getRating()));

        if(food.getFavorites().equals("0")){
            holder.favorites.setImageResource(android.R.drawable.btn_star_big_off);
        }
        else{
            holder.favorites.setImageResource(android.R.drawable.btn_star_big_on);
        }

        //현재 유저의 위치와 맛집의 위치의 거리를 계산해 보여줍니다.
        String Km= calcDistance(GeoItem.getKnownLocation().latitude,GeoItem.getKnownLocation().longitude,
                food.getLati(),food.getLogi());
        Log.e("내 현재 lati",String.valueOf(GeoItem.getKnownLocation().latitude));
        Log.e("내 현재 logi",String.valueOf(GeoItem.getKnownLocation().longitude));
        Log.e("맛집 lati", String.valueOf(food.getLati()));
        Log.e("맛집 logi", String.valueOf(food.getLogi()));
        Log.e("맛집과의 거리", Km);
        holder.Km.setText(Km);              //맛집과의 거리를 나타낸다.

        //리뷰 사진과 평점의 숫자
        holder.reviewim.setImageResource(android.R.drawable.ic_menu_edit);
        holder.reviewN.setText(food.getRating_num());

        //현재 사용자가 해당 맛집을 추가했었을 경우 꽉 찬 별을 그려줍니다.
        holder.favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
                String g= pref.getString("ing", "");
                favorst=food.getFavorites();

                if(favorst.equals("0")){
                favorst ="1";
                food.setFavorites("1");
                FavorData FD = new FavorData();
                FD.execute(g,food.getFood_name());
                holder.favorites.setImageResource(android.R.drawable.btn_star_big_on);
               }else {
                favorst="0";
                food.setFavorites("0");
                FavorDataDel FD = new FavorDataDel();
                FD.execute(g,food.getFood_name());
                holder.favorites.setImageResource(android.R.drawable.btn_star_big_off);
                }
            }
        });

        //맛집 그림을 글라이드에 보여줍니다.
        Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                .load(food.getImage_path())
                .apply(new RequestOptions()
                .error(R.drawable.fbnull)
                .centerCrop())
                .into(holder.imageView);

        //맛집의 상세정보로 보내쥬는 리스너
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(food);
            }
        };
        holder.imageView.setOnClickListener(listener);
        holder.fname.setOnClickListener(listener);
        holder.faddress.setOnClickListener(listener);
        holder.Rating.setOnClickListener(listener);
        holder.Km.setOnClickListener(listener);
        holder.reviewim.setOnClickListener(listener);
        holder.reviewN.setOnClickListener(listener);

        // holder.tvRating.setText(food.getRating());
    }
    @Override
    public int getItemCount() {
        return foodList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView fname;     //맛집 이름
        TextView faddress;  //맛집 주소
        TextView Km;        //맛집 거리
        TextView Rating;    //맛집 평점
        TextView reviewN;   //맛집 리뷰갯수
        ImageView imageView,favorites,reviewim; //맛집 사진, 즐겨찾기 그림, 리뷰 사진
        public MyViewHolder(View view) {
            super(view);
            fname = (TextView) view.findViewById(R.id.tvTitle);                     //푸드 이름
            faddress = (TextView) view.findViewById(R.id.tvYear);                   //푸드 주소
            Km = (TextView) view.findViewById(R.id.Km);
            Rating = (TextView) view.findViewById(R.id.rating);
            imageView = (ImageView) view.findViewById(R.id.imageView);              //푸드의 사진
            favorites = (ImageView) view.findViewById(R.id.favorites);
            reviewim = (ImageView) view.findViewById(R.id.review);
            reviewN = (TextView) view.findViewById(R.id.review_num);
        }
    }
    public void clear(){
        foodList.clear();
    }

    //현재 유저의 위치와 맛집의 거리를 계산하여 반환해주는 메쏘드
    public static String calcDistance(double lat1, double lon1, double lat2, double lon2){  // 거리를 구하는 식
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        double rslt = Math.round((ret/1000)*10) / 10.0;
        String result = rslt + " km";
        if(rslt < 1) result = Math.round(ret) +" m";

        return result;
    }


    private OnItemClickListener onItemClickListener;
    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(Tasteitem item);
    }

    //사용자가 맛집을 즐겨찾기 눌렀을 경우 서버에 해당 유저가 맛집을 추가했다고 요청합니다.
    @SuppressLint("StaticFieldLeak")
    class FavorData extends AsyncTask<String, Void, String> {
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
            String status = (String)params[1];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"favorites.php";
            String postParameters = "name=" + name +"&status=" + status;

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
    class FavorDataDel extends AsyncTask<String, Void, String> {
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
            String status = (String)params[1];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"favorites_del.php";
            String postParameters = "name=" + name +"&status=" + status;

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
