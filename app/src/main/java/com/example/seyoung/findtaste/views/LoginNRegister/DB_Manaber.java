package com.example.seyoung.findtaste.views.LoginNRegister;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by seyoung on 2017-09-28.
 * 회원가입과 아이디 중복확인을 할 수 있는 클래스입니다.
 * 유저가 회원가입을 하기 전에 아이디를 디비에 중복되게 저장하지 않도록 클래스를 만들었습니다.
 * 종복이 되지 않았다면 유저가 적은 회원가입 데이터를 저장할 수 있도록 하였습니다.
 */

public class DB_Manaber {
      //사용자가 회원가입을 할 때 확인이 필요한 서버 주소입니다. (회원가입, 아이디중복처리 주소)
    private String signup_user_infomation_UrlPath =  // 디비에 저장을 할 회원가입 주소입니다.
            "http://findtaste.vps.phps.kr/user_signup/signup_user_information.php";
    private String signup_user_joinchk_UrlPath =     // 아이디 중복 확인 주소입니다
            "http://findtaste.vps.phps.kr/user_signup/signup_user_join_chk.php";

    private String user_id;                       //회원가입을 위한 액티비티 소스입니다.
    private String user_name;                     // 사용자의 아이디와 닉네임, 비밀번호, 전화번호, 이메일과 이미지를
    private String user_password;                 // 사용자에게 받아서 서버에 보낼 겁니다.
    private String user_phone;
    private String user_email;
    private String user_image;
    String c =null;
    private String symbolvalue;

    // 사용자가 회원가입을 하기 위한 메소드입니다.
    public void signup_user_information(String user_id, String user_name, String user_password, String user_phone, String user_email, String user_image){
        //사용자가 적은 아이디와 닉네임, 비밀번호, 전화번호, 이메일과 이미지를
        this.user_id= user_id;         // 현재 변수에 넣어줍니다.
        this.user_name= user_name;
        this.user_password= user_password;
        this.user_phone= user_phone;
        this.user_email= user_email;
        this.user_image= user_image;
        try {
            new SignupUserInformation().execute().get();   //그리고 사용자의 회원가입 정보를 서버에 보내서 디비에 저장을 시킵니다.
        } catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }
    }

    // 사용자가 아이디 중복확인을 원할 때 사용하는 소스입니다
    public String inquiryUser(String symbolvalue) {
                                                    // 사용자가 적은 아이디를 서버에 보내서
        this.symbolvalue = symbolvalue;             // 중복인지 아닌지를 서버에서 받아와 결과값을 리턴해 보내줍니다.
        String results=null;
        try {
            results = new JoginchkDB().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return results;
    }

    @SuppressLint("StaticFieldLeak")
    public class JoginchkDB extends AsyncTask<Object, Object, String> { //중복확인 메소드입니다.
        String data = "";           // 아이디 중복확인의 결과값을 받을 변수입니다.

        @Override
        protected String doInBackground(Object... unused) {

/* 인풋 파라메터값 생성 */
            String param = "u_id=" + symbolvalue + "";      //사용자가 적은 아이디를 서버가 받을 수 있게
            Log.e("POST", param);                       //만들어줍니다.
            try {
/* 서버연결 */
                URL url = new URL(                          //서버의 중복확인 주소를 적어줍니다.
                        "http://findtaste.vps.phps.kr/user_signup/signup_user_join_chk.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");              // 서버 주소로 값을 못읽게 설정
                conn.setDoInput(true);                      //post로 보낼 수 있게 설정
                conn.connect();                             //서버와 연결시킵니다.

/* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream(); //연결된 서버에 아이디 값을 보냅니다.
                outs.write(param.getBytes("UTF-8"));    //한글이 깨지지않도록 UTF-8로 지정합니다.
                outs.flush();
                outs.close();                               //서버에 보낸 뒤 보내주는 기능을 닫아줍니다.

/* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;                      // 서버에서 안드로이드로 값(Byte)을 받을 스트림입니다.
                BufferedReader in = null;                   //라인단위 또는 캐릭터 배열 단위의 입력을 효과적으로 받을 수 있다.

                //받은 값을 눈으로 볼 수 있도록 (Byte->Stirng)버퍼리더로 설정합니다.

                is = conn.getInputStream();                 //연결된 서버에서 안드로이드로 값(Byte)을 받습니다.
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);   //받은 BYte값을 문자String으로 바꿔줍니다.
                String line = null;
                StringBuffer buff = new StringBuffer();     //String을 계속해서 더할 수 있는 버퍼입니다. ex(안녕+자바)
                while ((line = in.readLine()) != null) {    //String으로 바꾼 서버값을 한 문장으로 받습니다.(read()는 한 글자씩 받음)
                    buff.append(line + "\n");               //buffer에 String으로 바꾼 값들을 계속 더합니다.
                }
                data = buff.toString().trim();              //더했던 buffer값을 앞 뒤 공백이 없이 결과값에 넣어줍니다.

/* 서버에서 응답 */
                Log.e("RECV DATA", data);

                if (data.equals("1")) {                     // 결과값이 1일 경우에
                    Log.e("RESULT", "성공적으로 처리되었습니다!");
                } else if (data.equals("0")) {              //결과값이 0일 경우에
                    Log.e("RESULT", "에러 발생! ERRCODE = " + data);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;                                    //데이터를 onPostExecute에 보냅니다.
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            if (data.equals("1")) {
                // Toast.makeText(getActivity(),"사용가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
            } else if (data.equals("0")) {
                //Toast.makeText(getActivity(),"아이디가 중복입니다", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("RESULT", "에러 발생! ERRCODE = " + data);

            }
        }


    }


    @SuppressLint("StaticFieldLeak")
    private class SignupUserInformation extends AsyncTask<Void, Void, ArrayList<String>>{           // 회원가입을 하는 소스입니다.

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
          try{
              URL url = new URL(signup_user_infomation_UrlPath);                                                           // 회원가입 서버 주소를 지정합니다
              HttpURLConnection con = (HttpURLConnection) url.openConnection();
              con.setDoInput(true);
              con.setDoOutput(true);
              con.setUseCaches(false);
              con.setRequestMethod("POST");                                                         //post로 사용자의 정보를 서버의 주소로 확인하지 못하게 만듭니다.

              String param =
                      "user_id="+user_id+"&user_name="+user_name+"&user_password="+user_password+"&user_phone="+user_phone+"&user_email="+user_email+"&user_image="+user_image;

              OutputStream outputStream = con.getOutputStream();                                     //서버에 연결 후
              outputStream.write(param.getBytes());                                                  // 아이디와 닉네임, 비밀번호와 번호와 이메일과 이미지를 보냅니다.
              outputStream.flush();                                                                  //완료되면 서버를 닫습니다
              outputStream.close();

              BufferedReader rd = null;
              rd = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
              String line = null;
              while ((line = rd.readLine()) != null){                                                //서버에서 다시 돌아온 결과값을 로그로 남겨서
                  Log.d("BufferedReader:",line);                                                 // 사용자가 추가되었다는 값을 확인할 수 있습니다
              }
          } catch (MalformedURLException e) {
              e.printStackTrace();
          } catch (ProtocolException e) {
              e.printStackTrace();
          } catch (IOException e) {
              e.printStackTrace();
          }

            return null;
        }
        protected void onPostExecute(ArrayList<String> qResults){
            super.onPostExecute(qResults);
        }
    }

}
