package com.manuelpuchner.backend.customer;

import lombok.Data;

@Data
public class CreateCustomerDTO {
    private String name;

    // geocoding api  https://nominatim.openstreetmap.org/search?<params>
    private String country;
    private String postalcode;
    private String address;

    private String phone;
}
