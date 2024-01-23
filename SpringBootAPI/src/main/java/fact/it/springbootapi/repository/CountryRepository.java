package fact.it.springbootapi.repository;

import fact.it.springbootapi.model.Country;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    @Query("SELECT c FROM Country c WHERE CONTAINS(c.countryArea, :point) = true")
    Country findByGeometryContains(@Param("point") Point point);

    @Query("SELECT ST_ASTEXT(c.countryArea) FROM Country c")
    List<String> getAllBorders();
}
