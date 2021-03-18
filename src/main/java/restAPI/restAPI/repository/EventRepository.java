package restAPI.restAPI.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import restAPI.restAPI.domian.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

}
