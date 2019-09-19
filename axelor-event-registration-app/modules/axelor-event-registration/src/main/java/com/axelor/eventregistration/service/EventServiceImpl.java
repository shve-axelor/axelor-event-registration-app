package com.axelor.eventregistration.service;

import com.axelor.event.registration.db.Discount;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;

import java.math.BigDecimal;
import java.time.Period;
import java.util.List;

public class EventServiceImpl implements EventService {

  @Override
  public int setDiscountList(Event event) {
    Period period = Period.between(event.getRegOpenDate(), event.getRegCloseDate());
    List<Discount> discountList = event.getDiscountList();
    Discount discount = new Discount();
    int flag = 0;
    for (Discount discounts : discountList) {
      if (discounts.getBeforeDays() > period.getDays()) {
        discount = discounts;
        flag = 1;
        break;
      } else {
        BigDecimal total = discounts.getDiscountPercent().multiply(event.getEventFees());
        discounts.setDiscountAmount(total.divide(BigDecimal.valueOf(100)));
      }
    }
    discountList.remove(discount);
    event.setDiscountList(discountList);
    return flag;
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
