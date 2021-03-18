package restAPI.restAPI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import restAPI.restAPI.common.TestDescription;
import restAPI.restAPI.domian.Event;
import restAPI.restAPI.domian.EventDto;
import restAPI.restAPI.domian.EventStatus;
import restAPI.restAPI.repository.EventRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
        EventDto eventDto = EventDto.builder().build();
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
                .andExpect(status().isCreated())
                .andDo(print())
        //then
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.PUBLISHED.name()))
                ;
    }

}
