package com.example.seyoung.findtaste.views.SeeTasteInfo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by seyoung on 2017-11-03.
 * 맛집의 상세페이지를 감싸는 '스크롤 뷰'에
 * 들어가있는 프래그먼트의 구글맵이 위아래로 스크롤이 안되기에 (왜냐면 맛집화면의 페이지를 스크롤되게 했기에, 구글맵은 위아래로 터치가 안된다.)
 * 프래그먼트에서 드래그를 하면 프래그먼트 안이 움직일 수 있도록 따로 지정을 했다
 * 터치이벤트를 지정해줘서 프래그먼트를 위아래로 드래그할 경우, 맛집화면의 스크롤을 제어하고 구글맵의 지도가 움직이게 만든다.
 */

public class WorkaroundMapFragment extends SupportMapFragment {
    private OnTouchListener mListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstance) {
        View layout = super.onCreateView(layoutInflater, viewGroup, savedInstance);

        TouchableWrapper frameLayout = new TouchableWrapper(getActivity());
        //프래그먼트가 터치가 되도록, 뷰를 겹쳐서 그룹으로 만들어줍니다.
        //이때 겹친 뷰가 중복되어 보이지 안도록 프레임레이아웃을 투명하게 만들어줍니다.(구글맵만 보이게 만들고 터치이벤트는 프레임레이아웃이 하게끔 만든다)
        frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        ((ViewGroup) layout).addView(frameLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return layout;
    }
    //터치가 되게 리스너를 설정해줍니다.
    public void setListener(OnTouchListener listener) {
        mListener = listener;
    }
    //현재 프래그먼트에 터치이벤트를 상속해줍니다.
    public interface OnTouchListener {
        public abstract void onTouch();
    }

    public class TouchableWrapper extends FrameLayout {

        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                //프레임레이아웃을 드래그 할 경우, 구글맵이 터치되도록 만들어준다 (부모의 스크롤을 뺏어옴)
                case MotionEvent.ACTION_DOWN:
                    mListener.onTouch();
                    break;
                case MotionEvent.ACTION_UP:
                    mListener.onTouch();
                    break;
            }
            return super.dispatchTouchEvent(event);
        }
    }
}