package net.objecthunter.larch.integration.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created by ruckus on 20.05.14.
 */
public class TestMessageListener implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(TestMessageListener.class);
    private boolean messageReceived = false;
    private Message lastMessage;
    @Override
    public void onMessage(Message message) {
        messageReceived = true;
        lastMessage = message;
        log.info("Received message");
    }

    public boolean isMessageReceived() {
        return messageReceived;
    }

    public Message getLastMessage() {
        return lastMessage;
    }
}
