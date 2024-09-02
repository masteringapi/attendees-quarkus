package com.masteringapi.attendees.grpc;

import com.masteringapi.attendees.grpc.server.*;
import com.masteringapi.attendees.model.Attendee;
import com.masteringapi.attendees.model.AttendeeNotFoundException;
import com.masteringapi.attendees.service.AttendeeStore;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@GrpcService
public class AttendeesGrpcService implements AttendeesService {

    private final AttendeeStore store;

    private final Logger logger = LoggerFactory.getLogger(AttendeesGrpcService.class);

    private final Status attendeeNotFoundStatus;

    @Inject
    public AttendeesGrpcService(AttendeeStore store) {
        this.store = store;
        attendeeNotFoundStatus = Status.fromCode(Status.Code.NOT_FOUND)
                .withDescription("Attendee Not Found");
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
        CreateAttendeeResponse.Builder response = CreateAttendeeResponse.newBuilder();
        int id = this.store.addAttendee(new com.masteringapi.attendees.model.Attendee(request.getAttendee()));
        var attendee = com.masteringapi.attendees.grpc.server.Attendee.newBuilder().mergeFrom(request.getAttendee())
                .setId(id)
                .build();

        return Uni.createFrom().item(response.setAttendee(attendee).build());
    }

    @Override
    public Uni<GetAttendeeResponse> getAttendee(GetAttendeeRequest request) {
        try {
            GetAttendeeResponse.Builder responseBuilder = GetAttendeeResponse.newBuilder();
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
            throw new StatusRuntimeException(this.attendeeNotFoundStatus);
        }
    }

    @Override
    public Uni<DeleteAttendeeResponse> deleteAttendee(DeleteAttendeeRequest request) {
        try {
            this.store.removeAttendee(request.getId());
            return Uni.createFrom().item(DeleteAttendeeResponse.newBuilder().build());
        } catch(AttendeeNotFoundException e) {
            throw new StatusRuntimeException(this.attendeeNotFoundStatus);
        }
    }

    @Override
    public Uni<UpdateAttendeeResponse> updateAttendee(UpdateAttendeeRequest request) {
        return null;
    }
}
