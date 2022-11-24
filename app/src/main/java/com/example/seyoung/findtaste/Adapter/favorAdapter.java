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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
 * Created by seyoung on 2017-11-04.
 * 사용자가 즐겨찾기한 목록을 보여주는 어댑터입니다.
 *  맛집의 이름과 주소, 위도 경도, 평점, 리뷰 갯수를 저장하며,
 *  불러온 위도와 경도를 계산해 거리를 나타냅니다. ex(1.5Km)
 *  사용자가 즐겨찾기 목록에서 즐겨찾기를 해제할 때 서버에 해제한 맛집을 지워달라고 요청하며,
 *  즐겨찾기를 추가할 경우 다시 서버에 등록시킵니다.
 */

public class favorAdapter extends RecyclerView.Adapter<favorAdapter.MyViewHolder> {
    List<Tasteitem> favorList;
    Context context;
    String favorst;

    public favorAdapter(List<Tasteitem> favorList, Context context) {
        this.favorList = favorList;
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_row2, parent, false);          //미리 지정한 커스텀 뷰를 팽창시킨다
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Tasteitem faver = favorList.get(position);
        //불러온 맛집의 갯수를 알기위해 번호와 맛집이름을 같이 나타냅니다.
        holder.fname.setText((position+1)+". "+faver.getFood_name());   //앞글자에 기호가 들어가서 값을 빼줌.
        //맛집의 주소를 넣어줍니다.
        holder.faddress.setText(faver.getFood_address());
        //만약 현재 맛집의 즐겨찾기 상태가 0일 경우 빈 별을 그려주며
        if(faver.getFavorites().equals("0")){
            holder.favorites.setImageResource(android.R.drawable.btn_star_big_off);
        }
        //1일 경우 꽉 찬 별을 그려줍니다
        else{
            holder.favorites.setImageResource(android.R.drawable.btn_star_big_on);
        }
        //사용자와 맛집의 거리를 계산하는 메쏘드를 불러옵니다.
        String Km= calcDistance(GeoItem.getKnownLocation().latitude,GeoItem.getKnownLocation().longitude,
                faver.getLati(),faver.getLogi());
        //잘 불러왔는 지 확인하는 log
        Log.e("내 현재 lati",String.valueOf(GeoItem.getKnownLocation().latitude));
        Log.e("내 현재 logi",String.valueOf(GeoItem.getKnownLocation().longitude));
        Log.e("맛집 lati", String.valueOf(faver.getLati()));
        Log.e("맛집 logi", String.valueOf(faver.getLogi()));

        //계산한 거리를 지정.
        holder.Km.setText(Km);

        //즐겨찾기한 맛집의 리뷰 갯수.
        holder.review_num.setText(faver.getRating_num());

        //만약 해당 맛집의 평점이 0.0이면 사용자에게 표시를 안하며
        if(faver.getRating()==0.0)
            holder.Rating.setText("");
        else                //그 이상일 때는 평점을 표시해줍니다.
            holder.Rating.setText(String.valueOf(faver.getRating()));

        //즐겨찾기 버튼을 누를 경우 서버에 등록하기와 해제하기를 요청합니다.
        holder.favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
                String userId= pref.getString("ing", "");
                favorst=faver.getFavorites();
                // 사용자의 아이디와 맛집의 이름을 서버에 보내서 해당 유저의 즐겨찾기 목록에 추가합니다.
                if(favorst.equals("0")){
                    favorst ="1";
                    FavorData FD = new FavorData();
                    FD.execute(userId,faver.getFood_name());
                    faver.setFavorites("1");
                    holder.favorites.setImageResource(android.R.drawable.btn_star_big_on);
                }else {
                    favorst="0";
                    FavorDataDel FD = new FavorDataDel();
                    FD.execute(userId,faver.getFood_name());
                    faver.setFavorites("0");
                    holder.favorites.setImageResource(android.R.drawable.btn_star_big_off);
                }
            }
        });
        //맛집 사진을 글라이드로 추가시켜줍니다.
        Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                .load(faver.getImage_path())
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .into(holder.imageView);

        //맛집을 클릭 할 때 상세페이지로 넘어가게 만듭니다.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(faver);
            }
        };
        holder.imageView.setOnClickListener(listener);
        holder.fname.setOnClickListener(listener);
        holder.faddress.setOnClickListener(listener);
        holder.Rating.setOnClickListener(listener);
        holder.Km.setOnClickListener(listener);
        holder.review_num.setOnClickListener(listener);
    }
    @Override
    public int getItemCount() {
        return favorList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView fname;
        public TextView faddress;
        public TextView Km;
        public TextView Rating;
        public TextView review_num;
        public ImageView imageView,favorites;
        public MyViewHolder(View view) {
            super(view);
            fname = (TextView) view.findViewById(R.id.tvTitle);                     //푸드 이름
            faddress = (TextView) view.findViewById(R.id.tvYear);                   //푸드 주소
            Km = (TextView) view.findViewById(R.id.Km);                             //푸드 거리
            Rating = (TextView) view.findViewById(R.id.rating);                     //푸드 평점
            imageView = (ImageView) view.findViewById(R.id.imageView);              //푸드의 사진
            favorites = (ImageView) itemView.findViewById(R.id.favorites);          //푸드 즐겨찾기
            review_num = (TextView) view.findViewById(R.id.review_num);
        }
    }
    public void clear(){
        favorList.clear();
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
    //사용자의 위도 경도와 맛집의 위도 경도를 사용해 거리를 반환하는 메쏘드입니다.
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

        //만약 1km 이하일 경우 m를 뒤에 붙이며 이상일 경우 Km를 붙입니다.
        double rslt = Math.round((ret/1000)*10) / 10.0;
        String result = rslt + " km";
        if(rslt < 1) result = Math.round(ret) +" m";

        return result;
    }

    //서버에 해당 유저가 원하는 맛집을 즐겨찾기에 등록, 해제를 요청하는 async입니다.
    //서버와 안드로이드를 연동시킵니다.
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

    @SuppressLint("StaticFieldLeak")
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
