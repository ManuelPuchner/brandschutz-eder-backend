package com.manuelpuchner.backend.geocoding;


import com.manuelpuchner.backend.customer.CreateCustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final NominatimClient nominatimClient;

    public Optional<GeoCoordinates> geocode(CreateCustomerDTO dto) {
        List<NominatimResult> results = nominatimClient.search(
                dto.getAddress(),
                dto.getCountry(),
                dto.getPostalcode(),
                "json",
                1,
                1
        );

        if(results == null || results.isEmpty()) {
            return Optional.empty();
        }

        NominatimResult result = results.getFirst();
        return Optional.of(new GeoCoordinates(
                Double.parseDouble(result.lat()),
                Double.parseDouble(result.lon()),
                result.displayName()
        ));


    }

}
