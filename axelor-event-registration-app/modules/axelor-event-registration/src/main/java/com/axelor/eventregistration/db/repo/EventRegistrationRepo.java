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
    eventService.calculateTotalFields(event);
    return super.save(entity);
  }

  @Override
  public void remove(EventRegistration entity) {
    Event event = entity.getEvent();
    event.setTotalEntry(event.getTotalEntry() - 1);
    event.setAmountCollected(event.getAmountCollected().subtract(entity.getAmount()));
    event.setTotalDiscount(
        event.getTotalDiscount().subtract(event.getEventFees().subtract(entity.getAmount())));
    super.remove(entity);
  }
}
