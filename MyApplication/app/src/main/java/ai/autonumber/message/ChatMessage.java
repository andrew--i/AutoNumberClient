package ai.autonumber.message;

public class ChatMessage {
    public boolean left;
    public String comment;
    private String from;

    public ChatMessage(boolean left, String comment, String from) {
        this.left = left;
        this.comment = comment;
        this.from = from;
    }

}