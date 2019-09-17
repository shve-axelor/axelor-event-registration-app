package com.axelor.eventregistration.service;

import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;

public interface EventRegistrationService {
  public void calculateAmount(Event event, EventRegistration eventregistration);
}
