package fact.it.springbootapi.repository;

import fact.it.springbootapi.model.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignRepository extends JpaRepository<Sign, Integer> {
}
