package com.example.seyoung.findtaste.views.WriteReview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.views.FileLib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by seyoung on 2017-11-21.
 * 유저가 선택한 맛집의 리뷰를 적을 화면입니다.
 * 유저가 느꼈던 생각을 적을 수 있고, 찍었던 사진을 올릴 수 있으며
 * 좋았는 지 안좋았는 지 평점을 매겨서 다른 유저들도 참고 할 수 있도록 합니다.
 * 또한 적은 리뷰로 맛집의 평점과 리뷰갯수를 수정시켜
 * 유저들이 높거나 낮은 평점의 맛집, 리뷰순을 확인 할 수 있습니다.
 */


public class ReviewInfo extends Fragment implements View.OnClickListener{
    Context context;                //context설정
    String tastename;               // 식당이름
    TextView tv;                     //별점의 표시 숫자
    EditText editText;              //사용자의 리뷰 내용
    RatingBar rb;                    //별점
    Button bt_finish;                     //등록버튼
    View layout;                     //fragment의 뷰
    String Rating,Status;                   //별점을 저장할 스트링
    int reviewstatus;            // 이게 insert인지 update인지 확인 할 값
    int picnum;                  //받아온 사진의 갯수다
    int fina=0;                 //리니어 레이아웃을 스크롤뷰에 설정했을 때, 1로 바꿔서 중복되지 않게 만들 int값

    String name;        //유저의 이름
    String status;      //유저가 쓴 리뷰내용
    String time;        //현재 시간
    String foodname;    // 음식점 이름
    String rating;

    LinearLayout topLinearLayout;           //사용자가 사진을 추가할 경우에, 동적으로 만든 이미지뷰를 넣을 레이아웃입니다. 사용자가 바로 사진 확인이 가능합니다.
    private final String TAG = this.getClass().getSimpleName();
    ProgressDialog dialog;          //저장할 동안 나타나는 다이얼로그
    ScrollView down;                // 사용자가 리뷰의 내용을 적을 때, 스크롤뷰를 제일 밑으로 내려서 버튼에 가려지지 많게 만듭니다.
    HorizontalScrollView scrollView;            // 추가된 사진을 보여주는 레이아웃을 넣을 가로 스크롤뷰 (가로로 사진목록을 확인 가능)

    ArrayList<String> beforList = new ArrayList<>();    //수정된 리뷰를 적을 때, 사용자가 새로 추가한 이미지만 서버에 올려야 하므로,
                                                        //그 전에 추가했던 사진들만 화면에 보여주고 서버엔 보내지 않는다.

    ArrayList<String> remove = new ArrayList<>();       //사용자가 수정할 때, 올렸었던 사진을 지우고 저장할 경우-> 서버에 지운 사진들을 알려줄 리스트값입니다..
    ArrayList<String> imageList = new ArrayList<>();    //사진의 경로들을 추가하는 리스트. 그 전에 추가한 이미지리스트와 겹치지 않도록 해준다.

    ArrayList<String> list3 = new ArrayList<>();        // 수정 전에 추가한 사진의 경로와, 새로 추가한 사진의 경로를 합쳐서 넣어준 리스트이다.

    private static final int PICK_FROM_CAMERA = 0;   //사진 찍는 Result
    private static final int PICK_FROM_ALBUM = 1;    //앨범 가져온 것의 Result

    File imageFile;                                     //찍은 사진의 경로
    String imageFilename;                               //찍은 사진의 이름지정

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topLinearLayout = new LinearLayout(getActivity());          // 사진들을 추가할 레이아웃이다.
        if (getArguments().size()==1) {                               //리뷰를 추가할 때
            tastename = getArguments().getString("foodname");   //전의 프래그먼트에서 식당 이름을 받아온다
            reviewstatus=1;
        }
        else if(getArguments().size()==6){                          //리뷰를 수정할 때
            tastename= getArguments().getString("foodname");    //저장했었던 리뷰들의 값들을 가져온다.
            Status = getArguments().getString("status");
            Rating = String.valueOf(getArguments().getInt("rating"));
            time = getArguments().getString("time");
            if(Status!=null) beforList = getArguments().getStringArrayList("pic");

            picnum = getArguments().getInt("picnum");
            //리뷰 데이터를 잘 받아왔는 지 로그로 확인한다.
            Log.e("음식", String.valueOf(tastename));
            Log.e("리뷰", String.valueOf(Status));
            Log.e("별점", String.valueOf(Rating));
            Log.e("사진 리스트", String.valueOf(imageList.toString()));
            Log.e("사진 리스트", String.valueOf(beforList.toString()));
            Log.e("사진 갯수", String.valueOf(picnum));

            reviewstatus=5;
        }
        //getresponse();
    }

    @Override
    public void onDestroy() {           //화면이 파괴될 경우 다이얼로그도 같이 없애줘서 오류가 안나도록 해줘야 한다.
        super.onDestroy();
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = this.getActivity();
        View layout = inflater.inflate(R.layout.activity_reviewinfo, container, false);

        return layout;
    }

    @SuppressLint("ClickableViewAccessibility")
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv = view.findViewById(R.id.text);              //평점의 숫자를 나타내는 텍스트
        editText=view.findViewById(R.id.edittext);      //사용자가 리뷰 내용을 적을 에딧
        rb =view.findViewById(R.id.ratingBar);          //직접 사용자가 평점을 남길 별막대기.RatingBar
        bt_finish=view.findViewById(R.id.bt_finish);    //사용자가 적은 값들을 저장할 때 누르는 완료버튼

        //status값이 널이 아닐 때는 유저가 수정을 할 때의 행동이다.
        if(Status!=null) {
            rb.setRating(Integer.valueOf(Rating));  //저장했던 별점을 지정해주며
            tv.setText(Rating);                     //평점의 숫자도 지정
            tv.setFocusableInTouchMode(true);       //평점에 포커스를 줘서 키보드가 올라오는 것을 막는다.
            editText.setText(Status);               //리뷰값도 넣어준다.
            bt_finish.setBackgroundColor(Color.parseColor("#d84c4c"));  // 완료 버튼을 빨갛게 만든다.
        }

        down = view.findViewById(R.id.down);    //화면을 감싸는 스크롤뷰를 지정
        editText.setOnTouchListener(new View.OnTouchListener() {        //리뷰 에딧을 눌렀을 때 버튼에 가려지지 않도록 제일 맨 밑으로 스크롤을 내려줍니다.
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.edittext) {
                    v.getParent().requestDisallowInterceptTouchEvent(true); //에디트 텍스트의 부모 뷰는 클릭이 안되게 한다
                    down.smoothScrollBy(0, down.getBottom());               //또한 제일 아래로 스크롤을 내려서 포커스를 맞춰준다

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);    //에딧뷰에서 손을 땔 경우 다시 부모 뷰 터치가능
                            break;
                    }
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() { //리뷰에 값을 입력할 때마다 나타나는 리스너
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()>0)                                     //리뷰에 적었을 경우 버튼색을 레드로 처리합니다.
                    bt_finish.setBackgroundColor(Color.parseColor("#d84c4c"));
                if(s.length()==0)                                    //아무것도 리뷰에 안적었을 경우 버튼색을 회색처리합니다.
                    bt_finish.setBackgroundColor(getResources().getColor(R.color.com_facebook_button_border_color_focused));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        TextView imagepick = (TextView) view.findViewById(R.id.pick);       // 사진을 가져오는 버튼
        ImageView imageRegister = (ImageView) view.findViewById(R.id.camera); // 카메라로 사진을 찍는 버튼

        imageRegister.setOnClickListener(this);
        imagepick.setOnClickListener(this);
        bt_finish.setOnClickListener(this);

        //사진을 찍어서 가져올 경우.
        imageFilename = "tmp_" + String.valueOf(System.currentTimeMillis());    //사진 이름을 현재시간으로 지정
        imageFile = FileLib.getInstance().getImageFile(getApplicationContext(), imageFilename); // 찍은 사진의 경로를 지정해준다
        Log.e("사진 경로", String.valueOf(imageFile));

        layout = view;

        Rating = String.valueOf((int)rb.getRating());                           //별점의 숫자를 가져온다

        //Ratingbar를 눌러서 바꿔줄 때
        rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() { //사용자가 클릭이나 드래그할 때 바뀌는 값
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                if(rating<1){             //칠해진 별 값이 0일 경우
                    rb.setRating(1);      //1로 별을 칠하고 별점을 1로 만든다
                    rating=1;
                }
                if(ratingBar.getRating()==1)        //지정한 별 갯수만큼 별점 숫자를 지정한다.
                    rb.setRating(1);
                if(ratingBar.getRating()==2)
                    rb.setRating(2);
                if(ratingBar.getRating()==3)
                    rb.setRating(3);
                if(ratingBar.getRating()==4)
                    rb.setRating(4);
                if(ratingBar.getRating()==5)
                    rb.setRating(5);

                tv.setText(String.valueOf((int)rating));        //별점을 text로 표시
                Rating = String.valueOf((int)rating);           //서버에 저장할 별점을 지정해준다.
            }
        });

       scrollView =  layout.findViewById(R.id.hori);  //사용자가 저장했던 이미지를 넣을 가로스크롤뷰를 가져온다.
        if(Status!=null) {
            getimage(picnum,beforList);     //리뷰값이 존재할 때, 전에 저장한 이미지들을 가로 스크롤뷰에 넣는다.
        }


        //카메라와 사진을 읽고 보내는 권한을 체크한다.
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        5);
            }
        }
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        6);
            }
        }
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        7);
            }
        }
    }

    //권한 설정을 했는 지 확인하는 리턴값
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0     //카메라를 허용했을 때
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
            case 6: {                           //앨범을 허용했을 때
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
            case 7: {                           // 앨범을 허용했을 때
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }

        }
    }

    //권한 설정을 하나라도 안했을 경우에 어플을 종료시키는 메서드이다.
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(getActivity(), "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        getActivity().finish();
    }

    // HorizontalScrollView에 받아온 이미지갯수 만큼 동적으로 추가하려고 한다.
    public void getimage(int size, final ArrayList<String> list){
        scrollView.setVisibility(View.VISIBLE);
        list3.addAll(list); //이미지의 갯수만큼 리스트에 넣어준다.

        if(fina==0) {       //리니어레이아웃을 스크롤뷰에 설치 했었는지 확인해주는 if문이다.
            topLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        }

        for (int i = 0; i < size; i++){     //받아온 사진의 갯수만큼 동적으로 이미지뷰를 만들어준다.
            final ImageView imageView = new ImageView (getActivity());      //현재 화면에 사진이미지뷰와,
            final ImageView imagex = new ImageView(getActivity());          //제거를 담당할 엑스표시 이미지뷰를 만들어준다.

            Glide.with(this)                         //글라이드로 추가한 사진의 이미지를 이미지뷰에 넣어준다.
                    .load(list.get(i))
                    .apply(new RequestOptions()
                            .error(R.drawable.fbnull)
                            .override(400,400)
                            .centerCrop())
                    .into(imageView);

            final TextView textView = new TextView(getActivity());  //이미지의 이름을 텍스트뷰에 넣어준 후 안보이게 만든다.
            textView.setText(list.get(i));
            textView.setVisibility(View.GONE);

            imagex.setImageResource(R.drawable.com_facebook_close); //이미지뷰를 제거할 이미지뷰의 이미지를 지정한다.
            imagex.setMaxHeight(100);                               //사진뷰의 위쪽으로 올린다
            imagex.setMinimumHeight(100);
            imagex.setPadding(0,0,0,50);

            imageView.setAdjustViewBounds(true);       //사진이 마음대로 크기가 커지지 않도록 부모의 레이아웃에 크기를 맞춘다.
            topLinearLayout.addView(imageView);         // 리니어 레이아웃에 사진 갯수만큼 이미지뷰 추가
            topLinearLayout.addView(imagex);            // 이미지뷰를 지울 엑스뷰 추가
            topLinearLayout.addView(textView);          // 제거할 이미지의 이름을 찾을 수 있도록 텍스트도 값이 넣어준다.

            final int[] finalI = {i};
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO Auto-generated method stub
                    Log.e("Tag",""+imageView.getTag());     //사진뷰를 눌렀을 때 토스트창으로 이름을 띄운다
                    Toast.makeText(getActivity(),""+list3.get(Integer.parseInt(list.get(finalI[0]))),Toast.LENGTH_SHORT).show();
                }
            });

            //지우고 싶은 사진뷰의 엑스뷰를 누를 때
            imagex.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    topLinearLayout.removeView(imageView);  //리니어 레이아웃에서 이미지를 삭제해준다.
                    topLinearLayout.removeView(view);       //엑스뷰도 삭제한다

                    for(int i=0;i<beforList.size();i++){
                        if(beforList==null) {
                            imageList.remove(list.get(i));  //혹시 저장된 이미지 뷰가 없을 때에는 새로 추가된 이미지이니 새로 추가하는 리스트의 값을 지워준다.
                            //break;
                        }else if(beforList!=null) {         //저장된 이미지 뷰가 있고, 지웠던 이미지가 저장된 이미지 뷰였을 경우,
                            if (beforList.get(i).equals(textView.getText().toString())) {
                                Log.e("미리저장된 텍스트",textView.getText().toString());
                                Log.e("미리저장된 값",beforList.get(i));
                                remove.add(beforList.get(i));   //서버에 저장했던 사진을 지워달라는 리스트 목록에 추가한다.
                                finalI[0] =i;
                                break;
                            }
                            else{
                                imageList.remove(list.get(i));  // 새로 추가한 사진을 지웠을 때 서버에 저장이 안되게 리스트에서 제거한다.
                            }
                        }
                    }
                    for(int i=0;i<remove.size();i++) {          // 서버에 저장된 사진이 또 저장이 안되도록
                        list3.remove(remove.get(i));            //이미지 리스트에 전에 저장한 이미지뷰 값을 리스트에서 지운다.
                        Log.e("삭제된 원래사진",remove.get(i));
                    }

                    if(list3.size()==0) {                       // 추가했던 이미지가 아무것도 없으면 가로 스크롤뷰를 안보여준다.
                        scrollView.setVisibility(View.GONE);
                    }
                }
            });
        }
        if(list3.size()==0) {                                   // 처음부터 이미지가 없으면 추가한 리니어 레이아웃을 지우고
            scrollView.removeView(topLinearLayout);             // 가로스크롤뷰를 안보이게 만든다.
            scrollView.setVisibility(View.GONE);
        }
        if(fina==0)                                             //스크롤뷰에 리니어 레이아웃을 추가한 적이 없을 때
        scrollView.addView(topLinearLayout);                    //추가를 해준 뒤에 다시는 추가하지 않도록 해준다.
        //if(fina==1)

        fina=1;
    }

    //사진을 찍거나 사진을 가지러가는 다이얼로그를 부르는 메소드
    public void showImageDialog() {
        new AlertDialog.Builder(getActivity())
                // .setTitle("골라")
                .setSingleChoiceItems(R.array.camera_album_category, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    getImageFromCamera();   //사진을 찍으러 간다
                                } else {
                                    getImageFromAlbum();        //다시 사진을 가지러간다
                                }

                                dialog.dismiss();
                            }
                        }).show();
    }

    //카메라를 실행하는 메소드
    @TargetApi(18)
    private void getImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    /**
     * 앨범으로부터 이미지를 선택할 수 있는 액티비티를 시작한다.
     */
    @TargetApi(18)
    private void getImageFromAlbum() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent.createChooser(intent, "다중 선택은 포토클릭"), PICK_FROM_ALBUM);
    }

    //결과값을 받아왔을 때
    @Override
    @TargetApi(16)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FROM_CAMERA) {          //카메라로 찍은 것을 가지고 왔을 때
                imageList.add(String.valueOf(imageFile));   //사진 경로 저장
                getimage(imageList.size(),imageList);                  //화면에 사진 보여줌
            }

            else if (requestCode == PICK_FROM_ALBUM && data != null) {      //앨범에서 가지고 올 때

                if (data.getClipData() == null) {                       //갤러리에서 한 장만 가지고 왔을 때
                    imageList.add(String.valueOf(data.getData()));
                    getimage(imageList.size(),imageList);
                } else {                                                  //여러 장 가지고 왔을 때때
                   ClipData clibData = data.getClipData();
                    Log.i("clipdata", String.valueOf(clibData.getItemCount()));
                    if (clibData.getItemCount() > 20) {                    //20장 이상 골랐을 때,
                        Toast.makeText(getApplicationContext(), "사진은 20개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (clibData.getItemCount() == 1) {              //포토에서 한장 가져왔을 때
                        String dataStr = String.valueOf(clibData.getItemAt(0).getUri());
                        Log.i("2.clipdata choice", String.valueOf(clibData.getItemAt(0).getUri()));
                        Log.i("2.single choice", clibData.getItemAt(0).getUri().getPath());
                        imageList.add(dataStr);

                    } else if (clibData.getItemCount() > 1 && clibData.getItemCount() < 20) {   //1장 이상 20이하로 가져왔을 때
                        for (int i = 0; i < clibData.getItemCount(); i++) {
                            Log.i("3. single choice", String.valueOf(clibData.getItemAt(i).getUri()));
                            imageList.add(String.valueOf(clibData.getItemAt(i).getUri()));
                        }
                    }

                    getimage(imageList.size(),imageList); //사진 추가한 만큼 뷰에 추가
                }

            }
        }

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.camera) {  //사진 이미지 누를 때
            showImageDialog();
        } else if (view.getId() == R.id.pick) { //사진 텍스트를 누를때
            showImageDialog();                  //카메라와 앨범을 가지러 가는 다이얼로그 메소드를 실행
        }
        else if (view.getId() == R.id.bt_finish) {       //저장하는 버튼으 눌렀 을 때

            ImageAsyncTask lDB = new ImageAsyncTask();

            SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
             name= pref.getString("ing", "");     //유저의 이름

             status=editText.getText().toString();    //유저가 쓴 리뷰내용을 가져온다

            if(reviewstatus!=5){                    //사용자가 수정이 아닌 처음 저장할 때 시간을 현재시간으로 지정한다
                time = String.valueOf(System.currentTimeMillis());
            }
                //현재 시간
             foodname = tastename;        // 음식점 이름
             rating = Rating;             //별표 점수

            if(beforList!=null) {           //저장했던 이미지뷰의 리스트가 남아있을 때
                for(int j = 0; j < beforList.size(); j++) { //갯수만큼 돌린다.

                    for (int i = 0; i < imageList.size(); i++) {
                        Log.e("이미지 도는 갯수",imageList.size()+"");

                      // if (i!=0) i=i-1;
                        if (beforList.get(j).equals(imageList.get(i))) {    //새로 올린사진과 전에 올린 사진이 중복될 경우
                            Log.e("중복된 사진", imageList.get(i));      //중복된 사진을 지워서 새로 넣은 이미지 값만 서버에 보내준다.
                            imageList.remove(beforList.get(j));
                        }

                    }

                }
            }

            if(status.equals(null)||status.equals("")||status.equals(" ")||status.equals("  ")||status.equals("\n"))
                Toast.makeText(getActivity(),"내용을 적어주세요",Toast.LENGTH_SHORT).show();    //리뷰의 내용을 안 적었을 때
            else if(reviewstatus==1){                                                               // 새로 리뷰를 적을 경우 서버에 업로드 시킨다.
                lDB.execute(name,status,time,foodname,rating);
            }
            else if(reviewstatus==5){                                                               //리뷰를 수정할 경우 서버 디비 값을 수정한다.
                updateAsyncTask UDB = new updateAsyncTask();
                UDB.execute(name,status,time,foodname,rating);
            }
        }
    }

       @SuppressLint("StaticFieldLeak")
       public class ImageAsyncTask extends AsyncTask<String, Void, String> {

        String Add = "http://findtaste.vps.phps.kr/user_signup/uploadreview.php";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int maxBufferSize = 1 * 1024 * 1024;
        public static final int MAX_READ_TIME = 10000;
        public static final int MAX_CONNECT_TIME = 15000;
        int bufferSize;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 작업을 시작하기 전 할일
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle("저장 중");
            dialog.setMessage("잠시만 기다려주세요...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
            if (s != null) {
                Toast.makeText(getActivity(),"업로드 완료",Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(),"실패"+s,Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "POST response  - " + s);
        }


        //새로 추가한 이미지의 갯수만큼, 용량을 줄여서 서버에 보낸다
        @Override
        protected String doInBackground(String... params) {
            ArrayList<String> imageList7 = new ArrayList<>();   //용량을 줄인 사진의 경로를 저장할 리스트.
            InputStream is = null;
            for(int i=0; i<imageList.size();i++) {
                String name_Str = getImageNameToUri(imageList.get(i));  //앨범에서 가져온 경로를 갖고온다.
                Cursor c = getActivity().getContentResolver().query(Uri.parse(imageList.get(i)), null, null, null, null);
                c.moveToNext();
                try {
                    String absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));

                    Bitmap image_bitmap = null;

                    image_bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(imageList.get(i)));  //갖고온 사진을 비트맵화.

                    ///리사이징
                    int height = image_bitmap.getHeight()/2;        //사진의 크기를 절반으로 줄이고
                    int width = image_bitmap.getWidth()/2;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inJustDecodeBounds = true;
                    options.inSampleSize = 2;       //사진의 용량을 절반으로 만든다.

                    Bitmap src = BitmapFactory.decodeFile(absolutePath,options);

                    Bitmap resized = Bitmap.createScaledBitmap( src, width, height, true );

                    absolutePath = saveBitmaptoJpeg(resized, "seatdot", name_Str);  // 용량을 줄인 비트맵을 핸드폰에 저장 후에
                    imageList7.add(absolutePath);           //서버에 올릴 수 있게 경로를 추가해준다.
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Log.d("TEST", "file Path=>" + imageList7);
                c.close();
            }

            String name = (String)params[0];            //작성자 이름
            String status = (String)params[1];          //리뷰 내용
            String time = (String)params[2];            //리뷰 적은 시간
            String foodname = (String)params[3];        //맛집 이름
            String rating = (String)params[4];          //평점 값

            //  String postParameters = "name=" + name + "&status=" + status
            //         + "&time=" + time + "&foodname=" + foodname + "&rating=" + rating;

            try {
                URL connectUrl = new URL(Add);
                FileInputStream mFileInputStream ;
                HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
                conn.setConnectTimeout(MAX_CONNECT_TIME);
                conn.setReadTimeout(MAX_READ_TIME);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects( false ) ;
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("cache-control", "no-cache");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("cache-length", "length");
                for(int i=0; i<imageList7.size();i++) {         //사진의 경로들을 서버에 보낼 수 있도록 해준다.
                    conn.setRequestProperty("image"+i, imageList7.get(i));
                }
                conn.setRequestProperty("user-agent", "test");
                conn.connect();


                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"list\"\r\n\r\n" + imageList7.size());        //사진의 갯수를 보내고

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n" + URLEncoder.encode(name,"utf-8"));  //작성자 이름을 보냄

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"status\"\r\n\r\n"+URLEncoder.encode(status,"utf-8")); //리뷰 내용을 보냄

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"time\"\r\n\r\n"+ URLEncoder.encode(time, "UTF-8"));   //리뷰 작성시간을 보냄

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"foodname\"\r\n\r\n"+ URLEncoder.encode(foodname,"utf-8"));//맛집이름을 보냄

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"rating\"\r\n\r\n" +  URLEncoder.encode(rating,"utf-8")); // 평점을 보냄

                if(imageList7.size()==1) {          //새로 추가한 사진이 1일 경우
                    mFileInputStream = new FileInputStream(imageList7.get(0));
                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"image0\";filename=\"" + imageList7.get(0) + "\"" + lineEnd);
                                                                                        //사진의 이름을 보내고
                    dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                    dos.writeBytes(lineEnd);
                    int bytesAvailable = mFileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = mFileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    }                                                                   // 사진을 서버로 보내준다.
                    mFileInputStream.close();
                }
                else if(imageList7.size()>1) {           // 추가한 사진이 여러개일 경우
                    for (int i = 0; i < imageList7.size(); i++) {
                        mFileInputStream = new FileInputStream(imageList7.get(i));

                        dos.writeBytes("\r\n--" + boundary + "\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"image"+i+"\";filename=\"" + imageList7.get(i) + "\"" + lineEnd);
                        dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                        dos.writeBytes(lineEnd);
                        Log.d("image"+i,imageList7.get(i));
                        int bytesAvailable = mFileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        byte[] buffer = new byte[bufferSize];
                        int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                        while (bytesRead > 0) {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = mFileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                        }
                        mFileInputStream.close();
                    }
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();        //서버로 보내는 값을 닫는다.

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {  //성공 했을 경우
                    Log.d(TAG, "POST response code - " + conn.getResponseCode());
                    is = conn.getInputStream();     //서버에서 결과값을 받아온다
                } else {//실패
                    // Toast.makeText(AddtasteActivity.this,"실패",Toast.LENGTH_SHORT).show();
                    is = conn.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }


    }

    //서버에 올릴 때와 똑같은 원리다. 위위 aynctask와 같음
    //유저가 수정한 값을 서버에 보낸다.
    @SuppressLint("StaticFieldLeak")
    public class updateAsyncTask extends AsyncTask<String, Void, String> {

        String Add = "http://findtaste.vps.phps.kr/user_signup/updatereview.php";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int maxBufferSize = 1 * 1024 * 1024;
        public static final int MAX_READ_TIME = 10000;
        public static final int MAX_CONNECT_TIME = 15000;
        int bufferSize;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 작업을 시작하기 전 할일
            dialog = new ProgressDialog(getActivity());
            dialog.setTitle("저장 중");
             dialog.setMessage("잠시만 기다려주세요...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            if (s != null) {
                Toast.makeText(getActivity(),"수정 완료",Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(),"실패"+s,Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "POST response  - " + s);
        }



        @Override
        protected String doInBackground(String... params) {
            ArrayList<String> imageList7 = new ArrayList<>();
            InputStream is = null;
            for(int i=0; i<imageList.size();i++) {
                String name_Str = getImageNameToUri(imageList.get(i));
                Cursor c = getActivity().getContentResolver().query(Uri.parse(imageList.get(i)), null, null, null, null);
                c.moveToNext();
                try {
                    String absolutePath = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));

                    Bitmap image_bitmap = null;

                    image_bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(imageList.get(i)));

                    ///리사이징
                    int height = image_bitmap.getHeight()/2;
                    int width = image_bitmap.getWidth()/2;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inJustDecodeBounds = true;
                    options.inSampleSize = 2;

                    Bitmap src = BitmapFactory.decodeFile(absolutePath,options);

                    Bitmap resized = Bitmap.createScaledBitmap( src, width, height, true );

                    absolutePath = saveBitmaptoJpeg(resized, "seatdot", name_Str);
                    imageList7.add(absolutePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Log.d("TEST", "file Path=>" + imageList7);
                c.close();
            }

            String name = (String)params[0];
            String status = (String)params[1];
            String time = (String)params[2];
            String foodname = (String)params[3];
            String rating = (String)params[4];

            //  String postParameters = "name=" + name + "&status=" + status
            //         + "&time=" + time + "&foodname=" + foodname + "&rating=" + rating;

            try {
                URL connectUrl = new URL(Add);
                FileInputStream mFileInputStream ;
                HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
                conn.setConnectTimeout(MAX_CONNECT_TIME);
                conn.setReadTimeout(MAX_READ_TIME);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects( false ) ;
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("cache-control", "no-cache");
                conn.setRequestProperty( "charset", "utf-8");
                conn.setRequestProperty("cache-length", "length");
                for(int i=0; i<imageList7.size();i++) {
                    conn.setRequestProperty("image"+i, imageList7.get(i));
                }
                conn.setRequestProperty("user-agent", "test");
                conn.connect();


                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"list\"\r\n\r\n" + imageList7.size());

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"remove\"\r\n\r\n" + remove.size());

                for(int i=0;i<remove.size();i++){
                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"remove"+i+"\"\r\n\r\n" + URLEncoder.encode(remove.get(i),"utf-8"));

                }

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"\r\n\r\n" + URLEncoder.encode(name,"utf-8"));

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"status\"\r\n\r\n"+URLEncoder.encode(status,"utf-8"));

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"time\"\r\n\r\n"+ URLEncoder.encode(time, "UTF-8"));

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"foodname\"\r\n\r\n"+ URLEncoder.encode(foodname,"utf-8"));

                dos.writeBytes("\r\n--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"rating\"\r\n\r\n" +  URLEncoder.encode(rating,"utf-8"));

                if(imageList7.size()==1) {
                    mFileInputStream = new FileInputStream(imageList7.get(0));
                    dos.writeBytes("\r\n--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"image0\";filename=\"" + imageList7.get(0) + "\"" + lineEnd);

                    dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                    dos.writeBytes(lineEnd);
                    int bytesAvailable = mFileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    byte[] buffer = new byte[bufferSize];
                    int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = mFileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    }
                    mFileInputStream.close();
                }
                if(imageList7.size()>1) {
                    for (int i = 0; i < imageList7.size(); i++) {
                        mFileInputStream = new FileInputStream(imageList7.get(i));

                        dos.writeBytes("\r\n--" + boundary + "\r\n");
                        dos.writeBytes("Content-Disposition: form-data; name=\"image"+i+"\";filename=\"" + imageList7.get(i) + "\"" + lineEnd);
                        dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
                        dos.writeBytes(lineEnd);
                        Log.d("image"+i,imageList7.get(i));
                        int bytesAvailable = mFileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        byte[] buffer = new byte[bufferSize];
                        int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                        while (bytesRead > 0) {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = mFileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                        }
                        mFileInputStream.close();
                    }
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "POST response code - " + conn.getResponseCode());
                    is = conn.getInputStream();
                } else {//실패
                    // Toast.makeText(AddtasteActivity.this,"실패",Toast.LENGTH_SHORT).show();
                    is = conn.getErrorStream();
                }
                InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }


    }

    //가지고온 앨범 사진의 이름을 갖고오는 메소드이다.
    public String getImageNameToUri(String data)
    {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().managedQuery(Uri.parse(data), proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);    //사진의 경로에서
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1); //이름만 가져온다.

        return imgName;
    }

    //용량을 줄인 비트맵을 서버에 올릴 수 있도록 사진으로 저장시킨다.
    public static String saveBitmaptoJpeg(Bitmap bitmap,String folder, String name){
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard
        String foler_name = "/"+folder+"/"; //지정한 폴더로 사진을 저장
        String file_name = name+".jpg";     // 현재 시간으로 사진의 이름을 정한다.
        String string_path = ex_storage+foler_name; // 지정한 폴더의 경로를 기기안에 넣는다.
        String UploadImgPath = string_path+file_name;   // 지정한 폴더의 사진을 서버에 업로드 시킨다.


        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();         //지정한 폴더가 없을 경우 폴더를 만든다.
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name); //사진을 추가한다.

            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);   //70의 용량으로 사진을 추가.
            out.flush();
            out.close();

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
        return UploadImgPath;
    }

/*
    public void show(){                     // 리뷰 상세정보에서 수정했을 경우 메소드인데 아직 구현 못함.
        Intent returnIntent = new Intent();
        returnIntent.putExtra("name", name);
        returnIntent.putExtra("status", status);
        returnIntent.putExtra("time", time);
        returnIntent.putExtra("foodname", foodname);
        returnIntent.putExtra("rating", rating);
        returnIntent.putExtra("list",list3);
        getActivity().setResult(100,returnIntent);
        getActivity().finish();
    }*/


}
