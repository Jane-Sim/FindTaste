package com.example.seyoung.findtaste.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
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
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by seyoung on 2017-11-04.
 * 사용자가 검색창에서 원하는 맛집을 검색했을 때 해당 검색에 해당되는 맛집 정보를
 * 서버에서 가져와 보여줍니다. 사용자가 적은 검색값은 빨간색으로 글씨가 보이게 만듭니다. ex) 스시를 적었을 때 '스시'진  <--스시는 빨간글씨로 표시하고 나머지는 흰색.
 */

public class TasteArrayAdapter extends RecyclerView.Adapter<TasteArrayAdapter.MyViewHolder> implements Filterable {
    private List<Tasteitem> favorList = new ArrayList<Tasteitem>();
    Context context;                                        //리싸이클뷰가 가질 뷰값.
    private Tasteitem faver;                                        // 서버에서 받아올 값을 고정시킨다.
    private Filter listFilter;                                      //필터 처리가 될 listFilter
    private String mSearchText;                                     //사용자가 검색창에 입력한 값.

    public TasteArrayAdapter(Context context) {             //메인에서 처음 어댑터 설정할 때 쓰,는 것
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
            FilterResults results = new FilterResults();                //필터의 결과
            final ArrayList<Tasteitem> itemList = new ArrayList<>();    //결과를 보낼 어레이리스트
            if(constraint==null||constraint.length()==0){
                results.values=favorList;                               //아무것도 안 적었을 때 아무값도 없는  favor리스트에 결과값을 붙인다.
                results.count=favorList.size();
                itemList.clear();                                       //리스트와 어댑터를 비워놓는다.
                favorList.clear();
                //그리고 필터로 서버에서 부른 리스트를 clear로 제거
            }else {
                // favorList.clear();                                     // 메인에서 에디트 텍스트에 값을 넣었을 때,
                itemList.clear();
                //사용자의 위도와 경도를 가져온다. 없을 경우 남성역으로 지정해줌.
                Double lati = GeoItem.getKnownLocation().latitude;
                if(lati==null){
                    lati =37.484876;
                    }
                Double logi = GeoItem.getKnownLocation().longitude;
                if(logi==null) {
                    logi = 126.970673;
                }

                SharedPreferences pref = context.getSharedPreferences("pref", MODE_PRIVATE);
                String userId= pref.getString("ing", "");

                //현재 유저의 아이디와 위도 경로와 검색값을 서버에 보낸다.
                //맛집을 가까운 순으로 서버에서 데이터를 가져오기 위해 보낸다.
                getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
                Call<List<Tasteitem>> call = apiInterface.gettaste(String.valueOf(constraint),userId, lati,logi);        //서버에 에디트텍스트를 보낸다
                call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과
                    @Override
                    public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                        if (response == null) {                                                       //서버에서 받지 못했을 경우.
                            Toast.makeText(context, "오류", Toast.LENGTH_SHORT).show();
                        } else {
                            for (Tasteitem taste : response.body()) {
                                // favorList.add(taste);
                                faver=taste;                    // '스'를 넣었을 때 가져오는 값이 피자스쿨, 스시오, 스시향
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
                //String prefixstring = getRegEx(constraint).toLowerCase();
                mSearchText = constraint.toString().toLowerCase();
                results.values=itemList;                                    //리스트에 담긴 데이터를 필터 결과에 넣는다.
                results.count=itemList.size();
            }
            return results;
        }


/*        public String getRegEx(CharSequence elements){
            String result = "(?i).*";
            for(String element : elements.toString().split("\\s")){
                result += element + ".*";
            }
            result += ".*";
            return result;
        }*/

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            favorList=(ArrayList<Tasteitem>)results.values;                 // 필터로 처리한 결과는 favorlist가 된다.
            //서버에서 받아온 값이 있을 경우 해당 어댑터를 새로고침해서 보여준다.
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
                .inflate(R.layout.activity_tastearray, parent, false);          //미리 지정한 커스텀 뷰를 팽창시킨다
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        faver = favorList.get(position);                                      //위에서 필터로 처리해서 얻은 favorlist를 favor에 넣는다.
        holder.fname.setText(faver.getFood_name());                            //얻은 결과의 맛집 이름을 넣어줌
        Log.e("맛집의 이름",faver.getFood_name());

        String fullText = faver.getFood_name();

        //사용자가 검색한 값이 있을 경우.
        if (mSearchText != null && !mSearchText.isEmpty()) {
            //검색한 값의 시작과 끝을 알아낸다.
            int startPos = fullText.toLowerCase(Locale.KOREA).indexOf(mSearchText.toLowerCase(Locale.KOREA));
            int endPos = startPos + mSearchText.length();
            //그래서 맛집 이름에서 검색한 값의 이름만 빨간색으로 칠해준다.
            if (startPos != -1) {
                Spannable spannable = new SpannableString(fullText);
                ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.RED});
                TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);
                spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.fname.setText(spannable);
                Log.e("spannable", String.valueOf(spannable));
            } else {
                holder.fname.setText(fullText);
                Log.e("spannable", fullText);
            }
        } else {
            holder.fname.setText(fullText);
        }


        //맛집리스트의 돋보기 이미지
        holder.fimage.setImageResource(android.R.drawable.ic_search_category_default);
        //리스트를 누르면 맛집 결과창으로 이동시킨다.
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                       //리스트를 누를 경우, 리스트의 포지션과 뷰를 건네줌
                int posi = position;
                onItemClickListener.onItemClick(v,posi);
            }
        };
        holder.fname.setOnClickListener(listener);
        holder.fimage.setOnClickListener(listener);


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
        public TextView fname;                                             //맛집 이름
        public ImageView fimage;                                            // 옆에 붙는 돋보기 이미지
        public MyViewHolder(View view) {
            super(view);
            fname = (TextView) view.findViewById(R.id.tvTitle);                     //푸드 이름
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


}
