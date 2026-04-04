package com.manuelpuchner.backend.customer;

import com.manuelpuchner.backend.geocoding.GeoCoordinates;
import com.manuelpuchner.backend.geocoding.GeocodingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    // -------------------------------------------------------------------------
    // getAllCustomers
    // -------------------------------------------------------------------------

    @Test
    void getAllCustomers_givenCustomersExist_thenReturnsAllCustomers() {
        Customer c1 = buildCustomer(1L, "Alice");
        Customer c2 = buildCustomer(2L, "Bob");
        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Customer::getName).containsExactly("Alice", "Bob");
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getAllCustomers_givenNoCustomersExist_thenReturnsEmptyList() {
        when(customerRepository.findAll()).thenReturn(List.of());

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).isEmpty();
        verify(customerRepository, times(1)).findAll();
    }

    // -------------------------------------------------------------------------
    // createCustomer
    // -------------------------------------------------------------------------

    @Test
    void createCustomer_givenGeocodingSucceeds_thenSavesCustomerWithCoordinates() {
        CreateCustomerDTO dto = buildDto("Alice", "Austria", "4040", "Hauptstraße 1");
        GeoCoordinates coords = new GeoCoordinates(48.3, 14.2, "Hauptstraße 1, 4040, Austria");
        Customer mappedCustomer = buildCustomer(null, "Alice");
        mappedCustomer.setLat(48.3);
        mappedCustomer.setLon(14.2);
        Customer savedCustomer = buildCustomer(1L, "Alice");
        savedCustomer.setLat(48.3);
        savedCustomer.setLon(14.2);

        when(geocodingService.geocode(dto)).thenReturn(Optional.of(coords));
        when(customerMapper.toCustomer(dto, coords)).thenReturn(mappedCustomer);
        when(customerRepository.save(mappedCustomer)).thenReturn(savedCustomer);

        Customer result = customerService.createCustomer(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLat()).isEqualTo(48.3);
        assertThat(result.getLon()).isEqualTo(14.2);
        verify(customerMapper, times(1)).toCustomer(dto, coords);
        verify(customerMapper, never()).toCustomer(dto);
        verify(customerRepository, times(1)).save(mappedCustomer);
    }

    @Test
    void createCustomer_givenGeocodingFails_thenSavesCustomerWithoutCoordinates() {
        CreateCustomerDTO dto = buildDto("Bob", "Germany", "10115", "Unknown Street 99");
        Customer mappedCustomer = buildCustomer(null, "Bob");
        Customer savedCustomer = buildCustomer(2L, "Bob");

        when(geocodingService.geocode(dto)).thenReturn(Optional.empty());
        when(customerMapper.toCustomer(dto)).thenReturn(mappedCustomer);
        when(customerRepository.save(mappedCustomer)).thenReturn(savedCustomer);

        Customer result = customerService.createCustomer(dto);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getLat()).isNull();
        assertThat(result.getLon()).isNull();
        verify(customerMapper, times(1)).toCustomer(dto);
        verify(customerMapper, never()).toCustomer(eq(dto), any(GeoCoordinates.class));
        verify(customerRepository, times(1)).save(mappedCustomer);
    }

    @Test
    void createCustomer_givenGeocodingSucceeds_thenNeverCallsMapperWithoutCoords() {
        CreateCustomerDTO dto = buildDto("Charlie", "Austria", "1010", "Ringstraße 5");
        GeoCoordinates coords = new GeoCoordinates(48.2, 16.3, "Ringstraße 5, 1010, Austria");
        Customer mappedCustomer = buildCustomer(null, "Charlie");
        Customer savedCustomer = buildCustomer(3L, "Charlie");

        when(geocodingService.geocode(dto)).thenReturn(Optional.of(coords));
        when(customerMapper.toCustomer(dto, coords)).thenReturn(mappedCustomer);
        when(customerRepository.save(mappedCustomer)).thenReturn(savedCustomer);

        customerService.createCustomer(dto);

        verify(customerMapper, never()).toCustomer(dto); // single-arg overload never called
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Customer buildCustomer(Long id, String name) {
        Customer c = new Customer();
        c.setId(id);
        c.setName(name);
        return c;
    }

    private CreateCustomerDTO buildDto(String name, String country, String postalcode, String address) {
        CreateCustomerDTO dto = new CreateCustomerDTO();
        dto.setName(name);
        dto.setCountry(country);
        dto.setPostalcode(postalcode);
        dto.setAddress(address);
        return dto;
    }
}