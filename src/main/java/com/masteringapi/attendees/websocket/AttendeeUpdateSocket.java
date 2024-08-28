package com.masteringapi.attendees.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masteringapi.attendees.model.Attendee;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/ws/attendees")
@ApplicationScoped
public class AttendeeUpdateSocket {

    private final ObjectMapper objectMapper;
    private Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private Logger log = LoggerFactory.getLogger(AttendeeUpdateSocket.class);

    @Inject
    public AttendeeUpdateSocket(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @OnOpen
    public void onOpen(Session session) {
        log.info("Session opened: {}", session.getId());
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Session closed: {}", session.getId());
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("Session error {}: ",session.getId());
        sessions.remove(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        //No longer respond to inbound messages
    }

    public void broadcastNewAttendee(Attendee attendee) {
        if(! sessions.isEmpty()) {
            sessions.forEach(session -> {
                if(session.isOpen()) {
                    try {
                        session.getAsyncRemote().sendText(objectMapper.writeValueAsString(attendee));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

}
