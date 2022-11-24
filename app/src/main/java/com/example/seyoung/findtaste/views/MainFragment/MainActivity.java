package com.example.seyoung.findtaste.views.MainFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;

import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.views.AddTasteInfor.AddtasteActivity;
import com.example.seyoung.findtaste.views.ChatView.ChatMainActivity;
import com.example.seyoung.findtaste.views.WriteReview.ReviewActivity;


/** 사용자가 로그인 후에 사용할 메인 액티비티입니다.
 *  메인 화면에 1. 맛집 리스트와 2. 맛집 검색 화면과 3.맛집, 리뷰 등록 버튼 4. 리뷰들을 볼 수 있는 화면 5. 마이페이지
 *  총 5개의 화면을 불러오도록 했습니다. 한 화면에서 버튼 클릭으로 원하는 페이지를 불러올 경우
 *  사용자가 더 편하게 어플을 사용 할 수 있다고 생각했습니다.
 */

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private static final int FRAGMENT_ONE = 1;      // 맛집 리스트 프래그먼트
    private static final int FRAGMENT_TWO = 2;      // 맛집 검색 화면
    //private static final int FRAGMENT_THR = 3;    // 3번 프래그먼트는 사용을 안합니다.
    private static final int FRAGMENT_FO = 4;       // 뉴스피드 창(리뷰모아보기)
    private static final int FRAGMENT_FIVE = 5;     // 마이페이지
    ImageButton bt_fiveFragment;
    ImageButton bt_fiveFragment2;
    ImageButton bt_fiveFragment3;
    ImageButton bt_fiveFragment4;
    ImageButton bt_fiveFragment5;
    int mCurrentFragmentIndex=1;                    // 처음 어플에 들어올 경우 맛집 리스트가 먼저 보이도록 설정.



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_fiveFragment = (ImageButton)findViewById(R.id.imageButton);          //1번 프래그먼트로 이동할 수 있는 1.버튼 세팅과 2.클릭 리스너. 설정.
        bt_fiveFragment.setOnClickListener(this);
        bt_fiveFragment2 = (ImageButton)findViewById(R.id.imageButton2);        //2번 프래그먼트로 이동할 수 있는 1.버튼 세팅과 2.클릭 리스너. 설정.
        bt_fiveFragment2.setOnClickListener(this);
        bt_fiveFragment3 = (ImageButton)findViewById(R.id.imageButton3);        // 1.맛집 등록 2.리뷰 등록 3.채팅하기를  선택할 수 있는 선택형다이얼로그를 띄우는 버튼.
        bt_fiveFragment3.setOnClickListener(this);
        bt_fiveFragment4 = (ImageButton)findViewById(R.id.imageButton4);        //4번 프래그먼트로 이동할 수 있는 1.버튼 세팅과 2.클릭 리스너. 설정.
        bt_fiveFragment4.setOnClickListener(this);
        bt_fiveFragment5 = (ImageButton)findViewById(R.id.imageButton5);        //5번 프래그먼트로 이동할 수 있는 1.버튼 세팅과 2.클릭 리스너. 설정.
        bt_fiveFragment5.setOnClickListener(this);
        bt_fiveFragment.setSelected(true);

        fragmentReplace(mCurrentFragmentIndex);                                             // 먼저 1번 프래그먼트를 기본으로 메인화면에서 보여준다.

    }

    //프래그먼트를 바꿔주는 메소드
    public void fragmentReplace(int reqNewFragmentIndex) {      // 사용자가 원하는 화면의 버튼을 눌렀을 경우 ex)맛집리스트 버튼
        Fragment newFragment = null;
        newFragment = getFragment(reqNewFragmentIndex);         //해당 화면을 가져온다.
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction(); //Activit에서 프래그먼트를 추가,삭제,교체를 해주는
        transaction.replace(R.id.ll_fragment, newFragment);                                     // 프래그먼트 트랜잭션을 불러와서
        transaction.commit();                                                                   //원하는 프래그먼트로 교체를 하고 난 뒤에 최종반영을 한다.
    }

    //원하는 화면을 가져오고 싶을 때, 원하는 화면의 프래그먼트를 보내줍니다.
    private Fragment getFragment(int idx){              //ex) 1을 넣으면 OneFragment를 호출합니다
        Fragment newFragment = null;
        if(idx==1) {
            newFragment = new OneFragment();
        }
        if(idx==2) {
            newFragment = new TwoFragment();
        }
        if(idx==4) {
            newFragment = new FourFragment();
        }
        if(idx==5) {
            newFragment = new FiveFragment();
        }
        return newFragment;             //결정한 프래그먼트를 돌려준다.
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageButton :         //첫번째 맛집리스트를 보는 버튼을 누르면
                mCurrentFragmentIndex = FRAGMENT_ONE;
                fragmentReplace(mCurrentFragmentIndex); // 맛집리스트 프래그먼트로 바꿔준다
                bt_fiveFragment.setSelected(true);
                bt_fiveFragment2.setSelected(false);
                bt_fiveFragment4.setSelected(false);
                bt_fiveFragment5.setSelected(false);

                break;
            case R.id.imageButton2 :
                mCurrentFragmentIndex = FRAGMENT_TWO;       //
                fragmentReplace(mCurrentFragmentIndex);
                bt_fiveFragment.setSelected(false);
                bt_fiveFragment2.setSelected(true);
                bt_fiveFragment4.setSelected(false);
                bt_fiveFragment5.setSelected(false);
                break;
            case R.id.imageButton3 :                        // 맛집 , 리뷰 등록과 채팅하기중 선택하는 선택형 다이얼로그를 불러옵니다
                showImageDialog();

                break;
            case R.id.imageButton4 :                        //
                mCurrentFragmentIndex = FRAGMENT_FO;
                fragmentReplace(mCurrentFragmentIndex);
                bt_fiveFragment.setSelected(false);
                bt_fiveFragment2.setSelected(false);
                bt_fiveFragment4.setSelected(true);
                bt_fiveFragment5.setSelected(false);
                break;
            case R.id.imageButton5 :                        //
                mCurrentFragmentIndex = FRAGMENT_FIVE;
                fragmentReplace(mCurrentFragmentIndex);
                bt_fiveFragment.setSelected(false);
                bt_fiveFragment2.setSelected(false);
                bt_fiveFragment4.setSelected(false);
                bt_fiveFragment5.setSelected(true);

                break;
        }
    }

    //선택형 다이얼로그입니다.
    public void showImageDialog() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(R.array.choicetaste, -1,  //한 개만 선택할 수 있게 합니다.
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {                      //첫번째를 클릭하면 ->맛집 등록하기
                                    Intent intent = new Intent(MainActivity.this,AddtasteActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);                     //*** intent에 액티비티가 두개가 뜨지않도록
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);                           //    설정했습니다. 하나만 뜨며, 중복된 화면을
                                    startActivity(intent);                                                      //    다시 누를 경우 쌓여져 있는 액티비티를 맨 위로
                                } else if (which == 1){                //2번 ->리뷰 쓰기                         //    올려줍니다.
                                    Intent intent = new Intent(MainActivity.this,ReviewActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                } /*else if (which == 2){                //3번 채팅하기  <--- 연습용 채팅 액티비티입니다. 현재는 사용하지 않습니다.
                                    Intent intent = new Intent(MainActivity.this,Friend.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                }*/ else {                               //3번 ->채팅하기
                                    Intent intent = new Intent(MainActivity.this,ChatMainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                }
                                dialog.dismiss();
                            }
                        }).show();
    }


}
