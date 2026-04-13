package com.manuelpuchner.backend.contact_request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.core.RepositoryCreationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ContactRequestService {

    private final ContactRequestRepository contactRequestRepository;

    public ContactRequest createContactRequest(ContactRequest contactRequestToCreate) {
        if(contactRequestToCreate.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be empty");
        }
        String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        if(!Pattern.compile(emailRegex).matcher(contactRequestToCreate.getEmail()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must be valid");
        }
        if(contactRequestToCreate.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message must not be empty");
        }

        return contactRequestRepository.save(contactRequestToCreate);
    }

    public List<ContactRequest> getAllContactRequests() {
        return contactRequestRepository.findAll(Sort
                .by("status").descending()
                .and(Sort.by("createdAt").descending())
        );
    }

    public ContactRequest findContactRequestById(Long id) {
        Optional<ContactRequest> contactRequestOptional = contactRequestRepository.findById(id);

        if(contactRequestOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact Request with id: " + id + " not found");
        }

        return contactRequestOptional.get();
    }

    public boolean updateContactRequestStatus(Long id, ContactRequestStatus status) {
        Optional<ContactRequest> contactRequestOptional = contactRequestRepository.findById(id);
        if(contactRequestOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact Request with id: " + id + " not found");
        }
        ContactRequest contactRequest = contactRequestOptional.get();
        contactRequest.setStatus(status);
        contactRequestRepository.save(contactRequest);
        return true;
    }
}
