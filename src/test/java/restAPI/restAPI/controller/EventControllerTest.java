package restAPI.restAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import restAPI.restAPI.common.RestDocsConfiguration;
import restAPI.restAPI.common.TestDescription;
import restAPI.restAPI.domian.Event;
import restAPI.restAPI.domian.EventDto;
import restAPI.restAPI.domian.EventStatus;
import restAPI.restAPI.repository.EventRepository;

import javax.print.attribute.standard.Media;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.mockito.Mockito.description;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ModelMapper modelMapper;

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    //Dto로 값을 제대로 전달해준 경우
    public void createEvent() throws Exception {
        EventDto eventDto = EventDto.builder()
                            .name("Spring")
                            .description("RestAPI")
                            .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                            .closeEnrollmentDateTime(LocalDateTime.of(2028, 11, 24, 10, 10))
                            .beginEventDateTime(LocalDateTime.of(2018, 11, 20, 9, 10))
                            .endEventDateTime(LocalDateTime.of(2030, 11, 20, 8, 10))
                            .basePrice(100)
                            .maxPrice(200)
                            .limitOfEnrollment(100)
                            .location("강남역")
                            .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("offline").value(Matchers.not(true)))
                .andExpect(jsonPath("free").value(Matchers.not(true)))
                //마지막 세 줄이 중요한데
                //어떤 값을 사용자가 입력해도 그걸 서버 로직에 맞게 바꿔서 저장해야 한다.
                //아무 값이나 그냥 입력 됐다고 저장하면 절대 안된다.
                //그 로직에 맞게 저장하는 방법은 event를 controller로 보낼 때 EventDto와 같은 출력 폼에 따로 담아서 보내고 (새로운 객체 생성)
                //controller에서 그걸 다시 매핑하면 원래 만들어놨던 로직대로 데이터를 저장할 수 있다.

                //==링크 정보 제대로 받는지에 대한 테스트 코드==//
                //link에 self, query-events, update-events 등이 있는지 확인하는 것이다.
                //세 가지의 링크가 응답으로 나오길 기대한다.
                //클라이언트가 json형식의 응답을 보고 어디로 전이할지 선택할 수 있어야 하는데 그걸 가능하게 하는 기능들을 포함하는게 hateoas이다.
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query_events").exists())
                .andExpect(jsonPath("_links.update_event").exists())

                //원래는 기본적인 문서만 생성되었는데
                //이러면 link와 관련된 문서 조각도 함께 생성되게 된다.
                //이런 식으로 request respond 다 만들면 된다.
                //기본 생성할 때 포함되어 있는데 직접 명시하지 않은 것들이 있으면 에러를 출력한다.
                //그러면 앞에 relaxed prefix를 붙여서 기준을 완화시켜준다.
                //relaxed는 일부분만 테스트할 수 있지만 확실하게 명확한 문서를 만드는 것을 방해한다.

                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query_events").description("link to query_events"),
                                linkWithRel("update_event").description("link to update_event")),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("accept header")),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("Name of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("Name of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("Name of new event"),
                                fieldWithPath("beginEventDateTime").description("Name of new event"),
                                fieldWithPath("endEventDateTime").description("Name of new event"),
                                fieldWithPath("location").description("Name of new event"),
                                fieldWithPath("basePrice").description("Name of new event"),
                                fieldWithPath("maxPrice").description("Name of new event"),
                                fieldWithPath("limitOfEnrollment").description("Name of new event"))
                                )
                        )
                ;
    }

    @Test
    //Dto로 전달해야 하는 값을 Event로 잘못 전달한 경우
    @TestDescription("입력할 수 없는 값을 추가로 입력했을 때 발생하는 이벤트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("RestAPI")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                .endEventDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .offline(true)
                .free(true)
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
        //마지막 세 줄이 중요한데
        //어떤 값을 사용자가 입력해도 그걸 서버 로직에 맞게 바꿔서 저장해야 한다.
        //아무 값이나 그냥 입력 됐다고 저장하면 절대 안된다.
        //그 로직에 맞게 저장하는 방법은 event를 controller로 보낼 때 EventDto와 같은 출력 폼에 따로 담아서 보내고 (새로운 객체 생성)
        //controller에서 그걸 다시 매핑하면 원래 만들어놨던 로직대로 데이터를 저장할 수 있다.
        ;
    }

    @Test
    @TestDescription("@Empty , @NotNull 어노테이션 확인")
    public void createEvent_Bad_Request_Empty_Input() throws Exception{
        //given
        EventDto eventDto = new EventDto();
        eventDto.setName("");
        //when

        //then
        this.mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(eventDto)))
                    .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("잘못된 입력값이 입력됬을 때")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 23, 14, 23))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 21, 14, 23))
                .beginEventDateTime(LocalDateTime.of(2018, 12, 24, 14, 30))
                .endEventDateTime(LocalDateTime.of(2018, 12, 6, 14, 30))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("D Start up Factory")
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andDo(print())

                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].rejectedValue").exists())
        ;
    }

    @Test
    public void testFree() throws Exception{
        //given
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("RestAPI")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                .closeEnrollmentDateTime(LocalDateTime.of(2028, 11, 24, 10, 10))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 20, 9, 10))
                .endEventDateTime(LocalDateTime.of(2030, 11, 20, 8, 10))
                .basePrice(200)
                .maxPrice(300)
                .limitOfEnrollment(100)
                .build();

        //when
        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andDo(print())
        //then
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.PUBLISHED.name()))
                ;
    }

    @Test
    @TestDescription("30개의 이벤트 중 10개씩 두 번째 페이지 조회하기")
    public void getEvents() throws Exception {
        //given
        IntStream.range(0, 30).forEach(i -> {
            this.generateEvent(i);
        });
        //when
        this.mockMvc.perform(get("/api/events")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "name,DESC")
                        )
                    .andDo(print())
                    .andExpect(status().isOk())

        ;
        //then
    }

    @Test
    @TestDescription("기존의 이벤트 하나 조회하기")
    public void getEvent() throws Exception{
        //given
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 20, 14, 23))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 21, 14, 23))
                .beginEventDateTime(LocalDateTime.of(2018, 12, 5, 14, 30))
                .endEventDateTime(LocalDateTime.of(2018, 12, 6, 14, 30))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("D Start up Factory")
                .build();
        Event event = modelMapper.map(eventDto, Event.class);
        eventRepository.save(event);
        //when
        this.mockMvc.perform(get("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event))
        )
        .andDo(print())
        .andExpect(status().isOk())
                //응답 받는 데이터들 확인하고 싶으면 무조건 jaonPath 왜냐면 json으로 파싱해서 데이터 보내니까 확인하려면 json풀어야지
        .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
        .andExpect(jsonPath("name").exists())
        .andExpect(jsonPath("id").exists())
        .andExpect(jsonPath("description").exists())
        .andExpect(jsonPath("_links.self").exists())
        ;
    }

    @Test
    @TestDescription("기존의 이벤트 하나 조회하기에서 만약 조회한 값이 없다면")
    //아무것도 없는 이벤트를 조회하면 이벤트 응답이
    //404응답이 나와야 한다.
    public void getEmptyEvent() throws Exception{
        //given 이벤트 생성하고 넣으면 당연히 200 응답 요청 return 하지
        //Event event = this.generateEvent(200);
        //when
        this.mockMvc.perform(get("/api/events/{id}", 200))
                .andDo(print())
                //.andExpect(status().is4xxClientError());
                .andExpect(status().isNotFound());
    }

    @Test
    @TestDescription("정상적으로 수정하기 예제")
    public void updateEvent() throws Exception{
        //given
        //evet에 담았다가 eventDto로 매핑함.
        Event event = generateEvent(200);
        EventDto eventDto = modelMapper.map(event, EventDto.class);
        String updatedName = "updated";
        eventDto.setName(updatedName);
        //when
        mockMvc.perform(put("/api/events/{id}", event.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(updatedName))
                .andExpect(jsonPath("id").value(event.getId()))
                .andExpect(jsonPath("_links.self").exists())
                ;
        //then
    }

    @Test
    @TestDescription("입력값이 자체가 잘못된 경우 수정하기 실패 예제")
    public void updateEventErrorEmpty() throws Exception{
        //given
        //그냥 값 자체가 잘못된 경우 사실 event 여기선 생성할 필요도 없는데 일단 생성
        Event event = generateEvent(200);
        EventDto eventDto = new EventDto();
        //값을 안 넣었으니까 @Valid 어노테이션에서 에러 잡히겠네
        //when
        this.mockMvc.perform(put("api/events/{id}", event.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("name").value(updatedName))
//                .andExpect(jsonPath("id").value(event.getId()))
//                .andExpect(jsonPath("_links.self").exists())
        ;
        //then
    }

    @Test
    @TestDescription("입력값이 잘못된 경우 수정하기 실패 예제")
    public void updateEventErrorWrong() throws Exception{
        //given
        //그냥 값 자체가 잘못된 경우
        Event event = generateEvent(200);
        EventDto eventDto = new EventDto();
        eventDto.setName("updated");
        eventDto.setBasePrice(300);
        //값을 안 넣었으니까 @Valid 어노테이션에서 에러 잡히겠네
        //when
        this.mockMvc.perform(put("api/events/{id}", event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                //이름이 Spring이라는 것은 update가 제대로 반영되지 않음을 뜻한다.
                .andExpect(jsonPath("name").value("Spring"))
                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("name").value(updatedName))
//                .andExpect(jsonPath("id").value(event.getId()))
//                .andExpect(jsonPath("_links.self").exists())
        ;
        //then
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정하기 실패 예제")
    public void updateEventErrorEmptyId() throws Exception{
        //given
        //그냥 값 자체가 잘못된 경우
        Event event = generateEvent(200);
        EventDto eventDto = new EventDto();
        eventDto.setName("updated");
        eventDto.setBasePrice(300);
        //값을 안 넣었으니까 @Valid 어노테이션에서 에러 잡히겠네
        //when
        this.mockMvc.perform(put("api/events/{id}", 201)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                //이름이 Spring이라는 것은 update가 제대로 반영되지 않음을 뜻한다.
                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("name").value(updatedName))
//                .andExpect(jsonPath("id").value(event.getId()))
//                .andExpect(jsonPath("_links.self").exists())
        ;
        //then
    }

    private Event generateEvent (int index) {
        Event event = Event.builder()
                .id(index)
                .name("Spring" + index)
                .description("RestAPI" + index)
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 30, 10, 10))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                .endEventDateTime(LocalDateTime.of(2018, 11, 21, 10, 10))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .offline(true)
                .free(true)
                .build();
        eventRepository.save(event);
        return event;
    }

}
