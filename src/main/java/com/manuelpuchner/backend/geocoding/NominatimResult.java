package com.manuelpuchner.backend.geocoding;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NominatimResult(
        @JsonProperty("lat") String lat,
        @JsonProperty("lon") String lon,
        @JsonProperty("display_name") String displayName
) {

}
