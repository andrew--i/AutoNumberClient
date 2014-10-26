package ai.autonumber.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ai.autonumber.model.ChatMessage;
import ai.autonumber.model.User;

/**
 * Created by Andrew on 26.10.2014.
 */
public class ChatMessagesCache extends SQLiteOpenHelper {
    private static final String TABLE_CHAT_MESSAGES = "CHAT_MESSAGES";
    private final String tag = ChatMessagesCache.class.getName();

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MESSAGE = "message";

    private static final String DATABASE_NAME = "chatmessages.db";
    private static final int DATABASE_VERSION = 4;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_CHAT_MESSAGES + "(" + COLUMN_ID
            + " integer primary key, " + COLUMN_MESSAGE
            + " text not null);";

    private SQLiteDatabase database;

    public ChatMessagesCache(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public ChatMessage getMessage(String messageId) {
        if (database == null)
            open();
        Cursor cursor = database.query(TABLE_CHAT_MESSAGES,
                new String[]{COLUMN_MESSAGE}, COLUMN_ID + " = " + messageId, null,
                null, null, null);

        if (!cursor.moveToFirst())
            return null;
        String string = cursor.getString(0);
        cursor.close();
        return ChatMessage.fromJson(string);
    }

    public void addMessage(ChatMessage message) {
        if (getMessage(message.getMessageId()) != null)
            return;
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, Long.parseLong(message.getMessageId()));
        values.put(COLUMN_MESSAGE, message.toJson());
        database.insert(TABLE_CHAT_MESSAGES, null, values);
    }

    public void updateMessages(User user) {
        if (database == null)
            open();
        Cursor cursor = database.query(TABLE_CHAT_MESSAGES,
                new String[]{COLUMN_MESSAGE}, null, null,
                null, null, null);
        while (cursor.moveToNext()) {
            String string = cursor.getString(0);
            ChatMessage chatMessage = ChatMessage.fromJson(string);
            if (chatMessage.getUser().getId().equals(user.getId())) {
                chatMessage.setUser(user);
                updateMessage(chatMessage);
            }
        }
        cursor.close();
    }

    private void updateMessage(ChatMessage message) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE, message.toJson());
        database.update(TABLE_CHAT_MESSAGES, values, COLUMN_ID + " = " + message.getMessageId(), null);
    }

    public void open() {
        database = getWritableDatabase();
    }

    @Override
    public synchronized void close() {
        super.close();
        database = null;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

        Log.w(tag,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGES);
        onCreate(database);
    }


}
