package com.manuelpuchner.backend.geocoding;

import com.manuelpuchner.backend.customer.CreateCustomerDTO;
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
class GeocodingServiceTest {

    @Mock
    private NominatimClient nominatimClient;

    @InjectMocks
    private GeocodingService geocodingService;

    @Test
    void geocode_givenValidAddress_thenReturnsCoordinates() {
        CreateCustomerDTO dto = buildDto("Austria", "4040", "Hauptstraße 1");
        NominatimResult result = new NominatimResult("48.3", "14.2", "Hauptstraße 1, 4040, Austria");

        when(nominatimClient.search("Hauptstraße 1", "Austria", "4040", "json", 1, 1))
                .thenReturn(List.of(result));

        Optional<GeoCoordinates> coords = geocodingService.geocode(dto);

        assertThat(coords).isPresent();
        assertThat(coords.get().lat()).isEqualTo(48.3);
        assertThat(coords.get().lon()).isEqualTo(14.2);
        assertThat(coords.get().displayName()).isEqualTo("Hauptstraße 1, 4040, Austria");
    }

    @Test
    void geocode_givenNoResults_thenReturnsEmpty() {
        CreateCustomerDTO dto = buildDto("Germany", "99999", "Nowhere Street 0");
        when(nominatimClient.search("Nowhere Street 0", "Germany", "99999", "json", 1, 1))
                .thenReturn(List.of());

        Optional<GeoCoordinates> coords = geocodingService.geocode(dto);

        assertThat(coords).isEmpty();
    }

    @Test
    void geocode_givenNullResults_thenReturnsEmpty() {
        CreateCustomerDTO dto = buildDto("Germany", "99999", "Nowhere Street 0");
        when(nominatimClient.search("Nowhere Street 0", "Germany", "99999", "json", 1, 1))
                .thenReturn(null);

        Optional<GeoCoordinates> coords = geocodingService.geocode(dto);

        assertThat(coords).isEmpty();
    }

    @Test
    void geocode_givenMultipleResults_thenReturnsFirstOnly() {
        CreateCustomerDTO dto = buildDto("Austria", "1010", "Ringstraße 1");
        NominatimResult first  = new NominatimResult("48.2", "16.3", "Ringstraße 1, Vienna");
        NominatimResult second = new NominatimResult("47.1", "15.1", "Ringstraße 1, Graz");
        when(nominatimClient.search("Ringstraße 1", "Austria", "1010", "json", 1, 1))
                .thenReturn(List.of(first, second));

        Optional<GeoCoordinates> coords = geocodingService.geocode(dto);

        assertThat(coords).isPresent();
        assertThat(coords.get().lat()).isEqualTo(48.2);
        assertThat(coords.get().lon()).isEqualTo(16.3);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateCustomerDTO buildDto(String country, String postalcode, String address) {
        CreateCustomerDTO dto = new CreateCustomerDTO();
        dto.setCountry(country);
        dto.setPostalcode(postalcode);
        dto.setAddress(address);
        return dto;
    }
}