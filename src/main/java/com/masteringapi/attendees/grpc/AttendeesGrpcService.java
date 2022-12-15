package com.masteringapi.attendees.grpc;

import com.google.rpc.Code;
import com.google.rpc.Status;
import com.masteringapi.attendees.grpc.server.*;
import com.masteringapi.attendees.model.Attendee;
import com.masteringapi.attendees.model.AttendeeNotFoundException;
import com.masteringapi.attendees.model.AttendeeResponse;
import com.masteringapi.attendees.service.AttendeeStore;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


@GrpcService
public class AttendeesGrpcService implements AttendeesService {

    private final AttendeeStore store;

    private final Logger logger = LoggerFactory.getLogger(AttendeesGrpcService.class);

    @Inject
    public AttendeesGrpcService(AttendeeStore store) {
        this.store = store;
    }

    @Override
    public Uni<GetAttendeesResponse> getAttendees(GetAttendeesRequest request) {
        GetAttendeesResponse.Builder responseBuilder = GetAttendeesResponse.newBuilder();

        for(Attendee attendee: this.store.getAttendees()) {
            var gprcAttendee = com.masteringapi.attendees.grpc.server.Attendee.newBuilder()
                    .setId(attendee.getId())
                    .setGivenName(attendee.getGivenName())
                    .setEmail(attendee.getEmail())
                    .setSurname(attendee.getSurname())
                    .build();
            responseBuilder.addAttendees(gprcAttendee);
        }

        return Uni.createFrom().item(responseBuilder.build());
    }

    @Override
    public Uni<CreateAttendeeResponse> createAttendee(CreateAttendeeRequest request) {
        return null;
    }

    @Override
    public Uni<GetAttendeeResponse> getAttendee(GetAttendeeRequest request) {
        GetAttendeeResponse.Builder responseBuilder = GetAttendeeResponse.newBuilder();

        try {
            var attendee = this.store.getAttendee(request.getId());
            var grpcAttendee = com.masteringapi.attendees.grpc.server.Attendee.newBuilder()
                    .setId(attendee.getId())
                    .setGivenName(attendee.getGivenName())
                    .setSurname(attendee.getSurname())
                    .setEmail(attendee.getEmail())
                    .build();
            responseBuilder.setAttendee(grpcAttendee);
            return Uni.createFrom().item(responseBuilder.build());
        } catch (AttendeeNotFoundException e) {
            //FIXME How do I propagate a failure here?
            return null;
        }
    }

    @Override
    public Uni<DeleteAttendeeResponse> deleteAttendee(DeleteAttendeeRequest request) {
        return null;
    }

    @Override
    public Uni<UpdateAttendeeResponse> updateAttendee(UpdateAttendeeRequest request) {
        return null;
    }
}
