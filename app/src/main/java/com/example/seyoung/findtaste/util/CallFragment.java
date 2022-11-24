/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.seyoung.findtaste.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.views.ChatView.MyService;

import org.webrtc.RendererCommon.ScalingType;

/**
 * Fragment for call control.
 */
public class CallFragment extends Fragment {
  private View controlView;
  private TextView contactView;
  private ImageButton disconnectButton;
  private ImageButton cameraSwitchButton;
  private ImageButton videoScalingButton;
  private ImageButton toggleMuteButton;
  private TextView captureFormatText;
  private OnCallEvents callEvents;
  private SeekBar captureFormatSlider;
  private ScalingType scalingType;
  private boolean videoCallEnabled = true;
  String activityTag = "CallFragment";
  private final Handler handler = new Handler();  //서비스와 데이터를 주고받게 만드는 핸들러
  private Messenger mService=null;
  private final Messenger mMessenger = new Messenger(new IncomingHandler());
  //바인드 연결 유무
  private static boolean mIsBound = false;
  //채팅방 연결 유무를 나타낸다.

  String friendid;

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
            callEvents.onCallHangUp();
          }

          break;
        default:
          super.handleMessage(msg);
      }
    }
  }
  /**
   * Call control interface for container activity.
   */
  public interface OnCallEvents {
    void onCallHangUp();
    void onCameraSwitch();
    void onVideoScalingSwitch(ScalingType scalingType);
    void onCaptureFormatChange(int width, int height, int framerate);
    boolean onToggleMic();
  }

  @Override
  public View onCreateView(
          LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    controlView = inflater.inflate(R.layout.fragment_call, container, false);
    CheckIfServiceIsRunning();
    // Create UI controls.
    contactView = (TextView) controlView.findViewById(R.id.contact_name_call);
    disconnectButton = (ImageButton) controlView.findViewById(R.id.button_call_disconnect);
    cameraSwitchButton = (ImageButton) controlView.findViewById(R.id.button_call_switch_camera);
    videoScalingButton = (ImageButton) controlView.findViewById(R.id.button_call_scaling_mode);
    toggleMuteButton = (ImageButton) controlView.findViewById(R.id.button_call_toggle_mic);
    captureFormatText = (TextView) controlView.findViewById(R.id.capture_format_text_call);
    captureFormatSlider = (SeekBar) controlView.findViewById(R.id.capture_format_slider_call);

    // Add buttons click events.
    disconnectButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendFaceOffToService("/faceoff$"+friendid+"$");
        callEvents.onCallHangUp();
      }
    });

    cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        callEvents.onCameraSwitch();
      }
    });

    videoScalingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (scalingType == ScalingType.SCALE_ASPECT_FILL) {
          videoScalingButton.setBackgroundResource(R.drawable.ic_action_full_screen);
          scalingType = ScalingType.SCALE_ASPECT_FIT;
        } else {
          videoScalingButton.setBackgroundResource(R.drawable.ic_action_return_from_full_screen);
          scalingType = ScalingType.SCALE_ASPECT_FILL;
        }
        callEvents.onVideoScalingSwitch(scalingType);
      }
    });
    scalingType = ScalingType.SCALE_ASPECT_FILL;

    toggleMuteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean enabled = callEvents.onToggleMic();
        toggleMuteButton.setAlpha(enabled ? 1.0f : 0.3f);
      }
    });

    return controlView;
  }

  @Override
  public void onStart() {
    super.onStart();

    boolean captureSliderEnabled = false;
    Bundle args = getArguments();
    if (args != null) {
      String contactName = args.getString("username");
      contactView.setText(contactName);
      friendid = args.getString("friendid");
      videoCallEnabled = args.getBoolean(CallActivity.EXTRA_VIDEO_CALL, true);
      captureSliderEnabled = videoCallEnabled
              && args.getBoolean(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, false);
    }
    if (!videoCallEnabled) {
      cameraSwitchButton.setVisibility(View.INVISIBLE);
    }
    if (captureSliderEnabled) {
      captureFormatSlider.setOnSeekBarChangeListener(
              new CaptureQualityController(captureFormatText, callEvents));
    } else {
      captureFormatText.setVisibility(View.GONE);
      captureFormatSlider.setVisibility(View.GONE);
    }
  }

  // TODO(sakal): Replace with onAttach(Context) once we only support API level 23+.
  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    callEvents = (OnCallEvents) activity;
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
    getActivity().bindService(new Intent(getActivity(), MyService.class), mConnection, Context.BIND_AUTO_CREATE);
    mIsBound = true;
    Log.e("바인드 연결됌","OK");
    // textStatus.setText("Binding.");
  }

  //페이스타임을 서비스에 보낸다.
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
      getActivity().unbindService(mConnection);
      mIsBound = false;
      //  textStatus.setText("Unbinding.");
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    try {
      doUnbindService();
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
}