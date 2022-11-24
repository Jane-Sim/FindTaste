package com.example.seyoung.findtaste.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.views.ChatView.MyService;

import java.io.IOException;

/**
 * Created by seyoung on 2018-01-22.
 */

public class wait_facetime extends AppCompatActivity {
    TextView username;
    ImageView userpic,call_connect,call_off;
    String roomEditText,userid,friendid,userPic,friendname;
    private String activityTag = "waitfacetimeActivity";
    private final Handler handler = new Handler();  //서비스와 데이터를 주고받게 만드는 핸들러
    private Messenger mService=null;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    //바인드 연결 유무
    private static boolean mIsBound = false;
    //채팅방 연결 유무를 나타낸다.
    //해당 액티비티가 뜨면 벨소리를 띄운다
    MediaPlayer mPlayer;

    //서비스에서 현재 액티비티에 데이터를 보낼 때,
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyService.MSG_SET_INT_VALUE:
                    //  textIntValue.setText("Int Message: " + msg.arg1);
                    break;
                //서비스에서 메세지를 보냈을 경우
                case MyService.MSG_SET_STRING_VALUE:
                    String str1 = msg.getData().getString("chating");
                    String[] results = str1.split("\\$");
                    if (results[0].equals("/faceoff")) {
                        finish();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_wait);
        username = findViewById(R.id.username);
        userpic = findViewById(R.id.userpic);
        call_connect = findViewById(R.id.call_connect);
        call_off = findViewById(R.id.call_off);
        CheckIfServiceIsRunning();

        final Intent intent = getIntent();
        if(intent.getStringExtra("영상통화")!=null) {
            if (intent.getStringExtra("영상통화").equals("yes")) {
                roomEditText=intent.getStringExtra("roomEditText");
                userid=intent.getStringExtra("userid");
                friendid=intent.getStringExtra("friendid");
                userPic=intent.getStringExtra("userPic");
                friendname=intent.getStringExtra("friendname");
                Log.e("서비스에서","영상통화 받아왔다");
                Log.e("영상통화 방번호",roomEditText);
            }
        }
        username.setText(friendname);
        Glide.with(this)                         //글라이드로 빠르게 사진을 넣는다.
                .load(userPic)
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .fitCenter()
                        .centerCrop()
                        .circleCrop())
                .into(userpic);
        call_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), FacetimeActivity.class);
                intent1.putExtra("영상통화","yes");
                intent1.putExtra("roomEditText",roomEditText);
                intent1.putExtra("userid",userid);
                intent1.putExtra("friendid",friendid);
                intent1.putExtra("friendname",friendname);
                startActivity(intent1);
                mPlayer.release();
                finish();
            }
        });

        call_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String face_off = "/faceoff"+"$"+friendid+"$";
                sendFaceOffToService(face_off);
                mPlayer.release();
                finish();
            }
        });
        //해당 액티비티가 실행되면 벨소리를 울려서 전화온 걸 알려준다.
        Lingtone();
    }

    public void Lingtone(){
        mPlayer = new MediaPlayer();         // 객체생성
// TYPE_RINGTONE 을 하면 현재 설정되어 있는 밸소리를 가져온다.
// 만약 알람음을 가져오고 싶다면 TYPE_ALARM 을 이용하면 된다
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        try {
            // 이렇게 URI 객체를 그대로 삽입해줘야한다.
            //인터넷에서 url.toString() 으로 하는것이 보이는데 해보니까 안된다 -_-;
            mPlayer.setDataSource(this, alert);
            // 출력방식(재생시 사용할 방식)을 설정한다. STREAM_RING 은 외장 스피커로,
            // STREAM_VOICE_CALL 은 전화-수신 스피커를 사용한다.
            mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mPlayer.setLooping(true);  // 반복여부 지정
            mPlayer.prepare();    // 실행전 준비
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        CheckIfServiceIsRunning();

    }

    @Override
    public void onResume() {
        super.onResume();
        CheckIfServiceIsRunning();

    }
    //서비스가 실행 중인지 판단 여부.
    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (MyService.isRunning()) {
            doBindService();
        }
    }
    //실행되고 있으면 서비스와 현재 액티비티를 연결시킨다.
    void doBindService() {
        bindService(new Intent(this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.e("바인드 연결됌","OK");
        // textStatus.setText("Binding.");
    }

    //페이스타임종료를 서비스에 보낸다.
    private void sendFaceOffToService(String facetoff) {
        Log.e("바운드",mIsBound+"");
        if (mIsBound) {
            //    Log.e("서비스",mService.toString());
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("faceoff", facetoff);
                    Log.e("페이스타임 종료 보낸다",facetoff);
                    Message msg = Message.obtain(null, MyService.MSG_SET_STRING_FACEOFF);
                    msg.setData(b);
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    //바인드를 해제한다.
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("activity",activityTag);
                    Message msg = Message.obtain(null, MyService.MSG_UNREGISTER_CLIENT);
                    msg.setData(b);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            //  textStatus.setText("Unbinding.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
            if(mPlayer!=null)
                mPlayer.release();
        } catch (Throwable t) {
            Log.e("waitfacetimeActivity", "Failed to unbind from the service", t);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.e("챗룸액티비티","서비스 연결시킴");
            //   textStatus.setText("Attached.");
            try {
                Bundle b = new Bundle();
                b.putString("activity",activityTag);
                Message msg = Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
                msg.setData(b);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            Log.e("챗룸액티비티","서비스 연결안되어잇움");
            // textStatus.setText("Disconnected.");
        }
    };

    //사용자가 해당 액티비티에서 응답과 거절 버튼만 사용하도록 한다.
    //뒤로가기 버튼을 막아서 해당 액티비티 종료 x
    @Override
    public void onBackPressed() {
       // super.onBackPressed();
    }
}
