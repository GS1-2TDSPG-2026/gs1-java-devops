package br.com.fiap.Phycocarbon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoDTOs(

        @JsonProperty("latitude")
        Double latitude,

        @JsonProperty("longitude")
        Double longitude,

        @JsonProperty("timezone")
        String timezone,

        @JsonProperty("hourly")
        HourlyData hourly
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HourlyData(


            @JsonProperty("time")
            List<String> time,


            @JsonProperty("shortwave_radiation")
            List<Double> shortwaveRadiation,


            @JsonProperty("cloud_cover")
            List<Double> cloudCover,


            @JsonProperty("temperature_2m")
            List<Double> temperature2m,


            @JsonProperty("relative_humidity_2m")
            List<Double> relativeHumidity2m
    ) {}
}