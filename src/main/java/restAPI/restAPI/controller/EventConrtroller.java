package restAPI.restAPI.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import restAPI.restAPI.domian.Event;
import restAPI.restAPI.repository.EventRepository;
import restAPI.restAPI.service.EventService;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class EventConrtroller {

    private final EventRepository eventRepository;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody Event event) {
        Event newEvent = eventRepository.save(event);
        URI createdUri = linkTo(EventConrtroller.class).slash(newEvent.getId()).toUri();

        return ResponseEntity.created(createdUri).body(event);
        //원래 연습했던 거랑 똑같은데 그 때는 html로 직접 보냈다면 지금은 응답  api로 변환해서 보내준다.
    }
}
