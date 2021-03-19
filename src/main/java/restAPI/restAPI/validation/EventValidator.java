package restAPI.restAPI.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import restAPI.restAPI.domian.EventDto;

import java.time.LocalDateTime;

@Component
@Slf4j
public class EventValidator {
    public void validate(EventDto eventDto, Errors errors) {
        int maxPrice = eventDto.getMaxPrice();
        LocalDateTime closeEnrollmentDateTime = eventDto.getCloseEnrollmentDateTime();
        LocalDateTime endEventDateTime = eventDto.getEndEventDateTime();
        if (maxPrice < eventDto.getBasePrice()) {
            //log.info("왜 안나와");
            errors.rejectValue("maxPrice", "wrong.value", "maxPrice is wrong");
            errors.rejectValue("basePrice", "wrong.value", "basePrice is wrong");
        }
        if (closeEnrollmentDateTime.isBefore(eventDto.getBeginEnrollmentDateTime()) || endEventDateTime.isBefore(eventDto.getBeginEventDateTime())) {
            //log.info("왜 안나와");
            errors.rejectValue("closeEnrollmentDateTime", "wrong.value", "DateTime is wrong");
        }
    }
}
