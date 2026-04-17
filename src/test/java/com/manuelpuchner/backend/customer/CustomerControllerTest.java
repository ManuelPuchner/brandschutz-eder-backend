package com.manuelpuchner.backend.customer;

import com.manuelpuchner.backend.security.JwtAuthFilter;
import com.manuelpuchner.backend.security.JwtUtil;
import com.manuelpuchner.backend.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import({JwtUtil.class, JwtAuthFilter.class, SecurityConfig.class})
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-for-unit-tests-minimum-32chars!!",
        "app.jwt.expiration=3600000",
        "app.admin.username=testadmin",
        "app.admin.password=testpass"
})
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
    void getAllCustomers_givenCustomersExist_thenReturns200WithList() throws Exception {
        Customer c1 = buildCustomer(1L, "Alice", "AT", "1010");
        Customer c2 = buildCustomer(2L, "Bob",   "DE", "10115");
        when(customerService.getAllCustomers()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/customers").with(user("testuser")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id",      is(1)))
                .andExpect(jsonPath("$[0].name",    is("Alice")))
                .andExpect(jsonPath("$[0].country", is("AT")))
                .andExpect(jsonPath("$[1].id",      is(2)))
                .andExpect(jsonPath("$[1].name",    is("Bob")));

        verify(customerService).getAllCustomers();
    }

    @Test
    void getAllCustomers_givenNoCustomers_thenReturns200WithEmptyList() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of());

        mockMvc.perform(get("/customers").with(user("testuser")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllCustomers_givenUnauthenticated_thenReturns403() throws Exception {
        mockMvc.perform(get("/customers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void getCustomerById_givenCustomerExists_thenReturns200WithCustomerDto() throws Exception {
        Customer customer = buildCustomer(1L, "Alice", "AT", "1010");
        CustomerDTO dto    = buildCustomerDto(1L, "Alice", "AT", "1010", 48.2, 16.3);

        when(customerService.getCustomerById(1L)).thenReturn(customer);
        when(customerMapper.toDto(customer)).thenReturn(dto);

        mockMvc.perform(get("/customers/1").with(user("testuser")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",   is(1)))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.lat",  is(48.2)))
                .andExpect(jsonPath("$.lon",  is(16.3)));

        verify(customerService).getCustomerById(1L);
        verify(customerMapper).toDto(customer);
    }

    @Test
    void getCustomerById_givenCustomerNotFound_thenReturns404() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Customer with id: 99 not found"));

        mockMvc.perform(get("/customers/99").with(user("testuser")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // POST /customers
    // -------------------------------------------------------------------------

    @Test
    void createCustomer_givenValidInput_thenReturns201WithCoordinates() throws Exception {
        CreateCustomerDTO input = buildCreateDto("Alice", "Austria", "Vienna", "4040", "Hauptstraße 1", "+431234");
        Customer saved          = buildCustomer(1L, "Alice", "Austria", "4040");
        saved.setLat(48.3);
        saved.setLon(14.2);
        CustomerDTO responseDto = buildCustomerDto(1L, "Alice", "Austria", "4040", 48.3, 14.2);

        when(customerService.createCustomer(any(CreateCustomerDTO.class))).thenReturn(saved);
        when(customerMapper.toDto(saved)).thenReturn(responseDto);

        mockMvc.perform(post("/customers").with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",   is(1)))
                .andExpect(jsonPath("$.name", is("Alice")))
                .andExpect(jsonPath("$.lat",  is(48.3)))
                .andExpect(jsonPath("$.lon",  is(14.2)));

        verify(customerService).createCustomer(any(CreateCustomerDTO.class));
        verify(customerMapper).toDto(saved);
    }

    @Test
    void createCustomer_givenGeocodingFails_thenReturns201WithoutCoordinates() throws Exception {
        CreateCustomerDTO input = buildCreateDto("Bob", "Germany", "Berlin", "10115", "Unknown Street 99", null);
        Customer saved          = buildCustomer(2L, "Bob", "Germany", "10115");
        CustomerDTO responseDto = buildCustomerDto(2L, "Bob", "Germany", "10115", null, null);

        when(customerService.createCustomer(any(CreateCustomerDTO.class))).thenReturn(saved);
        when(customerMapper.toDto(saved)).thenReturn(responseDto);

        mockMvc.perform(post("/customers").with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",   is(2)))
                .andExpect(jsonPath("$.name", is("Bob")))
                .andExpect(jsonPath("$.lat").doesNotExist())
                .andExpect(jsonPath("$.lon").doesNotExist());
    }

    // -------------------------------------------------------------------------
    // DELETE /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void deleteCustomer_givenCustomerExists_thenReturns204() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/customers/1").with(user("testuser")))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void deleteCustomer_givenCustomerNotFound_thenReturns404() throws Exception {
        doThrow(new ResponseStatusException(NOT_FOUND, "Customer with id: 99 not found"))
                .when(customerService).deleteCustomer(99L);

        mockMvc.perform(delete("/customers/99").with(user("testuser")))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PUT /customers/{id}
    // -------------------------------------------------------------------------

    @Test
    void updateCustomer_givenValidInput_thenReturns200WithUpdatedCustomer() throws Exception {
        CreateCustomerDTO input = buildCreateDto("Alice Updated", "Austria", "Vienna", "1010", "Ringstraße 5", "+431234");
        Customer updated        = buildCustomer(1L, "Alice Updated", "Austria", "1010");
        updated.setLat(48.2);
        updated.setLon(16.3);
        CustomerDTO responseDto = buildCustomerDto(1L, "Alice Updated", "Austria", "1010", 48.2, 16.3);

        when(customerService.updateCustomer(eq(1L), any(CreateCustomerDTO.class))).thenReturn(updated);
        when(customerMapper.toDto(updated)).thenReturn(responseDto);

        mockMvc.perform(put("/customers/1").with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",   is(1)))
                .andExpect(jsonPath("$.name", is("Alice Updated")))
                .andExpect(jsonPath("$.lat",  is(48.2)))
                .andExpect(jsonPath("$.lon",  is(16.3)));

        verify(customerService).updateCustomer(eq(1L), any(CreateCustomerDTO.class));
        verify(customerMapper).toDto(updated);
    }

    @Test
    void updateCustomer_givenCustomerNotFound_thenReturns404() throws Exception {
        CreateCustomerDTO input = buildCreateDto("Ghost", "AT", "Vienna", "1010", "Nowhere 0", null);

        when(customerService.updateCustomer(eq(99L), any(CreateCustomerDTO.class)))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Customer with id: 99 not found"));

        mockMvc.perform(put("/customers/99").with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
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

    private CreateCustomerDTO buildCreateDto(String name, String country, String city,
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
