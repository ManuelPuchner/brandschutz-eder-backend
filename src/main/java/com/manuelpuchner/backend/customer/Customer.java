package com.manuelpuchner.backend.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.io.Serializable;

@Entity
@Data
public class Customer implements Serializable {
    //region getter & setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // geocoding api  https://nominatim.openstreetmap.org/search?<params>
    private String country;
    private String postalcode;
    private String city;
    private String address;

    private Double lat;
    private Double lon;

    private String phone;
}
