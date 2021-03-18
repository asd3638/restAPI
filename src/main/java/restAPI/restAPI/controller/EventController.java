package restAPI.restAPI.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.PagedResourcesAssemblerArgumentResolver;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import restAPI.restAPI.common.ErrorResource;
import restAPI.restAPI.domian.Event;
import restAPI.restAPI.domian.EventDto;
import restAPI.restAPI.repository.EventRepository;
import restAPI.restAPI.resource.EventResource;
import restAPI.restAPI.validation.EventValidator;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
//
@RequiredArgsConstructor
@Log4j
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

        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();

        EventResource eventResource = new EventResource(event);
        //event를 eventResource로 변환하면 링크를 추가할 수 있다.
        eventResource.add(linkTo(EventController.class).withRel("query_events"));
        //수정이나 자기자신이나 링크는 같은데 담고 있는 정보가 다르다?
        //이거 뭔 말인지 모르겠음!
        eventResource.add(selfLinkBuilder.withSelfRel());
        eventResource.add(selfLinkBuilder.withRel("update_event"));

        return ResponseEntity.created(createdUri).body(eventResource);
        //원래 연습했던 거랑 똑같은데 그 때는 html로 직접 보냈다면 지금은 응답  api로 변환해서 보내준다.
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable,
                                      PagedResourcesAssembler<Event> assembler) {
        Page<Event> page = this.eventRepository.findAll(pageable);
        //var pagedResources = assembler.toModel(page, e -> new EventResource(e));
        PagedModel<EntityModel<Event>> models = assembler.toModel(page);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        //optional은 만약에 null값을 읽었을 때 오류 메세지 제대로 전달할 수 있기 위한 클래스라고 보면 된다.
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Event event = optionalEvent.get();
        return ResponseEntity.ok(new EventResource(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        this.eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event savedEvent = this.eventRepository.save(this.modelMapper.map(eventDto, Event.class));

        EventResource eventResource = new EventResource(savedEvent);

        return ResponseEntity.ok(eventResource);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorResource(errors));
    }
}
