package com.axelor.eventregistration.service;

import com.axelor.event.registration.db.Event;

public interface EventService {
  public int setDiscountList(Event event);

  public Event calculateTotalFields(Event event);
}
