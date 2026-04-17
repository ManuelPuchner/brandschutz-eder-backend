package com.manuelpuchner.backend.customer;

import com.manuelpuchner.backend.geocoding.GeoCoordinates;
import com.manuelpuchner.backend.geocoding.GeocodingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(customerRepository.findAll(Sort.by("id").descending())).thenReturn(List.of(c1, c2));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Customer::getName).containsExactly("Alice", "Bob");
        verify(customerRepository).findAll(Sort.by("id").descending());
    }

    @Test
    void getAllCustomers_givenNoCustomers_thenReturnsEmptyList() {
        when(customerRepository.findAll(Sort.by("id").descending())).thenReturn(List.of());

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // createCustomer
    // -------------------------------------------------------------------------

    @Test
    void createCustomer_givenGeocodingSucceeds_thenSavesCustomerWithCoordinates() {
        CreateCustomerDTO dto    = buildDto("Alice", "Austria", "4040", "Hauptstraße 1");
        GeoCoordinates coords    = new GeoCoordinates(48.3, 14.2, "Hauptstraße 1, 4040, Austria");
        Customer mappedCustomer  = buildCustomer(null, "Alice");
        Customer savedCustomer   = buildCustomer(1L, "Alice");
        savedCustomer.setLat(48.3);
        savedCustomer.setLon(14.2);

        when(geocodingService.geocode(dto)).thenReturn(Optional.of(coords));
        when(customerMapper.toCustomer(dto, coords)).thenReturn(mappedCustomer);
        when(customerRepository.save(mappedCustomer)).thenReturn(savedCustomer);

        Customer result = customerService.createCustomer(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLat()).isEqualTo(48.3);
        assertThat(result.getLon()).isEqualTo(14.2);
        verify(customerMapper).toCustomer(dto, coords);
        verify(customerMapper, never()).toCustomer(dto);
    }

    @Test
    void createCustomer_givenGeocodingFails_thenSavesCustomerWithoutCoordinates() {
        CreateCustomerDTO dto   = buildDto("Bob", "Germany", "10115", "Unknown Street 99");
        Customer mappedCustomer = buildCustomer(null, "Bob");
        Customer savedCustomer  = buildCustomer(2L, "Bob");

        when(geocodingService.geocode(dto)).thenReturn(Optional.empty());
        when(customerMapper.toCustomer(dto)).thenReturn(mappedCustomer);
        when(customerRepository.save(mappedCustomer)).thenReturn(savedCustomer);

        Customer result = customerService.createCustomer(dto);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getLat()).isNull();
        assertThat(result.getLon()).isNull();
        verify(customerMapper).toCustomer(dto);
        verify(customerMapper, never()).toCustomer(eq(dto), any(GeoCoordinates.class));
    }

    @Test
    void createCustomer_givenGeocodingSucceeds_thenNeverCallsMapperWithoutCoordinates() {
        CreateCustomerDTO dto   = buildDto("Charlie", "Austria", "1010", "Ringstraße 5");
        GeoCoordinates coords   = new GeoCoordinates(48.2, 16.3, "Ringstraße 5, 1010, Austria");
        Customer mappedCustomer = buildCustomer(null, "Charlie");
        Customer savedCustomer  = buildCustomer(3L, "Charlie");

        when(geocodingService.geocode(dto)).thenReturn(Optional.of(coords));
        when(customerMapper.toCustomer(dto, coords)).thenReturn(mappedCustomer);
        when(customerRepository.save(mappedCustomer)).thenReturn(savedCustomer);

        customerService.createCustomer(dto);

        verify(customerMapper, never()).toCustomer(dto);
    }

    // -------------------------------------------------------------------------
    // deleteCustomer
    // -------------------------------------------------------------------------

    @Test
    void deleteCustomer_givenCustomerExists_thenDeletesById() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        customerService.deleteCustomer(1L);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomer_givenCustomerNotFound_thenThrowsNotFoundException() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomer(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");

        verify(customerRepository, never()).deleteById(any());
    }

    // -------------------------------------------------------------------------
    // updateCustomer
    // -------------------------------------------------------------------------

    @Test
    void updateCustomer_givenCustomerExistsAndGeocodingSucceeds_thenUpdatesAllFields() {
        Customer existing       = buildCustomer(1L, "Alice");
        existing.setLat(48.0);
        existing.setLon(16.0);
        CreateCustomerDTO update = buildDto("Alice Updated", "AT", "1010", "Ringstraße 5");
        GeoCoordinates newCoords = new GeoCoordinates(48.2, 16.3, "Ringstraße 5, 1010, AT");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(geocodingService.geocode(update)).thenReturn(Optional.of(newCoords));
        when(customerRepository.save(existing)).thenReturn(existing);

        Customer result = customerService.updateCustomer(1L, update);

        assertThat(result.getName()).isEqualTo("Alice Updated");
        assertThat(result.getLat()).isEqualTo(48.2);
        assertThat(result.getLon()).isEqualTo(16.3);
        verify(customerRepository).save(existing);
    }

    @Test
    void updateCustomer_givenGeocodingFails_thenKeepsExistingCoordinates() {
        Customer existing        = buildCustomer(1L, "Alice");
        existing.setLat(48.0);
        existing.setLon(16.0);
        CreateCustomerDTO update = buildDto("Alice", "AT", "1010", "Street 1");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(geocodingService.geocode(update)).thenReturn(Optional.empty());
        when(customerRepository.save(existing)).thenReturn(existing);

        Customer result = customerService.updateCustomer(1L, update);

        assertThat(result.getLat()).isEqualTo(48.0);
        assertThat(result.getLon()).isEqualTo(16.0);
    }

    @Test
    void updateCustomer_givenCustomerNotFound_thenThrowsNotFoundException() {
        CreateCustomerDTO update = buildDto("Ghost", "AT", "1010", "Nowhere 0");
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(99L, update))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");

        verify(customerRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getCustomerById
    // -------------------------------------------------------------------------

    @Test
    void getCustomerById_givenCustomerExists_thenReturnsCustomer() {
        Customer customer = buildCustomer(1L, "Alice");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(1L);

        assertThat(result).isSameAs(customer);
    }

    @Test
    void getCustomerById_givenCustomerNotFound_thenThrowsNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");
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
