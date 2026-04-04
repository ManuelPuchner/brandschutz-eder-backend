package com.manuelpuchner.backend.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomerMapper customerMapper;

    // -------------------------------------------------------------------------
    // GET /customers
    // -------------------------------------------------------------------------

    @Test
    void getAll_givenCustomersExist_thenReturns200WithCustomerList() throws Exception {
        Customer c1 = buildCustomer(1L, "Alice", "AT", "1010");
        Customer c2 = buildCustomer(2L, "Bob",   "DE", "10115");
        when(customerService.getAllCustomers()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/customers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id",      is(1)))
                .andExpect(jsonPath("$[0].name",    is("Alice")))
                .andExpect(jsonPath("$[0].country", is("AT")))
                .andExpect(jsonPath("$[1].id",      is(2)))
                .andExpect(jsonPath("$[1].name",    is("Bob")));

        verify(customerService, times(1)).getAllCustomers();
    }

    @Test
    void getAll_givenNoCustomers_thenReturns200WithEmptyList() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/customers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------------------------
    // POST /customers
    // -------------------------------------------------------------------------

    @Test
    void createCustomer_givenValidInput_thenReturns201WithCoordinates() throws Exception {
        CreateCustomerDTO input = buildDto("Alice", "Austria", "4040", "Hauptstraße 1");

        Customer saved = buildCustomer(1L, "Alice", "Austria", "4040");
        saved.setLat(48.3);
        saved.setLon(14.2);

        CustomerDTO responseDto = buildCustomerDto(1L, "Alice", "Austria", "4040", 48.3, 14.2);

        when(customerService.createCustomer(any(CreateCustomerDTO.class))).thenReturn(saved);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(responseDto);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",      is(1)))
                .andExpect(jsonPath("$.name",    is("Alice")))
                .andExpect(jsonPath("$.lat",     is(48.3)))
                .andExpect(jsonPath("$.lon",     is(14.2)));

        verify(customerService, times(1)).createCustomer(any(CreateCustomerDTO.class));
        verify(customerMapper, times(1)).toDto(any(Customer.class));
    }

    @Test
    void createCustomer_givenGeocodingFails_thenReturns201WithoutCoordinates() throws Exception {
        CreateCustomerDTO input = buildDto("Bob", "Germany", "10115", "Unknown Street 99");

        Customer saved = buildCustomer(2L, "Bob", "Germany", "10115"); // lat/lon null
        CustomerDTO responseDto = buildCustomerDto(2L, "Bob", "Germany", "10115", null, null);

        when(customerService.createCustomer(any(CreateCustomerDTO.class))).thenReturn(saved);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(responseDto);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",   is(2)))
                .andExpect(jsonPath("$.name", is("Bob")))
                .andExpect(jsonPath("$.lat").doesNotExist())
                .andExpect(jsonPath("$.lon").doesNotExist());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Customer buildCustomer(Long id, String name, String country, String postalcode) {
        Customer c = new Customer();
        c.setId(id);
        c.setName(name);
        c.setCountry(country);
        c.setPostalcode(postalcode);
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

    private CustomerDTO buildCustomerDto(Long id, String name, String country,
                                         String postalcode, Double lat, Double lon) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCountry(country);
        dto.setPostalcode(postalcode);
        dto.setLat(lat);
        dto.setLon(lon);
        return dto;
    }
}