package com.axelor.eventregistration.web;

import java.math.BigDecimal;
import java.time.Period;
import java.util.List;

import com.axelor.db.JpaSupport;
import com.axelor.event.registration.db.Discount;
import com.axelor.event.registration.db.Event;
import com.axelor.eventregistration.service.EventService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventController extends JpaSupport {

	@Inject
	EventService eventService;

	public void checkDates(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		if (event.getEndDate() != null && event.getStartDate() != null) {
			if (eventService.checkEndDate(event)) {
				response.setError("End Date Must be Greater Than Start Date");
			} else {
				if (event.getRegOpenDate() != null) {
					if (eventService.checkRegOpenDate(event)) {
						response.setError("Registration Open Date Must be Less Than Event Start Date");
					} else {
						if (event.getRegCloseDate() != null) {
							if (eventService.checkRegCloseDate(event)) {
								response.setError(
										"Registration Close Date Must be Grater Than Registration Open Date And Less Than Event Start Date");
							}
						}
					}
				}
			}
		}
	}

	public void calculateDiscount(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		if (event.getEventFees().intValue() == 0) {
			event.setDiscountList(null);
		}
	}

	public void setDiscountList(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		if (event.getRegOpenDate() != null && event.getRegCloseDate() != null) {
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
			response.setValues(event);
			if (flag == 1) {
				response.setFlash("Before Days not greater than " + period.getDays());
			}

		} else {
			response.setValue("discountList", null);
			response.setFlash("Please Fill Registration Open And Close Date First.");
		}
	}

	public void calculateAmountField(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		int totalRegistrations = event.getEventRegistrationList().size();
		if (totalRegistrations > event.getCapacity()) {
			response.setFlash("Total Number Of Registrations Are Exceeds Capacity");
			event.getEventRegistrationList().remove(totalRegistrations - 1);
		} else {
			int flag = eventService.calculateAmount(event);
			if (flag == 1) {
				response.setFlash("Registration Date For event Must Be In Between Registration Period");
			}
		}
		response.setValues(event);
	}

}
