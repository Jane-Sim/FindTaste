package com.example.seyoung.findtaste.views.ChatView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.ChatAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.databinding.ActivityChatroomBinding;
import com.example.seyoung.findtaste.item.Chat;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.Chattingstatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by seyoung on 2017-12-04.
 * 채팅방 액티비티입니다. 사용자들이 실시간으로 메세지를 주고 받을 때,
 * 채팅방 목록에 메세지를 표시합니다.
 * 채팅 서버와 연결이 됐을 경우에만 메세지를 보낼 수 있도록 했습니다.
 * 또한 메세지를 보낼 경우 서비스에 메세지를 보냅니다.
 */

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatRoomActivity";
    ActivityChatroomBinding binding;
    private static final int CHATROOM = R.layout.activity_chatroom;

    ChatRoomActivity activity;
    private Vector<Chat> chats;
    private ChatAdapter adapter;        //동적으로 추가되는 메세지데이터를 추가할 어댑터

    private final Handler handler = new Handler();  //서비스와 데이터를 주고받게 만드는 핸들러

    String name, userid;        //현재 유저의 아이디와 닉네임
    String friend, friendid, chatroom="";   //친구의 아이디와 닉네임, 현재 채팅방이름
    String 누구,방이름 = "3000";  //메세지를 서비스에서 받았을 때, 내가 보낸 건지, 상대방이 보낸 건지의 유무.
    String img; //상대방의 프로필 이미지.
    //서비스와 현재 액티비티가 실시간으로 데이터를 주고 받을 수 있도록 핸들러 추가.
    private Messenger mService=null;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    //바인드 연결 유무
    private static boolean mIsBound = false;

    //서비스에서 현재 액티비티에 데이터를 보낼 때,
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

                    assert str1 != null;
                    String[] results = str1.split("\\$");
                    //내 아이디와 받은 메세지의 아이디를 가지고 누가 보낸 것인지 판단한다.
                    if (name.equals(results[0])) {
                        누구 = "0";
                    } else {
                        누구 = "1";
                    }
                    //채팅방이 만들어진 경우 로그에 남긴다. 또한 채팅방 이름을 지정해준다.
                    if (results[0].equals("RommCreate") || results[0].equals("Roomin")) {
                        //  Toast.makeText(ChatRoomActivity.this, "방이 만들어졌습니다." + results[1], Toast.LENGTH_LONG).show();
                        Log.e("만들어진 방", "방이 만들어졌습니다." + results[1]);
                        //chatroom = results[1];
                        방이름 = results[1];
                    } // 만들어진 채팅방에서 메세지를 받앗을 때, 채팅방 목록에 해당 메세지를 추가시킨다.
                    else if (!results[0].equals("RommCreate")&&!results[0].equals("Roomin")&&
                            chatroom.equals(results[1]+"$"+results[2]+"$"+results[3])&&!results[0].equals("/facetime")&&!results[0].equals("/faceoff")) {
                        String msm = results[4];
                        //Chat(사용자아이디, 대화내용, 누가 보냈는가, 사진,시간) {
                        chats.add(new Chat(results[0], msm, 누구, results[6], results[5]));
                        adapter.notifyDataSetChanged();
                        binding.chatRoomListView.scrollToPosition(chats.size() - 1);
                        if(누구.equals("0")){
                            binding.msgEditText.setText(null);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    //채팅방 연결 유무를 나타낸다.
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.e("챗룸액티비티","서비스 연결시킴");
            //   textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
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

    @Override
    protected void onStart() {
        super.onStart();
        CheckIfServiceIsRunning();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckIfServiceIsRunning();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //채팅방에 들어오면 서비스를 시작한다.
        Intent inten = new Intent(this, MyService.class);
        startService(inten);
        //서비스가 실행중인지 확인한다.
        CheckIfServiceIsRunning();

        //현재 유저의 아이디를 가져온다.
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        userid= pref.getString("ing", "s");
       // onNewIntent(getIntent());
        //노티로 들어왔을 경우, 채팅방이름과 친구 아이디, 닉네임을 가져온다.
        Bundle intent3 = getIntent().getExtras();
        if (intent3 != null) {
            Log.e("노티에서 받아왔다", "굿");
            name = intent3.getString("name");
            Log.e("이름", name);
            friend = intent3.getString("friend");
            Log.e("친구", friend);
            friendid = intent3.getString("friendid");
            chatroom = intent3.getString("chatroom");
            Log.e("방이름", chatroom);
            img = intent3.getString("fImg");
        } else {
            Log.e("인텐트가 널값이다", "ㅠ");
            Intent intent = getIntent();
            name = intent.getStringExtra("name");
            friend = intent.getStringExtra("friend");
            friendid = intent.getStringExtra("friendid");
            chatroom = intent.getStringExtra("chatroom");
            img = intent.getStringExtra("fImg");
        }

        binding = DataBindingUtil.setContentView(this, CHATROOM);
        //String fName =friend;
        chats = new Vector<>();
        //리싸이클뷰를 리니어레이아웃으로 지정 후 세로로 나열시킨다.
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.chatRoomListView.setLayoutManager(manager);
        //채팅방 이름을 상대방 닉네임으로 지정한다.
        binding.chatRoomFriendNameTxtView.setText(friend);
        //서버에서 현재 유저의 현재 채팅방 메세지목록을 가져온다.
        getchattings();
        mIsBound = true;

        //서비스에 현재 채팅방 이름을 보내준다.
        sendChatroomToService(chatroom);
        adapter = new ChatAdapter(chats, this);
        binding.chatRoomListView.setAdapter(adapter);
        binding.backChatRoomListImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //메세지 보내기 버튼이다. 서비스에 메세지를 보낸 후 입력창을 비운 뒤 키보드를 내린후 제일 아래 창으로 리싸이클뷰를 이동시킨다.
        //사용자가 추가한 메세지를 보기 위해서.
        binding.sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (mIsBound) {
                        if(binding.msgEditText.getText().toString().length()>0) {
                            long time = System.currentTimeMillis();
                            Date date = new Date(time);
                            SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.KOREA);
                            String thisTime = sdf.format(date);
                            String msg2 = binding.msgEditText.getText().toString();
                            //sendMessageToService("/made$"+chatroom+"$maderoom");
                            String messinger = name + "$" + chatroom + "$" + msg2 + "$" + thisTime + "$" + img + "$" + userid + "$";
                            sendMessageToService(messinger);
                           // binding.msgEditText.setText(null);
                        }
                    }
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        public void run() {
                           showDialog(activity, "" + e, "안보내져");
                        }
                    });
                }

            }
        });
    }

    public static void showDialog(final Activity activity, String title, String text) {
        AlertDialog.Builder ad = new AlertDialog.Builder(activity);
        ad.setTitle(title);
        ad.setMessage(text);
        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.setResult(Activity.RESULT_OK);
            }
        });
        ad.create();
        ad.show();
    }

    //서비스가 실행 중인지 판단 여부.
    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (MyService.isRunning()) {
            doBindService();
            sendChatroomToService(chatroom);
        }
    }
    //실행되고 있으면 서비스에 현재 채팅방 이름을 보낸다.
    void doBindService() {
        bindService(new Intent(this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.e("바인드 연결됌","OK");
        handler.post(new Runnable() {
            public void run() {
                sendChatroomToService(chatroom);
                Log.e("서버에서 보내는가?",chatroom);
            }
        });
        // textStatus.setText("Binding.");
    }
    //메세지를 서비스에 보내준다.
    private void sendMessageToService(String chat) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("chating", chat);
                    Message msg = Message.obtain(null, MyService.MSG_SET_STRING_VALUE);
                    msg.setData(b);
                    mService.send(msg);

                } catch (RemoteException e) {
                }
            }
        }
    }
    //채팅방 이름을 서비스에 보낸다.
    private void sendChatroomToService(String chatroom) {
        Log.e("바운드",mIsBound+"");
        if (mIsBound) {
        //    Log.e("서비스",mService.toString());
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("chatroom", chatroom);
                    Log.e("진짜 방이름 보낸다",chatroom);
                    Message msg = Message.obtain(null, MyService.MSG_SET_STRING_CHATROOM);
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
                    Message msg = Message.obtain(null, MyService.MSG_UNREGISTER_CLIENT);
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
            sendChatroomToService("out");
            doUnbindService();
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }

    @Override
    public void onBackPressed() {
        sendChatroomToService("out");
        doUnbindService();
        finish();
    }
    //현재 채팅방의 메세지들을 서버에서 받아와 목록에 추가시켜준다.
    public void getchattings() {

        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        Call<List<Chattingstatus>> call = apiInterface.getchatting(chatroom);
        call.enqueue(new Callback<List<Chattingstatus>>() {
            @Override
            public void onResponse(Call<List<Chattingstatus>> call, Response<List<Chattingstatus>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getApplicationContext(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                        chats.clear();
                    for (Chattingstatus chat : response.body()) {
                        //Chat(사용자아이디, 대화내용, 누가 보냈는가, 사진,시간) {
                        if (userid.equals(chat.getUserId())) {
                            누구 = "0";
                        } else {
                            누구 = "1";
                        }
                        chats.add(new Chat(chat.getUserName(), chat.getStatus(), 누구, chat.getUserImage(), chat.getTime()));
                    }
                    adapter.notifyDataSetChanged();
                    binding.chatRoomListView.scrollToPosition(chats.size() - 1);
                    // 리스트에 계속 json 데이터를 축적시키며 추가한다.
                }
            }

            @Override
            public void onFailure(Call<List<Chattingstatus>> call, Throwable t) {        //서버와 연결 실패 할 경우
               // Toast.makeText(getApplicationContext(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle intent3 = getIntent().getExtras();
        if (intent3 != null) {
            Log.e("노티에서 받아왔다", "굿");
            name = intent3.getString("name");
            Log.e("이름", name);
            friend = intent3.getString("friend");
            Log.e("친구", friend);
            friendid = intent3.getString("friendid");
            chatroom = intent3.getString("chatroom");
            Log.e("방이름", chatroom);
            img = intent3.getString("fImg");
        } if (intent != null) {
            Log.e("인텐트가 널값이다", "ㅠ");
            //intent = getIntent();
            name = intent.getStringExtra("name");
            friend = intent.getStringExtra("friend");
            friendid = intent.getStringExtra("friendid");
            chatroom = intent.getStringExtra("chatroom");
            img = intent.getStringExtra("fImg");
        }
        CheckIfServiceIsRunning();
        getchattings();
    }
}