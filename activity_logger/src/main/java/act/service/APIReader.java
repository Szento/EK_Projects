package act.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;



public class APIReader {

    private static final String CITY_URL = "https://dawa.aws.dk/steder?hovedtype=Bebyggelse&undertype=by&prim%C3%A6rtnavn=Roskilde";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        APIReader apiReader = new APIReader();
        // String json = apiReader.readAPI(CITY_URL);
        // CityDTO cityDTO = apiReader.convertFromJson(json);
        // System.out.println(cityDTO);
        // System.out.println(apiReader.getWithJackson(CITY_URL));
        CityDTO cityDTO = apiReader.getWithJacksonGeneric(CITY_URL, CityDTO[].class)[0];
        System.out.println(cityDTO);
    }

    public String readAPI(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("GET request failed. Status code: " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public CityDTO convertFromJson(String json) {
        try {
            CityDTO[] cityDTOs = objectMapper.readValue(json, CityDTO[].class);
            return cityDTOs[0];
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    
    public CityDTO getWithJackson(String url){
        try {
            JsonNode node = objectMapper.readTree(new URI(url).toURL());
            return objectMapper.treeToValue(node, CityDTO[].class)[0];
        } catch (URISyntaxException | IOException e){
            throw new RuntimeException(e);
        }
    }

    public <T> T getWithJacksonGeneric(String url, Class<T> tClass){
        try {
            JsonNode node = objectMapper.readTree(new URI(url).toURL());
            return objectMapper.treeToValue(node, tClass);
        } catch (IOException | URISyntaxException e){
            throw new RuntimeException(e);
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CityDTO {
        @JsonProperty("Primærtnavn")
        String primaryName;
    
        @JsonProperty("hovedtype")
        String mainType;

        @JsonProperty("egenskaber")
        QualitiesDTO qualitiesDTO;

        @JsonProperty("bbox")
        List<String> bbox;

        @JsonProperty("kommuner")
        List<MunicipalityDTO> municipalityDTOS;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class QualitiesDTO {
        @JsonProperty("indbyggerantal")
        Integer numberOfCitizens ;
    
        @JsonProperty("bebyggelseskode")
        Integer code;
        
    }

    
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MunicipalityDTO {
        @JsonProperty("href")
        String href ;
    
        @JsonProperty("navn")
        String name;
        
    }
}
