package com.masteringapi.attendees.grpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import com.masteringapi.attendees.grpc.server.*;
import com.masteringapi.attendees.model.Attendee;
import com.masteringapi.attendees.model.AttendeeNotFoundException;
import com.masteringapi.attendees.service.AttendeeStore;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

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

        assertThrows(StatusRuntimeException.class,
                () -> attendeesClient.getAttendee(request).await().atMost(Duration.ofSeconds(5)));
    }

    @Test
    void return_attendee_for_given_id() throws AttendeeNotFoundException {
        GetAttendeeRequest request = GetAttendeeRequest.newBuilder().setId(1).build();
        when(mockAttendeeStore.getAttendee(any(Integer.class))).thenReturn(testAttendee());

        GetAttendeeResponse response = attendeesClient.getAttendee(request).await().atMost(Duration.ofSeconds(5));
        assertThat(response.getAttendee().getEmail(), equalTo("jim@gough"));
    }

    @Test
    void create_an_attendee() {
        CreateAttendeeRequest request = CreateAttendeeRequest.newBuilder().setAttendee(testGrpcAttendee()).build();
        when(mockAttendeeStore.addAttendee(ArgumentMatchers.isA(Attendee.class))).thenReturn(1);

        CreateAttendeeResponse response = attendeesClient.createAttendee(request).await().atMost(Duration.ofSeconds(5));
        assertThat(response.getAttendee().getId(), equalTo(1));
        verify(mockAttendeeStore).addAttendee(ArgumentMatchers.isA(Attendee.class));
    }

    @Test
    void error_when_deleting_a_non_existing_attendee() throws AttendeeNotFoundException {
        DeleteAttendeeRequest request = DeleteAttendeeRequest.newBuilder().setId(1).build();
        doThrow(new AttendeeNotFoundException()).when(mockAttendeeStore).removeAttendee(isA(Integer.class));

        assertThrows(StatusRuntimeException.class,
                () -> attendeesClient.deleteAttendee(request).await().atMost(Duration.ofSeconds(5)));
    }

    @Test
    void delete_a_known_attendee() throws AttendeeNotFoundException {
        DeleteAttendeeRequest request = DeleteAttendeeRequest.newBuilder().setId(1).build();
        attendeesClient.deleteAttendee(request).await().atMost(Duration.ofSeconds(5));

        verify(mockAttendeeStore).removeAttendee(ArgumentMatchers.isA(Integer.class));
    }

    @Test
    void error_when_updating_a_missing_attendee() throws AttendeeNotFoundException {
        UpdateAttendeeRequest.Builder request = UpdateAttendeeRequest.newBuilder();
        request.setAttendee(testGrpcAttendee());
        doThrow(new AttendeeNotFoundException()).when(mockAttendeeStore)
                .updateAttendee( isA(Integer.class), isA(Attendee.class));

        assertThrows(StatusRuntimeException.class,
                () -> attendeesClient.updateAttendee(request.build()).await().atMost(Duration.ofSeconds(5)));
    }

    @Test
    void update_a_given_attendee() throws  AttendeeNotFoundException {
        UpdateAttendeeRequest updateAttendeeRequest = UpdateAttendeeRequest.newBuilder().setAttendee(testGrpcAttendee()).build();

        attendeesClient.updateAttendee(updateAttendeeRequest).await().atMost(Duration.ofSeconds(5));
        verify(mockAttendeeStore).updateAttendee(ArgumentMatchers.isA(Integer.class), ArgumentMatchers.isA(Attendee.class));
    }

    private Attendee testAttendee() {
        Attendee attendee = new Attendee();
        attendee.setSurname("Gough");
        attendee.setId(1);
        attendee.setGivenName("Jim");
        attendee.setEmail("jim@gough");
        return attendee;
    }

    private com.masteringapi.attendees.grpc.server.Attendee testGrpcAttendee() {
        return com.masteringapi.attendees.grpc.server.Attendee.newBuilder()
                .setId(1)
                .setEmail("jim@gough")
                .setSurname("Gough")
                .setGivenName("Jim")
                .build();
    }
}
