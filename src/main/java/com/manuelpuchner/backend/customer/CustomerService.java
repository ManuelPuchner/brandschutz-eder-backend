package com.manuelpuchner.backend.customer;

import com.manuelpuchner.backend.geocoding.GeoCoordinates;
import com.manuelpuchner.backend.geocoding.GeocodingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final GeocodingService geocodingService;
    private final CustomerMapper customerMapper;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll(Sort.by("id").descending());
    }

    @Transactional
    public Customer createCustomer(CreateCustomerDTO createCustomerDTO) {
        Optional<GeoCoordinates> geoCoordinates = geocodingService.geocode(createCustomerDTO);
        //Optional<GeoCoordinates> geoCoordinates = Optional.empty();

        Customer customer;
        if(geoCoordinates.isEmpty()) {
            customer = customerMapper.toCustomer(createCustomerDTO);
        } else {
            customer = customerMapper.toCustomer(createCustomerDTO, geoCoordinates.get());
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        boolean exists = customerRepository.existsById(id);

        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer with id: " + id + " not found");
        }

        customerRepository.deleteById(id);
    }

    @Transactional
    public Customer updateCustomer(Long customerId, CreateCustomerDTO newCustomer) {
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if(customerOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer with id: " + customerId + " not found");
        }

        Customer customer = customerOptional.get();

        System.out.println(customer);
        customer.setName(newCustomer.getName());
        customer.setAddress(newCustomer.getAddress());
        customer.setPostalcode(newCustomer.getPostalcode());
        customer.setCountry(newCustomer.getCountry());
        customer.setPhone(newCustomer.getPhone());

        Optional<GeoCoordinates> geoCoordinates = geocodingService.geocode(newCustomer);
        //Optional<GeoCoordinates> geoCoordinates = Optional.empty();

        if(geoCoordinates.isPresent()) {
            customer.setLat(geoCoordinates.get().lat());
            customer.setLon(geoCoordinates.get().lon());
        }

        customerRepository.save(customer);

        return customer;
    }
}
