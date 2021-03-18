package restAPI.restAPI.resource;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import restAPI.restAPI.controller.EventController;
import restAPI.restAPI.domian.Event;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class EventResource extends EntityModel<Event> {

    public EventResource(Event event, Link... links) {
        super(event, links);
        //리소스 자체에 자기 자신에 대한 링크를 추가하도록 생성자 안에 add 넣어버림
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }
}