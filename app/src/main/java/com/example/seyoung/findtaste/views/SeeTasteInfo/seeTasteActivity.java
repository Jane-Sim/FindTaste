package com.example.seyoung.findtaste.views.SeeTasteInfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.seyoung.findtaste.Adapter.FeedAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.Constant;
import com.example.seyoung.findtaste.config.GeoItem;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.FeedItem;
import com.example.seyoung.findtaste.model.Tasteitem;
import com.example.seyoung.findtaste.views.WriteReview.ReviewActivity;
import com.example.seyoung.findtaste.views.WriteReview.ReviewComment;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.kakaonavi.KakaoNaviParams;
import com.kakao.kakaonavi.KakaoNaviService;
import com.kakao.kakaonavi.Location;
import com.kakao.kakaonavi.NaviOptions;
import com.kakao.kakaonavi.options.CoordType;
import com.skt.Tmap.TMapTapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.login.widget.ProfilePictureView.TAG;

/**
 * Created by seyoung on 2017-11-03.
 * 선택한 맛집정보를 볼 수 있는 화면입니다.
 * 맛집을 등록할 때 넣은 사진과, 해당 맛집리뷰에 사진이 있을 경우
 * 상세화면 제일 위에 사진들을 넣어놔서 사용자가 사진들을 모아 볼 수 있습니다.
 * 맛집상세페이지에서 사용자가 적은 리뷰들도 볼 수 있으며,
 * 상세페이지 안에 있는 구글 맵으로도 맛집의 위치를 마커로 볼 수 있습니다.
 */

public class seeTasteActivity extends AppCompatActivity implements View.OnClickListener ,OnMapReadyCallback{
    private String tastename, tel;                      // 맛집의 이름과 전화번호
    int fullsize=0;                                     //해당 맛집의 사진 갯수
    ArrayList<String> patharray;                        //해당 맛집의 사진경로를 담을 리스트입니다.
    private static final String TAG_JSON="imagepath";   // 사진들을 imagepath 라는 이름의 json으로 받아옵니다.

    //해당 맛집의 id와 사진 이름, 사진 경로를 받는 변수다
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_PATH ="path";

    //유저가 즐겨찾기한 값과 서버에서 받아온 맛집 이름이다.
    String favorst="0",food;

    //서버에서 받아올 해당 맛집의 정보.
    Tasteitem tasteitem;
    //해당 맛집에서 띄울 구글맵
    GoogleMap map;
    //서버에서 받아올 해당 맛집의 댓글들
    FeedItem feed;
    //불러온 댓글들을 리스트로 보여주기 위해 필요한 어댑터와 재활용 뷰
    FeedAdapter feedadapter;
    RecyclerView recyclerView;
    //댓글 데이터를 담을 리스트
    ArrayList<FeedItem> feedList = new ArrayList<FeedItem>();
    //서버에서 받아 올 댓글을 감싼 포장지 이름과, 현재 사용자의 이름
    String mJsonString,userId;
    //맛집의 이름과 주소, 상세정보, 가장 맨 위에 표시할 맛집이름,
    TextView foodname,address,description,toptext,review1,rating,findmap2,navi2,copy2;
    ImageView favorites,review,findmap,navi,copy;   //즐겨찾기, 리뷰, 지도 어플에 연동할 수 있는 이미지뷰
    ImageButton calling;        //맛집에 전화하기 버튼
    Button morereview;          //리뷰를 더 보기 버튼
    ImageButton kakaobutton,facebookbutton; //카카오톡과 페이스북에 공유할 수 있는 버튼
    HorizontalScrollView scrollView;    // 사진들을 가로로 나열시킬 수 있는 스크롤
    LinearLayout topLinearLayout;       // 사진들을 담을 레이아웃

    Bitmap[] bmp= new Bitmap[]{null};   //페이스북에 공유하기를 할 때 서버에서 받아온 사진 경로값을 비트맵으로 보내줄 때 사용.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taste);

        scrollView = findViewById(R.id.hori);           //지도 어플로 연동해주는 길경로 버튼입니다.
        findmap = findViewById(R.id.findmap);
        findmap2 = findViewById(R.id.findmap2);

        findmap.setOnClickListener(this);
        findmap2.setOnClickListener(this);

        navi = findViewById(R.id.navi);                 // 내비게이션 어플에서 길찾기 버튼을 가져온다
        navi2 = findViewById(R.id.navi2);

        navi.setOnClickListener(this);
        navi2.setOnClickListener(this);

        copy = findViewById(R.id.copy);                 // 맛집의 주소를 복사할 버튼을 가져온다
        copy2 = findViewById(R.id.copy2);

        copy.setOnClickListener(this);
        copy2.setOnClickListener(this);

        topLinearLayout = new LinearLayout(this);// 맛집사진들을 추가할 레이아웃을 불러온다
        topLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        //페이스북의 담벼락에 맛집사진을 올릴 수 있도록 설정한다.
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //리싸이클 뷰가 스크롤되지 않도록 만든다.
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        // 사진들의 경로를 담을 리스트
        patharray= new ArrayList<String>();

        // 맛집 리스트에서 보내 준 데이터를 인텐트로 받는다
        Intent intent = getIntent();

        // 만약에 카카오톡에서 공유를 한 메세지 링크를 눌렀을 때
        Uri uri	=	intent.getData();
        if(uri != null)
        {// 지정했던 맛집의 이름을 받아온다. (맛집 이름이 있어야 서버에서 상세정보들을 가져올 수 있다.)
            tastename =uri.getQueryParameter("tastename");

            if(tastename != null)
                Log.d("kakaoLink", tastename);
        }

        // 카카오톡이 아닌 맛집리스트에서 눌렀을 때 해당 맛집이름을 가져온다
        else
            tastename = intent.getStringExtra("맛집이름");

        morereview = findViewById(R.id.morereview);     //해당 맛집리뷰를 더 많이 볼 수 있게 리뷰화면으로 이동 시켜주는 버튼

        kakaobutton = findViewById(R.id.kakao);         //카카오톡으로 현재 맛집정보를 카카오톡메세지로 공유하게 해준다
        kakaobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharekakao(tastename,patharray.get(0)); //이미지의 첫번째를 카카오메세지에 설정한다.
            }
        });
        facebookbutton = findViewById(R.id.facebook);
        facebookbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {            //페이스북 공유 버튼을 누를경우
                shareFacebook();                                //페이스북 뉴스피드에 해당 맛집 사진을 올릴 수 있게 만든다.
            }
        });
//        Log.e("맛집 이름은",tastename);
        gettasteinfo();                                       //서버에 맛집 이름을 보내서 맛집 정보들을 불러온다.
        InsertData task = new InsertData();
        task.execute(tastename);

        //getimage(fullsize);
        foodname = (TextView)findViewById(R.id.foodname);       //맛집 이름을 넣을 텍스트뷰
        address = (TextView)findViewById(R.id.address);         // 맛집 주소를 넣을 텍스트뷰
        description = (TextView)findViewById(R.id.description); // 맛집 상세정보를 넣을 텍스트뷰
        toptext = (TextView)findViewById(R.id.textView);        // 맛집 이름을 넣을 오렌지색 텍스트뷰
        rating = findViewById(R.id.rating);                     // 현재 맛집의 평점을 넣을 텍스트뷰

        //리뷰 등록 화면으로 이동할 레이아웃들
        review = findViewById(R.id.review);
        review1 = findViewById(R.id.review1);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //해당 맛집 이름으로 리뷰 작성 가능
                Intent intent = new Intent(seeTasteActivity.this,ReviewActivity.class);
                intent.putExtra("음식 값",tastename);
                startActivity(intent);
            }
        });
        review1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(seeTasteActivity.this,ReviewActivity.class);
                intent.putExtra("음식 값",tastename);
                startActivity(intent);
            }
        });

        // 사용자가 해당 맛집의 즐겨찾기를 눌렀을 때
        favorites= findViewById(R.id.favorite);
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                userId= pref.getString("ing", "");
                //현재 맛집의 좋아요를 서버에 추가해달라고 한다
                if(favorst.equals("0")){
                    favorst="1";
                    FavorData FD = new FavorData();
                    FD.execute(userId,tastename);
                    favorites.setImageResource(android.R.drawable.btn_star_big_on);
                }
                //현재 맛집의 좋아요를 서버에 제거해달라고 한다
                else {
                    favorst="0";
                    FavorDataDel FD = new FavorDataDel();
                    FD.execute(userId,tastename);
                    favorites.setImageResource(android.R.drawable.btn_star_big_off);
                }
            }
        });

        //전화걸기 버튼을 누를 떼
        calling = findViewById(R.id.call);
        calling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //해당 맛집의 전화번호를 전화걸기액티비티에 넘기고 실행시킨다
                startActivity(new Intent("android.intent.action.DIAL", Uri.parse("tel:" +tel)));
            }
        });

        feedadapter = new FeedAdapter(feedList, this);                              //받아온 맛집의 리뷰데이터를 리싸이클뷰의 아이템과 연결시켜준다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);          //리싸이클뷰에 필요한 매니저도 찾아준다.
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);                         //리니어 매니저는 세로로 리싸이클뷰를 나열시킨다.

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(feedadapter);

        //맛집 리뷰 중 하나를 눌렀을 때 해당 리뷰 상세보기 화면으로 넘긴다.
            feedadapter.setOnItemClickListener(new FeedAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(FeedItem item) {
                    //해당 리뷰에 사진이 있을 경우, 사진의 경로를 리스트에 추가한다.
                    ArrayList<String> PicN = new ArrayList<String>();
                    for (int i = 0; i < item.getReviewpic().size(); i++) {
                        PicN.add(item.getReviewpic().get(i).getPath());
                    }   //선택한 리뷰의 상세값을 다음 화면에 보내준다.
                    Intent intent = new Intent(seeTasteActivity.this, ReviewComment.class);
                    intent.putExtra("음식 값", item.getFoodname());
                    intent.putExtra("리뷰 값", item.getStatus());
                    intent.putExtra("평점 값", item.getRating());
                    intent.putExtra("시간 값", item.getTimestamp());
                    intent.putExtra("프로필 값", item.getProfilepic());
                    intent.putExtra("닉네임 값", item.getUsername());
                    intent.putExtra("아이디 값", item.getUsername2());
                    intent.putExtra("좋아요 값", item.getGood());
                    intent.putExtra("댓글 값", item.getComment());
                    intent.putExtra("하트 값", item.getLikey());
                    intent.putExtra("사진 값", PicN);
                    intent.putExtra("사진 갯수", item.getReviewpic().size());
                    startActivity(intent);
                }
            });

            feedadapter.setOnItemClickListener(new FeedAdapter.OnreviewItemClickListener() {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                String userId = pref.getString("ing", "");

                //사용자가 적은 리뷰를 수정하거나 삭제할 수 있도록 했다.
                //사용자가 수정과 삭제를 하기 위한 다이얼로그를 선택했을 때,
                @Override
                public void onreviewItemClick(final FeedItem item) {
                    if (userId.equals(item.getUsername2())) {
                        new AlertDialog.Builder(seeTasteActivity.this)
                                .setSingleChoiceItems(R.array.reviewcatagory, -1,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //수정하기를 눌렀을 경우
                                                if (which == 0) {
                                                    ArrayList<String> PicN = new ArrayList<String>();
                                                    for (int i = 0; i < item.getReviewpic().size(); i++) {
                                                        PicN.add(item.getReviewpic().get(i).getPath());
                                                    }
                                                    //해당 리뷰의 데이터들을 인텐트에 추가한다
                                                    // Toast.makeText(getActivity(), "수정하기", Toast.LENGTH_LONG).show();
                                                    Intent intent = new Intent(seeTasteActivity.this, ReviewActivity.class);
                                                    intent.putExtra("음식 값", item.getFoodname());
                                                    intent.putExtra("리뷰 값", item.getStatus());
                                                    intent.putExtra("평점 값", item.getRating());
                                                    intent.putExtra("시간 값", item.getTimestamp());
                                                    intent.putStringArrayListExtra("사진 값", PicN);
                                                    //리뷰를 수정하는 화면으로 데이터 값을 넘긴 뒤 불러온다
                                                    intent.putExtra("사진 갯수", item.getReviewpic().size());
                                                    startActivityForResult(intent, 100);
                                                } else if (which == 1) {
                                                    //삭제하는 다이얼로그를 띄운다
                                                    // Toast.makeText(getActivity(), "삭제하기", Toast.LENGTH_LONG).show();
                                                    feed = item;
                                                    dialog();
                                                } else {

                                                }
                                                dialog.dismiss();
                                            }
                                        }).show();

                    } else {        //리뷰 작성자와 현재 사용자의 이름이 다를 경우
                        new AlertDialog.Builder(seeTasteActivity.this)
                                // .setTitle("골라")
                                .setSingleChoiceItems(R.array.reviewcatagory2, -1,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //신고하기 버튼을 클릭했 을 때
                                                if (which == 0) {
                                                    Toast.makeText(seeTasteActivity.this, "신고하기", Toast.LENGTH_LONG).show();
                                                } else {
                                                            // 취소버튼
                                                }
                                                dialog.dismiss();
                                            }
                                        }).show();
                    }
                }
            });

    }
    //다시 화면으로 돌아올 경우 데이터를 다시 불러옵니다
    @Override
    protected void onResume() {
        super.onResume();
     /*   patharray.clear();
        topLinearLayout.removeAllViews();
        gettasteinfo();*/
    }

    // 맛집의 사진들을 추가해줄 때 사용합니다.
    public void getimage(int size, ArrayList<String> array){
        //가로스크롤뷰에 리니어 레이아웃 추가
        //리니어 레이아웃에 받아온 이미지 갯수 만큼 이미지뷰를 동적으로 추가하려고 한다.
        for (int i = 0; i < size; i++){
            final ImageView imageView = new ImageView (seeTasteActivity.this);
            Glide.with(this)                         //글라이드로 빠르게 사진을 넣는다.
                    .load(array.get(i))
                    .apply(new RequestOptions()
                            .override(400, 400)
                            .centerCrop())
                    .into(imageView);
            imageView.setAdjustViewBounds(true);       //사진이 마음대로 크기가 커지지 않도록 부모의 레이아웃에 크기를 맞춘다.
            topLinearLayout.addView(imageView);         // 리니어 레이아웃에 사진 갯수만큼 이미지뷰 추가
            final int finalI = i;
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO Auto-generated method stub
                    Log.e("Tag",""+imageView.getTag());
                    //Toast.makeText(getApplicationContext(),""+array.get(finalI),Toast.LENGTH_SHORT).show();
                }
            });
        }
        scrollView.removeAllViews();
        scrollView.addView(topLinearLayout);            // 사진들을 가로로 볼 수 있도록 가로스크롤 뷰에 사진들을 추가한 리니어레이아웃을 넣어줍니다.
    }

    public void gettasteinfo() {                                                              //맛집 정보를 받아온다.
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결 시킨다.

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        userId= pref.getString("ing", "");
        //서버에 맛집 이름과 사용자의 이름을 보낸다. 맛집의 위치를 구글맵에서 보여줘야 하기에
        Call<List<Tasteitem>> call = apiInterface.gettastedetail(tastename,userId);
        call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과
            @Override
            public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                //서버에서 받지 못했을 경우.
                if (response == null) {
                    Toast.makeText(seeTasteActivity.this, "오류", Toast.LENGTH_SHORT).show();
                } //서버에서 받았을 경우.
                else {
                    for (Tasteitem taste : response.body()) {
                        //맛집의 이름과 주소, 상세정보를 서버에서 받아와서 상세정보에 추가한다.
                        foodname.setText(taste.getFood_name());
                        food=taste.getFood_name();
                        address.setText(taste.getFood_address());

                        //맛집의 상세정보를 받으면, 서버에서 해당 맛집의 리뷰들을 불러오게 한다.
                        getfeedList();

                        if(description.equals("")){             //맛집의 상세정보가 없을경우
                            description.setText("맛집의 상세 정보가 없습니다.");
                        }
                        else {
                            description.setText(taste.getFood_memo());
                        }

                        tel = taste.getFood_number();          //전화번호를 가져오고
                        toptext.setText(taste.getFood_name()); //맛집 이름을 가져오고
                        rating.setText(String.valueOf(taste.getRating()));  //별점도 가져온다.
                        if(rating.getText().toString()=="0.0"){     // 평점이 아직 없을경우
                            rating.setTextColor(Color.GRAY);        //별점의 글자색을 회색으로 만들어준다
                        }
                        tasteitem = taste;
                        getLocation();                              //가져온 맛집 위치로 구글맵에 표시하기 위해 구글맵을 불러옵니다
                        getfavorite();                              //현재 사용자가 해당 맛집을 즐겨찾기 했는 지 안했는 지 확인합니다.
                        Log.i("RESPONSE: ", "" + taste.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Tasteitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(seeTasteActivity.this, "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }
    public void getfavorite(){  //즐겨찾기의 상태를 확인할 수 있다.
        //0일 경우 즐겨찾기 버튼을 빈 별 이미지로 설정하고
        if(tasteitem.getFavorites().equals("0")){
            favorst=tasteitem.getFavorites();
            favorites.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_off));
        }else {
            //1일 경우 즐겨찾기 버튼을 노란 별 이미지로 설정한다
            favorst=tasteitem.getFavorites();
            favorites.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));
        }
    }

    //현재 맛집을 보는 사용자가 자신이 썼던 리뷰를 지우려고 할 때
    public void dialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(seeTasteActivity.this);
        builder .setMessage("삭제하시겠습까?")
                .setCancelable(false)
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {       //삭제하기를 선택했을 때 서버에 해당 리뷰를 지워달라고 요청한다
                        feed.getTimestamp();
                        reviewremove task = new reviewremove();
                        task.execute(feed.getFoodname(),userId,feed.getTimestamp());
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {       //다이얼로그를 닫는다
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(0x0000ff00));

        //Setting the title manually
        alert.show();

    }

    //지도 어플을 띄우는 다이얼로그입니다.
    //해당어플의 위도 경도를 어플에 보내줘서 찾을 수 있도록 만들어줍니다.
    public void fmdialog() {
        new AlertDialog.Builder(seeTasteActivity.this)
                .setTitle("길 찾기")
                .setSingleChoiceItems(R.array.길찾기, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {   //구글지도를 띄운다
                                    if(isExistApp("com.google.android.apps.maps")) {
                                        String uri = "http://maps.google.com/maps?saddr=" + GeoItem.getKnownLocation().latitude + "," + GeoItem.getKnownLocation().longitude + "&daddr=" + tasteitem.getLati() + "," + tasteitem.getLogi() + "&directionsmode=transit&zoom=15";
                                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                                Uri.parse(uri));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                        startActivity(intent);
                                    }else {
                                        Toast.makeText(seeTasteActivity.this,"앱 설치 후 다시 사용해주세요",Toast.LENGTH_SHORT).show();
                                    }
                                } else {            //다음지도를 띄우도록 한다
                                    Boolean map = isExistApp("net.daum.android.map");
                                    if(map) {
                                        String url = "daummaps://route?sp=" + GeoItem.getKnownLocation().latitude + "," + GeoItem.getKnownLocation().longitude + "&ep=" + tasteitem.getLati() + "," + tasteitem.getLogi()+"&by=PUBLICTRANSIT";
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        startActivity(intent);
                                    }else {
                                        Toast.makeText(seeTasteActivity.this,"앱 설치 후 다시 사용해주세요",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                dialog.dismiss();
                            }
                        }).show();
    }

    //내비게이션이 되는 어플을 띄워줍니다.
    public void fNdialog() {
        new AlertDialog.Builder(seeTasteActivity.this)
                .setTitle("내비게이션")
                .setSingleChoiceItems(R.array.네비게이션, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {   //카카오길안내를
                                    if(isExistApp("com.locnall.KimGiSa")) {

                                        final KakaoNaviParams.Builder builder = KakaoNaviParams.newBuilder(Location.newBuilder(tasteitem.getFood_name(), tasteitem.getLogi(), tasteitem.getLati()).build()).setNaviOptions(NaviOptions.newBuilder().setCoordType(CoordType.WGS84).setStartX(GeoItem.getKnownLocation().longitude).setStartY(GeoItem.getKnownLocation().latitude).build());

                                        KakaoNaviService.navigate(seeTasteActivity.this, builder.build());
                                        KakaoNaviService.shareDestination(seeTasteActivity.this, builder.build());
                                    }else {
                                        Toast.makeText(seeTasteActivity.this,"앱 설치 후 다시 사용해주세요",Toast.LENGTH_SHORT).show();
                                    }
                                } else if(which == 1) {
                                    Boolean map = isExistApp("com.skt.tmap.ku");
                                    if(map) {

                                        TMapTapi tmaptapi = new TMapTapi(seeTasteActivity.this);
                                        tmaptapi.setSKPMapAuthentication("8abaed54-852a-4d03-b4db-5281cd4ae354");
                                        HashMap pathInfo = new HashMap();
                                        pathInfo.put("rGoName", tasteitem.getFood_name());
                                        pathInfo.put("rGoY", String.valueOf(tasteitem.getLati()));
                                        pathInfo.put("rGoX", String.valueOf(tasteitem.getLogi()));
                                        pathInfo.put("rStName", "현재 위치");
                                        pathInfo.put("rStY", String.valueOf(GeoItem.getKnownLocation().latitude));
                                        pathInfo.put("rStX", String.valueOf(GeoItem.getKnownLocation().longitude));

                                        tmaptapi.invokeRoute(pathInfo);

                                    }else {
                                        Toast.makeText(seeTasteActivity.this,"앱 설치 후 다시 사용해주세요",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                dialog.dismiss();
                            }
                        }).show();
    }

    public static void ClipBoardLink(Context context , String link){
        ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", link);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(context, "주소가 복사되었습니다.", Toast.LENGTH_SHORT).show();
    }


    private boolean isExistApp( String packageName ){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);

            if (launchIntent == null) { // 단말기 내에 어플리케이션(앱)이 설치되어 있지 않음.
                return false;         // 앱이 설치되어 있지 않은 경우 Play스토어로 이동
            } else { // 단말기 내에 어플리케이션(앱)이 설치되어 있음.
                return true; // 앱이 설치되어 있으면 앱 실행.
            }
    }

    //서버에 리뷰데이터를 지우라고 요청합니다
    @SuppressLint("StaticFieldLeak")
    private class reviewremove extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(seeTasteActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != null) {
                getfeedList();
                Toast.makeText(seeTasteActivity.this,"삭제되었습니다",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(seeTasteActivity.this,"실패"+result,Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "POST response  - " + result);
        }


        @Override

        protected String doInBackground(String... params) {

            //맛집이름과 작성자 이름, 시간을 보냅니다
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

    //해당 맛집을 구글맵으로 보여주기 위해 프래그먼트에 구글맵을 넣어줍니다.
    public void getLocation(){
        FragmentManager fm = getSupportFragmentManager();
        WorkaroundMapFragment fragment = (WorkaroundMapFragment) fm.findFragmentById(R.id.map2);
        fragment.getMapAsync(this);

        fragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                //맛집 상세정보 안에서 구글맵이 위아래로 움직일 수 있도록
                //터치 모션을 넣어줍니다.
                ScrollView nScrollView = findViewById(R.id.scroll_view);
                nScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });
    }

    //구글맵이 준비되면 구글맵 안에
    //해당 맛집의 위치를 마커로 추가하고, 그 위치로 맵을 이동시켜주는 메서드
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;       //먼저 사용자가 위치 권한이 확인

        map.setMyLocationEnabled(true);

        UiSettings setting = map.getUiSettings();
        setting.setMyLocationButtonEnabled(true);
        setting.setCompassEnabled(true);
        setting.setZoomControlsEnabled(true);
        setting.setMapToolbarEnabled(true);
        //마커에 맛집의 위도와 경도를 정해주고
        //맵에 마커를 추가.
        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(tasteitem.getLati(), tasteitem.getLogi()));          //마커의 위치 지정
        //marker.draggable(true);
        map.addMarker(marker);                                                             // 마커 추가
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(tasteitem.getLati(), tasteitem.getLogi()), 18);
        map.moveCamera(cameraUpdate);                                                       //추가한 마커로 지도 초점 맞춤
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.findmap: fmdialog();break;
            case R.id.findmap2:fmdialog(); break;
            case R.id.navi:fNdialog(); break;
            case R.id.navi2:fNdialog(); break;
            case R.id.copy: ClipBoardLink(this,tasteitem.getFood_address()); break;
            case R.id.copy2: ClipBoardLink(this,tasteitem.getFood_address()); break;
        }
    }

    //카카오톡으로 공유하기 메세지를 보내는 메소드다.
    public void sharekakao(String title, String img){
        try {
            KakaoLink kakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
            KakaoTalkLinkMessageBuilder kakaoBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

            //해당 맛집의 첫번째 사진을 넣는다
            kakaoBuilder.addImage(img,95,95);
            //해당 맛집의 상세정보를 메세지안에 보이도록 넣는다
            kakaoBuilder.addText(title+"\nFindTaste 평점:" +rating.getText()+"/5.0\n"+address.getText().toString()+"\n"+tel);
            //카카오톡안에서 앱으로 보기 버튼을 누르면
            //현재 화면으로 이동되도록 만든다.
            kakaoBuilder.addAppButton("앱으로 보기",
            new AppActionBuilder().addActionInfo(AppActionInfoBuilder.createAndroidActionInfoBuilder().setExecuteParam("tastename="+tastename).build()) //해당 맛집이름을 넣어서 원하는 맛집 화면으로 이동시ㅣㅁ
                    .build());
            //메세지 보내기
            kakaoLink.sendMessage(kakaoBuilder,this);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //페이스북에 공뷰하는 메서드이다
    public void shareFacebook(){
        //맛집의 첫번째 사진을 비트맵으로 만든 뒤, 페이스북 사진 보내주기에 넣는다
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bmp[0])
                .build();
        //사진과 맛집 이름을 같이 지정해준다.
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .setPlaceId(tastename)
                .build();
        //페이스북 담벼락을 현재 화면에서 다아얼로그로 띄운다
        ShareDialog shareDialog = new ShareDialog(seeTasteActivity.this);
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);

    }

    //해당 맛집의 이름을 서버에 보내서,
    //맛집의 상세정보를 받아와 뿌려주는 클래스다.
    @SuppressLint("StaticFieldLeak")
    private class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(seeTasteActivity.this,
                    "잠시만 기다려주세요", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
           // mTextViewResult.setText(result);
            Log.d("결과", "POST response  - " + result);
            //서버에서 값을 못 가져왔을 때
            if (result == null){
                Toast.makeText(getApplicationContext(),errorString,Toast.LENGTH_SHORT).show();
            } //값을 가져왔으면
            else {
                mJsonString = result;           //json의 이름
                showResult();                   //가져온 json의 값을 꺼내오도록 실행
            }
        }


    @Override
    protected String doInBackground(String... params) {

            String name = (String)params[0];
            String serverURL = "http://findtaste.vps.phps.kr/user_signup/get_imagepath.php";
            String postParameters = "name=" + name;             //맛집 이름을 서버에 보낸다.
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
                Log.d("", "POST response code - " + responseStatusCode);

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


                return sb.toString().trim();


            } catch (Exception e) {

               // Log.d(TAG, "InsertData: Error ", e);
                Log.d("", "InsertData: Error ", e);

                return null;
            }

        }
    }
    //서버에서 받아왓을 때 실행되는 메서드입니다.
    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
                //받아온 값을 어레이로 만들어줍니다
            for(int i=0;i<jsonArray.length();i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                String id = item.getString(TAG_ID);             //맛집의 id
                String name = item.getString(TAG_NAME);         //맛집의 사진 이름
                String path = item.getString(TAG_PATH);         //맛집의 사진 경로


                HashMap<String, String> hashMap = new HashMap<>();

                hashMap.put(TAG_ID, id);
                hashMap.put(TAG_NAME, name);
                hashMap.put(TAG_PATH, path);
                patharray.add(path);                //사진의 경로를 리스트에 추가.
            }
                //만약 페이스북으로 공유를 해야할 경우,
                //글라이드로 해당 맛집 사진을 비트맵화 시켜놓는다.
                Glide.with(seeTasteActivity.this)
                        .asBitmap()
                        .load(patharray.get(0))    // you can pass url too
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                //  imgView.setImageBitmap(resource);
                                bmp[0] = resource;
                            }
                        });

            for(int i=0;i<jsonArray.length();i++){

            }   //화면의 위에있는 가로 스크롤뷰에 사진을 추가할 갯수를 정한다.
                fullsize=patharray.size();          // 추가할 이미지뷰 갯수
                Log.e("fullsize", String.valueOf(patharray.size()));
                // 화면의 위에 있는 가로 스크롤뷰에 사진을 추가시키는 메서드를 실행
                getimage(fullsize,patharray);
        } catch (JSONException e) {
            Log.d("", "showResult : ", e);
        }

    }

    //사용자가 해당 맛집 상세정보 화면에서 즐겨찾기 버튼을 눌렀을 때,
    //즐겨찾기를 했다고 서버에 유저의 아이디와 맛집의 이름을 보낸다
    @SuppressLint("StaticFieldLeak")
    class FavorData extends AsyncTask<String, Void, String>{
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

            String name = (String)params[0];    //유저 이름
            String status = (String)params[1];  //맛집 이름
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"favorites.php"; //서버에 보낼 주소
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

    //사용자가 해당 맛집 상세정보 화면에서 즐겨찾기를 해제했을 때,
    //즐겨찾기를 지운다고 서버에 유저의 아이디와 맛집의 이름을 보낸다
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

    //해당 맛집의 리뷰들을 서버에서 불러오는 메서드입니다
    public void getfeedList() {
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        //맛집의 정보를 서버에 보내서 해당 리뷰들을 불러옵니다
        Call<List<FeedItem>> call = apiInterface.getFeedcomment(userId,foodname.getText().toString());
        call.enqueue(new Callback<List<FeedItem>>() {
            @Override
            public void onResponse(Call<List<FeedItem>> call, Response<List<FeedItem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(seeTasteActivity.this, "오류", Toast.LENGTH_SHORT).show();
                }
                //불러왔을 때 리뷰의 정보를 리스트에 넣고 어댑터를 새로고침 해준다.
                else {
                    feedList.clear();
                    feedadapter.clear();
                    int e=0;
                    ArrayList<String> path = new ArrayList<>();

                    for (FeedItem feed : response.body()) {
                        feedList.add(feed);
                        e=e+1;
                            Log.i("RESPONSE: ", "" + feed.toString());
                            for(int i=0; i<feed.getReviewpic().size(); i++) {
                                path.add(feed.getReviewpic().get(i).getPath());
                                patharray.add(feed.getReviewpic().get(i).getPath());
                            }
                        getimage(feed.getReviewpic().size(),path);
                    }
                    if(e==0)
                        morereview.setVisibility(View.GONE);
                }
                feedadapter.notifyDataSetChanged();                     // 화면 새로고침
            }

            @Override
            public void onFailure(Call<List<FeedItem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(seeTasteActivity.this, "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

}
