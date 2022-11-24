package com.example.seyoung.findtaste.Adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seyoung.findtaste.R;
import com.example.seyoung.findtaste.model.FeedComment;

import java.util.List;

/**
 * Created by seyoung on 2017-11-24.
 * 댓글을 작성한 데이터를 레이아웃에 지정하는 어댑터입니다.
 * 사용자들이 작성한 댓글들을 나열해 보여줍니다.
 * 댓글을 작성한 사용자의 이름과 내용, 사진, 시간을 보여줍니다.
 *
 */

public class FeedCommentAdapter extends RecyclerView.Adapter<FeedCommentAdapter.MyViewHolder> {
    private List<FeedComment> FeedList;
    Context context;

    public FeedCommentAdapter(List<FeedComment> FeedList, Context context) {
        this.FeedList = FeedList;
        this.context = context;
    }
    //댓글을 단 시간을 나타내기 위해 지정한 초~ 달 단위
    private static class TIME_MAXIMUM{
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_com_item, parent, false);          //미리 지정한 커스텀 뷰를 팽창시킨다
        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final FeedComment item = FeedList.get(position);
        //item.getId();
        //댓글을 작성한 사용자의 이름을 넣습니다.
        holder.name.setText(item.getUsername());

        // 현재시간에서 작성한 리뷰의 시간의 차이를 계산해 보여줍니다. ex(3일 전.)
        long curTime = System.currentTimeMillis();
        long diffTime;
        diffTime = ( curTime- Long.valueOf(item.getTimestamp_user())) / 1000;
        String msg = null;

        if (diffTime < TIME_MAXIMUM.SEC) {
            msg = "방금 전";
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            msg = (diffTime) + "일 전";
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            msg = (diffTime) + "달 전";
        } else {
            msg = (diffTime) + "년 전";
        }
        holder.timestamp.setText(msg);

        //댓글 내용이 없으면 표시하지 않습니다.
        if (!TextUtils.isEmpty(item.getComment())) {
            holder.statusMsg.setText(item.getComment());
            holder.statusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            holder.statusMsg.setVisibility(View.GONE);
        }

        // 유저의 이미지를 글라이드로 넣어줍니다.
        Glide.with(context)                         //글라이드로 빠르게 사진을 넣는다.
                .load(item.getUserpic())
                .apply(new RequestOptions()
                        .error(R.drawable.fbnull)
                        .override(100, 100)
                        .centerCrop()
                        .circleCrop())
                .into(holder.profilePic);

        //댓글을 길게 누르면 삭제를 진행하게 해줍니다.
            View.OnLongClickListener listener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {


                        onItemLongClickListener.OnItemLongClick(item, position);
                        return true;
                    }

            };
            holder.name.setOnLongClickListener(listener);
            holder.timestamp.setOnLongClickListener(listener);
            holder.statusMsg.setOnLongClickListener(listener);
            holder.profilePic.setOnLongClickListener(listener);

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView timestamp;
        TextView statusMsg;
        ImageView profilePic;


        public MyViewHolder(View convertView) {
            super(convertView);

            name = (TextView) convertView.findViewById(R.id.name);
            timestamp = (TextView) convertView
                    .findViewById(R.id.timestamp);
            statusMsg = (TextView) convertView
                    .findViewById(R.id.comment);
            profilePic = (ImageView) convertView
                    .findViewById(R.id.profilePic);

        }
    }

    @Override
    public int getItemCount() {
        return FeedList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        FeedList.clear();
    }

    public void removeItem(int position) {
        int newPosition = position;
        FeedList.remove(newPosition);
    }

    private OnItemLongClickListener onItemLongClickListener;
    public OnItemLongClickListener getOnItemClickListener() {
        return onItemLongClickListener;
    }

    public void setOnLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }
    public interface OnItemLongClickListener{
        boolean OnItemLongClick(FeedComment item,int position);
    }

}