package restAPI.restAPI.domian;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
public class Event {

    @Id @GeneratedValue
    private Integer id;

    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location;
    private int basePrice;
    private int maxPrice;
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;

    private LocalDateTime nowDateTime = LocalDateTime.now();

    //도메인 영역에서 처리해도 돼
    public void update() {
        //free
        if(this.basePrice == 0 && this.maxPrice ==0) {
            this.free = true;
        }
        else {
            this.free = false;
        }
        //offline
        if (this.location.isBlank() || this.location == null) {
            this.offline = true;
        } else {
            this.offline = false;
        }
        //eventStatus
        if (nowDateTime.isBefore(this.getBeginEnrollmentDateTime())) {
            this.eventStatus = EventStatus.DRAFT;
        } else if (nowDateTime.isBefore(this.getBeginEventDateTime())) {
            this.eventStatus = EventStatus.ENROLLMENT;
        } else if (nowDateTime.isAfter(this.getBeginEventDateTime()) && nowDateTime.isBefore(this.getEndEventDateTime())) {
            this.eventStatus = EventStatus.PUBLISHED;
        } else if (nowDateTime.isBefore(this.getCloseEnrollmentDateTime())) {
            this.eventStatus = EventStatus.ENROLLMENT;
        }
    }

}
