package restAPI.restAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import restAPI.restAPI.domian.Event;

public interface EventRepository extends JpaRepository<Event, Integer> {


}
