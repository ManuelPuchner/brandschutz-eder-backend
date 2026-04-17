package com.manuelpuchner.backend.contact_request;

import com.manuelpuchner.backend.security.JwtAuthFilter;
import com.manuelpuchner.backend.security.JwtUtil;
import com.manuelpuchner.backend.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactRequestController.class)
@Import({JwtUtil.class, JwtAuthFilter.class, SecurityConfig.class})
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-for-unit-tests-minimum-32chars!!",
        "app.jwt.expiration=3600000",
        "app.admin.username=testadmin",
        "app.admin.password=testpass"
})
class ContactRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContactRequestService contactRequestService;

    // -------------------------------------------------------------------------
    // GET /contact-requests  (public)
    // -------------------------------------------------------------------------

    @Test
    void getAllContactRequests_givenPublicAccess_thenReturns200WithList() throws Exception {
        ContactRequest r1 = buildRequest(1L, "Alice", "alice@example.com", ContactRequestStatus.NEW);
        ContactRequest r2 = buildRequest(2L, "Bob",   "bob@example.com",   ContactRequestStatus.READ);
        when(contactRequestService.getAllContactRequests()).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/contact-requests").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id",     is(1)))
                .andExpect(jsonPath("$[0].name",   is("Alice")))
                .andExpect(jsonPath("$[0].status", is("NEW")))
                .andExpect(jsonPath("$[1].id",     is(2)))
                .andExpect(jsonPath("$[1].name",   is("Bob")));

        verify(contactRequestService).getAllContactRequests();
    }

    @Test
    void getAllContactRequests_givenNoRequests_thenReturns200WithEmptyList() throws Exception {
        when(contactRequestService.getAllContactRequests()).thenReturn(List.of());

        mockMvc.perform(get("/contact-requests").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // POST /contact-requests  (public)
    // -------------------------------------------------------------------------

    @Test
    void createContactRequest_givenValidBody_thenReturns201WithSavedRequest() throws Exception {
        ContactRequest body  = buildRequest(null, "Alice", "alice@example.com", null);
        body.setMessage("Hello, I need a quote.");
        ContactRequest saved = buildRequest(1L, "Alice", "alice@example.com", ContactRequestStatus.NEW);
        saved.setMessage("Hello, I need a quote.");

        when(contactRequestService.createContactRequest(any(ContactRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/contact-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",     is(1)))
                .andExpect(jsonPath("$.name",   is("Alice")))
                .andExpect(jsonPath("$.status", is("NEW")));

        verify(contactRequestService).createContactRequest(any(ContactRequest.class));
    }

    @Test
    void createContactRequest_givenInvalidInput_thenReturns400() throws Exception {
        ContactRequest body = buildRequest(null, "", "notanemail", null);
        body.setMessage("");

        when(contactRequestService.createContactRequest(any(ContactRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be empty"));

        mockMvc.perform(post("/contact-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /contact-requests/{id}  (requires auth)
    // -------------------------------------------------------------------------

    @Test
    void getContactRequestById_givenValidIdAndAuthenticated_thenReturns200() throws Exception {
        ContactRequest request = buildRequest(1L, "Alice", "alice@example.com", ContactRequestStatus.NEW);
        when(contactRequestService.findContactRequestById(1L)).thenReturn(request);

        mockMvc.perform(get("/contact-requests/1").with(user("testuser")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",   is(1)))
                .andExpect(jsonPath("$.name", is("Alice")));

        verify(contactRequestService).findContactRequestById(1L);
    }

    @Test
    void getContactRequestById_givenNotFound_thenReturns404() throws Exception {
        when(contactRequestService.findContactRequestById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact Request with id: 99 not found"));

        mockMvc.perform(get("/contact-requests/99").with(user("testuser")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getContactRequestById_givenUnauthenticated_thenReturns403() throws Exception {
        mockMvc.perform(get("/contact-requests/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // PATCH /contact-requests/{id}?status=READ  (requires auth)
    // -------------------------------------------------------------------------

    @Test
    void updateContactRequestStatus_givenValidStatusAndAuthenticated_thenReturns200WithTrue() throws Exception {
        when(contactRequestService.updateContactRequestStatus(1L, ContactRequestStatus.READ)).thenReturn(true);

        mockMvc.perform(patch("/contact-requests/1").with(user("testuser")).param("status", "READ"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(contactRequestService).updateContactRequestStatus(1L, ContactRequestStatus.READ);
    }

    @Test
    void updateContactRequestStatus_givenNotFound_thenReturns404() throws Exception {
        when(contactRequestService.updateContactRequestStatus(eq(99L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact Request with id: 99 not found"));

        mockMvc.perform(patch("/contact-requests/99").with(user("testuser")).param("status", "READ"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateContactRequestStatus_givenUnauthenticated_thenReturns403() throws Exception {
        mockMvc.perform(patch("/contact-requests/1").param("status", "READ"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ContactRequest buildRequest(Long id, String name, String email, ContactRequestStatus status) {
        ContactRequest r = new ContactRequest();
        r.setId(id);
        r.setName(name);
        r.setEmail(email);
        r.setStatus(status != null ? status : ContactRequestStatus.NEW);
        return r;
    }
}
