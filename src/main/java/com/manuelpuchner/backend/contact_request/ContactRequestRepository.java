package com.manuelpuchner.backend.contact_request;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ContactRequestRepository extends Repository<ContactRequest, Long> {
    ContactRequest save(ContactRequest contactRequest);

    List<ContactRequest> findAll(Sort sort);

    Optional<ContactRequest> findById(Long id);
}
