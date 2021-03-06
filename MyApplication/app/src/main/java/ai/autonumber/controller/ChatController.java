package ai.autonumber.controller;


import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ai.autonumber.activiti.AutoNumberChatActivity;
import ai.autonumber.R;
import ai.autonumber.adapter.LazyChatItemsLoadAdapter;
import ai.autonumber.cache.ChatMessagesCache;
import ai.autonumber.gcm.ServerUtilities;
import ai.autonumber.model.ChatMessage;
import ai.autonumber.model.User;

public class ChatController extends Controller {


    public ChatController(AutoNumberChatActivity activity, final ControllerManager controllerManager, ChatMessagesCache chatMessagesCache) {
        super(activity, controllerManager);
        //init chat view
        ListView chatView = (ListView) findViewById(R.id.chatView);
        LazyChatItemsLoadAdapter adapter = new LazyChatItemsLoadAdapter(activity.getApplicationContext(), chatMessagesCache);
        chatView.setAdapter(adapter);
    }

    @Override
    public List<Integer> getUsingControlsIds() {
        return Arrays.asList(R.id.chatView, R.id.camButton, R.id.chatInput, R.id.chatButton);
    }

    @Override
    protected void onHandleStartActive() {
        final Button camButton = (Button) findViewById(R.id.camButton);
        final Button chatButton = (Button) findViewById(R.id.chatButton);
        final EditText chatInput = (EditText) findViewById(R.id.chatInput);
        chatButton.setText(R.string.sendButtonName);
        camButton.setText(R.string.mainButtonName);
        chatInput.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.2f));
        camButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
        chatButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addChatMessageFromInput();
            }
        });

        chatButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    addChatMessageFromInput();
                    return true;
                }
                return false;
            }
        });

        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controllerManager.setMainControllerActive();
            }
        });
    }

    @Override
    protected void onHandleEndActive() {

    }


    public void handleChatMessage(String messageId) {
        final ListView chatView = (ListView) findViewById(R.id.chatView);
        ((LazyChatItemsLoadAdapter) chatView.getAdapter()).add(messageId);
        chatView.post(new Runnable() {
            @Override
            public void run() {
                chatView.setSelection(chatView.getAdapter().getCount() - 1);
            }
        });
    }

    private void addChatMessageFromInput() {
        final EditText chatInput = (EditText) findViewById(R.id.chatInput);
        String comment = chatInput.getText().toString();
        chatInput.setText("");
        sendMessage(comment);
    }


    protected void sendMessage(final String message) {
        runAsync(new Action() {
            @Override
            public void doAction() throws IOException {
                ServerUtilities.chatMessage(message, activity.regid);
            }
        });
    }

    @Override
    public void resumeController() {
        restoreChatMessages();
    }

    protected void restoreChatMessages() {
        runAsync(new Action() {
            @Override
            public void doAction() throws IOException {
                ServerUtilities.restoreChatMessages(activity.regid, -1);
            }
        });
    }

    public void handleChangeUserInfo(final User user) {
        final ListView chatView = (ListView) findViewById(R.id.chatView);
        chatView.post(new Runnable() {
            @Override
            public void run() {
                ((LazyChatItemsLoadAdapter) chatView.getAdapter()).updateUserMessages(user);
            }
        });
    }
}
