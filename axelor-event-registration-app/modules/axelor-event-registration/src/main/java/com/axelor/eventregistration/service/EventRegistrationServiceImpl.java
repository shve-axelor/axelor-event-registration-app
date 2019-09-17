package com.axelor.eventregistration.service;

import com.axelor.event.registration.db.Discount;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

public class EventRegistrationServiceImpl implements EventRegistrationService {

  @Override
  public void calculateAmount(Event event, EventRegistration eventregistration) {
    List<Discount> discountList = event.getDiscountList();
    Period endPeriod =
        Period.between(
            eventregistration.getRegistrationDate().toLocalDate(), event.getRegCloseDate());
    if (discountList != null && !discountList.isEmpty()) {
      Comparator<Discount> comparator =
          (Discount d1, Discount d2) -> d1.getBeforeDays().compareTo(d2.getBeforeDays());
      discountList.sort(comparator.reversed());
      for (Discount discount : event.getDiscountList()) {
        if (endPeriod.getDays() >= discount.getBeforeDays()) {
          eventregistration.setAmount(event.getEventFees().subtract(discount.getDiscountAmount()));
          break;
        } else {
          eventregistration.setAmount(event.getEventFees());
        }
      }
    } else {
      eventregistration.setAmount(event.getEventFees());
    }
  }
}
