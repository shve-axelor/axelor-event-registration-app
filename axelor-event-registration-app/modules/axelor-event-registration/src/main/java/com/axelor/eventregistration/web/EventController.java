package com.axelor.eventregistration.web;

import com.axelor.data.ImportTask;
import com.axelor.data.Importer;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JpaSupport;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.eventregistration.service.EventService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.time.Period;
import java.util.List;

public class EventController extends JpaSupport {

	@Inject
	EventService eventService;

	public void checkDates(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		if (event.getStartDate() != null && event.getEndDate() != null) {
			if (eventService.checkEndDate(event)) {
				response.setError("End Date Must be Greater Than Start Date");
			} else if (event.getRegOpenDate() != null && eventService.checkRegOpenDate(event)) {
				response.setError("Registration Open Date Must be Less Than Event Start Date");
			} else if (event.getRegCloseDate() != null && event.getRegOpenDate() != null
					&& eventService.checkRegCloseDate(event)) {
				response.setError(
						"Registration Close Date Must be Grater Than Registration Open Date And Less Than Event Start Date");
			}
		}
		if (event.getCapacity() != null && event.getEventRegistrationList() != null
				&& !event.getEventRegistrationList().isEmpty()
				&& event.getCapacity() < event.getEventRegistrationList().size()) {
			response.setError("Capacity Cannot Set Less Than Total Number of Entrys.");
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
		Period period = Period.between(event.getRegOpenDate(), event.getRegCloseDate());
		if (event.getRegOpenDate() != null && event.getRegCloseDate() != null) {
			int flag = eventService.setDiscountList(event);
			response.setValues(event);
			if (flag == 1) {
				response.setFlash("Before Days not greater than " + period.getDays());
			}
		}
		if (event.getDiscountList() != null && !event.getDiscountList().isEmpty()) {
			response.setAttr("startDate", "readonly", true);
			response.setAttr("regOpenDate", "readonly", true);
			response.setAttr("regCloseDate", "readonly", true);
		} else {
			response.setAttr("startDate", "readonly", false);
			response.setAttr("regOpenDate", "readonly", false);
			response.setAttr("regCloseDate", "readonly", false);
		}
	}

	public void checkEventRegistrationList(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
		EventRegistration eventRegistration1 = new EventRegistration();
		int totalRegistrations = 0;
		if (event.getEventRegistrationList() != null && !event.getEventRegistrationList().isEmpty()) {
			totalRegistrations = event.getEventRegistrationList().size() - 1;
		}
		if (totalRegistrations == event.getCapacity() || event.getCapacity() == 0) {
			response.setFlash("Total Number Of Registrations Are Exceeds Capacity");
			eventRegistration1 = eventRegistrations.get(totalRegistrations);
		} else {
			for (EventRegistration eventRegistration : eventRegistrations) {
				Period endPeriod = Period.between(eventRegistration.getRegistrationDate().toLocalDate(),
						event.getRegCloseDate());
				Period startPeriod = Period.between(event.getRegOpenDate(),
						eventRegistration.getRegistrationDate().toLocalDate());
				if (endPeriod.getDays() < 0 || startPeriod.getDays() < 0) {
					eventRegistration1 = eventRegistration;
				}
			}
		}
		eventRegistrations.remove(eventRegistration1);
		/*
		 * if(eventRegistrations != null && !eventRegistrations.isEmpty()) {
		 * response.setAttr("discountList", "readonly", true); }
		 */
		eventService.calculateTotalFields(event);
		response.setValues(event);
	}

	public void setTotalEntrys(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		eventService.calculateTotalFields(event);
		response.setValues(event);
	}

	public void importEventRegistration(ActionRequest request, ActionResponse response) {
		// Event event = request.getContext().asType(Event.class);
		Importer importer = new CSVImporter("event-registration.xml","data/csv-multi");
		importer.run();
	}
}
