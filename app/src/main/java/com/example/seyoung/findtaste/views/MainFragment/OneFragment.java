package com.example.seyoung.findtaste.views.MainFragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.foodAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.GeoItem;
import com.example.seyoung.findtaste.config.GeoLib;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.Tasteitem;
import com.example.seyoung.findtaste.views.ChatView.MyService;
import com.example.seyoung.findtaste.views.SeeTasteInfo.seeTasteActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.facebook.login.widget.ProfilePictureView.TAG;
import static com.google.android.gms.internal.zzahn.runOnUiThread;

/**
 * 사용자들이 등록한 맛집 리스트를 보여주기 위한 화면입니다.
 * 맛집의 리스트를 사용자가 원하는 카테고리로 나눌 수 있습니다. (평점순, 거리순, 리뷰순)
 * 사용자에게 맛집의 리스트를 1.사진과 2.맛집의 이름, 3. 맛집의 주소, 4. '유저와의 거리'와 해당 맛집의 적힌 5.리뷰의 '갯수', 6. 리뷰로 매긴 '평점'을 보여줍니다.
 * 불러온 값들로 맛집을 평점순, 거리순, 리뷰순으로 나열할 수 있습니다.
 */

public class OneFragment extends Fragment {
    public static OneFragment newInstance() {
        return new OneFragment();
    }
    foodAdapter foodadapter;            // '서버에서 가져온 데이터'를 리스트로 뿌려줄 수 있게 '맛집 데이터' 와 '리싸이클뷰의 아이템'을 연결시켜주는 연결점입니다.
    RecyclerView recyclerView;          // 사용자에게 보여질 재활용되는 뷰입니다. 어댑터와 연결시키면 리스트의 형식으로 볼 수 있습니다.
    ArrayList<Tasteitem> foodList = new ArrayList<Tasteitem>();  // 서버에서 가져온 데이터들을 관리하기 편하도록, 데이터를 모아 담아놓은 리스트입니다.
    LocationListener locationListener;                  // 사용자의 위치를 확인할 수 있도록 위치정보를 받아오는 이벤트입니다.
    LocationManager locationManager;                    // 사용자의 기기에서 위치기반을 켰는 지 확인해주는 위치 매니저입니다
    String userId;                      // 현재 사용자의 아이디입니다.
    int sort=0;                         // 맛집 정보를 원하는 카테고리로 나열할 수 있게 만들어주는, 선택한 카테고리의 값입니다. ex) 0이면 평점순으로 나열, 1이면 리뷰순으로 나열....
    double lati,logi;                   // 사용자의 현재 위치의 위도, 경도입니다.
    ArrayAdapter sadapter;              // 사용자에게 카테고리를 고를 수 있도록, 카테고리들의 글자들을 콤보박스에 연결시킬 연결점입니다.
    Spinner spinner;                    // 사용자가 선택할 콤보박스 기능을 해줄 스피너. 카테고리들이 드롭다운 형식으로 나타납니다.
    Parcelable recyclerViewState;       // 다른 화면이 현재 화면의 위에 있을 경우에, 현재 리싸이클뷰의 스크롤 위치를 저장할 변수입니다.
                                        //ex)다시 화면으로 돌아와도 스크롤한 위치를 잃지않고 그 위치로 가게해줌.
    ProgressDialog progressDoalog;      //서버에서 받아 올 동안 다이얼로그를 띄웁니다. 사용자에게 받아오는 걸 실시간으로 보여줌



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.onefragment, container, false);
        Intent inten = new Intent(getContext(), MyService.class);
        getContext().startService(inten);
        //사용자의 위치를 가져옵니다.
        lati= GeoItem.knownLatitude;
        logi=GeoItem.getKnownLocation().longitude;
//        Location location = GeoItem.getKnownLocation2();
        //만약 가져온 위치값이 아무 것도 없을 경우 위치값을 다시 불러옵니다.
        if(lati == 0.000) {
            progressDoalog = new ProgressDialog(getActivity(), android.support.v4.app.DialogFragment.STYLE_NO_TITLE);
            progressDoalog.setMessage("현재 위치를 확인 중입니다.");
            progressDoalog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();

            getLocation();
        }



        recyclerView = view.findViewById(R.id.recycler_view);

        //유저의 아이디를 쉐어드에서 가져옵니다.
        SharedPreferences pref = getContext().getSharedPreferences("pref", MODE_PRIVATE);
        userId= pref.getString("ing", "");                          //유저가 즐겨찾기를 했던 값을 가져와야 하기에
                                                                          //사용자의 아이디가 꼭 필요합니다.
        //Collections.sort(foodList); 서버에서 순서나열을 정해주기에 필요x

        getfoodList();  //사용자가 볼 맛집 정보를 불러옵니다

        //맛집 정보를 foodlist에 담고 나서 어댑터에 값을 넣습니다.
        foodadapter = new foodAdapter(foodList, getActivity());
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);     //이때 리싸이클뷰를 행에 한줄씩이 아닌,
        recyclerView.setLayoutManager(mLayoutManager);                                                     //그리드뷰로 2개씩 표시하게 만든 뒤, 설정해줍니다.
        recyclerView.setItemAnimator(new DefaultItemAnimator());                                           // 리싸이클뷰의 애니메이션은 기본으로 설정하며
        recyclerView.addItemDecoration(new GridSpacingdecoration(2, dpToPx(10), true));       // 리싸이클뷰의 중간의 구분선을 설정하며 아이템끼리의 패딩을 설정해줍니다.
        recyclerView.setAdapter(foodadapter);                                                              //그리고 어댑터와 리스트를 연결해줍니다.

        //리싸이클뷰에 추가된 데이터들의 레이아웃을 누를 때 리스너입니다.
        foodadapter.setOnItemClickListener(new foodAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Tasteitem item) {
                Toast.makeText(getActivity(), item.getFood_name(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(),seeTasteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("맛집이름", item.getFood_name());                                   //맛집 이름을 맛집 상세보기 화면으로 넘긴 후
                startActivity(intent);                                                                   //상세보기 화면을 불러옵니다.
            }
        });

        sadapter = ArrayAdapter.createFromResource(getActivity(),R.array.정렬,                            //연결점(어댑터)에 카테고리 문자들을 넣습니다.
                android.R.layout.simple_spinner_item);

        sadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);                 // 드롭형식으로 어댑터를 설정해서 고를 수 있게 합니다.

        spinner = view.findViewById(R.id.txt_question_type);                                             //스피너를 부른 뒤, 콤보박스형 어댑터와 연결시킵니다.
        spinner.setAdapter(sadapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {                     //카테고리 중 하나를 고를 경우,
            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {
                Toast.makeText(getActivity(),
                        sadapter.getItem(position)+"", Toast.LENGTH_SHORT).show();
                if(position==0) {           //0이면 평점순,
                    sort = position;
                }
                else if(position==1) {
                    sort = position;        // 1이면 리뷰순,
                }
                else{
                    sort = position;        // 값이 2일 때, 거리순으로 서버에서 데이터를 불러 올 카테고리를 설정합니다.
                }
                getfoodList();              //서버에서 불러오기 메소드 시작
            }
            public void onNothingSelected(AdapterView<?>  parent) {
            }
        });

               //현재 화면에서 위치 권한이 있는 지 확인을 한다
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }           // 현재 위치를 사용할것이기에 꼭 퍼미션 확인을 해주었는 지 확인.

        return view;
            }

    @Override
    public void onResume() {
        super.onResume();
        if(recyclerViewState != null)       // 멈췄을 때 리싸이클뷰의 저장한 값이 있을 경우.
            getfoodList();                  // 서버에 다시 막집 데이터를 불러와서 맛집 리스트를 갱신시킵니다.
        Log.e("멈췄나요","?");      //ex)사용자가 맛집상세정보창에서 즐겨찾기 버튼을 누르고 다시 맛집 리스트로 돌아갔을 때, 해당 맛집 즐겨찾기 버튼의 상태를 바꿔줍니다
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);  //리싸이클뷰의 저장되었던 위치를 불러와 다시 지정해줍니다.
        // feedadapter.notifyDataSetChanged();                                      // 스크롤한 값을 잃어버리지 않아서 사용자가 다시 스크롤을 할 필요x
    }

    @Override
    public void onStop() {                   //멈췄을 때 리싸이클뷰의 현재 스크롤 위치를 저장해준다.
        super.onStop();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();

    }

    @Override
    public void onPause() {                 //멈췄을 때 리싸이클뷰의현재 스크롤 위치를 저장해준다.
        super.onPause();
        recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 200:
                //사용자가 위치 기반 액티비티에서 GPS 활성 시켰는지 검사
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)     //위치 기반을 켰을 경우에
                        || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    Log.d(TAG, "onActivityResult : 퍼미션 가지고 있음");
                }
                else {
                    getActivity().finish();                                            //사용자가 위치를 안 키고 돌아오면 앱을 종료시킵니다.
                    Toast.makeText(getActivity(),"권한이 없어서 어플을 종료합니다",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    //사용자의 위치를 가져오는 메소드입니다.
    public void getLocation(){
        //위치 매니저 호출
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //인터넷과 gps를 확인한다.
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //인터넷이나 위치가 연결되었을 경우에,
        if (isGPSEnabled || isNetworkEnabled) {
            Log.e("GPS Enable", "true");
            //사용자의 위치를 받아오는 행동입니다.
            final List<String> m_lstProviders = locationManager.getProviders(false);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (progressDoalog.isShowing())     //결과를 받아왔을 때 다이얼로그를 종료시킵니다.
                        progressDoalog.dismiss();
                    Log.e("onLocationChanged", "onLocationChanged");
                    Log.e("location", "[" + location.getProvider() + "] (" + location.getLatitude() + "," + location.getLongitude() + ")");
                    GeoLib.getInstance().setLastKnownLocation(getContext());    // 사용자 위치정보를 마지막에 업데이트한 곳으로 현재 위치를 지정합니다.
                    lati=location.getLatitude();                // 사용자의 위도와 경도를 업데이트 시켜줍니다.
                    logi=location.getLongitude();
                    getfoodList();
                    //onResume();
                    locationManager.removeUpdates(locationListener);    // 그 후 업데이트를 종료합니다.
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.e("onStatusChanged", "onStatusChanged");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.e("onProviderEnabled", "onProviderEnabled");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.e("onProviderDisabled", "onProviderDisabled");
                }
            };
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (String name : m_lstProviders) {
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            return;
                        }
                        locationManager.requestLocationUpdates(name, 100000, 0, locationListener); //십분에 한 번씩 사용자의 위치를 업데이트한다.
                    }

                }
            });
        } else {                                                //인터넷이 연결 안됐을 경우
            Log.e("GPS Enable", "false");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),"앱을 사용하기 위해서는 위치 서비스가 필요합니다.",Toast.LENGTH_SHORT).show();
                    Intent callGPSSettingIntent         // 위치 서비스를 킬 수 있도록 위치 기반을 키는 화면을 불러옵니다.
                            = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, 200);

                }
            });
        }

    }
    //서버에서 맛집의 데이터를 가져오는 메소드입니다.
    public void getfoodList() {
        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨 후 유저의 아이디와 위도, 경도,
        Call<List<Tasteitem>> call = apiInterface.getFood(userId,lati,logi,sort);             //카테고리를 결정한 값을 서버에 보내줍니다.
        call.enqueue(new Callback<List<Tasteitem>>() {                                        //서버와 연결하고 나서 받아온 결과입니다.
            @Override
            public void onResponse(Call<List<Tasteitem>> call, Response<List<Tasteitem>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우. 오류 알림창을 띄웁니다.
                    Toast.makeText(getActivity(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                    foodList.clear();                                   // 맛집 데이터를 넣을 리스트를 비워줍니다. 데이터가 중복되지 않도록,
                    foodadapter.clear();                                // 어댑터에 추가되었던 맛집데이터들도 비워줍니다.

                    for (Tasteitem taste : response.body()) {           // 받아온 맛집의 갯수만큼 리스트에 넣어줍니다.
                        foodList.add(taste);
                        Log.i("RESPONSE: ", "" + taste.toString());
                    }
                }
                foodadapter.notifyDataSetChanged();                     // 리스트에 추가된 데이터가 어댑터에 추가되었으니,
            }                                                           // 어댑터를 새로고침해서 리싸이클뷰가 변경된 것을 유저에게 보여줍니다.

            @Override
            public void onFailure(Call<List<Tasteitem>> call, Throwable t) {        //서버와 연결 실패 할 경우
               if (progressDoalog.isShowing())
                progressDoalog.dismiss();
                Toast.makeText(getActivity(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }

/**
* 격자의 항목 사이에 간격을 추가합니다. 의미, 외부에 여백이 추가되지 않습니다.
* 모서리의 가장자리.
*/
    public class GridSpacingdecoration extends RecyclerView.ItemDecoration {
        private int span;
        private int space;
        private boolean include;

        public GridSpacingdecoration(int span,int space, boolean include){
            this.span = span;
            this.space = space;
            this.include = include;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int posion =parent.getChildAdapterPosition(view);
            int column = posion % span;

            if(include){                //true일 때 리싸이클뷰의 가장자리를 만들어줍니다. ex) 리사이클뷰의 위와 양쪽의 여백을 만들어준다
                outRect.left = space -column * space / span;
                outRect.right = (column + 1)* space / span;

                if(posion<span){
                    outRect.top = space;
                }
                outRect.bottom = space;
            } else {                     //flase일 때 리싸이클뷰의 가장자리를 없애줍니다. ex) 리사이클뷰의 위와 양쪽의 여백을 만들지 않는다
                outRect.left = column * space / span;
                outRect.right = space - (column + 1) * space / span;
                if(posion>=span){
                    outRect.top = space;
                }
            }
        }
    }

    private int dpToPx(int dp){             // DP 를 픽셀로 변환하는 메소드. 기기마다 핸드폰 해상도가 다르고 화면 크기가 다르기에
        Resources r = getResources();       // 다른 기기들도 화면의 비율을 맞춰서 보여주기 위한 것이다.
        return  Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics()));
    }


}
