package com.manuelpuchner.backend.geocoding;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "nominatim",
        url = "https://nominatim.openstreetmap.org",
        configuration = FeignConfig.class
)
public interface NominatimClient {

    @GetMapping("/search")
    List<NominatimResult> search(
            @RequestParam("street") String street,
            @RequestParam("country") String country,
            @RequestParam("postalcode") String postalcode,
            @RequestParam("format") String format,
            @RequestParam("limit") int limit,
            @RequestParam("addressdetails") int addressDetails
    );
}
