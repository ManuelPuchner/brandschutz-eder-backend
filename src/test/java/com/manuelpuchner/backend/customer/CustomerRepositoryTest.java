package com.manuelpuchner.backend.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void findById_givenCustomerExists_thenReturnsCustomer() {
        Customer saved = entityManager.persistAndFlush(buildCustomer("Alice"));

        Optional<Customer> result = customerRepository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice");
    }

    @Test
    void findById_givenCustomerDoesNotExist_thenReturnsEmpty() {
        Optional<Customer> result = customerRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_givenMultipleCustomers_thenReturnsAll() {
        entityManager.persistAndFlush(buildCustomer("Alice"));
        entityManager.persistAndFlush(buildCustomer("Bob"));

        List<Customer> result = customerRepository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Customer::getName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    void save_givenNewCustomer_thenPersistsCorrectly() {
        Customer customer = buildCustomer("Charlie");
        customer.setCountry("AT");
        customer.setPostalcode("1010");
        customer.setPhone("+431234567");

        Customer saved = customerRepository.save(customer);

        assertThat(saved.getId()).isNotNull(); // DB assigned it
        assertThat(entityManager.find(Customer.class, saved.getId())).isNotNull();
    }

    private Customer buildCustomer(String name) {
        Customer c = new Customer();
        c.setName(name);
        return c;
    }
}