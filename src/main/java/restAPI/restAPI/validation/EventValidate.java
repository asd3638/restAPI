package restAPI.restAPI.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import restAPI.restAPI.domian.EventDto;

import java.time.LocalDateTime;

@Component
public class EventValidate {

    public void validate (EventDto eventDto, Errors errors) {
        if(eventDto.getBasePrice() > eventDto.getMaxPrice()) {
            errors.rejectValue("basePrice", "wrongValue", "basePrice is wrong");
            errors.rejectValue("maxPrice", "wrongValue", "maxPrice is wrong");
        }
    }
}
