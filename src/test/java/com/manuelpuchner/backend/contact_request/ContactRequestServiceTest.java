package com.manuelpuchner.backend.contact_request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactRequestServiceTest {

    @Mock
    private ContactRequestRepository contactRequestRepository;

    @InjectMocks
    private ContactRequestService contactRequestService;

    // -------------------------------------------------------------------------
    // createContactRequest – validation
    // -------------------------------------------------------------------------

    @Test
    void createContactRequest_givenValidInput_thenSavesAndReturnsContactRequest() {
        ContactRequest request = buildRequest("Alice", "alice@example.com", "Hello!");
        when(contactRequestRepository.save(request)).thenReturn(request);

        ContactRequest result = contactRequestService.createContactRequest(request);

        assertThat(result).isSameAs(request);
        verify(contactRequestRepository).save(request);
    }

    @Test
    void createContactRequest_givenBlankName_thenThrows400() {
        ContactRequest request = buildRequest("  ", "alice@example.com", "Hello!");

        assertThatThrownBy(() -> contactRequestService.createContactRequest(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(contactRequestRepository, never()).save(any());
    }

    @Test
    void createContactRequest_givenBlankMessage_thenThrows400() {
        ContactRequest request = buildRequest("Alice", "alice@example.com", "   ");

        assertThatThrownBy(() -> contactRequestService.createContactRequest(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(contactRequestRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"notanemail", "@nodomain", "missing@", "space @test.com", "plainaddress"})
    void createContactRequest_givenInvalidEmail_thenThrows400(String invalidEmail) {
        ContactRequest request = buildRequest("Alice", invalidEmail, "Hello!");

        assertThatThrownBy(() -> contactRequestService.createContactRequest(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@example.com", "first.last@sub.domain.org", "test.name@company.de"})
    void createContactRequest_givenValidEmail_thenSavesSuccessfully(String validEmail) {
        ContactRequest request = buildRequest("Alice", validEmail, "Hello!");
        when(contactRequestRepository.save(request)).thenReturn(request);

        ContactRequest result = contactRequestService.createContactRequest(request);

        assertThat(result).isSameAs(request);
    }

    // -------------------------------------------------------------------------
    // getAllContactRequests
    // -------------------------------------------------------------------------

    @Test
    void getAllContactRequests_givenRequestsExist_thenReturnsSortedList() {
        Sort expectedSort = Sort.by("status").descending().and(Sort.by("createdAt").descending());
        ContactRequest r1 = buildRequest("Alice", "alice@example.com", "Hello!");
        ContactRequest r2 = buildRequest("Bob",   "bob@example.com",   "Hi!");
        when(contactRequestRepository.findAll(expectedSort)).thenReturn(List.of(r1, r2));

        List<ContactRequest> result = contactRequestService.getAllContactRequests();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ContactRequest::getName).containsExactly("Alice", "Bob");
        verify(contactRequestRepository).findAll(expectedSort);
    }

    @Test
    void getAllContactRequests_givenNoRequests_thenReturnsEmptyList() {
        Sort expectedSort = Sort.by("status").descending().and(Sort.by("createdAt").descending());
        when(contactRequestRepository.findAll(expectedSort)).thenReturn(List.of());

        List<ContactRequest> result = contactRequestService.getAllContactRequests();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findContactRequestById
    // -------------------------------------------------------------------------

    @Test
    void findContactRequestById_givenRequestExists_thenReturnsRequest() {
        ContactRequest request = buildRequest("Alice", "alice@example.com", "Hello!");
        request.setId(1L);
        when(contactRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        ContactRequest result = contactRequestService.findContactRequestById(1L);

        assertThat(result).isSameAs(request);
    }

    @Test
    void findContactRequestById_givenRequestNotFound_thenThrows404() {
        when(contactRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactRequestService.findContactRequestById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // updateContactRequestStatus
    // -------------------------------------------------------------------------

    @Test
    void updateContactRequestStatus_givenValidId_thenUpdatesStatusAndReturnsTrue() {
        ContactRequest request = buildRequest("Alice", "alice@example.com", "Hello!");
        request.setId(1L);
        request.setStatus(ContactRequestStatus.NEW);
        when(contactRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(contactRequestRepository.save(request)).thenReturn(request);

        boolean result = contactRequestService.updateContactRequestStatus(1L, ContactRequestStatus.READ);

        assertThat(result).isTrue();
        assertThat(request.getStatus()).isEqualTo(ContactRequestStatus.READ);
        verify(contactRequestRepository).save(request);
    }

    @Test
    void updateContactRequestStatus_givenRequestNotFound_thenThrows404() {
        when(contactRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactRequestService.updateContactRequestStatus(99L, ContactRequestStatus.READ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(contactRequestRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContactRequest buildRequest(String name, String email, String message) {
        ContactRequest r = new ContactRequest();
        r.setName(name);
        r.setEmail(email);
        r.setMessage(message);
        return r;
    }
}
