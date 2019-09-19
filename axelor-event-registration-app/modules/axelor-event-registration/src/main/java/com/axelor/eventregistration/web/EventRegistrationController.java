package com.axelor.eventregistration.web;

import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.event.registration.db.repo.EventRegistrationRepository;
import com.axelor.event.registration.db.repo.EventRepository;
import com.axelor.eventregistration.service.EventRegistrationService;
import com.axelor.eventregistration.service.EventService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventRegistrationController {

  @Inject EventRegistrationService eventRegistrationService;

  @Inject EventService eventService;

  @Inject EventRepository eventRepository;

  @Inject EventRegistrationRepository eventRegistrationRepository;

  public void setEventField(ActionRequest request, ActionResponse response) {
    EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
    if (request.getContext().getParent() != null) {
      eventRegistration.setEvent(request.getContext().getParent().asType(Event.class));
      response.setAttr("event", "hidden", true);
      response.setValues(eventRegistration);
    }
  }

  public void validateEventRegistration(ActionRequest request, ActionResponse response) {
    EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
    if (eventRegistration.getEvent() != null) {
      if (eventRegistrationService.checkEventCapacity(eventRegistration)) {
        response.setError("Total Number Of Registrations Are Exceeds Capacity");
      } else if (eventRegistration.getRegistrationDate() != null
          && eventRegistrationService.checkEventRegistrationDate(eventRegistration)) {
        response.setError("Registration Date For event Must Be In Between Registration Period");
      } else if (eventRegistration.getRegistrationDate() != null) {
        eventRegistrationService.calculateAmount(eventRegistration.getEvent(), eventRegistration);
        response.setValues(eventRegistration);
      }
    }
  }

  public void calculateAmountField(ActionRequest request, ActionResponse response) {
    EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
    Event event = eventRegistration.getEvent();
    if (request.getContext().getParent() != null) {
      event = request.getContext().getParent().asType(Event.class);
    }
    if (eventRegistration.getEvent() != null) {
      if (eventRegistration.getRegistrationDate() != null) {
        if (eventRegistrationService.checkEventRegistrationDate(eventRegistration)) {
          response.setError("Registration Date For event Must Be In Between Registration Period");
        } else {
          eventRegistrationService.calculateAmount(event, eventRegistration);
        }
      }
      response.setValues(eventRegistration);
    }
  }
}
