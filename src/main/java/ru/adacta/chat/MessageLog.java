package ru.adacta.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageLog {

    private long sendTimeMillis = 0;
    private long receivedTimeMillis = 0;
    private String status;
    private int senderUserId = 0;
    private int recipientUserId = 0;
    private long sendThreadId;
    private long receivedTreadId;

    public MessageLog(Long sendTimeMillis, String status, int senderUserId, long sendThreadId) {
        this.sendTimeMillis = sendTimeMillis;
        this.status = status;
        this.senderUserId = senderUserId;
        this.sendThreadId = sendThreadId;
    }


    @Override
    public String toString() {
        return String.format("sent [%d]: %d, received [%d]: %d", sendThreadId, sendTimeMillis, receivedTreadId, receivedTimeMillis );
    }
}
