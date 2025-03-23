package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.websocket.ClientEndpointConfig;

public class StompClient {
    private static Logger logger = LogManager.getLogger(StompClient.class);

    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    public ListenableFuture<StompSession> connect() {

        try {
            Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
            List<Transport> transports = Collections.singletonList(webSocketTransport);

            SockJsClient sockJsClient = new SockJsClient(transports);
            sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

            String url = "ws://localhost:8080/hello";
            return stompClient.connect(url, headers, new MyHandler(), "localhost", 8080);
        } catch (Exception e){
           e.printStackTrace();
        }
        return null;
    }

    public void subscribeMessages(StompSession stompSession, MagicMirrorContext context) throws ExecutionException, InterruptedException {
        stompSession.subscribe("/topic/greetings", new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                context.reportExternalChange(Integer.parseInt(new String((byte[]) o)));
            }
        });
    }

    public void sendHello(StompSession stompSession, int status ) {
        String jsonHello = "{ \"status\" : \""+Integer.toString(status)+"\" }";
        stompSession.send("/app/hello", jsonHello.getBytes());
    }

    private class MyHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            logger.info("Now connected");
        }
    }
}
