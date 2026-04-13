package com.manuelpuchner.backend.customer;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String name;

    // geocoding api  https://nominatim.openstreetmap.org/search?<params>
    private String country;
    private String city;
    private String postalcode;
    private String address;

    private Double lat;
    private Double lon;

    private String phone;
}
