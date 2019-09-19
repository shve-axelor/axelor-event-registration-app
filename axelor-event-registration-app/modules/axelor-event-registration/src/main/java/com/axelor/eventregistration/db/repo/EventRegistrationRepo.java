package com.axelor.eventregistration.db.repo;

import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.event.registration.db.repo.EventRegistrationRepository;
import com.axelor.eventregistration.service.EventService;
import com.google.inject.Inject;

public class EventRegistrationRepo extends EventRegistrationRepository {
  
  @Inject EventService eventService;
  
  @Override
  public EventRegistration save(EventRegistration entity) {
    Event event = entity.getEvent();
    eventService.calculateTotalFields(event);
    entity.setEvent(event);
    return super.save(entity);
  }
}
