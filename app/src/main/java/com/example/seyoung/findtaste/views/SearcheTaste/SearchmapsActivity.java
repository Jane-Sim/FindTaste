package com.example.seyoung.findtaste.views.SearcheTaste;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.FoodHorizonAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.GeoItem;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.Tasteitem;
import com.example.seyoung.findtaste.views.SeeTasteInfo.seeTasteActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by seyoung on 2017-11-09.
 * 유저가 지도에서 맛집을 찾을 때 보이는 화면입니다.
 * 사용자의 현재위치에서 찾거나 원하는 위치로 맵을 움직여서 지도를 찾을 수 있습니다.
 * 구글맵에서 마커들로 맛집들을 보여주며, 마커를 누르면 상세화면을 보여줍니다.
 * 또한 500m, 1km, 3lm 반경으로 맛집을 찾을 수 있습니다.
 *
 */

public class SearchmapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private GoogleApiClient mGoogleApiClient = null;
    Marker selectedMarker;          //지도에서 마커를 누를 때 빨간 마커로 표시하도록 해준다.
    private static final String TAG = "SearchmapsActivity";  //
    Circle myCircle;                 // 현재 위치에서 맛집을 불러 올 공간을 원으로 그려준다
    FoodHorizonAdapter foodadapter;  // 지도의 상세정보를 보여줄 리싸이클뷰와 연결해주는 연결점(어댑터)
    ArrayAdapter sadapter;           // selectbox의 목록값(500m, 1km ...)을 콤보박스와 연결해주는 어댑터
    RecyclerView recyclerView;      // 맛집 상세정보를 리스트로 보여줄 재활용뷰
    Button button;                   // 맛집데이터를 서버에서 불러오는 버튼
    Spinner spinner;                 // selectbox의 역할을 하는 스피너
    ArrayList<Tasteitem> foodList = new ArrayList<Tasteitem>(); // 맛집의 정보를 담을 리스트
    View marker_root_view;                  //커스텀한 마커를, 현재 구글맵위에 보이도록할 프래임 레이아웃(레이아웃이 겹쳐져서 마커가 그 위에 보인다).
    TextView tv_marker;                      //커스텀한 마커. (텍스트뷰로 먼둘오서, 마커의 숫자가 보이도록 해준다. 1번일 때 마커 가운데에 1이 보이도록 한다)
    private GoogleMap mMap;                 //구글맵을 불러온다
    String 반경="500";                       //반경이다. 처음에는 500m로 지정.
    int sort=0;                             //서버에서 맛집을 거리순으로 불러올 지, 평점일 지 구분해주는 숫자. 현재는 거리순으로만 불러오게 했다.
    double lati = GeoItem.knownLatitude;  // 위도는 마지막으로 업데이트한 사용자의 위치 값이다
    double logi = GeoItem.knownLongitude; // 경도는 마지막으로 업데이트한 사용자의 위치 값이다

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;    //사용자가 gps를 키고 왓는 지 확인할 값이다
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;   //사용자가 위치 권한을 허용했는 지 확인할 값

    boolean askPermissionOnceAgain = false;         // 사용자가 권한을 줬을 때, 다시 묻지 않도록 해주는 값.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);         // 레이아웃 연결

        //만약 사용자의 위치값이 없을 경우, 기본 위치를 남성역으로 지정해준다.
        if(lati==0||lati==0.0){
            lati =37.484876;
        }
        if(logi==0||logi==0.0){
            logi =126.970673;
        }
        //맛집의 상세정보를 보여줄 재활용뷰와, 맛집 데이터를 불러올 버튼이다
        recyclerView = findViewById(R.id.recycler_view);
        button = findViewById(R.id.find);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {        //버튼을 누르면
                getMarkerItems(Integer.parseInt(반경),sort);   //설정한 거리만큼 맛집데이터를 서버에서 불러온다
                AddCircle(Integer.parseInt(반경));        //사용자가 지정한 거리만큼 원을 지도에 그림
                button.setVisibility(View.GONE);         // 맛집 가져오기 버튼을 숨기고 맛집의 상세정보를(리싸이클뷰) 다시 화면에 보여준다
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        //콤보박스에 보여줄 목록글들을 콤보박스와 연결해주는 어댑터.
        sadapter =ArrayAdapter.createFromResource(this,R.array.반경,
                android.R.layout.simple_spinner_dropdown_item);      // 지정했던 목록글들을 어댑터에 넣어준다
        spinner = findViewById(R.id.txt_question_type);
        spinner.setAdapter(sadapter);                                   //목록글을 담은 어댑터를 스피너와 연결
        //콤보박스 중 하나를 누를 경우, 맛집을 다시 보여주는 이벤트다.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {
                Toast.makeText(SearchmapsActivity.this,
                        sadapter.getItem(position)+"", Toast.LENGTH_SHORT).show();
                //만약 500미터를 누를 경우, 서버에서 500안에 포함된 맛집데이터를 가져온다.
                if(position==0) {
                    반경 = "500";
                }
                else if(position==1) {
                    반경 = "1000";
                }
                else{
                    반경 = "3000";
                }
                getMarkerItems(Integer.parseInt(반경),sort);          //고른 아이템의 거리만큼 서버에다가 맛집데이터를  호출
                AddCircle(Integer.parseInt(반경));                    //원 그림
                button.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?>  parent) {
            }
        });

        // 맛집 데이터를 어댑터에 넣는다
        foodadapter = new FoodHorizonAdapter(foodList, this);

        // 리싸이클뷰를 가로 리스트로 만든다
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);

        SnapHelper snapHelper = new PagerSnapHelper();           // 스크롤이 한 개씩만 되도록 리싸이클뷰에서
                                                                 // 지원해주는 pagerSnapHelper를 이용
        snapHelper.attachToRecyclerView(recyclerView);          // 연결
        recyclerView.setOnFlingListener(snapHelper);            // 리스너와 연결
        //recyclerView.setClipToPadding(false);

        //리싸이클뷰의 구분선을 나눌 때 쓰는 것이지만, 리싸이클뷰를 옆으로 넘길 때마다 발생하는 이벤트도 같이 설정했다.
        recyclerView.addItemDecoration(new VerticalOffsetDecoration(this));

        recyclerView.setAdapter(foodadapter);                               //리싸이클뷰 어댑터 설정

        //맛집의 상세정보를 누를 때, 해당 맛집의 상세정보로 넘어가게 만든다
        foodadapter.setOnItemClickListener(new FoodHorizonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Tasteitem item) {
                Toast.makeText(SearchmapsActivity.this, item.getFood_name(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SearchmapsActivity.this,seeTasteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("맛집이름", item.getFood_name());
                intent.putExtra("사진이름", item.getImage_name());
                startActivity(intent);      //상세 정보로 이동
            }
        });

        //구글맵 프래그먼트 불러옴
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    //리싸이클뷰를 옆으로 넘길 때마다 발생하는 이벤트도 있다.
    public class VerticalOffsetDecoration extends RecyclerView.ItemDecoration {
        private Activity context;

        public VerticalOffsetDecoration(Activity context) {
            this.context = context;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            // 5
            int position = parent.getChildAdapterPosition(view);        //리싸이클뷰의 현재 위치를 가져옴
            int total = parent.getAdapter().getItemCount();             //리싸이클뷰의 총 갯수
            if (position < 0 && position > total)       // 리싸이클뷰의 아이템의 크기가 0보다 작고 총합보다 클 때
                return;                                 // 종료시킨다

/*            // 6
            Display display = context.getWindowManager().getDefaultDisplay();
            Point displaySize = new Point();
            display.getSize(displaySize);
            int displayWidth = displaySize.x;
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            float viewWidth = params.width;
            // 7
            int offset = (int)(displayWidth - viewWidth) / 2 ;
            if (position == 0)
                outRect.left = offset - params.getMarginStart();
            if (position == total - 1)
                outRect.right = offset- params.getMarginEnd();*/

            //리싸이클 뷰가
           /* for(int i=0; i<total; i++){
                if(position==i) {
                    if(i>=0&&i<=total) {      */  // 0이거나 총합일 때만 할 수 있게 한다.


             // 선택된 리싸이클뷰의 마커를 빨간색 마커로 지정
            changeSelectedMarker(selectedMarker);
            //나머지 마커들은 회색 마커로 지정해준다.
            for (int j = position - 1; j >= 0; j--) {
                addMarker(foodList.get(j), false);          // 나머지 마커는 회색으로 지정
            }
            for (int j = position + 1; j < total; j++) {
                addMarker(foodList.get(j), false);          // 나머지 마커는 회색으로 지정
            }
            addMarker(foodList.get(position), true);
                  /*  }*/

            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(foodList.get(position).getLati(),
                    foodList.get(position).getLogi()));  //해당 마커의 중심으로 지도 화면을 변경한다
            mMap.animateCamera(center);             // 이동시킨다

            Log.e("현재위치는 ", position+1+"");
                /*}
            }*/
        }
    }

    //구글맵이 준비됐을 때 시작하는 행동
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //사용자의 위치로 맵을 이동시킨다.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati, logi), 15));
        mMap.setOnMarkerClickListener(this);        //마커 누를 때 리스너
        mMap.setOnMapClickListener(this);           // 맵 누를 때 리스너
        // 사용자 퍼미션 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //현재 위치 버튼추가
        mMap.setMyLocationEnabled(true);
        //내 위치로 이동버튼을 누를 때,
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                button.setVisibility(View.GONE);        //맛집 불러오기 버튼을 숨기고
                recyclerView.setVisibility(View.VISIBLE);   //맛집 상세정보 리싸이클뷰를 불러온다
                lati = GeoItem.getKnownLocation().latitude;     //내 현재 위치로 이동시킨다
                logi = GeoItem.getKnownLocation().longitude;
                getMarkerItems(Integer.parseInt(반경),sort);  //맛집데이터를 불러오고
                AddCircle(Integer.parseInt(반경));            //내 위치에서 반경을 그려준다
                return true;
            }
        });


        setCustomMarkerView();                      //내가 커스텀한 마커를 구글맵에 추가한다
        //getMarkerItems(Integer.parseInt(반경),sort);
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {            //카메라 리스너
                lati=mMap.getCameraPosition().target.latitude;            //카메라가 이동한 곳의 위치값(Latlng)을 가져온다
                logi=mMap.getCameraPosition().target.longitude;

                Log.i("centerLat","현재 구글 중심위도"+mMap.getCameraPosition().target.latitude);

                Log.i("centerLong","현재 구글 중심경도"+mMap.getCameraPosition().target.longitude);
            }

        });
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if(i==REASON_GESTURE){              //맵을 누르거나 드래그의 제스처를 취할 경우.
                   // changeSelectedMarker(null);                      //빈 맵을 클릭할 경우 선택했던 마커를 회색마커로 변경
                    recyclerView.setVisibility(View.GONE);      //리싸이클뷰를 숨기고
                    button.setVisibility(View.VISIBLE);         // 맛집 가져오기 버튼을 나타낸다.
                    for (int j = 0; j < foodList.size(); j++) {
                        addMarker(foodList.get(j), false);          // 나머지 마커들도 회색으로 지정
                    }

                }


            }
        });

    }
    //원하는 위치에서 맛집을 가져오는 반경의 범위를 보여주기 위해, 구글맵위에 원을 그린다.
    public void AddCircle(int 범위){
       if(myCircle!=null) myCircle.remove();                //구글맵에 원이 구려져 있을경우 원 삭제

        myCircle = mMap.addCircle(new CircleOptions()      //원 그리기
                .center(new LatLng(lati,
                        logi))         // center 내가 있는 위치나, 지정 위치
                .radius(범위)                       // 원을 그릴 반경
                .strokeColor(Color.parseColor("#884169e1"))       // 원의 윤곽선 색깔 찐파랑
                .strokeWidth(2f)                                   //원의 윤곽선 굵기
                .fillColor(Color.parseColor("#5587cefa")));       // 원을 채울 색 연파랑
    }

    private void setCustomMarkerView() {            //커스텀 마커의 레이아웃을 가져오기

        marker_root_view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);     //마커를 커스텀한 레이아웃 xml
        tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);                   //레이아웃에서 커스텀한 마커(텍스트)를 부른다
    }

    // 서버에서 맛집의 데이터를 가져온다 (사용자가 원하는 범위, 정렬 카테고리[혅재 거리순이 기본])
    private void getMarkerItems(int 범위,int sort) {        // 여기서 마커를 추가한다
        mMap.clear();
        if (selectedMarker != null) selectedMarker.remove();
        foodList.clear();                       //  리스트 삭제
                                 // 맵의 마커 삭제
        //맛집을 찾길 원하는 위치와 찾을 범위, 거리순으로 데이터를 달라고 서버에 요청한다
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        Call<List<Tasteitem>> call = apiInterface.getmapFood(lati,logi,범위,sort);             //위도 경도 반경을 보냄
        call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과
            @Override
            public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    for (Tasteitem taste : response.body()) {
                        addMarker(taste, false);                             //마커를 추가한다
                        foodList.add(taste);
                        Log.i("RESPONSE: ", "" + taste.toString());
                    }                                                   // 리스트에 계속 json 데이터를 축적시키며 추가한다.
                }
                foodadapter.notifyDataSetChanged();                     // 맛집 상세정보를 새로고침한다
            }

            @Override
            public void onFailure(Call<List<Tasteitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
                Toast.makeText(getApplicationContext(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

    // 마커를 추가한다. 마커를 클릭할 때의 이벤트도 같이 지정해준다.
    private Marker addMarker(Tasteitem taste, boolean isSelectedMarker) {
        //해당 맛집의 위치를 가져온다
        LatLng position = new LatLng(taste.getLati(), taste.getLogi());                     //추가한 마커의 경도 위도를 가져온다
        int number = taste.getId();        //마커에다가 숫자를 매길, 맛집의 순서 번호를 겟한다

        tv_marker.setText(String.valueOf(number));      //겟한 숫자를 커스텀한 마커에 새긴다

        if (isSelectedMarker) {                         //마커를 클릭할 때
            tv_marker.setBackgroundResource(R.drawable.onclick);    //빨간색 마커로 바꾼다
            tv_marker.setTextColor(Color.MAGENTA);                  //글자는 마젠타
        }
        else {                                          //마커 클릭 안할 때
            tv_marker.setBackgroundResource(R.drawable.offclick);   //회색 마커로 바꿈
            tv_marker.setTextColor(Color.GRAY);                     //마커 글자도 회색
        }

        MarkerOptions markerOptions = new MarkerOptions();           //마커의 옵션. 마커를 추가하기 전에 설정한다.
        markerOptions.title(Integer.toString(number));                //마커의 타이틀은 숫자로 표시한다
        markerOptions.position(position);                            //마커의 위치를 넣는다. position.
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view))); //커스텀마커를 아이콘에 추가.
                                                                                                                   // 비트맵처리로 보이게 추가한다.
        return mMap.addMarker(markerOptions);    //구글맵에 마커를 추가한 것을 리턴한다

    }

    // View를 Bitmap으로 변환
    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        //view.bringToFront();

        return bitmap;
    }


    private Marker addMarker(Marker marker, boolean isSelectedMarker) {     //다시 마커를 구글맵에 추가시키는 클래스
        double lat = marker.getPosition().latitude;                         //추가되었던 마커의 위도 경도를 가져온다
        double lon = marker.getPosition().longitude;
        int number = Integer.parseInt(marker.getTitle());                    //마커의 타이틀
        Tasteitem temp = new Tasteitem(lat, lon, number);                    // 마커의 마커아이템을 다시 만들어서 넣는다.
        return addMarker(temp, isSelectedMarker);                             //구글맵 추가 클래스 실행

    }


    @Override
    public boolean onMarkerClick(Marker marker) {           // 마커를 누를 때


        for (int i = 0; i < foodList.size(); i++) {
            addMarker(foodList.get(i), false);          // 나머지 마커는 회색으로 지정
        }
        recyclerView.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());  //마커의 중간으로 화면을 변경한다
       // mMap.moveCamera(center);
        mMap.animateCamera(center);   // 이동시킨다
        recyclerView.scrollToPosition(Integer.parseInt(marker.getTitle())-1);
        changeSelectedMarker(marker);            //마커 색깔 변경

        return true;
    }



    private void changeSelectedMarker(Marker marker) {      //마커 색깔 변경 클래스
        // 선택했던 마커 되돌리기
        if (selectedMarker != null) {           //마커를 클릭 안할 경우
            addMarker(selectedMarker, false);   //회색마커로 화면에 추가하고
            selectedMarker.remove();             //전의 빨간 마커를 없앤다
        }

        // 선택한 마커 표시
        if (marker != null) {                     //마커를 클릭했을 때
            selectedMarker = addMarker(marker, true);   // 회색 마커를 빨간 마커로 바꾼다
            marker.remove();                              //회색 마커는 삭제한다
        }


    }


    @Override
    public void onMapClick(LatLng latLng) {         //맵을 누를 때
       // changeSelectedMarker(selectedMarker);
        changeSelectedMarker(null);                      //빈 맵을 클릭할 경우 회색마커로 변경
        for (int i = 0; i < foodList.size(); i++) {
            addMarker(foodList.get(i), false);          // 나머지 마커는 회색으로 지정
        }

        recyclerView.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);
    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && fineLocationRationale==false) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
            if ( mGoogleApiClient.isConnected() == false) {
                Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {
            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionAccepted) {
                if ( !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    mGoogleApiClient.connect();
                }

            } else {
                checkPermissions();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchmapsActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(SearchmapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                askPermissionOnceAgain = true;
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myAppSettings);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finish();
            }
        });
        builder.create().show();
    }
    //다이얼로그의 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : 퍼미션 가지고 있음");
                        if ( !mGoogleApiClient.isConnected() ) {
                            Log.d( TAG, "onActivityResult : mGoogleApiClient connect ");
                            mGoogleApiClient.connect();
                        }

                        return;
                    }
                }
                break;
        }

    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


}
