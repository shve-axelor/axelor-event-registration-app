package com.axelor.eventregistration.db.repo;

import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.event.registration.db.repo.EventRegistrationRepository;
import com.axelor.eventregistration.service.EventService;
import com.google.inject.Inject;

public class EventRegistrationRepo extends EventRegistrationRepository {
  
  @Inject EventService eventService;
  
  @Inject EventRegistrationRepository eventRegistrationRepo;
  
  @Override
  public EventRegistration save(EventRegistration entity) {
    Event event = entity.getEvent();
    /*EventRegistration eventRegistration = eventRegistrationRepo.find(Long.parseLong(""+entity.getId())) ;
    if(eventRegistration != null) {
      
    }*/
    eventService.calculateTotalFields(event);
    entity.setEvent(event);
    return super.save(entity);
  }
}
