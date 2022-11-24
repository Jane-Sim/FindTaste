package com.example.seyoung.findtaste.listener;

import com.example.seyoung.findtaste.config.Constant;
import com.example.seyoung.findtaste.model.AddFriendItem;
import com.example.seyoung.findtaste.model.ChatList;
import com.example.seyoung.findtaste.model.Chattingstatus;
import com.example.seyoung.findtaste.model.FeedComment;
import com.example.seyoung.findtaste.model.FeedItem;
import com.example.seyoung.findtaste.model.ProfileMe;
import com.example.seyoung.findtaste.model.Tasteitem;
import com.example.seyoung.findtaste.model.Tastepath;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by seyoung on 2017-11-01.
 */

public interface getfood {
    //php서버에서 맛집 정보와 즐겨찾기 유무를 불러옵니다.
    @GET(Constant.URL_GETFOOD)
    Call<List<Tasteitem>> getFood(@Query("user_name") String user_name,
                                   @Query("lati") double latitude,
                                  @Query("logi") double longitude,
                                  @Query("sort") int sort);
    //사용자들의 리뷰를 요청합니다.
    @GET("get_feed.php")
    Call<List<FeedItem>> getFeed(@Query("user_name") String user_name);
    //지정한 맛집의 리뷰를 가져옵니다.
    @GET("get_feed2.php")
    Call<List<FeedItem>> getFeedcomment(@Query("user_name") String user_name,
                                 @Query("foodname") String food);
    //내가 적은 리뷰들을 가져옵니다.
    @GET("get_feed_my.php")
    Call<List<FeedItem>> getFeedmy(@Query("user_name") String user_name);

    //서버에 저장된 맛집 데이터를 지도액티비티에서 요청합니다.
    @GET("get_maptaste.php")
    Call<List<Tasteitem>> getmapFood(@Query("lati") double latitude
                                    ,@Query("logi") double longitude
                                    ,@Query("position") int positon
                                    ,@Query("sort") int sort);

    // 해당 유저의 즐겨찾기 목록을 불러옵니다.
    @GET("get_favor.php")
    Call<List<Tasteitem>> getfavor(@Query("user_name") String g);

    //php서버에서 맛집 정보를 불러드리는 값, 맛집 상세정보를 받음
    @GET(Constant.URL_GETTASTE)
    Call<List<Tasteitem>> gettaste(@Query("food_name") String tastename,
                                   @Query("user_name") String username,
                                   @Query("lati") double latitude
                                    ,@Query("logi") double longitude);

    @GET("get_tastedetail.php")
    Call<List<Tasteitem>> gettastedetail(@Query("food_name") String tastename,
                                   @Query("user_name") String username);

    //사용자가 검색창에 적은 맛집 데이터를 요청합니다.
    @GET("get_tasteresponse.php")
    Call<List<Tasteitem>> gettasteresponse(@Query("food_name") String tastename,
                                           @Query("user_name") String username,
                                           @Query("lati") double latitude
                                            ,@Query("logi") double longitude);
    //사용자들의 리뷰들을 요청합니다.
    @GET("get_reviewtaste.php")
    Call<List<Tasteitem>> getreviewtaste(@Query("user_name") String user_name
                                        ,@Query("lati") double latitude
                                        ,@Query("logi") double longitude);

    //사진의 경로들을 받아온다.
    @GET(Constant.URL_GETPATH)
    Call<List<Tastepath>> getpath(@Query("image_name") String image_name);

    // 리뷰의 댓글들을 요청
    @GET("get_feedcomment.php")
    Call<List<FeedComment>> getcomment(@Query("food_name") String tastename,
                                       @Query("user_name") String username,
                                       @Query("timestamp") String timestamp,
                                       @Query("user") String user);
    //리뷰에서 작성한 댓글을 서버에 추가합니다.
    @GET("uplode_feedcomment.php")
    Call<List<FeedComment>> putcomment(@Query("user_name") String username,
                                       @Query("food_name") String tastename,
                                       @Query("timestamp") String timestamp,
                                       @Query("user") String user,
                                       @Query("comment") String comment,
                                       @Query("timestamp_user") String timestamp_user);

    //해당 댓글을 삭제합니다.
    @GET("remove_feedcomment.php")
    Call<List<FeedComment>> removecomment(@Query("user_name") String username,
                                       @Query("food_name") String tastename,
                                       @Query("timestamp") String timestamp,
                                       @Query("user") String user,
                                       @Query("comment") String comment,
                                       @Query("timestamp_user") String timestamp_user);

    //사용자의 프로필 정보를 받아옴
    @GET("get_profile.php")
    Call<List<ProfileMe>> getPFme(@Query("id") String username,@Query("user") int user);

    //친구 추가창에서 검색한 유저를 불러온다.
    @GET("get_Alluser.php")
    Call<List<AddFriendItem>> getfindfriend(@Query("user_name") String username, @Query("friend_name") String friendname);

    //채팅 메세지를 가져온다.
    @GET("getchatting.php")
    Call<List<Chattingstatus>> getchatting(@Query("chatroom") String chatroom);

    //채팅방 리스트를 받아온다.
    @GET("get_chat_list.php")
    Call<List<ChatList>> getchatlist(@Query("userID") String userId);


}
