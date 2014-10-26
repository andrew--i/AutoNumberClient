package ai.autonumber.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.autonumber.R;
import ai.autonumber.cache.ChatMessagesCache;
import ai.autonumber.model.ChatMessage;
import ai.autonumber.model.User;
import ai.autonumber.util.ChatMessageLoader;


public class LazyChatItemsLoadAdapter extends BaseAdapter {


    private List<String> messagesIds = new ArrayList<String>();
    private LayoutInflater rowInflater;
    private ChatMessageLoader chatMessageLoader;

    public void add(String messageId) {
        for (String id : messagesIds) {
            if (messageId.equalsIgnoreCase(id))
                return;
        }

        messagesIds.add(messageId);
        Collections.sort(messagesIds, new Comparator<String>() {
            @Override
            public int compare(String m1, String m2) {
                Long ml1 = Long.parseLong(m1);
                Long ml2 = Long.parseLong(m2);
                return ml1.compareTo(ml2);
            }
        });
        notifyDataSetChanged();
    }

    public LazyChatItemsLoadAdapter(Context context, ChatMessagesCache chatMessagesCache) {
        chatMessageLoader = new ChatMessageLoader(chatMessagesCache, this);
        rowInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return this.messagesIds.size();
    }

    public String getItem(int index) {
        return this.messagesIds.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = rowInflater.inflate(R.layout.chat_item, parent, false);
        }
        chatMessageLoader.loadMessage(getItem(position), row);
        return row;
    }

    public void updateRow(View row, ChatMessage message) {
        TextView chatMessageView = (TextView) row.findViewById(R.id.chatMessage);
        chatMessageView.setText(message.getText());
        chatMessageView.setBackgroundResource(message.isLeft() ? R.drawable.bubble_yellow : R.drawable.bubble_green);
        LinearLayout.LayoutParams chatMessageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chatMessageLayoutParams.gravity = (message.isLeft() ? Gravity.START : Gravity.END);
        chatMessageView.setLayoutParams(chatMessageLayoutParams);

        ImageView userImageView = (ImageView) row.findViewById(R.id.userImage);
        userImageView.setImageResource(message.isLeft() ? R.drawable.user1 : R.drawable.user2);

        TextView userNameView = (TextView) row.findViewById(R.id.userName);
        userNameView.setText(message.getUser().getViewName());

        TextView messageTime = (TextView) row.findViewById(R.id.time);
        String time = message.getTime();
        messageTime.setText(time.substring(0, time.lastIndexOf(":")));
        LinearLayout.LayoutParams timeLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeLayoutParams.gravity = (message.isLeft() ? Gravity.END : Gravity.START);
        messageTime.setLayoutParams(timeLayoutParams);

        LinearLayout chatItemTop = (LinearLayout) row.findViewById(R.id.chatItemTop);
        chatItemTop.setGravity(!message.isLeft() ? Gravity.START : Gravity.END);
    }

    public void updateUserMessages(User user) {
        chatMessageLoader.updateMessages(user);
        notifyDataSetChanged();
    }
}