package com.example.seyoung.findtaste.views.opengl;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.seyoung.findtaste.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by seyoung on 2018-01-09.
 * opencv에서 찍은 사진을 가져와 원하는 색상 필터를 적용시켜주는 액티비티입니다.
 * 실시간으로 필터를 바꿔주며, 사용자가 원하는 필터에서 사진을 저장하면,
 * 해당 사진을 서버에 보내준 후 현재 유저의 프로필사진을 변경시켜줍니다.
 */

@SuppressLint("Registered")
public class ActivityFilter extends AppCompatActivity implements View.OnClickListener{
    GLSurfaceView mView;
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }
  //  ImageView imageVIewInput;       //사진을 보여줄
    ImageView 뽀샤시,winter,toon, gray;    //필터를 적용한 미리보기 뷰이며, 클릭하면 해당 필터를 적용시켜줍니다.
    ImageView original;     //아무 필터도 적용하지 않은 뷰.
    Button save;            //해당 필터사진을 서버에 보내 줄 저장버튼

    private static final String TAG = "opencv";
    static final int PERMISSION_REQUEST_CODE = 1;       //사진 권한이 있는지 확인
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};
    String 사진경로,userId;     // opencv의 사진 경로

    @SuppressLint("WrongConstant")
    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }

        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //사용자가 권한설정을 거부했을 때 다이얼로그를 띄우며
                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }else
                        {   //권한을 허용하면opengl을 실행합니다.
                            //read_image_file();
                            imageprocess_and_showResult();
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        mView = findViewById(R.id.Surfaceview);
        //얼굴인식으로 찍은 사진을 가져옵니다.
        Intent intent = getIntent();
        사진경로 = intent.getStringExtra("사진경로");

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        userId= pref.getString("ing", "s");

        //이미지뷰들을 액티비티와 연결시킵니다.
        original = findViewById(R.id.original);
        뽀샤시 = findViewById(R.id.뽀샤시);
        winter = findViewById(R.id.winter);
        toon = findViewById(R.id.toon);
        winter = findViewById(R.id.winter);
        gray = findViewById(R.id.gray);
        save = findViewById(R.id.save);
        //클릭 리스너를 지정합니다.
        original.setOnClickListener(this);
        뽀샤시.setOnClickListener(this);
        winter.setOnClickListener(this);
        toon.setOnClickListener(this);
        winter.setOnClickListener(this);
        gray.setOnClickListener(this);
        save.setOnClickListener(this);

        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        } else {
            //이미 사용자에게 퍼미션 허가를 받음.
            //read_image_file();
            imageprocess_and_showResult();
        }
    }
        //해당 뷰에 opengl을 불러와 도형을 그린 후 사진을 입힙니다.
    private void imageprocess_and_showResult() {
        int filter =0;
        mView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mView.setEGLContextClientVersion(2);
        mView.setRenderer(new GLLayer(this,사진경로,filter));
        mView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

    }

    @Override
    public void onResume() {
        super.onResume();
        mView.onResume();

    }
    protected void onPause() {
        super.onPause();
        mView.onPause();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //original 이미지뷰를 누르면 원본 사진을 가져옵니다.
            case R.id.original:
                int filter =0;
                GLLayer.shader_selection =filter;
                break;
            case R.id.뽀샤시:      // 엠보싱 이미지뷰를 누르면 원본사진에 엠보싱 효과를 넣어줍니다.
                filter =3;
                GLLayer.shader_selection =filter;
                break;
            case R.id.winter:      //원본 사진에 살짝 어두운 명암을 넣습니다.
                filter =8;
                GLLayer.shader_selection =filter;
                break;
            case R.id.toon:         //물감으로 칠한 듯한 효과를 줍니다
                filter =9;
                GLLayer.shader_selection =filter;
                break;
            case R.id.gray:         //회색으로 이미지를 만들어줍니다.
                filter =7;
                GLLayer.shader_selection =filter;
                break;
                //저장하기를 눌렀을 때,
            case R.id.save:
                //gl에서 비트맵을 가져왔을 때, 빈 값이면 로그로 남겨줍니다.
                if(GLLayer.returebm()==null) {
                    Log.e("비트맵아 널","");
                }else { //비트맵이 있을 경우 현재 시간으로 서버에 사진을 저장.
                    Log.e("비트맵이 낫 널","");
                    Long tsLong = System.currentTimeMillis();
                    String timestamp = tsLong.toString();
                    //서버에 해당 사진 비트맵과 현재시간으로 이미지를 저장하게 합니다
                    new Upload(GLLayer.returebm(), "IMG_" + timestamp).execute();
                }
                break;

        }
    }
    //사용자가 추가한 파라미터를 서버에서 받을 수 있게 인코딩시킵니다.
    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    //서버에 이미지 비트맵과 이름, 유저 아이디를 보냅니다.
    // 해당 유저의 프로필 경로를 바꾸고 이미지를 서버에 추가합니다.
    @SuppressLint("StaticFieldLeak")
    private class Upload extends AsyncTask<Void,Void,String> {
        private Bitmap image;
        private String name;
        public String SERVER = "http://findtaste.vps.phps.kr/user_signup/saveImage.php";
        public Upload(Bitmap image,String name){
            this.image = image;
            this.name = name;
        }

        @Override
        protected String doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            //compress the image to jpg format
            image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            /*
            * encode image to base64 so that it can be picked by saveImage.php file
            * */

            String encodeImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);

            //generate hashMap to store encodedImage and the name

            HashMap<String,String> detail = new HashMap<>();
            detail.put("name", name);
            detail.put("image", encodeImage);
            detail.put("username", userId);

            try{
                //convert this HashMap to encodedUrl to send to php file

                String dataToSend = hashMapToUrl(detail);
                //make a Http request and send data to saveImage.php file

                String response = Request.post(SERVER,dataToSend);

                //return the response

                return response;

            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"ERROR  "+e);
                return null;
            }
        }


        //서버에서 이미지 업로드 완료 후 해당 화면을 벗어납니다.
        @Override
        protected void onPostExecute(String s) {
                Log.e("이미지 올리는 중 ",s);
                finish();
        }
    }

    public static class Request {

        private final String TAG = Request.class.getSimpleName();

        public static String post(String serverUrl, String dataToSend){
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //set timeout of 30 seconds
                con.setConnectTimeout(1000 * 30);
                con.setReadTimeout(1000 * 30);
                //method
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

                //make request
                writer.write(dataToSend);
                writer.flush();
                writer.close();
                os.close();

                //get the response
                int responseCode = con.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    //read the response
                    StringBuilder sb = new StringBuilder();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String line;

                    //loop through the response from the server
                    while ((line = reader.readLine()) != null){
                        sb.append(line).append("\n");
                    }

                    //return the response
                    return sb.toString();
                }else{
                    Log.e("","ERROR - Invalid response code from server "+ responseCode);
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("","ERROR "+e);
                return null;
            }
        }
    }
}