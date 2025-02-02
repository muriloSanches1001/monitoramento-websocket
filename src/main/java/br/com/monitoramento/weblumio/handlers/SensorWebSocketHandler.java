package br.com.monitoramento.weblumio.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class SensorWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, List<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long sensorId = getSensorIdFromPath(session);
        log.info("Connected with {} id", sensorId);

        sessions.computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>()).add(session);
        sessionMap.put(sensorId, session);

        log.info("Current new sessions map: {}", sessionMap);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long sensorId = getSensorIdFromPath(session);
        log.info("Connection closed with sensor id: {}", sensorId);



        List<WebSocketSession> sessionList = sessions.get(sensorId);
        if (sessionList != null) {
            sessionList.remove(session);
            if (sessionList.isEmpty()) {
                sessions.remove(sensorId);
            }
        }

        log.info("Current sessions after close: {}", sessions);
    }

    public void sendSensorData(Long sensorId, String data) throws IOException {
        List<WebSocketSession> sessionList = sessions.get(sensorId);

        log.info("teste {}", sessionMap);

        log.info("All sessions: {}", sessions);
        log.info("Sessions to send data: {}", sessionList);
        if (sessionList != null) {
            for (WebSocketSession session : sessionList) {
                session.sendMessage(new TextMessage(data));
            }
        }
    }

    private Long getSensorIdFromPath(WebSocketSession session) {
        String path = session.getUri().getPath();
        return Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
    }
}