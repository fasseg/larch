
package net.objecthunter.larch.integration.helpers;

import java.util.LinkedList;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ruckus on 20.05.14.
 */
public class TestMessageListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(TestMessageListener.class);

    private LinkedList<Message> messages = new LinkedList<>();

    @Override
    public void onMessage(Message message) {
        messages.add(message);
        log.info("Received message");
    }

    public boolean isMessageReceived() {
        return messages.size() > 0;
    }

    public Message getMessage() {
        return messages.poll();
    }
}
