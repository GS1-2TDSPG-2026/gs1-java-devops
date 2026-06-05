package br.com.fiap.Phycocarbon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NasaPowerDTOs(
        @JsonProperty("properties") Properties properties
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Properties(
            @JsonProperty("parameter") Map<String, Map<String, Double>> parameter
    ) {}
}
