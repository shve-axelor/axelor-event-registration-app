package com.axelor.eventregistration.service;

import java.math.BigDecimal;
import java.time.Period;
import java.util.Comparator;
import java.util.List;

import com.axelor.event.registration.db.Discount;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;


public class EventServiceImpl implements EventService {

	@Override
	public int calculateAmount(Event event) {
		EventRegistration eventRegistrations = new EventRegistration();
		int flag = 0;
		List<Discount> discountList = event.getDiscountList();
		List<EventRegistration> eventRegistrationList = event.getEventRegistrationList();
		BigDecimal amaountCollected = BigDecimal.ZERO;
		BigDecimal totalDiscount = BigDecimal.ZERO;
		for (EventRegistration eventRegistration : eventRegistrationList) {	
			Period endPeriod = Period.between(eventRegistration.getRegistrationDate().toLocalDate(),
					event.getRegCloseDate());
			Period startPeriod = Period.between(event.getRegOpenDate(),
					eventRegistration.getRegistrationDate().toLocalDate());
			if (endPeriod.getDays() < 0 || startPeriod.getDays() < 0) {
				eventRegistrations = eventRegistration;
				flag = 1;
				break;
			}
			Comparator<Discount> comparator = (Discount d1, Discount d2) -> d1.getBeforeDays()
					.compareTo(d2.getBeforeDays());
			discountList.sort(comparator.reversed());
			for (Discount discount : event.getDiscountList()) {
				if (endPeriod.getDays() >= discount.getBeforeDays()) {
					eventRegistration.setAmount(event.getEventFees().subtract(discount.getDiscountAmount()));
					totalDiscount = totalDiscount.add(discount.getDiscountAmount());
					break;
				} else {
					eventRegistration.setAmount(event.getEventFees());
				}
			}
			amaountCollected = amaountCollected.add(eventRegistration.getAmount());
		}
		event.setTotalDiscount(totalDiscount);
		event.setAmountCollected(amaountCollected);
		if (flag == 1) {
			eventRegistrationList.remove(eventRegistrations);
		}
		return flag;
	}

	@Override
	public boolean checkEndDate(Event event) {
		if (event.getEndDate().compareTo(event.getStartDate()) <= 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean checkRegOpenDate(Event event) {
		if (event.getStartDate().toLocalDate().compareTo(event.getRegOpenDate()) <= 0) {
			return true;
		}
		return false;
	}

	@Override
	public boolean checkRegCloseDate(Event event) {
		if (event.getStartDate().toLocalDate().compareTo(event.getRegCloseDate()) <= 0
				|| event.getRegOpenDate().compareTo(event.getRegCloseDate()) >= 0) {
			return true;
		}
		return false;
	}

}
