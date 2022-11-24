package com.example.seyoung.findtaste.views.FiveFgChilds;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
 * Created by seyoung on 2017-11-04.
 * 마이페이지에서 사용자가 적은 리뷰를 모아 볼 수 있는 화면입니다.
 * 현재 화면에서 좋아요를 하거나 리뷰 수정, 삭제가 가능합니다.
 * 또한 리뷰의 상세페이지로 넘어갈 수 있게 해줍니다.
 *
 */

public class Child2Fragment extends Fragment {
    //public static ChildFragment newInstance() {
    //    return new ChildFragment();
   // }

    FeedAdapter feedadapter;            // 리뷰 데이터를 리싸이클뷰와 연결 시키는 어댑터입니다. 사용자가 리뷰를 볼 수 있게 만들어주는 어댑터입니다.
    RecyclerView recyclerView;          // 리뷰를 리스트로 보여주기 위해 필요한 리싸이클뷰입니다. 맛집리스트를 어떤 형태로 만들 지 처리해줍니다.
    ArrayList<FeedItem> feedList = new ArrayList<FeedItem>();      //서버에서 받아온 리뷰의 데이터를 담을 리스트입니다. 여러개의 리뷰들을 볼 수 있도록 해줍니다.
    FeedItem feed;                      //서버에서 받아온 리뷰 데이터를 그릇으로 담아 사용할 수 있는 리뷰클래스입니다.
    String userId;                      //서버에 보내 줄 현재 사용자의 아이디입니다. 어떤 사용자의 리뷰를 불러올지 아이디값으로 판별합니다.
    Parcelable recyclerViewState;

    public static Child2Fragment newInstance() {
        return new Child2Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child2, container, false);
        recyclerView = view.findViewById(R.id.recyclerview2);
        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        userId= pref.getString("ing", "");

        getfeedList();          //현재 사용자의 리뷰들을 불러옵니다.

        feedadapter = new FeedAdapter(feedList, getActivity());                         //리뷰의 데이터를 어댑터에 넣어줍니다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());      //뷰를 세로로 리스트화 시켜줍니다.
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //layoutManager.scrollToPosition(currPos);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);                                  //스크롤을 하지 않도록만들어서, 해당 데이터만큼 화면에 길게 뿌려줍니다.

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(feedadapter);       //리뷰데이터와 설정해준 뷰를 어댑터로 연결시켜줍니다.
        //사용자가 해당 리뷰를 눌렀을 경우
        feedadapter.setOnItemClickListener(new FeedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FeedItem item) {
                //해당 리뷰의 사진갯수만큼 사진경로를 경로리스트에 담아줍니다.
                ArrayList<String> picN = new ArrayList<String>();
                for(int i=0; i<item.getReviewpic().size();i++) {
                    picN.add(item.getReviewpic().get(i).getPath());
                }
                //그리고 리뷰상세페이지에 현재 리뷰 데이터들을 넘겨줍니다.
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
                intent.putExtra("사진 값",picN);
                intent.putExtra("사진 갯수",item.getReviewpic().size());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        //사용자가 자신이 적은 리뷰 중 삭제하거나 수정하고 싶은 리뷰의, 점 3개 이미지를 눌렀을 때의 이벤트입니다.
        //수정삭제를 선택하는 다이얼로그를 띄웁니다.
        feedadapter.setOnItemClickListener(new FeedAdapter.OnreviewItemClickListener(){

            @Override
            public void onreviewItemClick(final FeedItem item) {
                //현재
                    new AlertDialog.Builder(getContext())
                            .setSingleChoiceItems(R.array.reviewcatagory, -1,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //수정하기를 선택했을 때
                                            if (which == 0) {
                                                //해당 리뷰의 사진갯수만큼 사진경로를 경로리스트에 담아줍니다.
                                                ArrayList<String> picN = new ArrayList<String>();
                                                for(int i=0; i<item.getReviewpic().size();i++) {
                                                    picN.add(item.getReviewpic().get(i).getPath());
                                                }
                                                //그리고 리뷰 수정화면으로 현재 리뷰의 데이터들을 넘겨줍니다.
                                                // Toast.makeText(getActivity(), "수정하기", Toast.LENGTH_LONG).show();
                                                Intent intent= new Intent(getActivity(), ReviewActivity.class);
                                                intent.putExtra("음식 값",item.getFoodname());
                                                intent.putExtra("리뷰 값",item.getStatus());
                                                intent.putExtra("평점 값",item.getRating());
                                                intent.putExtra("시간 값",item.getTimestamp());
                                                intent.putStringArrayListExtra("사진 값",picN);
                                                intent.putExtra("사진 갯수",item.getReviewpic().size());
                                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                startActivityForResult(intent,100); //리뷰수정 화면을 불러옵니다.
                                            } else if (which == 1) {
                                                //삭제하기를 선택했을 때 정말 삭제하겠냐는 다이얼로그를 띄웁니다.
                                                // Toast.makeText(getActivity(), "삭제하기", Toast.LENGTH_LONG).show();
                                                feed = item;
                                                dialog();
                                            } else {

                                            }
                                            dialog.dismiss();
                                        }
                                    }).show();
            }
        });


        return view;
    }

    //다시 화면으로 돌아왔을 때 저장된 리싸이클뷰의 스크롤 위치로 화면을 이동시켜줍니다.
    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewState != null)
            getfeedList();
        Log.e("멈췄나요","?");
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        // feedadapter.notifyDataSetChanged();
    }

    //현재 사용자의 리뷰 데이터를 서버에 불러오게 요청합니다
    public void getfeedList() {
        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        userId= pref.getString("ing", "");
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        //현재 사용자의 아이디를 보내줍니다.
        Call<List<FeedItem>> call = apiInterface.getFeedmy(userId);
        call.enqueue(new Callback<List<FeedItem>>() {
            @Override
            public void onResponse(Call<List<FeedItem>> call, Response<List<FeedItem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getActivity(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    feedList.clear();           //현재 저장된 데이터를 지워주고
                    feedadapter.clear();
                    for (FeedItem feed : response.body()) {
                        feedList.add(feed);     //새로 받아온 데이터 갯수만큼 저장 해줍니다
                        for(int i=0;i<feed.getReviewpic().size();i++)
                            Log.i("RESPONSE: ", "" + feed.toString());  //사진경로들이 잘 받아와졌는 지 로그로 확인
                    }
                }
                feedadapter.notifyDataSetChanged();                     // 화면 새로고침
            }

            @Override
            public void onFailure(Call<List<FeedItem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getActivity(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

    //화면이 멈추거나 중지될 때 현재 리싸이클뷰의 스크롤위치를 저장해줍니다.
    @Override
    public void onStop() {
        super.onStop();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

    }

    @Override
    public void onPause() {
        super.onPause();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }

    //사용자가 선택한 리뷰를 삭제할 경우, 정말 삭제할거냐고 묻는 다이얼로그입니다.
    public void dialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        //Uncomment the below code to Set the message and title from the strings.xml file
        //builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

        //Setting message manually and performing action on button click
        builder .setMessage("삭제하시겠습까?")
                .setCancelable(false)
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        feed.getTimestamp();
                        reviewremove task = new reviewremove();
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

        //Setting the title manually
        alert.show();

    }

    //리뷰를 삭제할 때 서버에 요청하는 이벤트입니다.
    @SuppressLint("StaticFieldLeak")
    private class reviewremove extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(),
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                //리뷰가 삭제될 경우 삭제된 리뷰를 없애기 위해 다시 리뷰 데이터들을 서버에서 불러옵니다.
                getfeedList();
                Toast.makeText(getActivity(),"삭제되었습니다",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(),"실패"+result,Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "POST response  - " + result);
        }


        @Override

        protected String doInBackground(String... params) {
            //삭제하는 리뷰의 맛집이름, 작성자 이름, 시간을 보내줍니다.
            String foodname = (String) params[0];
            String username = (String) params[1];
            String timestamp = (String) params[2];
            String serverURL = "http://findtaste.vps.phps.kr/user_signup/get_reviewremove.php";
            String postParameters = "food_name=" + foodname + "&user_name=" + username + "&timestamp=" + timestamp;
            Log.e("삭제값",postParameters);
            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);
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
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString().trim();
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();
                return null;
            }
        }
    }


}