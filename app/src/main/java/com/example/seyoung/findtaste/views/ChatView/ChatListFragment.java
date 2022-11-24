package com.example.seyoung.findtaste.views.ChatView;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.seyoung.findtaste.Adapter.ChatListAdapter;
import com.example.seyoung.findtaste.Base.RetroFitApiClient;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.databinding.FragmentChatlistBinding;
import com.example.seyoung.findtaste.item.Profile;
import com.example.seyoung.findtaste.listener.getfood;
import com.example.seyoung.findtaste.model.ChatList;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by seyoung on 2017-11-29.
 * 채팅방 리스트 대기방입니다.
 * 사용자가 원하는 채팅방에 들어갈 수 있도록 하며, 메세지를 받을 때마다 동적으로 채팅방 내용을 바꿔줍니다.
 * 사용자가 메세지를 받을 경우, 받은 메세지의 유저 닉네임과 채팅내용, 시간을 리스트에 넣어줍니다.
 * 그리고 채팅방이 없을 땐 해당 채팅방을 추가시켜줍니다.
 */

public class ChatListFragment extends Fragment {
    FragmentChatlistBinding binding;
    Vector<Profile> profiles;
    String userid,친구이름,username;
    private Messenger mService;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private static boolean mIsBound = false;
    private final Handler handler = new Handler();
    String chatroom= "wait";
    HashMap<String,Integer> roomnumber;
    String activityTag = "ChatListFragment";

    //서비스와 현재 액티비티가 데이터를 주고받게 만들어주는 핸들러.
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyService.MSG_SET_INT_VALUE:
                    //  textIntValue.setText("Int Message: " + msg.arg1);
                    break;
                    //서버스에서 해당 액티비티에 메세지를 보낸 경우,
                case MyService.MSG_SET_STRING_VALUE:
                    String str1 = msg.getData().getString("chating");

                    assert str1 != null;
                    String[] results = str1.split("\\$");
                    Log.e("채팅대기방에서 받은 채팅",str1);
                    //방 만들기 메세지가 아닌, 채팅을 받았을 경우에
                    //메세지를 보낸 친구의 닉네임을 메세지에서 꺼내 지정해준다.
                    if (!results[0].equals("RommCreate")&&!results[0].equals("Roomin")&&!results[0].equals("/faceoff")) {          //방이 만들어지고, 사용자가 카톡을 보냈을 경우
                        if(!results[1].equals(userid)) {
                            친구이름 = results[0];            //누군가 만든 방이므로 친구이름을 두번째로 정해준다
                        }else {
                            친구이름 = username;
                        }
                        //메세지 내용도 꺼내온다.
                        String msm = results[4];
                            //해당 메세지의 채팅방이 있는 지 없는 지 유무를 찾음.
                            boolean roomYN =roomnumber.containsKey(results[1]+"$"+results[2]+"$"+results[3]);

                            //채팅방이 있을 때 해당 채팅방의 메세지 내용과 시간을 변경시킨다.
                        if(!results[0].equals("/facetime") && !results[0].equals("/faceoff")) {
                            if (roomYN) {
                                Log.e("채팅방이 잇다", "");
                                int rom = roomnumber.get(results[1] + "$" + results[2] + "$" + results[3]);
                                Profile obj = profiles.get(rom);
                                int profileint = profiles.indexOf(new Profile(obj.getUrl(), obj.getName(), obj.getMsg(), obj.getTime(), obj.getUserid(), obj.getRoomname(), obj.getUsername(), obj.getUserimage()));
                                // profiles.get(rom).setMsg(results[4]);
                                //profiles.get(rom).setTime(results[5]);
                                profiles.remove(new Profile(obj.getUrl(), obj.getName(), obj.getMsg(), obj.getTime(), obj.getUserid(), obj.getRoomname(), obj.getUsername(), obj.getUserimage()));
                                profiles.add(0,new Profile(results[6], 친구이름, msm, results[5], results[7], results[1] + "$" + results[2] + "$" + results[3], results[0], results[6]));

                                binding.myChatListView.getAdapter().notifyDataSetChanged();
                            } else {     //채팅방이 없으면 추가시킨다.
                                Log.e("채팅방이 없다", "");
                                profiles.add(0,new Profile(results[6], 친구이름, msm, results[5], results[7], results[1] + "$" + results[2] + "$" + results[3], results[0], results[6]));
                                roomnumber.put(results[1] + "$" + results[2] + "$" + results[3], profiles.size() - 1);
                                binding.myChatListView.getAdapter().notifyDataSetChanged();

                            }
                        }
                        // public Profile(String url, String name, String msg,String userid) {
                        //profiles.get()


                    }
                    // textStrValue.setText("Str Message: " + str1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_chatlist, container, false);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.myChatListView.setLayoutManager(manager);
        //현재 유저의 아이디를 서버에 보내, 채팅방 목록을 가져온다.
        SharedPreferences pref = getContext().getSharedPreferences("pref", MODE_PRIVATE);
        userid= pref.getString("ing", "s");

        profiles = new Vector<>();
       // Collections.reverse(profiles);

        roomnumber = new HashMap<>();
        //서비스가 실행중인지 확인 후, 서비스와 데이터를 주고받게 만들어준다.
        CheckIfServiceIsRunning();
        //서버에서 채팅방 가져오기
        getchatlist();

        binding.myChatListView.setAdapter(new ChatListAdapter(profiles, getContext(), getActivity()));
        View view = binding.getRoot();
        //here data must be an instance of the class MarsDataProvider
        return view;
    }


    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (MyService.isRunning()) {
            //서비스가 실행중이면 바인드를 실행한다.
            doBindService();
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            //서비스가 연결됐을 때 서비스의 메신저를 불러온다.
            mService = new Messenger(service);
            //   textStatus.setText("Attached.");
            try {
                //서비스에 메세지를 받는 액티비티를 추가시킨다.
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
            // textStatus.setText("Disconnected.");
        }
    };
    //바인드를 시작하고 현재 채팅방의 이름을 서비스로 보내서 노티를 띄울지 안 띄울지를 정한다.
    void doBindService() {
        getActivity().bindService(new Intent(getActivity(), MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        handler.post(new Runnable() {
            public void run() {
                sendChatroomToService(chatroom);
            }
        });
        // textStatus.setText("Binding.");
    }
    //바인드 해제시 현재 액티비티를 서비스와 데이터를 주고받지 못하게 만든다.
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
            getActivity().unbindService(mConnection);
            mIsBound = false;
            //  textStatus.setText("Unbinding.");
        }
    }

    //현재 액티비티가 파괴될 시 바인드를 해제
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }
    //서비스에 현재 채팅방 이름을 보내는 메소드다
    private void sendChatroomToService(String chatroom) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Bundle b = new Bundle();
                    b.putString("chatroom", chatroom);
                    Message msg = Message.obtain(null, MyService.MSG_SET_STRING_CHATROOM);
                    msg.setData(b);
                    mService.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    //서버에서 현재 유저의 채팅방 목록을 가져온다.
    public void getchatlist() {

        getfood apiInterface = RetroFitApiClient.getClient().create(getfood.class);           //서버와 연결을 시킨다.
        Call<List<ChatList>> call = apiInterface.getchatlist(userid);
        call.enqueue(new Callback<List<ChatList>>() {
            @Override
            public void onResponse(Call<List<ChatList>> call, Response<List<ChatList>> response) {
                if (response == null) {                                                       //서버에서 받지 못했을 경우.
                    Toast.makeText(getContext(), "오류", Toast.LENGTH_SHORT).show();
                } else {
                        profiles.clear();
                    for (ChatList chatli : response.body()) {
                        String friendID = null;
                        String[] results = chatli.getChatroom().split("\\$");

                        if(userid.equals(results[0])){
                            friendID = results[1];
                        }else friendID = results[0];

                        if(chatli.getStatus() != null) {
                            profiles.add(new Profile(chatli.getFriendImage(), chatli.getFriendName(), chatli.getStatus(), chatli.getStatustime(), friendID, chatli.getChatroom(),chatli.getUser_name(),chatli.getUser_image()));
                                                        //친구 사진             // 친구 이름            //채팅 내용             //채팅 시간             //친구 아이디
                            roomnumber.put(chatli.getChatroom(), profiles.size() - 1);
                            username = chatli.getUser_name();
                        }
                    }
                    binding.myChatListView.getAdapter().notifyDataSetChanged();
                    // 리스트에 계속 json 데이터를 축적시키며 추가한다.
                }
            }

            @Override
            public void onFailure(Call<List<ChatList>> call, Throwable t) {        //서버와 연결 실패 할 경우
                // Toast.makeText(getApplicationContext(), "서버에서 데이터를 받지 못 했습니다.: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ERROR: ", t.getMessage());
            }
        });

    }
}
