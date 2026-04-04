package com.manuelpuchner.backend.contact_request;


import com.manuelpuchner.backend.customer.CreateCustomerDTO;
import com.manuelpuchner.backend.customer.Customer;
import com.manuelpuchner.backend.customer.CustomerDTO;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact-requests")
@RequiredArgsConstructor
public class ContactRequestController {

    private final ContactRequestService contactRequestService;

    @GetMapping
    public List<ContactRequest> getAllContactRequests() {
        return contactRequestService.getAllContactRequests();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactRequest createNewCustomer(@RequestBody ContactRequest contactRequestToCreate) {
        return contactRequestService.createContactRequest(contactRequestToCreate);
    }

    @GetMapping("/{id}")
    public ContactRequest getContactRequestById(@PathVariable Long id) {
        return contactRequestService.findContactRequestById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Boolean> updateContactRequestStatus(@PathVariable Long id, @RequestParam String status) {
        boolean updated = contactRequestService.updateContactRequestStatus(id, ContactRequestStatus.valueOf(status));
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }
}
