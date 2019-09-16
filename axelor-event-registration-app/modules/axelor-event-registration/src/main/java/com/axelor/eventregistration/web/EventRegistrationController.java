package com.axelor.eventregistration.web;

import java.time.Period;
import com.axelor.db.JpaSupport;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.eventregistration.service.EventRegistrationService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventRegistrationController extends JpaSupport {

	@Inject
	EventRegistrationService eventRegistrationService;

	public void setEventField(ActionRequest request, ActionResponse response) {
		if (request.getContext().getParent() != null) {
			response.setValue("event", request.getContext().getParent().asType(Event.class));
			response.setAttr("event", "hidden", true);
		}
	}

	public void validateEventRegistration(ActionRequest request, ActionResponse response) {
		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		if (eventRegistration.getEvent() != null) {
			if (eventRegistration.getEvent().getCapacity() == eventRegistration.getEvent().getEventRegistrationList()
					.size()) {
				response.setError("Total Number Of Registrations Are Exceeds Capacity");
			}
			if (eventRegistration.getRegistrationDate() != null) {
				Period endPeriod = Period.between(eventRegistration.getRegistrationDate().toLocalDate(),
						eventRegistration.getEvent().getRegCloseDate());
				Period startPeriod = Period.between(eventRegistration.getEvent().getRegOpenDate(),
						eventRegistration.getRegistrationDate().toLocalDate());
				if (endPeriod.getDays() < 0 || startPeriod.getDays() < 0) {
					response.setError("Registration Date For event Must Be In Between Registration Period");
				} else {
					eventRegistrationService.calculateAmount(eventRegistration.getEvent(), eventRegistration);
				}
			}
		}
		response.setValues(eventRegistration);
	}

	public void calculateAmountField(ActionRequest request, ActionResponse response) {
		EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		Event event = eventRegistration.getEvent();
		if (request.getContext().getParent() != null) {
			event = request.getContext().getParent().asType(Event.class);
		}
		if (eventRegistration.getEvent() != null) {
			if (eventRegistration.getRegistrationDate() != null) {
				Period endPeriod = Period.between(eventRegistration.getRegistrationDate().toLocalDate(),
						event.getRegCloseDate());
				Period startPeriod = Period.between(event.getRegOpenDate(),
						eventRegistration.getRegistrationDate().toLocalDate());
				if (endPeriod.getDays() < 0 || startPeriod.getDays() < 0) {
					response.setError("Registration Date For event Must Be In Between Registration Period");
				} else {
					eventRegistrationService.calculateAmount(event, eventRegistration);
				}
			}
			response.setValues(eventRegistration);
		}
	}
}
