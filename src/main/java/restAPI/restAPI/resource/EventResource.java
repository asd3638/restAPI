package restAPI.restAPI.resource;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import restAPI.restAPI.domian.Event;

@Getter
@RequiredArgsConstructor
public class EventResource extends RepresentationModel {

    @JsonUnwrapped
    private final Event event;
}
