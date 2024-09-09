package com.masteringapi.attendees.service;

import com.masteringapi.attendees.model.Attendee;
import com.masteringapi.attendees.model.AttendeeNotFoundException;
import com.masteringapi.attendees.websocket.AttendeeUpdateSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collection;

@ApplicationScoped
public class AttendeeStore {
    private final AttendeeUpdateSocket attendeeUpdateSocket;

    @Inject
    public AttendeeStore(AttendeeUpdateSocket attendeeUpdateSocket) {
        this.attendeeUpdateSocket = attendeeUpdateSocket;
    }

    @Transactional
    public Collection<Attendee> getAttendees() {
        return Attendee.findAll().list();
    }

    @Transactional
    public Attendee getAttendee(Integer id) throws AttendeeNotFoundException {
        Attendee attendee = Attendee.findById(id);
        if(attendee != null) {
            return attendee;
        } else {
            throw new AttendeeNotFoundException();
        }
    }

    @Transactional
    public int addAttendee(Attendee attendee) {
        attendee.setId(null);
        attendee.persist();
        this.attendeeUpdateSocket.broadcastNewAttendee(attendee);
        return attendee.getId();
    }

    @Transactional
    public void removeAttendee(Integer id) throws AttendeeNotFoundException {
        Attendee.deleteById(id);
    }
}
