package com.example.seyoung.findtaste.Adapter;

/**
 * Created by seyoung on 2017-10-25.
 * 사용자가 맛집을 등록할 때 지도화면에서 검색하면 검색된 결과값을 나열시켜 보여줍니다.
 * 구글에서 검색해서 가져온 맛집의 이름과 주소를 가져온다.
 * 필터를 사용해서 실시간으로 데이터를 변경시킨다.
 */

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class PlaceArrayAdapter extends ArrayAdapter<PlaceArrayAdapter.PlaceAutocomplete> implements Filterable {

    public static final String TAG = "PlaceArrayAdapter";
    public GoogleApiClient mGoogleApiClient;    //구글에서 가져오기 때문에 구글클라이언트를 불러온다.
    public AutocompleteFilter mPlaceFilter;     //자동완성을 보여주기 위해 필터처리를 해준다.
    public LatLngBounds mBounds;                //한국에서만 검색을 할 수 있도록 거리를 지정해주는 위치바운드
    public ArrayList<PlaceAutocomplete> mResultList;    //자동완성된 데이터를 넣을 수 있는 리스트


    public PlaceArrayAdapter(Context context, int resource, LatLngBounds bounds,
                             AutocompleteFilter filter) {
        super(context, resource);
        mBounds = bounds;
        mPlaceFilter = filter;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {       //구글과 연결되면 구글에서 데이터를 가져올 수 있도록 세팅해준다.
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            mGoogleApiClient = null;
        } else {
            mGoogleApiClient = googleApiClient;
        }
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public PlaceAutocomplete getItem(int position) {
        return mResultList.get(position);
    }


    @Override
    public Filter getFilter() {             //필터처리를 해주는 메쏘드
        Filter filter = new Filter() {
            @Override
            public FilterResults performFiltering(CharSequence constraint) {
                //사용자가 작성한 검색어를 가지고 데이터를 가져와 나타낸다.
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    mResultList = getPredictions(constraint);

                    if (mResultList != null) {
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // 가져온 데이터가 있을 경우 어댑터를 새로고침해준다.
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                } else {    // 없을 경우 그대로 놔둔다.
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    //검색한 값으로 구글에서 데이터를 불러오는 메쏘드입니다.
    public ArrayList<PlaceAutocomplete> getPredictions(CharSequence constraint) {

        //구글과 연결이 되었을 때
        if (mGoogleApiClient != null) {

            //대한민국에서 검색한 결과값이 나오도록 대한민국을 사용자의 검색값과 붙여줍니다.
            Log.i(TAG, "Executing autocomplete query for: " + constraint);
            constraint = "대한민국 "+constraint.toString();
            //작성한 값을 지오코딩으로 위도 경도로 바꿔서 가져오도록 합니다.
            PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi
                    .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                            mBounds, mPlaceFilter);

            // 육십초 안에 받아오지 못할 경우 취소시킨다.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);

            //성공적으로 받아왔을 때
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(getContext(), "Error: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting place predictions: " + status
                        .toString());
                //서버에서 받아온 데이터를 버퍼시켜서 글자로 만들어준다.
                autocompletePredictions.release();
                return null;
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");
            //구글에서 받아온 데이터를 순서대로 리스트에 넣어줍니다.
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                    resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                            gettext((String) prediction.getFullText(null))));
            }
            autocompletePredictions.release();
            return resultList;
        }

        Log.e(TAG, "Google API client is not connected.");
        return null;
    }

    public class PlaceAutocomplete {
        //맛집의 이름과 주소를 받아오도록 합니다.

        public CharSequence placeId;
        public CharSequence description;

        PlaceAutocomplete(CharSequence placeId, CharSequence description) {
            this.placeId = placeId;
            this.description = description;
        }

        @Override
        public String toString() {
            return description.toString();
        }
    }
    // 구글에서 주소를 줄 때 앞에 대한민국을 지운채 리스트에 넣습니다.
    public String gettext(String string){
        string = string.substring((string.length())-(string.length()-5),string.length());
        return string;
    }
}
