package com.example.seyoung.findtaste.views.WriteReview;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.Adapter.FeedCommentAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.Constant;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.FeedComment;

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

/**
 * Created by seyoung on 2017-12-01.
 * 유저가 남긴 리뷰에 댓글을 달 수 있습니다.
 * 여러가지 표현을 댓글로 달 수 있도록
 * 만든 댓글 읽고, 쓰는 화면입니다
 * 또한 자신이 남긴 댓글만 수정, 삭제할 수 있도록 했습니다.
 */

    //FourFragment에서 리뷰를 클릭하거나 맛집의 상세페이지를 누를 경우, 현재 화면으로 넘어옵니다.
public class ReviewComment extends AppCompatActivity {
    TextView tastename;               // 식당이름
    TextView tv;                     //별점의 표시 숫자
    TextView editText;              //리뷰 작성자의 리뷰 내용
    TextView name;                  //작성자의 이름
    ImageView profilePic;           //작성자의 사진
    ImageView heart;                //좋아요이미지
    ImageView junm;                 // 리뷰를 수정,삭제,신고 할 수있는 이미지버튼

    TextView likenum;               //좋아요
    TextView comment;               //댓글
    TextView timestamp;             //작성한 시간

    EditText 코멘트;                 //댓글을 남길 수 있는 에딧
    Button send;                     //작성한 댓글을 서버로 보내는 버튼

    RatingBar rb;                    //별점
    int like,com;                    //좋아요와 댓글 갯수

    String user;    //유저가 쓴 리뷰내용
    String time;   //현재 시간
    String likey;   //사용자가 하트를 눌렀는 지 아닌 지 서버에 보내는 값
    RecyclerView recyclerView;      //댓글의 리스트를 나타낼 리싸이클뷰
    FeedCommentAdapter feedcommentadapter; //댓글의 어댑터.
    ArrayList<FeedComment> feedList = new ArrayList<FeedComment>(); //서버에서 받아온 댓글의데이터를 담을 리스트.

    private final String TAG = this.getClass().getSimpleName();
    ScrollView down;                //댓글을 작성할 경우 스크롤을 제일 아래롤 내릴 스크롤뷰
    HorizontalScrollView scrollView;    // 리뷰의 사진을 보여주는 가로 스크롤뷰
    LinearLayout topLinearLayout;    //작성자의 리뷰 이미지를 가로 스크롤뷰에 추가할 리니어 레이아웃

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_comment);      //리뷰에 필요한 값들을 현재 화면에 지정한다.
        tv = findViewById(R.id.text);
        tastename = findViewById(R.id.foodName);
        likenum =findViewById(R.id.likenum);
        comment=findViewById(R.id.cmnum);
        name = findViewById(R.id.name);
        editText=findViewById(R.id.txtStatusMsg);
        timestamp=findViewById(R.id.timestamp);
        rb =findViewById(R.id.ratingBar);
        profilePic = findViewById(R.id.profilePic);
        heart = findViewById(R.id.heart);
        scrollView = findViewById(R.id.horizon);
        topLinearLayout = new LinearLayout(ReviewComment.this);          //사용자가 남긴 리뷰에서 사진이 있을 경우, 사진을 넣을 레이아웃입니다.
        topLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        recyclerView = findViewById(R.id.recycler_view);                        //댓글의 데이터를 볼 수 있게 해주는 재활용뷰입니다.
        recyclerView.setNestedScrollingEnabled(false);

        send = findViewById(R.id.send);                                         //댓글을 적고, 등록하기 버튼입니다.
        코멘트= findViewById(R.id.코멘트);                                       //사용자가 댓글을 적을 수 있는 에딧창입니다.
        junm=findViewById(R.id.junm);                                           // 리뷰를 수정하거나 삭제하고 싶을 때 누르는 이미지입니다.

        down = findViewById(R.id.scroll);

        //키보드가 올라올 경우 올라온 만큼의 레이아웃을 위로 밀어버린다. (키보드 위에 댓글들이 보여야 하기에 아예 화면을 위로 넘겨버립니다.)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //클릭한 리뷰의 데이터를 가져와서 현재 화면의 리뷰에 넣어준다.
        final Intent intent = getIntent();
        tastename.setText(intent.getStringExtra("음식 값"));
        editText.setText(intent.getStringExtra("리뷰 값"));
        rb.setRating(intent.getIntExtra("평점 값",0));
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(intent.getStringExtra("시간 값")),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        time=intent.getStringExtra("시간 값");
        timestamp.setText(timeAgo);
        name.setText(intent.getStringExtra("닉네임 값"));    //사용자의 닉네임과
        user=intent.getStringExtra("아이디 값");            //사용자의 아이디이다.
        likenum.setText(intent.getStringExtra("좋아요 값")+"개");
        like= Integer.parseInt(intent.getStringExtra("좋아요 값"));
        comment.setText(intent.getStringExtra("댓글 값")+"개");
        com= Integer.parseInt(intent.getStringExtra("댓글 값"));
        likey=intent.getStringExtra("하트 값");

        //만약 서버에서 받아 온 하트값이 0일경우 빈 하트를 보여준다.
        if(intent.getStringExtra("하트 값").equals("0"))
        heart.setImageResource(R.drawable.nullheart);
        //1일 때 빨간하트 표시 (현재 사용자가 해당 리뷰에 좋아요를 눌른 것을 보여준다)
        else
        heart.setImageResource(R.drawable.fullheart);

        //좋아요 하트를 누를 경우,
        heart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                String user_id= pref.getString("ing", "");  //현재 좋아요를 누른 사용자의 아이디를 서버에 보내준다. (디비에 누가 누른것인지를알려줌)

                //만약 좋아요가 0이었을 경우, 빈 하트에서 좋아요를 눌러준 것으로 설정해준다
                if(likey.equals("0")){
                    likey ="1";
                    LikeData FD = new LikeData();       //좋아요한 맛집의 이름과 아이디와 눌렀던 시간을 서버에 보낸다.
                    FD.execute(user_id,intent.getStringExtra("음식 값"),intent.getStringExtra("아이디 값"),intent.getStringExtra("시간 값"));
                    like++;
                    likenum.setText(like+"개");          //현재 보이는 좋아요의 갯수를 한 개 추가
                    heart.setImageResource(R.drawable.fullheart);   //하트의 이미지도 바꾼다 (빈 하트)
                }
                //만약 좋아요가 1이었을 경우, 빨간 하트에서 좋아요를 해제 한 것으로 설정해준다
                else {
                    likey="0";
                    UnLikeData FD = new UnLikeData();   //좋아요한 맛집의 이름과 아이디와 눌렀던 시간을 서버에 보내서 좋아요한 데이터를 지운다.
                    FD.execute(user_id,intent.getStringExtra("음식 값"),intent.getStringExtra("아이디 값"),intent.getStringExtra("시간 값"));
                    like--;
                    likenum.setText(like+"개");          //현재 보이는 좋아요의 갯수를 한 개 제거
                    heart.setImageResource(R.drawable.nullheart);    //하트의 이미지도 바꾼다 (빨간 하트)
                }
            }

        });

        //리뷰 화면에 리뷰이미지를 추가해준다.
        final ArrayList<String> pic2;
        //pic2 = intent.getStringArrayExtra()
        pic2=intent.getStringArrayListExtra("사진 값");    //사진 경로가 담긴 리스트
        rb.setRating(intent.getIntExtra("평점 값",0)); //리뷰의 별점도 설정해주고 현재 사용자가 별점을 변경을 못하도록 해논다.

        Glide.with(this)                         //리뷰를 적은 유저의 프로필사진을 넣는다.
                .load(intent.getStringExtra("프로필 값"))
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .override(100, 100) //작게 크기를 지정해서 빠르게 이미지를 보이도록 한다
                        .centerCrop()
                        .circleCrop())
                .into(profilePic);

        if(pic2.size()>=1) {
            scrollView.setVisibility(View.VISIBLE); //만약 리뷰의 사진이 있을 경우,

            for (int i = 0; i < pic2.size(); i++){      //사진의 갯수만큼 리뷰안에 추가해준다
                Log.e("사진의 경로"+(i+1),pic2.get(i)); // 로그로 사진 경로 확인하기

                final ImageView imageView = new ImageView (ReviewComment.this); //이미지를 동적으로 추가하기 위해 새로 만듦

                Glide.with(ReviewComment.this)                         //글라이드로 빠르게 리뷰사진을 넣는다.
                        .load(pic2.get(i))
                        .apply(new RequestOptions()
                                .override(400,400)
                                .centerCrop())
                        .into(imageView);

                imageView.setAdjustViewBounds(true);       //사진이 마음대로 크기가 커지지 않도록 부모의 레이아웃에 크기를 맞춘다.
                topLinearLayout.addView(imageView);         // 리니어 레이아웃에 사진 갯수만큼 이미지뷰 추가
                final int finalI = i;
                imageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {       //리뷰사진을 눌렀을 때
                        // TODO Auto-generated method stub
                        Log.e("Tag",""+imageView.getTag());
                        Toast.makeText(getApplicationContext(),""+pic2.get(finalI),Toast.LENGTH_SHORT).show();
                    }
                });
            }
            scrollView.removeAllViews();                //스크롤뷰는 한개의 레이아웃만 가질 수 있기에 만약을 위해 다 제거해준다
            scrollView.addView(topLinearLayout);        // 그리고 사진들을 추가한 레이아웃을 스크롤 뷰 안에 넣는다
        }

        //만약 현재 화면에서 유저가 리뷰 수정하기를 누르면 리뷰의 저장된 값을 리뷰수정하기 화면에 보내준다.
        final ArrayList<String> finalPic = pic2;
        junm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                String userId= pref.getString("ing", "");
                // 리뷰 작성자와 현재 사용자의 아이디가 맞을 때
                if(userId.equals(user)) {
                    //수정하기와 삭제, 취소 증 1가지 선택형 다이얼로그를 띄운다.
                    new AlertDialog.Builder(ReviewComment.this)
                            .setSingleChoiceItems(R.array.reviewcatagory, -1,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //수정하기
                                            if (which == 0) {
                                                ArrayList<String> s = new ArrayList<String>();
                                                //리뷰 갯수만큼 사진 경로를 리스트에 넣은 뒤
                                                for(int i = 0; i< finalPic.size(); i++) {
                                                    s.add(finalPic.get(i));
                                                }
                                                // Toast.makeText(getActivity(), "수정하기", Toast.LENGTH_LONG).show();
                                                //모든 리뷰값을 보내주고 리뷰 수정 화면 실행.
                                                Intent intent= new Intent(ReviewComment.this, ReviewActivity.class);
                                                intent.putExtra("음식 값",tastename.getText().toString());
                                                intent.putExtra("리뷰 값",editText.getText().toString());
                                                intent.putExtra("평점 값",(int) rb.getRating());
                                                intent.putExtra("시간 값",time);
                                                intent.putStringArrayListExtra("사진 값",s);
                                                intent.putExtra("사진 갯수",s.size());
                                                startActivityForResult(intent,100);
                                            } else if (which == 1) {
                                            //삭제하기를 누를 때 정말 삭제하냐는 다이얼로그를 띄운다.
                                                dialog();
                                            } else {
                                            //취소 누를 시
                                            }
                                            dialog.dismiss();
                                        }
                                    }).show();
                }

                //리뷰작성자와 현재 사용자가 다를 때, 신고와 취소만 선택하게 만든다.
                else {
                    new AlertDialog.Builder(ReviewComment.this)
                            // .setTitle("골라")
                            .setSingleChoiceItems(R.array.reviewcatagory2, -1,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {       //취소하기 창만 띄운다
                                                Toast.makeText(ReviewComment.this, "신고하기", Toast.LENGTH_LONG).show();
                                            } else {

                                            }
                                            dialog.dismiss();
                                        }
                                    }).show();
                }
            }

    });

        //해당 리뷰에 달린 댓글 데이터를 가져온다.
        getfeedcommmentList();

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        final String userId= pref.getString("ing", "");

        feedcommentadapter = new FeedCommentAdapter(feedList, this);

        //내가 남긴 댓글을 길게 누를 경우, 삭제하겠냐는 다이얼로그가 뜬다
        feedcommentadapter.setOnLongClickListener(new FeedCommentAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnItemLongClick(final FeedComment feed, final int position) {
                //댓글 작성자와 현재 사용자가 동일할 때
                if(userId.equals(feed.getName())) {
                    new AlertDialog.Builder(ReviewComment.this)
                            .setSingleChoiceItems(R.array.removecomment, -1,
                                    new DialogInterface.OnClickListener() {
                                        //삭제하기 다이얼로그를 누르면 리뷰와 일치하는 값을 지운다.
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //삭제하기를 선택할 시,
                                            if (which == 0) {
                                                //댓글데이터를 삭제하는 서버와 연결시킨다
                                                getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);
                                                //정확한 댓글을 지우기 위해 댓글 사용자, 맛집이름, 시간, 댓글 내용 등...을 서버에 보내준다
                                                Call<List<FeedComment>> call = apiInterface.removecomment(feed.getWritename(),feed.getFoodname(),feed.getTimestamp(),userId,feed.getComment(),feed.getTimestamp_user());
                                                call.enqueue(new Callback<List<FeedComment>>() {
                                                    @Override
                                                    public void onResponse(Call<List<FeedComment>> call, Response<List<FeedComment>> response) {
                                                        //서버에서 받지 못했을 경우.
                                                        if (response == null) {
                                                            Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                                                        }
                                                        //서버에서 받았을 경우
                                                        else {
                                                            com--;                      // 현재 리뷰에 남겨진 댓글의 숫자를 하나 줄이고
                                                            comment.setText(com+"개");  // 댓글 갯수의 텍스트를 변경시킨다.
                                                        }
                                                        feedcommentadapter.removeItem(position);    // 어댑터에서 해당 데이터를 지운 뒤에
                                                        feedcommentadapter.notifyDataSetChanged();  //새로고침해서 유저에게 삭제된걸  보여준다.
                                                    }

                                                    @Override
                                                    public void onFailure(Call<List<FeedComment>> call, Throwable t) {        //서버와 연결 실패 할 경우
                                                        Toast.makeText(getApplicationContext(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                                        Log.e("ERROR: ", t.getMessage());
                                                    }
                                                });

                                            }
                                            //취소를 눌렀을 때
                                            else {

                                            }
                                            dialog.dismiss();
                                        }
                                    }).show();
                    }
                return true;
            }
        });

        //어댑터에 담아놓은 댓글 데이터를 리스트로 보여주기 위해 라싸이클뷰와 데이터를 어댑터로 연결시켜줍니다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //뷰를 세로로 나열시키게 만듭니다.
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(feedcommentadapter);

        //댓글 쓰기 버튼을 누를 때 리스너
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //코멘트값이 null일 경우
                if(코멘트.getText().length()==0){
                    Toast.makeText(ReviewComment.this,"댓글을 적어주세요",Toast.LENGTH_SHORT).show();
                }

                //코멘트값이 있을 경우
                //서버에 데이터를 보내줍니다.
                else {
                    String 시간 = String.valueOf(System.currentTimeMillis());
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    String userId = pref.getString("ing", "");

                    getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
                    // 서버에 리뷰 작성자 아이디와 맛집이름, 댓글 쓴 시간, 유저아이디, 댓글값, 리뷰의 시간을 서버에 보낸다.
                    Call<List<FeedComment>> call = apiInterface.putcomment(user, tastename.getText().toString(), time, userId, 코멘트.getText().toString(), 시간);
                    call.enqueue(new Callback<List<FeedComment>>() {
                        @Override
                        public void onResponse(Call<List<FeedComment>> call, Response<List<FeedComment>> response) {
                            if (response == null) {                                                       //서버에서 받지 못했을 경우.
                                Toast.makeText(ReviewComment.this, "오류", Toast.LENGTH_SHORT).show();
                            } else {
                                //작성한 내용을 서버에 보낸 뒤 다시 서버에서 돌려받아 댓글리스트에 추가를 해준다.
                                for (FeedComment feed : response.body()) {
                                    feedList.add(feed);
                                }
                                //댓글에딧에 널 값을 넣고
                                코멘트.setText("");
                                //키보드를 내려준다
                                InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(코멘트.getWindowToken(), 0);
                                //댓글이 추가가 되었으니 댓글 갯수를 올려준다
                                com++;
                                comment.setText(com+"개");
                            }
                            // 화면 새로고침
                            feedcommentadapter.notifyDataSetChanged();
                            //사용자가 적은 댓글을 바로 볼 수 있도록
                            //화면을 제일 아래로 내린다
                            recyclerView.scrollToPosition(feedcommentadapter.getItemCount() - 1);
                            down.scrollTo(down.getLeft(),down.getBottom());
                        }

                        //서버와 연결 실패 할 경우
                        @Override
                        public void onFailure(Call<List<FeedComment>> call, Throwable t) {
                            Toast.makeText(ReviewComment.this, "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("ERROR: ", t.getMessage());
                        }
                    });
                }
            }
        });
    }

    //다시 현재 화면으로 돌아올 때 댓글 데이터를 새로고침하기
    @Override
    protected void onResume() {
        super.onResume();
        getfeedcommmentList();
    }

    //삭제하기를 눌렀을 때 띄워주는 다이얼로그
    public void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ReviewComment.this);
        builder .setMessage("삭제하시겠습까?")
                .setCancelable(false)
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                        String g= pref.getString("ing", "");
                        reviewremove task = new reviewremove();
                        task.execute(tastename.getText().toString(),g,time);
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

    //댓글데이터를 서버에서 가져오는 메소드
    public void getfeedcommmentList() {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        String userId= pref.getString("ing", "");
        Log.e("맛집이름",tastename.getText().toString());
        Log.e("작성자아이디",user);
        Log.e("시간이름",time);
        Log.e("현재유저아이디",userId);
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        // 맛집이름과 리뷰 작성자, 리뷰 시간과, 현재 사용자의 아이디를 서버에 보낸다
        Call<List<FeedComment>> call = apiInterface.getcomment(tastename.getText().toString(),user,time,userId);
        call.enqueue(new Callback<List<FeedComment>>() {
            @Override
            public void onResponse(Call<List<FeedComment>> call, Response<List<FeedComment>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(ReviewComment.this, "오류", Toast.LENGTH_SHORT).show();
                }
                //리싸이클 뷰에 추가해준다.
                else {
                    feedList.clear();
                    feedcommentadapter.clear();
                    for (FeedComment feed : response.body()) {
                        feedList.add(feed);
                    }
                }
                // 화면 새로고침
                feedcommentadapter.notifyDataSetChanged();
            }

            //서버와 연결 실패 할 경우
            @Override
            public void onFailure(Call<List<FeedComment>> call, Throwable t) {
                Toast.makeText(ReviewComment.this, "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

    //좋아요를 눌렀을 때 서버에 좋아요한 데이터를 추가하는 클래스다.
    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    class LikeData extends AsyncTask<String, Void, String> {

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
            //
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

    //안좋아요를 눌렀을 때 서버에 좋아요한 데이터를 제거하는 클래스다.
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

    //리뷰를 지웠을 때 서버에 리뷰데이터를 제거하는 클래스다.
    @SuppressLint("StaticFieldLeak")
    private class reviewremove extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ReviewComment.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                finish();
                Toast.makeText(ReviewComment.this,"삭제되었습니다",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ReviewComment.this,"실패"+result,Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "POST response  - " + result);
        }


        @Override

        protected String doInBackground(String... params) {
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

