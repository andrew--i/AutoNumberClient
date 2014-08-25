package ai.autonumber.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ai.autonumber.R;
import ai.autonumber.message.ChatMessage;

public class ChatItemsArrayAdapter extends ArrayAdapter<ChatMessage> {


    private List<ChatMessage> messages = new ArrayList<ChatMessage>();

    @Override
    public void add(ChatMessage object) {
        messages.add(object);
        super.add(object);
    }

    public ChatItemsArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.messages.size();
    }

    public ChatMessage getItem(int index) {
        return this.messages.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.chat_item, parent, false);
        }


        ChatMessage message = getItem(position);

        TextView chatMessageView = (TextView) row.findViewById(R.id.chatMessage);
        chatMessageView.setText(message.comment);
        chatMessageView.setBackgroundResource(message.left ? R.drawable.bubble_yellow : R.drawable.bubble_green);
        LinearLayout.LayoutParams chatMessageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chatMessageLayoutParams.gravity = (message.left ? Gravity.START : Gravity.END);
        chatMessageView.setLayoutParams(chatMessageLayoutParams);

        ImageView userImageView = (ImageView) row.findViewById(R.id.userImage);
        userImageView.setImageResource(message.left ? R.drawable.user1 : R.drawable.user2);
        LinearLayout.LayoutParams userImageLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        userImageLayoutParams.gravity = (!message.left ? Gravity.START : Gravity.END);
        userImageView.setLayoutParams(userImageLayoutParams);
        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}