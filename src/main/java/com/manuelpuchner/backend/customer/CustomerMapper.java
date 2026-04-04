package com.manuelpuchner.backend.customer;

import com.manuelpuchner.backend.geocoding.GeoCoordinates;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public Customer toCustomer(CreateCustomerDTO createCustomerDTO) {
        Customer customer = new Customer();
        customer.setName(createCustomerDTO.getName());
        customer.setCountry(createCustomerDTO.getCountry());
        customer.setPostalcode(createCustomerDTO.getPostalcode());
        customer.setAddress(createCustomerDTO.getAddress());
        customer.setPhone(createCustomerDTO.getPhone());
        return customer;
    }

    public Customer toCustomer(CreateCustomerDTO createCustomerDTO, GeoCoordinates geoCoordinates) {
        Customer customer = toCustomer(createCustomerDTO);
        customer.setLat(geoCoordinates.lat());
        customer.setLon(geoCoordinates.lon());
        return customer;
    }

    public CustomerDTO toDto(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.getId());
        customerDTO.setName(customer.getName());
        customerDTO.setAddress(customer.getAddress());
        customerDTO.setCountry(customer.getCountry());
        customerDTO.setPostalcode(customer.getPostalcode());
        customerDTO.setLat(customer.getLat());
        customerDTO.setLon(customer.getLon());
        customerDTO.setPhone(customer.getPhone());
        return customerDTO;
    }
}
