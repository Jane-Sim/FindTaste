package com.example.seyoung.findtaste.views.AddTasteInfor;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.PlaceArrayAdapter;
import com.example.seyoung.findtaste.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

/**
 * Created by seyoung on 2017-10-23.
 * 맛집을 지도에서 찾을 수 있는 화면입니다.
 * 검색창에서 맛집이름이나 주소로 맛집을 찾을 수 있으며,
 * 현재위치에서 주변에 있는 맛집을 찾을 수 있습니다.
 * 검색한 목록 중 하나를 누르거나,내 위치의 주변에서 찾기 버튼을 누르면
 * 해당되는 맛집의 마커가 지도에 생깁니다.
 * 이 때, 생성된 마커를 누를 경우, 해당 맛집의 위치를 받아옵니다.
 * 사용자가 원하는 맛집을 결정 후 선택버튼을 누르면 다시
 * 맛집 등록화면으로 돌아가게 됩니다.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener,PlacesListener{
    private GoogleApiClient mGoogleApiClient = null;        //googleplaces를 사용하기 위한 구글클라이언트입니다. 구글맵에서 사용자의 현재위치를 알 수 있습니다.
    private GoogleMap mGoogleMap = null;            // 사용자에게 보여 줄 구글맵입니다.
    private Marker currentMarker = null;            //구글맵에서 맛집의 위치를 나타낼 마커입니다.
    InputMethodManager imm;                         //키보드를 관리하는 매니저를 갖고옵니다. 검색창에 나타난 결과를 누를 때 키보드를 숨기게 만들 매니저.
    private static final String TAG = "googlemap";
    String Title="",Adress="",lati="",logi="";      // 선택한 맛집 마커의 정보를 받아 올 맛집이름과 주소, 위도 경도입니다.
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView mAutocompleteTextView; //검색창에 맛집이름이나 주소를 입력하면 자동완성으로 보여주는 텍스트창입니다.
    private TextView mNameView;                         //선택한 맛집의 주소와 이름을 표시할 텍스트입니다.
    private PlaceArrayAdapter mPlaceArrayAdapter;       // 검색창에 입력한 값에 해당되는 데이터를 검색창의 리스트뷰와 연결 시켜줄 어댑터입니다.
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;    //사용자가 gps를 키고 왔는지 확인해주는 값입니다.
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;   //사용자가 위치에 대한 권한을 설정해줬는지 확인하는값입니다.
    private static final int UPDATE_INTERVAL_MS = 5000;  // 5초마다 사용자의 위치를 업데이트 시켜주빈다.
    private static final int FASTEST_UPDATE_INTERVAL_MS = 5000; // 5초 이내로는 위치를 업데이트 시키지 않게 합니다.
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    LatLng la;

    private AppCompatActivity mActivity;            // 현재 액티비티
    boolean askPermissionOnceAgain = false;         //
    boolean mRequestingLocationUpdates = false;     // 화면으로 돌아왔을 때 사용자의 위치를 다시 업데이트 시켜준다
    Location mCurrentLocatiion;                     // 사용자의 위치를 받아왔을 때 사
    boolean mMoveMapByUser = true;// 사용자의 현재 위치에 맞춰 맵을 이동시킨다. false일 경우 카메라 이동을 중지시킨다(구글맵 자동 이동 비활성)
    boolean mMoveMapByAPI = true;                   // 맵이 움직일 때와 맵이 움직이지 않을 때를 구별할 값이다.

    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)//배터리소모를 고려하지 않으며 정확도를 최우선으로 고려
            .setInterval(UPDATE_INTERVAL_MS) //위치가 update되는 주기
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS); //위치 획득 후 update되는 주기
    LatLng currentPosition;//현재 사용자의 위도와 경도
    List<Marker> previous_marker = null;        //주변 맛집들의 마커를 담아 관리할 리스트다
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(          //만약 한국으로 지정하면 검색한 값을 한국에서 찾을 수 있다.
            new LatLng(30.1391277, 115.0607471), new LatLng(48.940448, 139.7803655));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapactivity);


        mAutocompleteTextView = findViewById(R.id.autoCompleteTextView);
        mAutocompleteTextView.setThreshold(2);          //검색창에 최소 두글자를 써야 검색리스트가 뜬다
        RelativeLayout ll = findViewById(R.id.ll);
        ll.setOnClickListener(myClickListener);         //화면을 누를 경우, 키보드가 내려갑니다
        mNameView = findViewById(R.id.name);            //마커를 누를 경우, 주소와 이름을 표시할 텍스트뷰입니다.
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  //검색창의 리스트중 하나를 눌렀을 때 키보드를 숨기게 해주는 매니저
        ImageView clear = findViewById(R.id.clear);                      // 검색한 값을 한번에 지울 수 있도록 하는 엑스이미지

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAutocompleteTextView.setText("");  //적었던 에딧텍스트를 지워준다
            }
        });

        Log.d(TAG, "onCreate");
        mActivity = this;
        mGoogleApiClient = new GoogleApiClient.Builder(this) //구글 플레이스를 사용하기 위해 현재 화면에서 설정해준다
                .addConnectionCallbacks(this)   //연결될 때 이벤트와
                .addOnConnectionFailedListener(this)    // 연결이 실패될 때의 이벤트를 여기에 지정해준다
                .addApi(LocationServices.API)   //위치 api와
                .addApi(Places.GEO_DATA_API)    //위치의 위도경도를 주소로 바꿔주는 지오코딩
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID,this)
                .build();

        previous_marker = new ArrayList<Marker>();  //마커를 저장할 수 있는 리스트를 불러온다

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {  //주변의 맛집을 불러오는 버튼입니다.
            @Override
            public void onClick(View v) {
                hideKeyboard();         //키보드를 숨겨준다
                if (currentMarker != null) currentMarker.remove(); //만약 마커가 추가되어있다면 마커를 제거해준다
                mGoogleMap.clear();//지도 클리어         //지도에 그려진 모든것도 삭제해준다
                mMoveMapByAPI = true;                   //구글맵과 연결되어야 구글지도에 저장되어 있는 맛집리스트를 가져올 수 있다.
                if(PLACE_AUTOCOMPLETE_REQUEST_CODE==3){
                    showPlaceInformation(la);           //현재 위치에서 주변 맛집을 찾는 것이 아닌, 사용자가 검색한 맛집의 위치에서 주변맛집을 검색한다
                } else
                showPlaceInformation(currentPosition);  //현재위치에서 주변에 맛집을 불러온다
            }
        });
        Button button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {            //마커를 선택한 후 맛집선택 버튼을 누르면 마커의 이름 주소 위도경도를 가지고 맛집 등록 화면으로 돌아가게 만든다
                if(!Title.equals("")||!Adress.equals("")) {
                    Intent intent = new Intent();
                    intent.putExtra("title", Title);
                    intent.putExtra("adress", Adress);
                    intent.putExtra("lati", lati);
                    intent.putExtra("logi", logi);
                    Log.e("lati",lati);
                    Log.e("logii",logi);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else Toast.makeText(MapActivity.this, "마커의 주소를 지정해주세요", Toast.LENGTH_SHORT).show(); //아무 마커도 선택하지 않았을 경우
            }
        });
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);                        //현재 프래그먼트에 구글맵을 지정해준다
        mapFragment.getMapAsync(this);

        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);           // 자동완성 텍스트에 서버에서 받아온 데이터를 심플 리스트와 연결해서
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1,   //검색창 밑에 맛집 정보를 리스트로 보여준다
                BOUNDS_MOUNTAIN_VIEW, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);



    }

    @Override

    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, "onResume : call startLocationUpdates");
            if (!mRequestingLocationUpdates) startLocationUpdates();
        }                           // 앱이 다시 시작 될 때, 사용자의 위치를 업데이트 시켜준다.

        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;
                checkPermissions();                 //
            }
        }
    }

    private void startLocationUpdates() {           //사용자 위치 업데이트
        //사용자가 gps나 인터넷을 안 켰을 경우, 위치 기반 화면을 불러온다
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else { //사용자가 권한 설정을 안해줬을 경우.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }
            // 권한이 있을 경우 구글맵에 내 위치를 나타낸다.
            Log.d(TAG, "startLocationUpdates : call FusedLocationApi.requestLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mRequestingLocationUpdates = true;
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    //현재 화면이 멈출 경우, 위치 업데이트를 안해준다.
    private void stopLocationUpdates() {
        Log.d(TAG,"stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }

    //구글맵이 준비 되었을 때
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");
        mGoogleMap = googleMap;
        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();
        //내 위치 버튼과 확대 축소 버튼을 지정해준다
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //내 위치 버튼을 눌렀을 때
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){

            @Override
            public boolean onMyLocationButtonClick() {

                Log.d( TAG, "onMyLocationButtonClick : 위치에 따른 카메라 이동 활성화");
                //키보드를 숨겨준다
                hideKeyboard();
                //사용자의 위치를 계속 업데이트 시킨다
                mRequestingLocationUpdates=true;
                //사용자의 현재 위치에 맞춰 구글맵을 이동시켜준다
                mMoveMapByAPI = true;
                //만약 주변위치 버튼을 누를 경우 내 위치에서 맛집을 검색하게 만든다
                PLACE_AUTOCOMPLETE_REQUEST_CODE=1;
                return false;
            }

        });
        // 구글맵에 떠있는 마커를 눌렀을 때, 클릭한 마커의 정보를 가져온다
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                hideKeyboard();
                Title = marker.getTitle();
                Adress = marker.getSnippet();
                lati= String.valueOf(marker.getPosition().latitude);
                logi= String.valueOf(marker.getPosition().longitude);
                mNameView.setText(Html.fromHtml( marker.getSnippet()+"\n"+marker.getTitle()));
                Toast.makeText(getApplicationContext(),"선택한 "+Title+" 맛집입니다.",Toast.LENGTH_SHORT).show();
                // Determine what marker is clicked by using the argument passed in
                // for example, marker.getTitle() or marker.getSnippet().
                // Code here for navigating to fragment activity.
                    return false;
                }
        });
        // 구글맵을 눌렀을 때
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                Log.d( TAG, "onMapClick :");
            }

        });

        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            //구글맵이 이동될 경우, 주변맛집 불러오기 버튼을 누르면 현재 보이는 구글맵에서 맛집들의 마커를 추가해준다
            @Override
            public void onCameraMoveStarted(int i) {

                if (mMoveMapByUser == true && mRequestingLocationUpdates){
                    Log.d(TAG, "onCameraMove : 위치에 따른 카메라 이동 비활성화");

                }
                mMoveMapByUser = true;
            }
        });

        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                mMoveMapByAPI = false;
                double latitude = mGoogleMap.getCameraPosition().target.latitude;
                double longitude = mGoogleMap.getCameraPosition().target.longitude;
                la = new LatLng(latitude,longitude);
                currentPosition = la;
            }
        });
    }

    //계속해서 위치가 업데이트 될 경우,
    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG, "onLocationChanged : ");
        //업데이트된 위치에 마커를 추가한다. 현재 위치에 생긴 마커를 클릭 시, 주소를 반환시켜준다.
        String markerTitle = "";
                //"위도:" + String.valueOf(location.getLatitude())
                //+ " 경도:" + String.valueOf(location.getLongitude());
        String markerSnippet = getCurrentAddress(location); //지오코딩을 해서 현재 위치를 주소로 바꿔준다

        //현재 위치에 마커 생성하고 이동
        if(mRequestingLocationUpdates==true) {
            setCurrentLocation(location, markerTitle, markerSnippet);
        }
        // 사용자가 주변맛집불러오기 버튼을 누를 때, 업데이트된 위치에서 불러올 수 있도록 해줌.
        currentPosition
                = new LatLng( location.getLatitude(), location.getLongitude());
        mCurrentLocatiion = location;

    }
    //화면이 시작될 때 구글api연결이 안되어 있으면 연결시켜준다
    @Override
    protected void onStart() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() == false){
            Log.d(TAG, "onStart: mGoogleApiClient connect");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    // 현재 화면이 멈출 때,
    @Override
    protected void onStop() {
        //사용자의 위취 업데이트를 멈춘다
        if (mRequestingLocationUpdates) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }
        // 구글 api와의 연결도 끊는다
        if ( mGoogleApiClient.isConnected()) {
            Log.d(TAG, "onStop : mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    //구글맵과 연결했을 때 발생하는 이벤트
    @Override
    public void onConnected(Bundle connectionHint) {
        //업데이트가 안될 때
        if (!mRequestingLocationUpdates) {
            //기기가 마쉬멜로우 버전 이상일 때
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //권한을
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                    //위치 권한을 가지고 있지 않을 경우 다시 권한을 설정해달라고 묻는다
                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                } else { //권한을 벌써 줬을 경우, 위치를 업데이트 시키고 구글맵을 불러온다
                    Log.d(TAG, "onConnected : 퍼미션 가지고 있음");
                    Log.d(TAG, "onConnected : call startLocationUpdates");
                    startLocationUpdates();
                    mGoogleMap.setMyLocationEnabled(true);
                }


            }else{  //다시 업데이트를 시켜준다
                Log.d(TAG, "onConnected : call startLocationUpdates");
                startLocationUpdates();
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
        // 연결된 구글 api를 검색창으로 사용 할 수 있게 검색어댑터에 설정해준다
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(TAG, "Google Places API connected.");
    }


    @Override

    public void onConnectionSuspended(int cause) {

        Log.d(TAG, "onConnectionSuspended");
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
        onStart();
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(TAG, "Google Places API connection suspended.");
    }


    //구글api와 연결이 실패할 경우
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        //현재 구글맵 위치와 마커를 서울역에 지정한다
        setDefaultLocation();

       /* Log.e(TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();*/
    }


    public String getCurrentAddress(Location location) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        //받아온 주소값이 비거나 0일 경우
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }
    //현재 화면에서 gps와 인터넷 상태를 알려준다
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //현재 사용자의 위치를 받아온 경우, 현재위치에 마커를 추가한다
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        mMoveMapByUser = false;

        if ( mMoveMapByAPI==true ) {
        LatLng currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
        //구글맵의 디폴트 현재 위치는 파란색 동그라미로 표시
        //마커를 원하는 이미지로 변경하여 현재 위치 표시하도록 수정해야함.
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

            Log.d( TAG, "setCurrentLocation :  mGoogleMap moveCamera "
                    + location.getLatitude() + " " + location.getLongitude() ) ;
            //마커의 위치로 맵을 이동시켜준다
             CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
            // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    //권한을 안줬거나, 사용자의 위치를 못 받아왔을 때, 서울에 마커를 추가하고 구글맵을 이동시킨다
    public void setDefaultLocation() {
        mMoveMapByUser = false;
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";
        if (currentMarker != null) currentMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);
        currentPosition = DEFAULT_LOCATION;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }

//여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        //사용자가 위치 권한을 설정해줬을 때
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

    //권한 설정을 해줬는지 확인하는 값
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {
            //위치 권한을 허락 했을 때, 구글api와 연결시킨다.
            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionAccepted) {
                //구글api와 비연결일 때 다시 연결시켜준다
                if ( !mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    mGoogleApiClient.connect();
                }

            } else {
                //권한이 없을 때 다시 위치 권한을 설정해달라는 다이얼로그를 띄운다
                checkPermissions();
            }
        }
    }

    //사용자가 권한설정을 안해줬을 경우 권한 설정을 해 주겠냐는 알람창을 띄운다
    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });
        //사용자가 권한 설정을 거부했을 때, 어플을 종료시킨다
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    //위치기반 설정을 하러가도록 화면을 띄워준다.
    private void showDialogForPermissionSetting(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                askPermissionOnceAgain = true;
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
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



    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    // 구글에서 현재 위치에서 맛집 데이터를 받아온 경우,
    @Override
    public void onPlacesSuccess(final List<Place> places) {
        runOnUiThread(new Runnable() {

                @Override
                public void run () {
                    if(mMoveMapByAPI == true)
                    {
                for (noman.googleplaces.Place place : places) {
                    // 해당 맛집의 위도와 경도를 가져와서
                    LatLng latLng
                            = new LatLng(place.getLatitude()
                            , place.getLongitude());
                    //가져온 맛집 데이터를 마커에 지정해준다.
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.getIcon();
                    markerOptions.title(place.getName());
                    markerOptions.snippet(place.getVicinity());
                    Marker item = mGoogleMap.addMarker(markerOptions);
                    previous_marker.add(item);
                 //   Log.e(TAG, place.getName() + "");
                   // Log.e(TAG, place.getVicinity() + "");
                   // Toast.makeText(getApplicationContext(), place.getName(), Toast.LENGTH_SHORT).show();
                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                //다시 구글맵에 마커를 추가한다
                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }
            }
        });
    }

    // 구글api에 주변 맛집의 정보를 달라고 요청한다
    public void showPlaceInformation(LatLng location)
    {
        mGoogleMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(MapActivity.this)
                .key("AIzaSyDVD8E0DgHoiZeOKvRv1i3CiJykrvy1t0Y")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) //음식점
                .language("ko", "KR")
                .build()
                .execute();
    }
    @Override
    public void onPlacesFinished() {

    }

    //사용자가 검색창에서 나열된 맛집리스트중 하나를 눌렀을 때
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Selected: " + item.description);
            //해당 맛집의 이름의 마커를 추가하는 콜백을 실행합니다.
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    // 해당 맛집의 콜백입니다.
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            //해당 맛집의 정보르 못 갖고올 때
            if (!places.getStatus().isSuccess()) {
                Log.e(TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                //해당 맛집의 오류를 로그로 찍는다
                return;
            }
            // Selecting the first object buffer.
            //선택한 맛집을 가져온다
            final com.google.android.gms.location.places.Place place = places.get(0);
            CharSequence attributions = places.getAttributions();

            //mNameView.setText(Html.fromHtml(place.getAddress() + "\n"+place.getName()));
            //mNameView.setText(Html.fromHtml(place.getLatLng() + ""));
            Log.e(TAG, place.getName() + "");
            Log.e(TAG, place.getLatLng().latitude + "");
            Log.e(TAG, place.getLatLng().longitude + "");
            //그리고 키보드를 내린다
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mAutocompleteTextView.getWindowToken(), 0);
            //해당 맛집의 마커를 구글맵에 추가시킨다.
            setsearchLocation(place.getLatLng(),place.getName()+"",place.getAddress()+"");
            mMoveMapByAPI = false;
        }
    };
    //현재 화면의 아무곳이나 누를 경우 키보드를 내린다
    View.OnClickListener myClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            hideKeyboard();
        }
    };
    //마커를 누르는 등의 행동을 할 때 키보드를 숨긴다.
    private void hideKeyboard()
    {
        imm.hideSoftInputFromWindow(mAutocompleteTextView.getWindowToken(), 0);
    }

    public String gettext(String string){                                           // 두번째 부터 마지막까지 스트링을 반영
        string = string.substring((string.length())-(string.length()-5),string.length());
        return string;
    }
    //내가 검색으로 찾고차 하는 맛집을 마커로 추가시키고 지도를 마커의 위치로 옮긴다.
    public void setsearchLocation(LatLng location, String markerTitle1, String markerSnippet1) {
        la=location;
        mMoveMapByUser = false;
        mMoveMapByAPI = false;

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(location.latitude, location.longitude);
        String markerTitle = markerTitle1;
        //String markerSnippet = markerSnippet1;
        String markerSnippet = gettext(markerSnippet1);
        mGoogleMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어
        //현재 위치의 마커도 삭제한다
        if (currentMarker != null) currentMarker.remove();

        //마커의 정보를 설정해 준 뒤,
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);
        //구글맵에 마커를 추가하고 화면을 이동시켜준다
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);
        mRequestingLocationUpdates=false;
        PLACE_AUTOCOMPLETE_REQUEST_CODE=3;
    }
}
