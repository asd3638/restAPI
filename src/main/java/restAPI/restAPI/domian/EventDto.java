package restAPI.restAPI.domian;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


//Dto 는 사용자가 입력한 값을 처리하기 위한 클래스라고 생각하면 된다. 이건 Event class랑은 다르게 db랑 연결 되는게 아니라 사용자가 입력한 값만 처리한다.
//받기로 한 값들만 처리
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class EventDto {

    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    private LocalDateTime beginEnrollmentDateTime;
    @NotNull
    private LocalDateTime closeEnrollmentDateTime;
    @NotNull
    private LocalDateTime beginEventDateTime;
    @NotNull
    private LocalDateTime endEventDateTime;

    private String location;

    @Min(0)
    private int basePrice;
    @Min(0)
    private int maxPrice;
    @Min(0)
    private int limitOfEnrollment;

    public EventDto (String name, String description, int basePrice, int maxPrice, LocalDateTime beginEventDateTime, LocalDateTime endEventDateTime, LocalDateTime beginEnrollmentDateTime, LocalDateTime closeEnrollmentDateTime, String location, int limitOfEnrollment) {

        this.setName(name);
        this.setDescription(description);
        this.setBasePrice(basePrice);
        this.setMaxPrice(maxPrice);
        this.setBeginEventDateTime(beginEventDateTime);
        this.setEndEventDateTime(endEventDateTime);
        this.setBeginEnrollmentDateTime(beginEnrollmentDateTime);
        this.setCloseEnrollmentDateTime(closeEnrollmentDateTime);
        this.setLocation(location);
        this.setLimitOfEnrollment(limitOfEnrollment);
    }
}
