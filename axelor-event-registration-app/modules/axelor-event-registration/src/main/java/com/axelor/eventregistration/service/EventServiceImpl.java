package com.axelor.eventregistration.service;

import com.axelor.event.registration.db.Discount;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EventServiceImpl implements EventService {

  @Override
  public List<Discount> calculateDiscountList(Event event) {
    List<Discount> discounts = new ArrayList<Discount>();
    for (Discount discount : event.getDiscountList()) {
        BigDecimal total = discount.getDiscountPercent().multiply(event.getEventFees());
        discount.setDiscountAmount(total.divide(BigDecimal.valueOf(100))); 
        discounts.add(discount);
    }
    return discounts;
  }

  @Override
  public Event calculateTotalFields(Event event) {
    BigDecimal totalAmountCollected = BigDecimal.ZERO;
    int totalEntry = 0;
    BigDecimal totalDiscount = BigDecimal.ZERO;
    if (event.getEventRegistrationList() != null) {
      for (EventRegistration eventRegistration : event.getEventRegistrationList()) {
        totalAmountCollected = totalAmountCollected.add(eventRegistration.getAmount());
        totalDiscount =
            totalDiscount.add(event.getEventFees().subtract(eventRegistration.getAmount()));
      }
      totalEntry = event.getEventRegistrationList().size();
    }

    event.setTotalDiscount(totalDiscount);
    event.setTotalEntry(totalEntry);
    event.setAmountCollected(totalAmountCollected);
    return event;
  }
}
