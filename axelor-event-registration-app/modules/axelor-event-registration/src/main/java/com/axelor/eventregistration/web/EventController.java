package com.axelor.eventregistration.web;

import com.axelor.db.JpaSupport;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;

import com.axelor.eventregistration.service.EventRegistrationService;
import com.axelor.eventregistration.service.EventService;
import com.axelor.inject.Beans;

import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

import com.google.inject.Inject;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;

public class EventController extends JpaSupport {

	@Inject
	EventService eventService;

	@Inject
	EventRegistrationService eventRegistrationService;

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
		if (event.getEventRegistrationList() != null && !event.getEventRegistrationList().isEmpty()) {
			int totalRegistrations = 0;
			totalRegistrations = event.getEventRegistrationList().size() - 1;

			if (totalRegistrations == event.getCapacity() || event.getCapacity() == 0) {
				response.setError("Total Number Of Registrations Are Exceeds Capacity");
			}
			for (EventRegistration eventRegistration : event.getEventRegistrationList()) {
				if (eventRegistrationService.checkEventRegistrationDate(eventRegistration)) {
					response.setError("Registration Date For event Must Be In Between Registration Period");
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
				if (eventRegistrationService.checkEventRegistrationDate(eventRegistration)) {
					eventRegistration1 = eventRegistration;
				}
			}
		}
		eventRegistrations.remove(eventRegistration1);
		eventService.calculateTotalFields(event);
		response.setValues(event);
	}

	public void setTotalEntrys(ActionRequest request, ActionResponse response) {
		Event event = request.getContext().asType(Event.class);
		if (event.getEventRegistrationList() != null && !event.getEventRegistrationList().isEmpty()) {
			List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
			for (EventRegistration eventregistration : eventRegistrations) {
				eventRegistrationService.calculateAmount(event, eventregistration);
			}
		}
		eventService.calculateTotalFields(event);
		response.setValues(event);
	}

	@SuppressWarnings("unchecked")
	public void importEventRegistration(ActionRequest request, ActionResponse response) {
		LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
		MetaFile dataFile = Beans.get(MetaFileRepository.class).find(((Integer) map.get("id")).longValue());
		try {
			eventRegistrationService.importEventRegistration(dataFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
