package com.example.seyoung.findtaste.views.MainFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.FeedAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.FeedItem;
import com.example.seyoung.findtaste.views.WriteReview.ReviewActivity;
import com.example.seyoung.findtaste.views.WriteReview.ReviewComment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.login.widget.ProfilePictureView.TAG;

/**
 * Created by seyoung on 2017-10-14.
 * 사용자들이 맛집을 추가하고 보는 것에서 끝나지 않고,
 * 커뮤니케이션을 하면서 sns활동을 하게 만들자고 생각했습니다.
 * 사용자가 맛집의 리뷰를 적고 다른 사용자들도 적은 리뷰를 볼 수 있도록
 * 리뷰를 모아보게 만든 피드게시판입니다.
 */

public class FourFragment extends Fragment {
    public static FourFragment newInstance() {
        return new FourFragment();
    }
    FeedAdapter feedadapter;            // 서버에서 불러온 리뷰 데이터를 담은 리스트를 연결할 어댑터입니다.
    RecyclerView recyclerView;          // 리뷰를 리스트보여줄 리싸이클뷰입니다.
    ArrayList<FeedItem> feedList = new ArrayList<FeedItem>();      //서버에서 불러온 리뷰 데이터를 담을 리스트입니다.
    FeedItem feed;                      //
    String userId;                      //서버에 보낼 유저의 아이디 입니다. 아이디를 보내야 유저가 어떤 리뷰에 좋아요를 눌렀는 지 확인이 가능합니다.
    Parcelable recyclerViewState;       // 화면이 멈췄을 때 리싸이클뷰의 상태를 저장할 값을 담을 변수입니다.

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fourfragment, container, false);
        recyclerView =  view.findViewById(R.id.recycler_view);            //리싸이클 뷰를 화면에 설정.

        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        userId= pref.getString("ing", "");

        //리뷰 데이터를 불러옵니다.
        getfeedList();

        feedadapter = new FeedAdapter(feedList, getActivity());                         //리뷰 화면에 리싸이클뷰를 연결할 어댑터를 불러옵니다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());          //리싸이클뷰를 리니어레이아웃처럼 설정시킬 리니어 매니저를 불러줍니다..
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);                         //리니어 매니저는 세로로 리싸이클뷰를 나열시킨다.
        //layoutManager.scrollToPosition(currPos);
        recyclerView.setLayoutManager(layoutManager);                                       //설정한 리니어매니저값을 넣습니다.

        recyclerView.setItemAnimator(new DefaultItemAnimator());                            // 애니메이션을 기본으로 설정 후
        recyclerView.setAdapter(feedadapter);                                               // 데이터를 리싸이클뷰가 데이터와 연결되도록 어댑터를 리싸이클뷰에 설치합니다.

        feedadapter.setOnItemClickListener(new FeedAdapter.OnItemClickListener() {          //
            @Override
            public void onItemClick(FeedItem item) {                 //어댑터의 특정 아이템을 눌렀을 경우의 리스너입니다.
                ArrayList<String> picN = new ArrayList<>();          //사진들을 담을 어레이리스트를 만들어준다.
                for(int i=0; i<item.getReviewpic().size();i++) {     //사진갯수만큼 리스트에 사진 url을 담는다
                    picN.add(item.getReviewpic().get(i).getPath());
                }
                Intent intent = new Intent(getActivity(),ReviewComment.class);
                intent.putExtra("음식 값",item.getFoodname());
                intent.putExtra("리뷰 값",item.getStatus());
                intent.putExtra("평점 값",item.getRating());
                intent.putExtra("시간 값",item.getTimestamp());
                intent.putExtra("프로필 값",item.getProfilepic());
                intent.putExtra("닉네임 값",item.getUsername());     //닉네임이다
                intent.putExtra("아이디 값",item.getUsername2());   //사용자의 아이다
                intent.putExtra("좋아요 값",item.getGood());
                intent.putExtra("댓글 값",item.getComment());
                intent.putExtra("하트 값",item.getLikey());
                intent.putStringArrayListExtra("사진 값",picN);
                intent.putExtra("사진 갯수",item.getReviewpic().size());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);                              // 리뷰를 상세보기 할 수 있는 화면으로 데이터들을 넘기고 들어갑니다.
            }
        });

        // 리뷰 화면에서 ... 인 이미지버튼을 누를 때 나타나는 리스너입니다.
        feedadapter.setOnItemClickListener(new FeedAdapter.OnreviewItemClickListener(){
            @Override
            public void onreviewItemClick(final FeedItem item) {
                if(userId.equals(item.getUsername2())) {            // 리뷰를 쓴 사람이 현자 사용자와 같을 경우에는 리뷰를 수정, 삭제하게 해준다.
                    new AlertDialog.Builder(getContext())
                            .setSingleChoiceItems(R.array.reviewcatagory, -1,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {       // 수정하기 다이얼로그를 누르면 리뷰를 수정할 수 있도록
                                                ArrayList<String> PicN = new ArrayList<>();    //현재 리뷰 데이터를 수정하기 화면에 보내줍니다.
                                                for(int i=0; i<item.getReviewpic().size();i++) {
                                                    PicN.add(item.getReviewpic().get(i).getPath());
                                                }
                                                Intent intent= new Intent(getActivity(), ReviewActivity.class);
                                                intent.putExtra("음식 값",item.getFoodname());
                                                intent.putExtra("리뷰 값",item.getStatus());
                                                intent.putExtra("평점 값",item.getRating());
                                                intent.putExtra("시간 값",item.getTimestamp());
                                                intent.putStringArrayListExtra("사진 값",PicN);
                                                intent.putExtra("사진 갯수",item.getReviewpic().size());
                                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                startActivityForResult(intent,100);
                                            } else if (which == 1) {  // 삭제하기를 누르면 정말로 삭제할 거냐고 묻는 다이얼로그를 띄웁니다.
                                               // Toast.makeText(getActivity(), "삭제하기", Toast.LENGTH_LONG).show();
                                                feed = item;
                                                dialog();
                                            } else {

                                            }
                                            dialog.dismiss();
                                        }
                                    }).show();

                }
                else {
                    new AlertDialog.Builder(getContext())                               //글쓴이가 현재사용자가 아닐 경우에 신고하기와 취소 다이얼로그를 띄운다
                            // .setTitle("골라")
                            .setSingleChoiceItems(R.array.reviewcatagory2, -1,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                Toast.makeText(getActivity(), "신고하기", Toast.LENGTH_LONG).show();
                                            } else {

                                            }
                                            dialog.dismiss();
                                        }
                                    }).show();
                }
            }
        });
        return view;
            };

    @Override
    public void onResume() {              //멈췄다가 다시 돌아올 경우에는 저장된 리싸이클뷰의 상태를 갖고옵니다.
        super.onResume();
        if(recyclerViewState != null)
            getfeedList();                // 리뷰의 데이터를 다시 가져옵니다. 새로고침한 것처럼
        Log.e("멈췄나요","?");
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    public void getfeedList() {                     //서버에서 리뷰를 가져오는 메소드입니다.
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        Call<List<FeedItem>> call = apiInterface.getFeed(userId);                               //사용자의 아이디를 보낸다
        call.enqueue(new Callback<List<FeedItem>>() {
            @Override
            public void onResponse(Call<List<FeedItem>> call, Response<List<FeedItem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getActivity(), "오류", Toast.LENGTH_SHORT).show();
                } else {                                                                      //리뷰의 데이터가 중복되지 않도록 리스트를 비워줍니다.
                    feedList.clear();
                    feedadapter.clear();                                                      //어댑터에 추가된 데이터를 비워줍니다.
                    for (FeedItem feed : response.body()) {
                        feedList.add(feed);                                                    //비워진 리스트에 다시 데이터를 넣습니다.
                    }
                }
                feedadapter.notifyDataSetChanged();                                            //새로운 값이 들어간 어댑터를 새로고침해서 유저에게 보여줍니다.
            }

            @Override
            public void onFailure(Call<List<FeedItem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getActivity(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

    @Override
    public void onStop() {                      //멈췄을 때 리싸이클뷰의 상태를 저장합니다.
        super.onStop();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

    }

    @Override
    public void onPause() {                     //멈췄을 때 리싸이클뷰의 상태를 저장합니다.
        super.onPause();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    // 다이얼로그에서 삭제하기를 눌렀을 때 다시 띄우는 메쏘드입니다.
    public void dialog(){

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder .setMessage("삭제하시겠습까?")
                    .setCancelable(false)
                    .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            feed.getTimestamp();        //해당되는 값을 서버에 보내서 일치하는 리뷰를 지워줍니다.
                            reviewremove task = new reviewremove();     //삭제 클래스를 불러옵니다.
                            task.execute(feed.getFoodname(),userId,feed.getTimestamp());
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //  Action for 'NO' Button
                            dialog.cancel();
                        }
                    });

            //Creating dialog box
            AlertDialog alert = builder.create();
            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.show();
        }

    //지우려는 리뷰의 정보를 서버에 보내고 리뷰값을 디비에서 지우는 메소드입니다.
    @SuppressLint("StaticFieldLeak")
    private class reviewremove extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();   //서버에서 콜백이 올 때까지 다이얼로그를 띄웁니다.
            progressDialog = ProgressDialog.show(getActivity(),
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss(); //답이 올 경우 다이얼로그를 지워줍니다.
            if (result != null) {     // 돌아온 답이 공백이 아닐 경우,
                getfeedList();
                Toast.makeText(getActivity(),"삭제되었습니다",Toast.LENGTH_SHORT).show();
            } else {                  //공백인 경우에는 실패했다고 알려줍니다.
                Toast.makeText(getActivity(),"실패"+result,Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {
            String foodname = (String) params[0];       //적었던 리뷰의 맛집이름과
            String username = (String) params[1];       //작성자의 이름,
            String timestamp = (String) params[2];      //시간을 서버에 보냅니다
            String serverURL = "http://findtaste.vps.phps.kr/user_signup/get_reviewremove.php"; //서버의 주소값입니다.
            String postParameters = "food_name=" + foodname + "&user_name=" + username + "&timestamp=" + timestamp;
            Log.e("삭제값",postParameters);
            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);         //서버에 보냈을 때 5초 안에 답이 안오면 연결을 끊습니다.
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();        //연결된 서버에
                outputStream.write(postParameters.getBytes("UTF-8"));       //인코딩을 해서 한글이 깨지지않도록
                outputStream.flush();                                                   // 데이터를 보냅니다.
                outputStream.close();                                                   //그리고 보내주는 스트림을 꼭 닫아줍니다.

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);
                InputStream inputStream;

                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");  //한글일 경우 파라미터값을 한글로 인식할 수 있게
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);                          //만들어준 뒤에 받아옵니다.
                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();                                                                          //받아오는 리더를 닫아줍니다.
                return sb.toString().trim();                                                                     //받아온 값의 앞뒤 공백이 없게 한 뒤에
            } catch (Exception e) {                                                                              //리턴해줍니다.
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();
                return null;
            }
        }
    }
}

