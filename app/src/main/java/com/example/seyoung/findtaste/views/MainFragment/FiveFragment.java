package com.example.seyoung.findtaste.views.MainFragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.views.ChatView.MyService;
import com.example.seyoung.findtaste.views.FiveFgChilds.Child2Fragment;
import com.example.seyoung.findtaste.views.FiveFgChilds.ChildFragment;
import com.example.seyoung.findtaste.views.LoginNRegister.IndexActivity;
import com.facebook.login.LoginManager;

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

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by seyoung on 2017-10-14.
 * 사용자의 정보를 수정할 수 있도록 만들었습니다.
 * 사용자가 즐겨찾기한 맛집들과 사용자가 적은 리뷰 목록을 볼 수 있으며
 * 사용자가 로그아웃을 하거나 자신의 정보를 수정할 수 있는 화면입니다.
 */

public class FiveFragment extends Fragment implements View.OnClickListener {
    private static String TAG = "phpquerytest";         // json으로 유저의 정보를 담을 변수들입니다.
    private static final String TAG_JSON="webnautes";   // json이름을 설정해줍니다.
    private static final String TAG_NAME = "user_name"; // 설정한 json에서 가져올 변수 이름들입니다.
    private static final String TAG_PHONE = "user_phone";
    private static final String TAG_EMAIL = "user_email";
    private static final String TAG_IMAGE ="user_image";
    private static final String TAG_IMAGE_NAME ="user_image_name";
    public RequestManager mGlideRequestManager;

    ArrayList<HashMap<String, String>> mArrayList;  //서버에 받아온 값을 해쉬맵에 저장해서 리스트에 담는 리스트값입니다.
    TextView pname ;                                //사용자의 이름을 보여줄 텍스스트뷰 입니다.
    ImageView pimage;                               //사용자의 프로필 사진을 보여줄 이미지뷰입니다.
    Button logout;                                  //사용자가 로그아웃을 할 수 있게 해주는 버튼입니다.

    String mJsonString;                             //json array로 받아 올 수 있도록 해주는 오브젝트값입니다.
    String userId;                                  //받아온 오브젝트의 유저 데이터를 담을 스트링들 입니다.`
    String user_name ;
    String user_phone;
    String user_email;
    String user_image;
    String user_image_name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fivefragment,container,false);
        pname =  view.findViewById(R.id.p_name);            //사용자의 이름
        pimage = view.findViewById(R.id.p_image);           //사용자의 사진
        logout = view.findViewById(R.id.logout);            //로그아웃 버튼
        mGlideRequestManager = Glide.with(this);

        pimage.setOnClickListener(this);                    //사진의 이미지를 눌렀을 때의 리스너입니다.
        FragmentManager childFragMan = getChildFragmentManager();
        FragmentTransaction childFragTrans = childFragMan.beginTransaction();
        ChildFragment fragB = new ChildFragment();          //사용자의 즐겨찾기 목록을 가져옵니다.
        childFragTrans.add(R.id.FRAGMENT_PLACEHOLDER, fragB);   //현재 화면의 프래그먼트에 즐겨찾기 프래그먼트를 넣어줍니다.
        childFragTrans.addToBackStack("B");
        childFragTrans.commit();                            //즐겨찾기 프래그먼트로 최종결정을 합니다.

        final SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        userId= pref.getString("ing", "s");           //서버에 사용자의 아이디를 보내기 위해, 기기에서 아이디값을 가져옵니다.

        //로그아웃 버튼을 누를 때
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();    //페이스북으로 로그인했다면 로그아웃시켜줍니다.
                SharedPreferences.Editor editor = pref.edit();  //로그아웃을 했으니 자동로그인이 되지 않도록
                editor.remove("ing");                           //기기에 저장된 아이다값을 지워줍니다.
                editor.apply();

                Intent inten = new Intent(getActivity(), MyService.class);
                getActivity().stopService(inten);

                Intent intent = new Intent(getActivity(), IndexActivity.class); //다시 로그인부터 시작할 수 있도록
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);         //스플릿 화면으로 이동시킵니다.
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });
        ImageButton bt_fiveFragment = view.findViewById(R.id.imageButton);      // 즐겨찾기 목록을 불러오는 버튼과
        bt_fiveFragment.setOnClickListener(this);
        ImageButton bt_fiveFragment2 =view.findViewById(R.id.imageButton2);     // 리뷰 목록을 불러오는 버튼입니다.
        bt_fiveFragment2.setOnClickListener(this);


        GetData task = new GetData();                                           // 유저의 프로필 값을 가져옵니다.
        task.execute(userId);
        mArrayList = new ArrayList<>();                                         //서버로 값을 보내주고 받는 클래스를 시작합니다.

        return view;
    }




    @Override
    public void onResume() {                     //다시 화면으로 돌아왔을 경우에,
        super.onResume();                        //사용자가 프로필을 바꾸고 온 것이라면
        GetData task = new GetData();            //바꾼값으로 볼 수 있도록 다시 사용자의 데이터를 불러옵니다.
        task.execute(userId);

        pimage.setFocusableInTouchMode(true);   //이 때 화면의 포커스가 프래그먼트의 리싸이클뷰로 가기 때문에
        pimage.requestFocus();                  //사용자의 사진으로 포커스를 맞춰서 아래로 내려가지 않도록 해줍니다.
    }                                           //나중에 화면에서 돌아오는 값으로 다시 리싸이클뷰로 포커스를 가게 해 주면된다.

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onClick(View view) {
        Fragment fg;
        switch (view.getId()) {
            case R.id.imageButton:              // 즐겨찾기 버튼을 눌렀을 때
                fg = ChildFragment.newInstance();//즐겨찾기 프래그먼트로 바꿔줍니다.
                setChildFragment(fg);
                break;
            case R.id.imageButton2:             // 리뷰 모아보기 버튼을 눌렀을 때
                fg = Child2Fragment.newInstance();//리뷰 프래그먼트로 바꿔줍니다.
                setChildFragment(fg);
                break;
            case R.id.p_image:                  // 프로필 수정 화면으로 넘어갑니다.
/*                Intent intent = new Intent(getContext(), Activity_ProfileFix.class);
                intent.putExtra("name",user_name);
                intent.putExtra("email",user_email);
                intent.putExtra("phone",user_phone);
                intent.putExtra("image",user_image);
                intent.putExtra("imagename",user_image_name);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);          //유저의 정보들을 가지고 화면 이동*/
                Intent intent = new Intent(getContext(), opencv.class);
                startActivity(intent);
                break;
        }
    }

    private void setChildFragment(Fragment child) {         // 프래그먼트안에 프래그먼트를 넣을 때 쓰는 메소드입니다.
        FragmentTransaction childFt = getChildFragmentManager().beginTransaction();

        if (!child.isAdded()) {             // 프래그먼트가 추가가 안되어있을 경우 추가해줍니다.
            childFt.replace(R.id.FRAGMENT_PLACEHOLDER, child);
            childFt.addToBackStack(null);
            childFt.commit();               //그리고 프래그먼트를 최종결정합니다.
        }
    }

    //사용자의 정보를 서버에서 불러오는 클래스입니다.
    @SuppressLint("StaticFieldLeak")
    private class GetData extends AsyncTask<String, Void, String> {
    ProgressDialog progressDialog;          //서버에 보내고 받아올 동안 띄울 다이얼로그입니다.
    String errorString = null;              //에러가 날 경우에 에러값을 받을 변수입니다.

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(getActivity(),     //받아오는 동안 다이얼로그를 띄우고
                "Please Wait", null, true, true);
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        progressDialog.dismiss();                               //받아온 경우 다이얼로그를 닫아줍니다.

        Log.d(TAG, "response - " + result);
        if (result != null){                    //값을 받아왔을 때
            mJsonString = result;   //json어레이로  받아오는 메소드를 실행합니다.
            showResult();
        }
    }

    @Override

    protected String doInBackground(String... params) {
        String serverURL = "http://findtaste.vps.phps.kr/user_signup/profile_query.php";
        String postParameters = "user_id=" + userId;            //서버의 주소로 사용자의 아이디를 보내줍니다.
        try {

            URL url = new URL(serverURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
/// 안드로이드 -> 서버로 값을 전달
            OutputStream outputStream = httpURLConnection.getOutputStream();        //인코딩 후 서버로 값을 전달합니다.
            outputStream.write(postParameters.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();                                                   //전달 후 스트림을 꼭 닫아줍니다.

            int responseStatusCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "response code - " + responseStatusCode);
            InputStream inputStream;

            if(responseStatusCode == HttpURLConnection.HTTP_OK) {                   //
                inputStream = httpURLConnection.getInputStream();
            }
            else{
                inputStream = httpURLConnection.getErrorStream();
            }
/// 서버에서 안드로이들 결과값을 전달
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8"); //서버에서 받아온 값들을 받아옵니다.
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
            errorString = e.toString();     //에러값을 넣어줍니다.
            return null;
        }
    }
}

    //사용자의 데이터를 서버에서 받아왔을 때의 메소드입니다.
    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);    //받아온 json에서 하나하나 씩 값을 받아 저장합니다.

            for(int i=0;i<jsonArray.length();i++){                      //받아온 array개수만큼 해시맵에 저장시킵니다.
                JSONObject item = jsonArray.getJSONObject(i);
                user_name = item.getString(TAG_NAME);                   //이름 텍스트뷰에 유저 이름을 넣어줍니다.
                user_phone= item.getString(TAG_PHONE);                  //
                user_email= item.getString(TAG_EMAIL);                  //
                user_image = item.getString(TAG_IMAGE);                 //
                user_image_name = item.getString(TAG_IMAGE_NAME);       //

                HashMap<String,String> hashMap = new HashMap<>();       //사용자의 정보를 해쉬맵에 넣어서
                hashMap.put(TAG_NAME, user_name);
                hashMap.put(TAG_PHONE, user_phone);
                hashMap.put(TAG_EMAIL, user_email);
                hashMap.put(TAG_IMAGE, user_image);
                hashMap.put(TAG_IMAGE_NAME, user_image_name);

                mArrayList.add(hashMap);                //사용자의 데이터를 담은 해쉬맵을 리스트에 넣습니다.

                pname.setText(user_name);               // 사용자의 이름텍스트에 이름값을 넣습니다.

                mGlideRequestManager.load(user_image)
                        .apply(new RequestOptions()
                                .error(R.drawable.fbnull)
                                .override(300,300)
                                .fitCenter()
                                .centerCrop()
                                .circleCrop()
                        )
                        .into(pimage);
            }

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}
