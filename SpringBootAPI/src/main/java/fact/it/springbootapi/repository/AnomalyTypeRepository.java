package fact.it.springbootapi.repository;

import fact.it.springbootapi.model.AnomalyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnomalyTypeRepository extends JpaRepository<AnomalyType, Integer> {
}
