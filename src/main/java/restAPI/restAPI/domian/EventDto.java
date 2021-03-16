package restAPI.restAPI.domian;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;


//Dto 는 사용자가 입력한 값을 처리하기 위한 클래스라고 생각하면 된다. 이건 Event class랑은 다르게 db랑 연결 되는게 아니라 사용자가 입력한 값만 처리한다.
//받기로 한 값들만 처리
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class EventDto {

    private String name;
    private String description;
    private LocalDateTime beginErollmentDateTime;
    private LocalDateTime closeErollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
}
