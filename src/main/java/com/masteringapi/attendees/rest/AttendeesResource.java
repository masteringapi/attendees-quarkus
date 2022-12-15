package com.masteringapi.attendees.rest;

import com.masteringapi.attendees.model.Attendee;
import com.masteringapi.attendees.model.AttendeeNotFoundException;
import com.masteringapi.attendees.model.AttendeeResponse;
import com.masteringapi.attendees.service.AttendeeStore;
import org.jboss.resteasy.reactive.RestResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.ArrayList;

@Path("/")
public class AttendeesResource {

    private final AttendeeStore store;

    @Inject
    public AttendeesResource(AttendeeStore store) {
        this.store = store;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("attendees")
    public AttendeeResponse attendees() {
        return new AttendeeResponse(new ArrayList<>(this.store.getAttendees()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("attendees/{id}")
    public RestResponse<Attendee> attendeeById(Integer id) {
        try {
            return RestResponse.ResponseBuilder.ok(this.store.getAttendee(id)).build();
        } catch (AttendeeNotFoundException e) {
            return RestResponse.notFound();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("attendees")
    public RestResponse<Object> createAttendee(Attendee attendee) {
        int id = this.store.addAttendee(attendee);
        URI uri = URI.create("/attendees/" + id);
        return RestResponse.ResponseBuilder.created(uri).build();
    }

    @DELETE
    @Path("attendees/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<Object> deleteAttendee(Integer id) {
        try {
            this.store.removeAttendee(id);
            return RestResponse.ok();
        } catch (AttendeeNotFoundException e) {
            return RestResponse.notFound();
        }
    }

    @PUT
    @Path("attendees/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Attendee> updateAttendee(Integer id, Attendee attendee) {
        try {
            this.store.updateAttendee(id, attendee);
            return RestResponse.noContent();
        } catch (AttendeeNotFoundException e) {
            return RestResponse.notFound();
        }
    }
}