package com.example.seyoung.findtaste.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.GeoItem;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.Tasteitem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by seyoung on 2017-11-04.
 * 사용자가 맛집의 상세정보창에서 리뷰를 안달고
 * 메인화면에서 리뷰를 작성할 경우, 원하는 맛집을 선택할 수 있도록
 * 맛집 이름을 나열시켜주는 어댑터.
 * 사용자가 아무것도 작성을 안했을 경우, 가까운 순으로 나열하며
 * 결과값이 있으면 해당 맛집을 나열시켜줍니다.
 */

public class ReviewArrayAdapter extends RecyclerView.Adapter<ReviewArrayAdapter.MyViewHolder> implements Filterable {
    List<Tasteitem> favorList = new ArrayList<Tasteitem>();
    Context context;                                        //리싸이클뷰가 가질 뷰값.
    Tasteitem faver;                                        // 서버에서 받아올 값을 고정시킨다.
    Filter listFilter;                                      //필터 처리가 될 listFilter

    public ReviewArrayAdapter(List<Tasteitem> foodList,Context context) {             //메인에서 처음 어댑터 설정할 때 쓰,는 것
        this.favorList = foodList;
        this.context = context;
    }
    @Override
    public Filter getFilter() {                             // 메인에서 필터 처리를 하고 싶을 때 부른다
        if(listFilter == null){
            listFilter=new ListFilter();
        }
        return listFilter;
    }

    private class ListFilter extends Filter{                        //필터

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.e("필터값",constraint.toString());
            FilterResults results = new FilterResults();                //사용자가 검색창에 적은 데이터를 보여줄 필터결과값.
            final ArrayList<Tasteitem> itemList = new ArrayList<>();    //결과를 보낼 어레이리스트

            //현재 사용자의 위도와 경도를 구해 서버에 요청합니다.
            Double lati = GeoItem.getKnownLocation().latitude;
            if(lati==null){
                lati =37.484876;
            }
            Double logi = GeoItem.getKnownLocation().longitude;
            if(logi==null) {
                logi = 126.970673;
            }

            //사용자가 검색창에 아무것도 안 적었을 경우
            //가까운 순으로 맛집이름과 위도 경도를 달라고 서버에 요청합니다.
            if(constraint==null||constraint.length()==0||constraint==" "){
                itemList.clear();
                favorList.clear();
                SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
                String userId= pref.getString("ing", "");
                getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
                Call<List<Tasteitem>> call = apiInterface.getreviewtaste(userId,lati,logi);        //현재 유저의 아이디와 위도 경도를 보내줍니다.
                call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과
                    @Override
                    public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                        if (response == null) {                                                       //서버에서 받지 못했을 경우.
                            Toast.makeText(context, "오류", Toast.LENGTH_SHORT).show();
                        } else {
                            for (Tasteitem taste : response.body()) {
                                // favorList.add(taste);
                                faver=taste;                    // 가까운 순으로 데이터를 리스트에 추가시킵니다.
                                itemList.add(faver);
                                notifyDataSetChanged();         // 화면 새로고침
                                Log.i("RESPONSE: ", "" + taste.toString());
                            }                                                   // 리스트에 계속 json 데이터를 축적시키며 추가한다.
                            notifyDataSetChanged();             // 화면 새로고침
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Tasteitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                        //Toast.makeText(context, "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();

                        Log.e("ERROR: ", t.getMessage());
                    }
                });
                results.values=itemList;                               //결과값을 반환시킵니다.
                results.count=itemList.size();
                //그리고 필터로 서버에서 부른 리스트를 clear로 제거
            }else {
                //사용자가 검색창에 값을 적었을 때, 리스트를 비워낸 뒤
                // 검색값에 포함되는 맛집이름들을 서버에서 받아옵니다.
                // favorList.clear();                                     // 메인에서 에디트 텍스튿에 값을 넣었을 때,
                itemList.clear();
                SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
                String userId= pref.getString("ing", "");

                getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
                Call<List<Tasteitem>> call = apiInterface.gettaste(String.valueOf(constraint),userId,lati,logi);        //서버에 에디트텍스트를 보낸다
                call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과
                    @Override
                    public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                        if (response == null) {                                                       //서버에서 받지 못했을 경우.
                            Toast.makeText(context, "오류", Toast.LENGTH_SHORT).show();
                        } else {
                            for (Tasteitem taste : response.body()) {
                                // favorList.add(taste);
                                faver=taste;                    // '스'를 넣었을 때 가져오는 값이 피자스쿨, 스시오, 스시향
                                notifyDataSetChanged();         // 화면 새로고침
                                itemList.add(faver);     //itemlist에다가 저장을 한다.
                                Log.i("RESPONSE: ", "" + taste.toString());
                            }                                                   // 리스트에 계속 json 데이터를 축적시키며 추가한다.
                            notifyDataSetChanged();             // 화면 새로고침
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Tasteitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                        //Toast.makeText(context, "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();

                        Log.e("ERROR: ", t.getMessage());
                    }
                });
                results.values=itemList;                                    //필터 결과를 itemlist로 보고한다.
                results.count=itemList.size();
            }
            return results;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            favorList=(ArrayList<Tasteitem>)results.values;                //필터링된 데이터를 맛집이름 리스트에 추가합니다.

            if(results.count>0){
                notifyDataSetChanged();
            }else {
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_reviewarray, parent, false);          //미리 지정한 커스텀 뷰를 팽창시킨다
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        faver = favorList.get(position);                                      //위에서 필터로 처리해서 얻은 favorlist를 favor에 넣는다.
        holder.fname.setText(faver.getFood_name());                            //얻은 결과의 맛집 이름을 넣어줌
        Log.e("맛집의 이름",faver.getFood_name());
        //사진과 주소, 거리를 넣어줍니다.
        holder.fimage.setImageResource(android.R.drawable.ic_search_category_default);
        holder.Address.setText(faver.getFood_address());

        String KM= calcDistance(GeoItem.getKnownLocation().latitude,GeoItem.getKnownLocation().longitude,
                faver.getLati(),faver.getLogi());

        holder.Km.setText(KM);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                       //리스트를 누를 경우, 리스트의 포지션과 뷰를 건네줌
                int posi = position;                                                  //적는 리뷰의 맛집이름을 지정해주고, 평점과 사진, 내용을 적는 프래그먼트로 이동시킵니다.
                onItemClickListener.onItemClick(v,posi);
            }
        };
        holder.fname.setOnClickListener(listener);
        holder.fimage.setOnClickListener(listener);
        holder.Address.setOnClickListener(listener);
        holder.Km.setOnClickListener(listener);
    }
    @Override
    public int getItemCount() {
        return favorList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    public Tasteitem getItem(int position){                             //이게 중요하다. 필터의 결과인 favorlist의 포지션을 줘야 하는 것이다.
        return favorList.get(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView fname,Address,Km;                                             //맛집 이름,주소, 거리
        public ImageView fimage;                                            // 옆에 붙는 이미지
        public MyViewHolder(View view) {
            super(view);
            fname = (TextView) view.findViewById(R.id.Title);                     //푸드 이름
            Address = (TextView) view.findViewById(R.id.Address);                     //푸드 주소
            Km = (TextView) view.findViewById(R.id.KM);                     //푸드 km
            fimage = (ImageView) view.findViewById(R.id.imageView5);

        }
    }

    public void clear(){
        favorList.clear();
    }

    private OnItemClickListener onItemClickListener;                        //아이템을 클릭하면 설정되는 리스너

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {                                  // 메인에다가 설정하면 뷰와 포지션 값을 전달해준다.
        void onItemClick(View view, int position);
    }

    public static String calcDistance(double lat1, double lon1, double lat2, double lon2){  // 현재 유저와 맛집간의 거리를 구하는 식
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

}
