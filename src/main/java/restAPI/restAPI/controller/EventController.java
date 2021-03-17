package restAPI.restAPI.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import restAPI.restAPI.domian.Event;
import restAPI.restAPI.domian.EventDto;
import restAPI.restAPI.repository.EventRepository;
import restAPI.restAPI.validation.EventValidator;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
//
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final EventValidator eventValidator;
    private final ModelMapper modelMapper;


    @PostMapping
    //RequestBody 뿐 아니고 @Valid 어노테이션 주면 EventDto에 명시한 규칙에 맞게 매핑을 진행하고
    //만약 그러는 와중에 오류가 나타나면
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {

        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        eventValidator.validate(eventDto, errors);

        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        //원래대로라면 eventDto에 받은 값들을 여기서 다시 Event 객체 생성해서 변수들 다 넣어줘야 하는데
        /*
        * Event event = Event.Build()
        *                    .name("어쩌구 저쩌구") ....
        * 근데 하나 하나 이렇게 넣는 것을 대신 할 수 있는 기능이 있다.
        * */
        Event event = modelMapper.map(eventDto, Event.class);
        //save 하기 전에 update 하면서 값에 대한 변화
        event.update();
        Event newEvent = eventRepository.save(event);
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();

        return ResponseEntity.created(createdUri).body(event);
        //원래 연습했던 거랑 똑같은데 그 때는 html로 직접 보냈다면 지금은 응답  api로 변환해서 보내준다.
    }
}
