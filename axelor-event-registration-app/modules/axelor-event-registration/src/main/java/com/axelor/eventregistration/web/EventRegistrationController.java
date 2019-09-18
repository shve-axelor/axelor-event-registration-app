package com.axelor.eventregistration.web;

import com.axelor.db.JpaSupport;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.eventregistration.service.EventRegistrationService;
import com.axelor.eventregistration.service.EventService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class EventRegistrationController extends JpaSupport {

	@Inject
	EventRegistrationService eventRegistrationService;

	@Inject
	EventService eventService;

	public void setEventField(ActionRequest request, ActionResponse response) {
		//EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		if (request.getContext().getParent() != null) {
			response.setValue("event", request.getContext().getParent().asType(Event.class));
			response.setAttr("event", "hidden", true);
			/*eventRegistration.setEvent(request.getContext().getParent().asType(Event.class));
			if (eventRegistrationService.checkEventCapacity(eventRegistration)) {
				response.setError("Total Number Of Registrations Are Exceeds Capacity");
				
			}*/
		}
	}

	public void validateEventRegistration(ActionRequest request, ActionResponse response) {
		Context context = request.getContext();
		Event event = null;
		EventRegistration eventRegistration = new EventRegistration();
		List<EventRegistration> eventRegistrations = new ArrayList<EventRegistration>();
		if (request.getModel().equals("com.axelor.event.registration.db.Event")) {
			event = context.asType(Event.class);
		} else {
			eventRegistration = context.asType(EventRegistration.class);
		}
		if (event != null) {
			eventRegistrations = event.getEventRegistrationList();
		} else {
			eventRegistrations.add(eventRegistration);
		}

		for (EventRegistration eventRegistration2 : eventRegistrations) {
			if (eventRegistration2.getEvent() != null) {
				if (eventRegistrationService.checkEventCapacity(eventRegistration2)) {
					response.setError("Total Number Of Registrations Are Exceeds Capacity");
				}
				if (eventRegistration2.getRegistrationDate() != null) {
					if (eventRegistrationService.checkEventRegistrationDate(eventRegistration2)) {
						response.setError("Registration Date For event Must Be In Between Registration Period");
					} else {
						eventRegistrationService.calculateAmount(eventRegistration2.getEvent(), eventRegistration2);
					}
				}
			}
		}
		// event.setEventRegistrationList(eventRegistrations);
		// response.setValues(event);
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
