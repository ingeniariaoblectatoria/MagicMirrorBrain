package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
public class MyStompSessionHandler extends StompSessionHandlerAdapter {
    MagicMirrorContext magicMirrorContext = null;
    private Logger logger = LogManager.getLogger(MyStompSessionHandler.class);

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/topic/greetings", this);
        logger.info("Subscribed to /topic/greetings");
        logger.info("Message sent to websocket server");
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Greeting.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Greeting msg = (Greeting) payload;
        System.out.println("received something");
        if (magicMirrorContext != null){
            System.out.println("reporting external change");
            magicMirrorContext.reportExternalChange(Integer.parseInt(msg.getContent()));
        }
    }

    public void setMagicMirrorContext(MagicMirrorContext context){
        this.magicMirrorContext = context;
    }
}
