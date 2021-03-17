package restAPI.restAPI.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import restAPI.restAPI.domian.Event;
import restAPI.restAPI.domian.EventDto;
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
    //Dto로 값을 제대로 전달해준 경우
    public void createEvent() throws Exception {
        EventDto eventDto = EventDto.builder()
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
                            .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
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
        ;
    }

    @Test
    //Dto로 전달해야 하는 값을 Event로 잘못 전달한 경우
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
    //Dto로 전달해야 하는 값을 Event로 잘못 전달한 경우
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {

        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("RestAPI")
                .beginEnrollmentDateTime(LocalDateTime.of(2018, 11, 24, 10, 10))
                .closeEnrollmentDateTime(LocalDateTime.of(2018, 11, 20, 10, 10))
                .beginEventDateTime(LocalDateTime.of(2018, 11, 20, 9, 10))
                .endEventDateTime(LocalDateTime.of(2018, 11, 20, 8, 10))
                .basePrice(1000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역")
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(eventDto))
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

}
