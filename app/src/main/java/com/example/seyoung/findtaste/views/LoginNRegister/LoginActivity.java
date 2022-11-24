package com.example.seyoung.findtaste.views.LoginNRegister;

/**
 * Created by seyoung on 2017-09-30.
 * 회원가입을 한 유저가 로그인할 수 있도록 만든 로그인액티비티입니다.
 * 회원만 들어올 수 있도록 만들었습니다.
 * 페이스북으로도 로그인과 회원가입을 해서 디비에 저장하도록 만들었습니다.
 */

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.listener.LoginListener;
import com.example.seyoung.findtaste.listener.RegisterListener;
import com.example.seyoung.findtaste.loginaccount.LoginAccountApiIml;
import com.example.seyoung.findtaste.model.Account;
import com.example.seyoung.findtaste.registeraccount.RegisterAccountApiIml;
import com.example.seyoung.findtaste.views.MainFragment.MainActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;




public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    EditText et_id, et_pw;                                              //아이디와 비밀번호를 받을 에딧입니다.
    String sId, sPw, user_id,user_name,user_email,image;               //페이스북으로 로그인, 가입할 때 필요한 String입니다.
    Button login_btn,join_btn;                                          //로그인 버튼과 회원가입하러 가는 버튼
    private CallbackManager callbackManager;                            //페이스북에서 보낸 값을 돌려받게 도와주는 콜백입니다.
    RegisterAccountApiIml registerAccountApiIml;                        //페이스북으로 가입할 때 디비에 저장하는 클래스입니다.
    LoginAccountApiIml accountApiIml;                                   //페이스북으로 로그인할 때 디비에 저장된 아이디와 비밀번호를 찾아주는 클래스
    JSONObject profile_pic_data, profile_pic_url;                       //페이스북에서 받아올 프로필이미지와 프로필 이미지 주소

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));                    //이 함수는 Facebook SDK를 초기화합니다.이 함수가 호출되지 않으면
        FacebookSdk.sdkInitialize(LoginActivity.this);      //Facebook SDK 함수의 동작이 결정되지 않습니다. 가능한 빨리 호출되어야합니다.
                                                                            // 페이스북을 사용하기 위해 findtaste로 저장한 페이스북의
                                                                             //개발사이트에서 발급받은 id를 지정해줍니다.
        setContentView(R.layout.activity_login);
        et_id =  findViewById(R.id.IdText);
        et_pw =  findViewById(R.id.PasswordText);
        login_btn =  findViewById(R.id.loginbutton);
        join_btn =  findViewById(R.id.joinbutton);
        registerAccountApiIml = new RegisterAccountApiIml();                //페이스북 회원가입을 해주는 메서드를 호출합니다
        accountApiIml = new LoginAccountApiIml();                           //아이디로그인과 페이스북 로그인을 도와줄 메서드를 호출합니다.
        callbackManager = CallbackManager.Factory.create();                 //페이스북 콜백 메서트를 생성해줍니다.
        //Account account = (Account) getIntent().getSerializableExtra("Account");
        LoginButton fbloginButton = findViewById(R.id.fblogin_button);      //페이스북 로그인 버튼입니다.
        fbloginButton.setReadPermissions(Arrays.asList("public_profile", "email")); //페이스북의 프로필과 이메일을 가져올 수 있는 권한을 설정
        fbloginButton.setLoginBehavior(LoginBehavior.WEB_ONLY);                         // 오로지 웹으로만 사용할 수 있도록 만들어줍니다.(이상하게 설정을 안하면 작동x)
        fbloginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {   // 페이스북 버튼을 눌렀을 때 받아오는 콜백을 실행합니다
            @Override
            public void onSuccess(LoginResult loginResult) { //로그인 성공시 호출되는 메소드
                Log.e("토큰", loginResult.getAccessToken().getToken());   //페이스북에서 주는 토큰을 가져옵니다.
                Log.e("유저아이디", loginResult.getAccessToken().getUserId());   //페이스북에서 정해준 프로필 아이디를 가져옴

             //   Log.e("퍼미션 리스트", loginResult.getAccessToken().getPermissions() + "");   //퍼미션 리스트도 받아옵니다


                //loginResult.getAccessToken() 정보를 가지고 유저 정보를 가져올수 있습니다.    페이스북에 저장된 유저 아이디 정보가 노출되면 안되기에, 페이스북이 임의로 만든 아아디값을 받습니다.
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),  //임의로 만든 아이디값은 다시 로그인을 하거나 회원가입을 해도 변경되지 않습니다.
                        new GraphRequest.GraphJSONObjectCallback() {                            //페이스북의 그래프api를 써서 프로필 값을 받습니다.
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    Log.e("user profile", object.toString());
                                    Log.e("유저아이디", (object.getString("id")+"m"));
                                    Log.e("유저네임", object.getString("name"));
                                    Log.e("유저메일", object.getString("email"));
                                    Log.e("유저사진", object.get("picture").toString());
//                                    Log.e("유저유알엘", object.getString("data"));

                                    profile_pic_data = new JSONObject(object.get("picture").toString());        //사진의 정보를 받습니다.
                                    profile_pic_url = new JSONObject(profile_pic_data.getString("data"));   //사진의 url을 받습니다.
                                    image=profile_pic_url.getString("url");                                 //사진을 서버에 저장하기 위해 필요함.

                                    //프로필에서 가져온 데이더값입니다.
                                    user_id = object.getString("id");                                    //유저의 아이디를 받아와 스트링에 넣는다
                                    user_name = object.getString("name");                                //유저의 이름을 받습니다.
                                    user_email = object.getString("email");                              //유저의 이메일을 받습니다.

                                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE); //이전에 기기안에 쉐어드로 저장한 아이디값이 있다면
                                    String g= pref.getString("ing", "s");                                //불러옵니다.

                                    Log.e("저장값", g);

                                        accountApiIml.authAccount(user_id, new LoginListener() {               // 페이스북의 아이디로 로그인를 실행합니다.
                                            @Override
                                            public void getDataSuccess(Account account) {                       //데이터가 잘 불러와진다면

                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("Account", account);                     //프래그먼트에 값을 전달합니다.

                                                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = pref.edit();                  //그리고 다음부턴 로그아웃을 하기 전까지
                                                editor.putString("ing", user_id);                            //로그인 창을 안 보도록 자동로그인으로 만듭니다.
                                                editor.apply();                                                //쉐어드에 아이디 값을 넣습니다.

                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);         //그리고 메인 화면으로 넘겨줍니다
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);               // 화면이 여러개가 뜨지 않도록 인텐트를 설정
                                                startActivity(intent);                                          //한개만 뜨도록, 중복된 액티비티는 만들지 않고 제일 위해 하나만 띄우게하기
                                                finish();                                                       //메인액티비티를 실행후 로그인화면은 종료합니다
                                            }

                                            @Override
                                            public void getMessageError(Exception e) {                          //받아오는 게 오류가 날때
                                                Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });



                                    if(g.equals("s")) {                                                         //쉐어드에 저장된 값이 없으면
                                        registerAccountApiIml.registerAccount(user_id, user_name, user_email, image, new RegisterListener() {
                                            @Override
                                            public void getDataSuccess(Account account) {                       //로그인을 해본 적이 없으니 페이스북으로
                                                // 활동 결과를 통해 데이터를 보내 결과 표시                        //페이스북 의정보로  디비 저장을 해줍니다.
                                            //   Bundle bundle = new Bundle();
                                             //   bundle.putSerializable("Account", account);
                                                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = pref.edit();                  //자동로그인을 위해 쉐어드에 값을 ing로 넣고
                                                editor.putString("ing", user_id);                             //지워지지않게저장합니다.
                                                editor.apply();

                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);         //그리고 메인 화면으로 넘겨줍니다
                                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);               // 화면이 여러개가 뜨지 않도록 인텐트를 설정
                                               // intent.putExtras(bundle);
                                                startActivity(intent);                                          //현재창을 종료 후 메인창으로 이동합니다
                                                finish();
                                            }

                                            @Override
                                            public void getMessageError(Exception e) {                          //에러가 났을 시 뜨는 토스트 창입니다.
                                                Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();                       //번들로 페이스북정보를 가져옵니다
                parameters.putString("fields","id,name,email,picture.width(120).height(120)"); // 페이스북에서 받아오고 싶은 값을 정합니다.
                request.setParameters(parameters);                                              //아이디, 이름, 이메일, 사진 120_x 120_y
                request.executeAsync();                                                         //그리고 값을 불러옵니다.
            }

            @Override
            public void onError(FacebookException error) {
            }

            @Override
            public void onCancel() {
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {             //페이스북 창에서 데이터를 받아왔을 때
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);                        //로그인을 하거나 회원가입을 시킵니다.
    }                                                                                           // 둘 중 하나가 통과될 경우 메인액티비티로 이동합니다.

    @Override
    public void onClick(View view) {                        // 클릭 이벤트입니다.
        switch (view.getId()) {
            case R.id.loginbutton:                          //페이스북이 아닌 아이디로 저장하는 로그인 버튼을 누를 때
                try{
                    sId = et_id.getText().toString();       // 사용자가 적은 아이디와 비밀번호를 가져옵니다.
                    sPw = et_pw.getText().toString();
                    char[] charArray = sId.toCharArray();
                    int code=65;
                    int cd=65;
                    int cod=90;
                    for(int i = 0 ; i < sId.length() ; i++){    //아이디에 한글이 있을 경우에
                        for (int j=0 ;j <25 ; j++) {            // 영어로 적어달라고 토스트창을 띄웁니다.
                            if (charArray[i] >= (char) code && charArray[i] <= (char) cod){
                                code =+ j;//변환된 Char 출력
                            }
                            else if (charArray[i] < (char) cd && charArray[i] > (char) cod){
                                Toast.makeText(LoginActivity.this, "영어로 적으세요", Toast.LENGTH_SHORT).show();
                                break;
                            }                                   //사실 텍스트창을 영어+숫자로만 쓰게 해놔서 별 상관x
                        }
                    }
                }catch (NullPointerException e)
                {
                    Log.e("err",e.getMessage());
                }

                loginDB lDB = new loginDB();                    //서버에 아이디와 비밀번호 값을 보내는 메소드를 실행.
                lDB.execute();
                break;
            case R.id.joinbutton:                               //유저가 처음 방문할 경우 회원가입 창으로 보내는
                Intent joinIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(joinIntent);                      //회원가입 버튼입니다.
            break;

        }
    }

    @SuppressLint("StaticFieldLeak")
    public class loginDB extends AsyncTask<Void, Integer, Void> {

        String data = "";
        @Override
        protected Void doInBackground(Void... unused) {

/* 인풋 파라메터값 생성 */
            String param = "u_id=" + sId + "&u_pw=" + sPw + "";
            Log.e("POST",param);//현재 적은 id, pw 값을 log에 띄워서 값이 잘 들어가나 확인
            try {
/* 서버연결 */
                URL url = new URL(              //설정한 url로 서버에 연결시킵니다.
                        "http://findtaste.vps.phps.kr/user_signup/login.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

/* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();     //연결된 서버에 아이디+비번을 보내준 후
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();                                   //보내주는 output을 꼭 닫아줍니다.
/* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                //아이디가 있는 지, 비밀번호가 틀린것인지 확인
                is = conn.getInputStream();                     //서버에서 결과값을 안드로이드에서 받습니다.
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();

/* 서버에서 응답 */
                Log.e("RECV DATA",data);

                if(data.equals("0")){                           //0을 서버가 줄 경우 로그에 성공을 남깁니다.
                    Log.e("RESULT","성공적으로 처리되었습니다!");
                }else if(data.equals("1")){                     //1을 서버가 줄 경우 로그에 실패를 남깁니다.
                    Log.e("RESULT","에러 발생! ERRCODE = " + data);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(data.equals("0"))        //아이디가 있고, 비밀번호가 맞을 때, 0을 준다.
            {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("ing", sId);
                editor.apply();         //자동로그인을 위해 기기 디비에 아이디를 저장합니다. (쉐어드)

                Toast.makeText(LoginActivity.this, "로그인 되었습니다", Toast.LENGTH_SHORT).show();
                Log.e("RESULT","성공적으로 처리되었습니다!");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);   // 로그인이 되었다는 알림창과 화면전환을 해줍니다.(로그인->어플 메인)
                finish();
            }
             else if(data.equals("1"))    //아이디가 없을 때, 1을 준다.
            {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
            }
            else if(data.equals("00"))      //아이디가 있고 비밀번호가 틀릴 때, 00을 준다.
            {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
            } else {
                Log.e("RESULT","에러 발생! ERRCODE = " + data);
            }
        }


    }


    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public static String getURLEncode(String content){
        try {
//          return URLEncoder.encode(content, "utf-8");   // UTF-8
            return URLEncoder.encode(content, "euc-kr");  // EUC-KR
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


}

