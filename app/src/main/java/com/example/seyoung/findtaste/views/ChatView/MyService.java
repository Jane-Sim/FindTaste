package com.example.seyoung.findtaste.views.ChatView;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.config.Constant;
import com.example.seyoung.findtaste.util.wait_facetime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by seyoung on 2017-12-27.
 *
 * 서비스로 자바 서버와 연결해서 값을 주고 받습니다.
 * 채팅으로 받은 메세지를 서비스와 연결된 모든 액티비티에 보내줄 수 있습니다.
 * 만약 채팅방의 대기방일 경우, 채팅방의 리스트를 만들어줄 수 있습니다.
 * 채팅방일 경우, 메세지를 받거나, 다른 방의 채팅글을 받을 수 있습니다.
 */

public class MyService extends Service {
    NotificationManager nm;
    private NotificationManager notifManager;
    //int incrementby = 1;
    String chat = "", chatroom="null", facetime = "", faceoff = "";  //채팅 내용과 채팅방 이름입니다.
    private static boolean isRunning = false;   //서비스가 실행 중인지 판단

    ArrayList<Messenger> mClients = new ArrayList<>(); //액티비티들을 넣을 어레이리스트입니다.
    //HashMap<String,Messenger> mClients = new HashMap<>();
    //int mValue = 0; // Holds last value set by a client.
    public static final int MSG_REGISTER_CLIENT = 1;    // 액티비티들을 서비스에 등록시킬 때 사용
    public static final int MSG_UNREGISTER_CLIENT = 2;  // 액티비티가 종료 됐을 때, 서비스에서 해당 액티비티를 삭제해주는 값
    public static final int MSG_SET_INT_VALUE = 3;      //현재는 사용 x
    public static final int MSG_SET_STRING_VALUE = 4;   // 자바서버에서 받아온 메세지를 액티비티에 보낼 때 사용
    public static final int MSG_SET_STRING_CHATROOM = 5;// 사용자가 채팅방에 들어갔을 때, 해당 방 이름을 서비스에 보내줄 때 사용
    public static final int MSG_SET_STRING_FACETIME = 6;// 사용자가 영상통화를 요청 할 때 사용
    public static final int MSG_SET_STRING_FACEOFF = 7; // 상대방 사용자가 영상통화를 거부할 때 사용
    Handler mHandler = null;

    final Messenger mMessenger = new Messenger(new IncomingHandler()); // 액티비티와 서비스 간 데이터를 전달할 수 있도록 해주는 Messenger입니다.
    private final static String IP = "115.71.237.176";  // 서버의 아이피 주소
    private final static int PORT = 6003;   //자바서버의 소켓번호입니다.
    Socket socket;  //소켓
    InputStream in;
    //OutputStream out;
    private String txtReceive;      //자바 서버에서 받아올 메세지
    static String username="",userimage=""; //유저 이름과 유저 이미지
    private final Handler handler = new Handler();  //액티비티와 서비스와의 데이터를 주고받게 해주는 핸들러.
    View layout;

    String userId="";

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    //액티비티에서 전달받은 데이터를 자바서버에 보내줍니다.
    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                        mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_INT_VALUE:
                    //  incrementby = msg.arg1;
                    break;
                //액티비티에서 채팅방이름을 전달받았을 때입니다.
                case MSG_SET_STRING_CHATROOM:
                    chatroom = msg.getData().getString("chatroom");
                    //액티비티가 대기방이나 다른 액티비티화면에 있지 않았을 경우에 보낸 데이터일 때,
                    //사용자가 채팅방을 만들어다라고 서비스에 요청했을 때 자바서버에 방을 만듭니다.

                    if(!"out".equals(chatroom)&&!chatroom.equals("wait")){

                        String chatsend = "/made$" + chatroom+"$maderoom";
                        Log.e("채팅방 만들기",chatroom);
                        if (socket != null && socket.isConnected()) {
                            PrintWriter out;
                            try {
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                out = new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket.getOutputStream())), true);
                                // Log.e("보내지는 값",id +":"+"0:"+ msg+":"+thisTime);
                                out.println(chatsend + "\r\n");
                                out.flush();
                                //Toast.makeText(c)
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                //채팅방에서 메세지를 서비스로 보냈을 경우, 메세지를 자바서버에 보내줍니다.
                case MSG_SET_STRING_VALUE:
                    chat = msg.getData().getString("chating");
                    //소켓이 연결된 경우에만 보내줍니다.
                    if(socket == null || !socket.isConnected() || socket.isClosed()){
                        try {
                            Log.e("소켓 연결안되어서" , "안보내짐");
                            socket = new Socket(IP, PORT);
                            PrintWriter out2 = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                            Log.e("유저 만드는 값", "/user" + "$" + userId + "$inuser");
                            out2.println("/user" + "$" + userId + "$inuser" + "\r\n");
                            out2.flush();
                            PrintWriter out;
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                            // Log.e("보내지는 값",id +":"+"0:"+ msg+":"+thisTime);
                            out.println(chat+"\r\n");
                            out.flush();
                            //Toast.makeText(c)
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                    }
                    if (socket != null && socket.isConnected()) {
                        Log.e("소켓 연결되어서" , "보내짐");
                        PrintWriter out;
                        try {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                            // Log.e("보내지는 값",id +":"+"0:"+ msg+":"+thisTime);
                            out.println(chat+"\r\n");
                            out.flush();
                            //Toast.makeText(c)
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                //원하는 유저에게 영상통화를 걸 때, 자바서버에 알려줍니다.
                case MSG_SET_STRING_FACETIME :
                    facetime = msg.getData().getString("facetime");
                    if (socket != null && socket.isConnected()) {
                        Log.e("소켓 연결되어서" , "페이스타임 보내짐");
                        PrintWriter out;
                        try {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                            // Log.e("보내지는 값",id +":"+"0:"+ msg+":"+thisTime);
                            out.println(facetime+"\r\n");
                            out.flush();
                            //Toast.makeText(c)
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                // 영상통화를 받는 상대방이 거절하기를 했을 때, 자바서버에 알려줍니다.
                case MSG_SET_STRING_FACEOFF :
                    faceoff = msg.getData().getString("faceoff");
                    if (socket != null && socket.isConnected()) {
                        Log.e("소켓 연결되어서" , "페이스타임 보내짐");
                        PrintWriter out;
                        try {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            out = new PrintWriter(new BufferedWriter(
                                    new OutputStreamWriter(socket.getOutputStream())), true);
                            // Log.e("보내지는 값",id +":"+"0:"+ msg+":"+thisTime);
                            out.println(faceoff+"\r\n");
                            out.flush();
                            //Toast.makeText(c)
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    // 액티비티들에 자바서버에서 받은 메세지를 보내줍니다.
    private void sendMessageToUI(String send) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {

                Bundle b = new Bundle();
                b.putString("chating", send);
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }

        String[] results = send.split("\\$");
        //자바서버에서 채팅방 만들어준 게 아닌, 메세지를 전달해 주었을 때 액티비티에 노티피케이션을 띄워줍니다.
        if(results.length>6) {

            if (!results[0].equals("RommCreate") && !results[0].equals("/facetime")&& !results[0].equals("/faceoff")) {
                Log.e("현재 방 이름",chatroom);
                Log.e("서버에서 받은 방이름",results[1] + "$" + results[2] + "$" + results[3]);
                Log.e("현재 유저 이름",userId);
                Log.e("서버에서 받은 유저 이름",results[7]);

                if (!chatroom.equals(results[1] + "$" + results[2] + "$" + results[3])&&!userId.equals(results[7])&&!"Roomin".equals(results[1])) {
                    showNotification(send);
                } else {

                }

            }
        }
        else if(results.length<6) {
            // chatroom = results[1] + "$" + results[2] + "$" + results[3];
        }



    }
/*    private void sendMessageToFaceOffUI(String send) {
                Bundle b = new Bundle();
                b.putString("faceoff", send);
                Message msg = Message.obtain(null, MSG_SET_INT_VALUE);
                msg.setData(b);
                try {
                    mClients.get("CallFragment").send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyService", "Service Started.");

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);//검색한 값을 리스트에 추가하기 전에,
        userId = pref.getString("ing", "");
        //해당 유저의 아이디와 이미지 경로를 서버에서 받아옵니다.
        GetUserName GU = new GetUserName();
        GU.execute(userId);
        //서비스와 자바 서버를 연결을 시킵니다.

        serviceThread serviceThread = new serviceThread();
        serviceThread.start();

        isRunning = true;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        layout = inflater.inflate(R.layout.chattingtoast, null);

    }

    //채팅을 주고받는 스레드가 죽으면 다시 시작시켜주는 스레드
    class serviceThread extends Thread {
        public void run() {

            while(true)
            {
                try {
                    Log.e("쓰레드를","연결시킨다");
                    InputThread inputThread = new InputThread(IP, PORT);
                    inputThread.start();
                    inputThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    private void showNotification(String send) {

        final int NOTIFY_ID = 1002;
        String[] results = send.split("\\$");
        // There are hardcoding only for show it's just strings
        String name = "my_package_channel";
        String id = "my_package_channel_1"; // The user-visible name of the channel.
        String description = "my_package_first_channel"; // The user-visible description of the channel.

        Intent intent;
        PendingIntent pendingIntent;
        final NotificationCompat.Builder builder;

        if (notifManager == null) {
            notifManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        //26버전 이상일 경우 노티피케이션을 화면에 보이도록 띄워주며, 상대방의 이미지와
        //메세지 내용, 닉네임을 보이도록 지정합니다.
        //또한 노티피케이션이 발생할 때 소리와 진동을 내서 사용자가 알 수 있게 만들어줍니다.
        //사용자가 해당 노티를 클릭하면 해당 채팅방 액티비티로 이동시켜줍니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            assert notifManager != null;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, id);

            intent = new Intent(MyService.this, ChatRoomActivity.class);
            //해당 액티비티에 채팅방 이름과 상대방 아이디, 닉네임을 보내줍니다.
            intent.putExtra("name",username);
            Log.e("보내는 유저이름",username);
            intent.putExtra("friend",results[0]);
            intent.putExtra("friendid",results[7]);
            intent.putExtra("chatroom",results[1]+"$"+results[2]+"$"+results[3]);
            Log.e("노티에서 보내는 방이름",results[1]+"$"+results[2]+"$"+results[3]);
            intent.putExtra("fImg",userimage);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentTitle(results[0])  // required
                    .setSmallIcon(R.drawable.kakaotalk_icon) // required
                    .setContentText(results[4])  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(results[4])
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        }
        //26버전 이하일 경우입니다. 위에 같음
        else {

            builder = new NotificationCompat.Builder(getApplicationContext());

            intent = new Intent(MyService.this, ChatRoomActivity.class);

            intent.putExtra("name",username);
            Log.e("보내는 사람이름",username);
            intent.putExtra("friend",results[0]);
            intent.putExtra("friendid",results[7]);
            intent.putExtra("chatroom",results[1]+"$"+results[2]+"$"+results[3]);
            intent.putExtra("fImg",userimage);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentTitle(results[0])                         // required
                    .setSmallIcon(R.drawable.kakaotalk_icon) // required
                    .setContentText(results[4])  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(results[4])
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_DEFAULT);
        } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //글라이드로 노티피케이션의 사진을 만들어준다.
        Glide.with(getApplicationContext())
                .asBitmap()
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .fitCenter()
                        .centerCrop()
                        .circleCrop())
                .load(results[6])
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        Bitmap bitmap = ImageUtils.getCircularBitmap(resource);
                        builder.setLargeIcon(bitmap);
                        NotificationManagerCompat.from(getApplicationContext()).notify(NOTIFY_ID, builder.build());
                    }
                });

        //토스트창의 사진과 닉네임, 메세지내용을 설정해주고 띄워줍니다.
        ImageView image = (ImageView) layout.findViewById(R.id.image);
        Glide.with(getApplicationContext())
                .load(results[6])
                .apply(new RequestOptions()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .circleCrop())
                .into(image);
        TextView text = (TextView) layout.findViewById(R.id.name);
        text.setText(results[0]);

        TextView text2 = (TextView) layout.findViewById(R.id.chat);
        text2.setText(results[4]);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(3000);
        toast.setView(layout);
        toast.show();

    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyService", "Received start id " + startId + ": " + intent);
        //  START_REDELIVER_INTENT; // run until explicitly stopped.
        return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);

    }

    public static boolean isRunning() {
        return isRunning;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("MyService", "Service Stopped.");
        isRunning = false;

    }
    //자바서버와 연결시켜주는 쓰레드입니다.
    class InputThread extends Thread {
        private BufferedReader br = null;
        Socket sock;
        String IP;
        int PORT;
        OutputStream out;
        int me = 0;

        InputThread(String IP, int PORT) {
            this.IP = IP;
            this.PORT = PORT;
        }

        public void run() {
            try {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                sock = new Socket(IP, PORT);
                socket = sock;
                in = sock.getInputStream();
                out = sock.getOutputStream();
                int size;
                byte[] w = new byte[10240];
                txtReceive = "";
                // String a ="";
                //어플이 실행되면 해당 유저를 자바서버에 입장시킵니다.
                if (me == 0) {
                    StrictMode.ThreadPolicy policy2 = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy2);
                    PrintWriter out2 = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())), true);
                    Log.e("유저 만드는 값", "/user" + "$" + userId + "$inuser");
                    out2.println("/user" + "$" + userId + "$inuser" + "\r\n");
                    out2.flush();
                    me++;
                }
                boolean connected = !sock.getKeepAlive();
       /*             if (connected) {
                        *//*Log.e("소켓이","죽어이따 안살아잇음");
                        sock = new Socket(IP, PORT);
                        socket = sock;
                        PrintWriter out2 = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);
                        Log.e("유저 만드는 값", "/user" + "$" + userId + "$inuser");
                        out2.println("/user" + "$" + userId + "$inuser" + "\r\n");
                        out2.flush();*//*
                       this.interrupt();
                    }*/
                while (sock != null && sock.isConnected() && !sock.isClosed() ) {
                    if (!sock.isConnected()) {
                        Log.e("소켓이","죽어이따 while안");
                     /*   sock = new Socket(IP, PORT);
                        socket = sock;
                        PrintWriter out2 = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())), true);
                        Log.e("유저 만드는 값", "/user" + "$" + userId + "$inuser");
                        out2.println("/user" + "$" + userId + "$inuser" + "\r\n");
                        out2.flush();*/
                     this.interrupt();
                    }
                    try {
                        /*    if (sock == null) {
                                Log.e("소켓이", "죽어이따 null");
                                sock = new Socket(IP, PORT);
                                socket = sock;
                                PrintWriter out2 = new PrintWriter(new BufferedWriter(
                                        new OutputStreamWriter(socket.getOutputStream())), true);
                                Log.e("유저 만드는 값", "/user" + "$" + userId + "$inuser");
                                out2.println("/user" + "$" + userId + "$inuser" + "\r\n");
                                out2.flush();
                            }*/
                        //Log.e("소켓이", "살아이따");
                        size = in.read(w);
                        if (size <= 0)
                            continue;
                        txtReceive = new String(w, 0, size, "UTF-8");
                        Log.e("받는 값", txtReceive);
                        //자바서버에서 메세지를 받을 때마다 액티비티에 보내주며 서버에 저장시킵니다.
                        handler.post(new Runnable() {
                            public void run() {

                                sendMessageToUI(txtReceive);

                                String[] results = txtReceive.split("\\$");
                                if(results[0].equals("/facetime")){
                                    Intent i = new Intent(getApplicationContext(), wait_facetime.class);
                                    i.putExtra("영상통화","yes");
                                    i.putExtra("roomEditText",results[3]);
                                    i.putExtra("userid",results[2]);
                                    i.putExtra("friendid",results[1]);
                                    i.putExtra("userPic",results[4]);
                                    i.putExtra("friendname",results[5]);
                                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    PendingIntent p = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
                                    try {
                                        p.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }

                                }else if(results[0].equals("/faceoff")){
                                    //
                                }else {
                                    if (!results[0].equals("RommCreate") && !results[0].equals("Roomin") && userId.equals(results[7])) {
                                        uploadServer upserv = new uploadServer();
                                        upserv.execute(results[7], results[1] + "$" + results[2] + "$" + results[3], results[5], results[4]);
                                    }
                                }
                            }
                        });
                    }catch(IOException e) {
                        Log.e("소켓이","죽어이따 catch");
                        this.interrupt();
                        // sock = null;
                        // socket = null;
                        System.err.println(e);
                    }
                }


            } catch(UnknownHostException | SocketException e){
                Log.e("소켓이","죽어이따 SocketException");
                this.interrupt();
                // sock = null;
                // socket = null;
            } catch(IOException e) {
                Log.e("소켓이","죽어이따IOException");
                this.interrupt();
                // sock = null;
                // socket =null;
                System.err.println(e);
            }
            finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                    if (sock != null) {
                        try {
                            sock.close();
                            socket.close();
                            Log.e("소켓이","죽어이따 finally");
                            //sock = null;
                            //socket = null;
                            this.interrupt();
                        } catch (IOException e) {
                            Log.e("소켓이","죽어이따 finallyIOException");
                            this.interrupt();
                        }
                        //socket.close();
                    }
                } catch (Exception ignored) {
                    Log.e("소켓이","죽어이따 맨아래");
                    this.interrupt();
                    //sock = null;
                    //socket = null;
                }
            }
        }
    }
    //서버에 메세지 내용을 저장시켜줍니다.
    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    class uploadServer extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected String doInBackground(String... params) {
            //보낸 유저 아이디와 방이름, 시간, 메세지내욜을 보냅니다.
            String UserID = (String) params[0];
            String chatroom = (String) params[1];
            String time = (String) params[2];
            String status = (String) params[3];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1 + "upload_chatstatus.php";
            String postParameters = "UserID=" + UserID + "&chatroom=" + chatroom + "&time=" + time + "&status=" + status;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {

                return new String("Error: " + e.getMessage());
            }

        }
    }
    //해당 유저의 닉네임과 사진을 받아옵니다.
    @SuppressLint("StaticFieldLeak")
    class GetUserName extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            if(result.equals("fail"))
                username = result;

            Log.e("받아온 유저이름",result);
            super.onPostExecute(result);
        }

        @Override
        protected String doInBackground(String... params) {

            String name = (String)params[0];
            String ur1 = Constant.URL_BASE;
            String serverURL = ur1+"get_username.php";
            String postParameters = "name=" + name ;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();
                String[] results = sb.toString().split("\\$");

                username = results[0];
                userimage = results[1];
                return sb.toString();

            } catch (Exception e) {

                return new String("Error: " + e.getMessage());
            }

        }
    }

}