package com.manuelpuchner.backend.customer;

import com.manuelpuchner.backend.geocoding.GeoCoordinates;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private final CustomerMapper customerMapper = new CustomerMapper();

    // -------------------------------------------------------------------------
    // toCustomer(CreateCustomerDTO)
    // -------------------------------------------------------------------------

    @Test
    void toCustomer_givenDto_thenMapsAllFieldsWithoutCoordinates() {
        CreateCustomerDTO dto = buildDto("Alice", "Austria", "Vienna", "4040", "Hauptstraße 1", "+431234");

        Customer result = customerMapper.toCustomer(dto);

        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getCountry()).isEqualTo("Austria");
        assertThat(result.getCity()).isEqualTo("Vienna");
        assertThat(result.getPostalcode()).isEqualTo("4040");
        assertThat(result.getAddress()).isEqualTo("Hauptstraße 1");
        assertThat(result.getPhone()).isEqualTo("+431234");
        assertThat(result.getLat()).isNull();
        assertThat(result.getLon()).isNull();
        assertThat(result.getId()).isNull();
    }

    // -------------------------------------------------------------------------
    // toCustomer(CreateCustomerDTO, GeoCoordinates)
    // -------------------------------------------------------------------------

    @Test
    void toCustomer_givenDtoAndCoordinates_thenMapsAllFieldsIncludingCoordinates() {
        CreateCustomerDTO dto = buildDto("Alice", "Austria", "Linz", "4040", "Hauptstraße 1", "+431234");
        GeoCoordinates coords = new GeoCoordinates(48.3, 14.2, "Hauptstraße 1, 4040, Austria");

        Customer result = customerMapper.toCustomer(dto, coords);

        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getCountry()).isEqualTo("Austria");
        assertThat(result.getCity()).isEqualTo("Linz");
        assertThat(result.getPostalcode()).isEqualTo("4040");
        assertThat(result.getAddress()).isEqualTo("Hauptstraße 1");
        assertThat(result.getPhone()).isEqualTo("+431234");
        assertThat(result.getLat()).isEqualTo(48.3);
        assertThat(result.getLon()).isEqualTo(14.2);
    }

    // -------------------------------------------------------------------------
    // toDto(Customer)
    // -------------------------------------------------------------------------

    @Test
    void toDto_givenCustomerWithCoordinates_thenMapsAllFields() {
        Customer customer = buildCustomer(1L, "Alice", "Austria", "Vienna", "4040", 48.3, 14.2);

        CustomerDTO result = customerMapper.toDto(customer);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getCountry()).isEqualTo("Austria");
        assertThat(result.getCity()).isEqualTo("Vienna");
        assertThat(result.getPostalcode()).isEqualTo("4040");
        assertThat(result.getLat()).isEqualTo(48.3);
        assertThat(result.getLon()).isEqualTo(14.2);
    }

    @Test
    void toDto_givenCustomerWithoutCoordinates_thenLatAndLonAreNull() {
        Customer customer = buildCustomer(2L, "Bob", "Germany", "Berlin", "10115", null, null);

        CustomerDTO result = customerMapper.toDto(customer);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Bob");
        assertThat(result.getLat()).isNull();
        assertThat(result.getLon()).isNull();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateCustomerDTO buildDto(String name, String country, String city,
                                       String postalcode, String address, String phone) {
        CreateCustomerDTO dto = new CreateCustomerDTO();
        dto.setName(name);
        dto.setCountry(country);
        dto.setCity(city);
        dto.setPostalcode(postalcode);
        dto.setAddress(address);
        dto.setPhone(phone);
        return dto;
    }

    private Customer buildCustomer(Long id, String name, String country, String city,
                                   String postalcode, Double lat, Double lon) {
        Customer c = new Customer();
        c.setId(id);
        c.setName(name);
        c.setCountry(country);
        c.setCity(city);
        c.setPostalcode(postalcode);
        c.setLat(lat);
        c.setLon(lon);
        return c;
    }
}
