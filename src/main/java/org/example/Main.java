package org.example;

import ai.picovoice.porcupine.PorcupineException;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    private static String URL = "ws://localhost:8080/gs-guide-websocket/";
    public static void main(String[] args) throws PorcupineException, ExecutionException, InterruptedException, IOException {

        List<Transport> transports = new ArrayList<>(2);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        WebSocketClient client = new SockJsClient(transports);

        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        MyStompSessionHandler sessionHandler = new MyStompSessionHandler();
        System.out.println("creating connection");
        CompletableFuture<StompSession> test = stompClient.connectAsync(URL, sessionHandler);
        StompSession stompSession = test.get();
        System.out.println("connected");

        String accessKey = args[0];

        MagicMirrorContext context = new MagicMirrorContext(accessKey);
        sessionHandler.setMagicMirrorContext(context);
        System.out.println("context created");


        while(true){
            context.update();
            stompSession.send("/app/hello", new Message(Integer.toString(context.getState())));
        }
    }
}