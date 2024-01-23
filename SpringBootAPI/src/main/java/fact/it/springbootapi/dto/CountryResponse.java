package fact.it.springbootapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountryResponse {
    private Integer id;
    private String name;
    private String countryArea;
}
