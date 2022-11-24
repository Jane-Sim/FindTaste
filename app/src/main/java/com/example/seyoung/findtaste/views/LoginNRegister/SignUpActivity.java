package com.example.seyoung.findtaste.views.LoginNRegister;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seyoung.findtaste.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by seyoung on 2017-09-29.
 * 유저가 어플을 효과적으로 이용하기 위해 회원제를 도입했습니다.
 * 유저가 핸드폰을 바꿔도 자신의 데이터를 불러올 수 있도록 회원가입을 시켜줍니다.
 * 또한 아이디를 중복되게 저장하지 않도록 여러가지 예외처리를 만들었습니다.
 * */

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    public String symbolvalue;                 // 사용자의 아이디를 담을 string입니다.

    private DB_Manaber db_manaber;              // 중복확인과 회원가입을 할 db_manaber클래스를 불러옵니다.
    private EditText et_id;                     //아이디와 닉네임 패스워드, 패스워드 재확인 edittext 입니다.
    private EditText et_name;
    private EditText et_passWord;
    private EditText et_passWordAgain;
    private TextView Oktext;                    // 아이디가 중복 될 경우에 아이디 에딧 밑에 빨간 텍스트창을 띄웁니다.
    private ImageView imageview;
    String results;                             // 아이디 중복확인의 결과값을 받을 string입니다.
    boolean idchkboolean = false;               // 아이디 중복 처리의 통과유무를 나타낼 불린변수입니다.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        et_id =  findViewById(R.id.user_id);         //에딧텍스트와 이미지, 버튼을 현재 액티비티에 지정합니다.
        et_name = findViewById(R.id.user_name);
        et_passWord =  findViewById(R.id.user_passWord);
        et_passWordAgain = findViewById(R.id.user_passWordAgain);
        Oktext = findViewById(R.id.Oktext);             //아이디 중복처리를 보여주기 위한 텍스트입니다.
        ImageView imageView = findViewById(R.id.imageView3);
        Button btn_agreeJoin = findViewById(R.id.btn_agreeJoin);    //회원가입 버튼입니다.


        btn_agreeJoin.setOnClickListener(this);

        //아이디 입력값에서 다른 곳으로 포커스가 이동되면, 아이디 예외처리를 해줍니다
        et_id.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == false) {
                    idchk();
                }
            }
        });

        et_id.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_NEXT:    //키보드에서 다음이라는 엔터키를 누를 때
                        idchk();        // 아이디 에딧에서 닉네임 에딧으로 넘어가는 리스너입니다.
                        if(idchkboolean)// 아이디가 중복이 안됐을 경우 닉네임 에딧으로 넘어가집니다.
                            et_name.requestFocus();
                        break;          //엔터을 때 사용하는 메소드입니다.
                    default:
                        // 기본 엔터키 동작
                        return false;
                }
                return true;
            }
        });

        et_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {                      //키보드에서 다음이라는 엔터키를 누를 때
                    case EditorInfo.IME_ACTION_NEXT:    //닉네임에서 비밀번호 에딧으로 넘어가는 리스너입니다.
                        et_passWord.requestFocus();
                        break;
                    default:
                        idchk();
                        return true;
                }
                return true;
            }
        });

        et_passWord.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_NEXT://키보드에서 다음이라는 엔터키를 누를 때
                        et_passWordAgain.requestFocus();    //비밀번호에서 비밀번호 확인에딧으로 넘어가는 리스너입니다.
                        break;
                    default:
                        idchk();
                        return true;
                }
                return true;
            }
        });

     //   et_passWord.setFilters(new InputFilter[]{filterAlphaNum});          // 비밀번호에 필터기능을 넣습니다. (영어,숫자)가 꼭 들어가야함
      //  et_passWordAgain.setFilters(new InputFilter[]{filterAlphaNum});     // 비밀번호재확인에 필터기능을 넣습니다. (영어,숫자)가 꼭 들어가야함
        // et_phone.setOnClickListener(this);
        // et_email.setOnClickListener(this);

        db_manaber = new DB_Manaber();                                      // 서버에 아이디 중복과 회원가입 할 클래스를 볼러옵니다.
    }


    //아이디 중복검사 메소드입니다.
    @SuppressLint("SetTextI18n")
    public void idchk(){
        symbolvalue = et_id.getText().toString();          // 사용자가 적은 아이디 값을 가져옵니다.
        results = db_manaber.inquiryUser(symbolvalue);     //그리고 서버에 아이디를 보내서 중복확인을 해줍니다
        //Log.d("results:", results);
        String isExistld = results;                        //아이디가 존재하는 지 안하는지 결과값을 받아옵니다.
        //Log.d("isExistID:", isExistld);

        //원래 사용한 값이지만 서버에서 다 처리를 해주어서 이젠 필요없는 값입니다.
       /*
        char[] charArray2 = et_id.toString().toCharArray();         //아이디의 한 글자마다 배열에 집어넣습니다
        int code1 = 65;                                             //만약에 아이디에 한글이 들어갔을 경우, 아스키코드로 확인하는 방법이다.
        for (int i = 0; i < et_id.length(); i++) {
            for (int j = 0; j < 25; j++) {
                if (charArray2[i] >= (char) code1 && charArray2[i] <= (char) code1) {
                    code1 = +j;//변환된 Char 출력
                    Toast.makeText(SignUpActivity.this, "영어로 적으세요", Toast.LENGTH_SHORT).show();
                } else if (charArray2[i] <= (char) code1 && charArray2[i] >= (char) code1) {
                    Toast.makeText(SignUpActivity.this, "영어로 적으세요", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }*/

        if (isExistld.equals("0")) {    //서버에서 받아온 결과값이 0일 경우에
            idchkboolean = false;                                               //다이얼로그를 끌 수 있돌고 불린값을 트루로 만들어줍니다
            Toast.makeText(SignUpActivity.this, "아이디 중복", Toast.LENGTH_SHORT).show();//아이디가 중복이 되었을 때
            Oktext.setText("아이디 중복");   //아이디 에딧 밑에 텍스트를 표시하며 색깔을 지정합니다.
            Oktext.setTextColor(Color.RED);
        }

        if (isExistld.equals("1")) {   //서버에서 받아온 결과값이 1일 경우에                                             //아이디가 중복이 아닐 때
            idchkboolean = true;                                               //다이얼로그를 끌 수 있돌고 불린값을 트루로 만들어줍니다
            Toast.makeText(SignUpActivity.this, "아이디 사용 가능", Toast.LENGTH_SHORT).show();
            Oktext.setText("아이디 사용 가능");
            Oktext.setTextColor(Color.DKGRAY);  //아이디 에딧 밑에 텍스트를 표시하며 색깔을 지정합니다.
        }

        if (isExistld.equals("3")) {   //서버에서 받아온 결과값이 3일 경우에
            idchkboolean = false;        // 아이디값이 6~12자 이내가 아니거나 한글이거나 영+숫+특이 다 안들어간 경우
            Toast.makeText(SignUpActivity.this, "ID는 6~12자의 영문자+숫자여야 합니다", Toast.LENGTH_SHORT).show();
            Oktext.setText("ID는 6~12자의 영어+숫자여야 합니다");
            Oktext.setTextColor(Color.RED); //아이디 에딧 밑에 텍스트를 표시하며 색깔을 지정합니다.
        }

        //아이디 중복이 안됬을 경우의 처리
        else if (!idchkboolean) {
            // Oktext.setText("ID는 6~12자의 영문자나 숫자어야 합니다");
            et_id.requestFocus();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case  R.id.user_passWordAgain:
                if(!idchkboolean)
                    idchk();
                break;
            case R.id.btn_agreeJoin:                                        // 회원가입 버튼을 누를 때
                String password = et_passWord.getText().toString();         //비밀번호를 에딧에서 가져옵니다.
                String passwordchk = et_passWordAgain.getText().toString(); //비밀번호 확인도 에딧에서 가져옵니다.

                if (et_id.length() < 6) {                                   //아이디가 6자리보다 작을 때,
                    Toast.makeText(SignUpActivity.this, "아이디가 공백이거나 6보다 작습니다", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (et_id.length() > 12 | et_id.length() < 6) {             //아이디가 6~12자리 이하일 때,
                    Toast.makeText(SignUpActivity.this, "아이디 6~12자 이내로 적으세요", Toast.LENGTH_SHORT).show();
                    break;
                }
                char[] charArray2 = et_id.toString().toCharArray();         //아이디의 한 글자마다 배열에 집어넣습니다
                int code1 = 65;                                             //만약에 아이디에 한글이 들어갔을 경우, 아스키코드로 확인하는 방법이다.
                for (int i = 0; i < et_id.length(); i++) {
                    for (int j = 0; j < 25; j++) {
                        if (charArray2[i] >= (char) code1 && charArray2[i] <= (char) code1) {
                            code1 = +j;//변환된 Char 출력
                            Toast.makeText(SignUpActivity.this, "영어로 적으세요", Toast.LENGTH_SHORT).show();
                        } else if (charArray2[i] <= (char) code1 && charArray2[i] >= (char) code1) {
                            Toast.makeText(SignUpActivity.this, "영어로 적으세요", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
                if (et_name.length() < 1) {                                 // 닉네임이 공백일 때
                    Toast.makeText(SignUpActivity.this, "닉네임이 공백입니다", Toast.LENGTH_SHORT).show();
                    break;
                }

                if (password.length() <= 1) {                                      // 비밀번호가 공백일 때
                    Toast.makeText(SignUpActivity.this, "비밀번호가 공백입니다", Toast.LENGTH_SHORT).show();
                    break;
                }
                char[] charArray = password.toCharArray();
                int code = 65;
                for (int i = 0; i < password.length(); i++) {                      //만약에 비밀번호에 한글이 들어갈 경우.
                    for (int j = 0; j < 25; j++) {
                        if (charArray[i] >= (char) code && charArray[i] <= (char) code) {
                            code = +j;//변환된 Char 출력
                        } else if (charArray[i] <= (char) code && charArray[i] >= (char) code) {
                            Toast.makeText(SignUpActivity.this, "영어로 적으세요", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
                if (password.length() > 14 || password.length() < 6) {                    // 비밀번호 6~14 자 밖일 때,
                    Toast.makeText(SignUpActivity.this, "비밀번호 6~14자 이내로 적으세요", Toast.LENGTH_SHORT).show();
                    break;
                } else if (!password.equals(passwordchk)) {                                  // 비밀번호가 재확인이 안될 때
                    Toast.makeText(SignUpActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    break;
                }  else if(!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{6,14}$", password)) {
                    Toast.makeText(this,"비밀번호 형식을 지켜주세요.",Toast.LENGTH_SHORT).show();
                    break;
                }else if (!idchkboolean) {         //아이디 중복처리가 안됐을 경우
                    idchk();
                    break;
                } else {                            //모든 예외처리를 통과했을 경우에 회원가입을 시켜줍니다.
                    //idchk();
                    String user_id = et_id.getText().toString();
                    String user_name = et_name.getText().toString();
                    String user_password = et_passWord.getText().toString();
                    String user_phone = "";
                    String user_email = "";
                    db_manaber.signup_user_information(user_id, user_name, user_password, user_phone, user_email, "");  //모든 조건이 맞을 경우 회원가입을 진행한다.
                    Toast.makeText(SignUpActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
                    finish();                       //회원가입 완료 후 현재 액티비티를 종료합니다.
                }

                break;
        }

    }

    public InputFilter filterAlphaNum = new InputFilter() {         //필터입니다. 영어와 숫자를 필터링합니다.
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^(?=.*[a-zA-Z]+)(?=.*[0-9]+).{6,12}$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };
/*    protected InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^(?=.*[a-zA-Z]+)(?=.*[0-9]+).{6,12}$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return source;
        }
    };*/


    private static final String Passwrod_PATTERN =  "^(?=.*[a-zA-Z]+)(?=.*[!@#$%^*+=-]|.*[0-9]+).{8,16}$";

    public boolean Passwrodvalidate(final String hex) {

        Pattern pattern = Pattern.compile(Passwrod_PATTERN);

        Matcher matcher = pattern.matcher(hex);

        return matcher.matches();

    }
}
