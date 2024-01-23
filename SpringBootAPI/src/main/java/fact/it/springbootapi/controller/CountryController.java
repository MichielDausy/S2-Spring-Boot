package fact.it.springbootapi.controller;

import fact.it.springbootapi.dto.CountryResponse;
import fact.it.springbootapi.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<CountryResponse> getAllCountries() {
        return  countryService.getAllCountries();
    }
}
