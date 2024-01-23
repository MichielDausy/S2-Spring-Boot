package fact.it.springbootapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fact.it.springbootapi.dto.CountryResponse;
import fact.it.springbootapi.model.Country;
import fact.it.springbootapi.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CountryService {
    private final CountryRepository countryRepository;

    private CountryResponse mapToCountryResponse(Country country) {
        return CountryResponse.builder()
                .id(country.getId())
                .name(country.getName())
                .countryArea(country.getCountryArea().toString())
                .build();
    }

        public List<CountryResponse> getAllCountries() {
            List<Country> countries = countryRepository.findAll();
            return countries.stream().map(this::mapToCountryResponse).toList();
        }
    }
