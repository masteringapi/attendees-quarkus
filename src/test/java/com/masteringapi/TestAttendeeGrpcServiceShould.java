package com.masteringapi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.masteringapi.attendees.grpc.AttendeesGrpcService;
import com.masteringapi.attendees.grpc.server.*;
import com.masteringapi.attendees.model.Attendee;
import com.masteringapi.attendees.model.AttendeeNotFoundException;
import com.masteringapi.attendees.service.AttendeeStore;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class TestAttendeeGrpcServiceShould {
    @InjectMock
    private AttendeeStore mockAttendeeStore;

    @GrpcClient
    AttendeesService attendeesClient;

    @Test
    void return_empty_list_when_no_attendees_in_store() {
        when(mockAttendeeStore.getAttendees()).thenReturn(new ArrayList<>());

        GetAttendeesResponse response = attendeesClient.getAttendees(GetAttendeesRequest.newBuilder().build()).await().atMost(Duration.ofSeconds(5));

        assertThat(response.getAttendeesList().isEmpty(), equalTo(true));
    }

    @Test
    void return_attendee_when_in_store() {
        List<Attendee> attendees = new ArrayList<>();
        attendees.add(testAttendee());
        when(mockAttendeeStore.getAttendees()).thenReturn(attendees);

        GetAttendeesResponse response = attendeesClient.getAttendees(GetAttendeesRequest.newBuilder().build()).await().atMost(Duration.ofSeconds(5));
        assertThat(response.getAttendeesList().size(), equalTo(1));
        assertThat(response.getAttendeesList().get(0).getEmail(), equalTo("jim@gough"));
    }

    @Test
    void throw_an_error_when_attendee_does_not_exist() throws AttendeeNotFoundException {
        GetAttendeeRequest request = GetAttendeeRequest.newBuilder().setId(1).build();
        when(mockAttendeeStore.getAttendee(anyInt())).thenThrow(new AttendeeNotFoundException());

        GetAttendeeResponse response = attendeesClient.getAttendee(request).await().atMost(Duration.ofSeconds(5));
        assertThat(response.hasAttendee(), equalTo(false));
    }

    private Attendee testAttendee() {
        Attendee attendee = new Attendee();
        attendee.setSurname("Gough");
        attendee.setId(1);
        attendee.setGivenName("Jim");
        attendee.setEmail("jim@gough");
        return attendee;
    }
}
