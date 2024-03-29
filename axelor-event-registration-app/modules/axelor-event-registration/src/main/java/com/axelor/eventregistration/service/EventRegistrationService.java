package com.axelor.eventregistration.service;

import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.meta.db.MetaFile;

public interface EventRegistrationService {
  public void calculateAmount(Event event, EventRegistration eventregistration);

  public void importEventRegistration(MetaFile dataFile, Event event);

  public boolean checkEventCapacity(Event event);

  public boolean checkEventRegistrationDate(EventRegistration eventRegistration);
}
