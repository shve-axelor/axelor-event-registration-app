package com.axelor.eventregistration.service;

import java.util.List;

import com.axelor.event.registration.db.Discount;
import com.axelor.event.registration.db.Event;

public interface EventService {
  public List<Discount> calculateDiscountList(Event event);

  public Event calculateTotalFields(Event event);
}
