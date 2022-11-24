package com.example.seyoung.findtaste.Adapter;

import android.content.Context;
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
import com.example.seyoung.findtaste.config.GeoItem;
import com.example.seyoung.findtaste.model.Tasteitem;

import java.util.List;

/**
 * Created by seyoung on 2017-11-01.
 * 지도의 액티비티에서 마커에 추가된 맛집의 상세정보를 나열하는 어댑터입니다.
 *
 */

public class FoodHorizonAdapter extends RecyclerView.Adapter<FoodHorizonAdapter.MyViewHolder> {
    List<Tasteitem> foodList;
    Context context;
    public FoodHorizonAdapter(List<Tasteitem> foodList, Context context) {
        this.foodList = foodList;
        this.context = context;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_list_horizon, parent, false);          //미리 지정한 커스텀 뷰를 팽창시킨다
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        //맛집이름, 주소 평점, 리뷰 갯수를 보여줍니다.
        final Tasteitem food = foodList.get(position);
        holder.fname.setText((position+1)+". "+food.getFood_name());   //앞글자에 기호가 들어가서 값을 빼줌.
        holder.faddress.setText(food.getFood_address());
        if(food.getRating()==0.0)
            holder.Rating.setText("");
        else
            holder.Rating.setText(String.valueOf(food.getRating()));
        //현재 사용자의 거리 계산 값
        String Km= calcDistance(GeoItem.getKnownLocation().latitude,GeoItem.getKnownLocation().longitude,
                food.getLati(),food.getLogi());

        holder.reviewim.setImageResource(android.R.drawable.ic_menu_edit);
        holder.reviewN.setText(food.getRating_num());

        Log.e("내 현재 lati",String.valueOf(GeoItem.getKnownLocation().latitude));
        Log.e("내 현재 logi",String.valueOf(GeoItem.getKnownLocation().longitude));
        Log.e("맛집 lati", String.valueOf(food.getLati()));
        Log.e("맛집 logi", String.valueOf(food.getLogi()));
        Log.e("맛집과의 거리", Km);

        holder.Km.setText(Km);              //맛집과의 거리를 나타낸다.

        Glide.with(context)                         //글라이드로 빠르게 해당 맛집 사진을 넣는다.
                .load(food.getImage_path())
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .override(400,400)
                .centerCrop())
                .into(holder.imageView);

        //맛집 상세정보로 이동시키는 리스너입니다.
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
        TextView fname;
        TextView faddress;
        TextView Km;
        TextView Rating;
        TextView reviewN;
        ImageView imageView,reviewim;
        public MyViewHolder(View view) {
            super(view);
            fname = (TextView) view.findViewById(R.id.tvTitle);                     //푸드 이름
            faddress = (TextView) view.findViewById(R.id.tvYear);                   //푸드 주소
            Rating = (TextView) view.findViewById(R.id.rating);
            Km = (TextView) view.findViewById(R.id.Km);
            imageView = (ImageView) view.findViewById(R.id.imageView);              //푸드의 사진
            reviewim = (ImageView) view.findViewById(R.id.review);
            reviewN = (TextView) view.findViewById(R.id.review_num);
        }
    }
    public void clear(){
        foodList.clear();
    }

    //현재 유저와 맛집의 거리를 계산해서 반환하는 메쏘드입니다.
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


}
