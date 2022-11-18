package com.jpgough;


import com.jpgough.attendees.Attendee;
import com.jpgough.attendees.AttendeeResponse;
import com.jpgough.attendees.AttendeesRequest;
import com.jpgough.attendees.AttendeesService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;


@GrpcService
public class AttendeesGrpcService implements AttendeesService {

    @Override
    public Uni<AttendeeResponse> getAttendees(AttendeesRequest request) {
        Attendee attendee = Attendee.newBuilder()
                .setEmail("jpgough@gmail.com")
                .setId(1)
                .setGivenName("Jim")
                .setSurname("Gough")
                .build();

        return Uni.createFrom().item(AttendeeResponse.newBuilder().addAttendees(attendee).build());

    }
}
