package com.axelor.eventregistration.web;

import com.axelor.db.JpaSupport;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.eventregistration.service.EventService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class EventRegistrationController extends JpaSupport {
	
	@Inject EventService eventService;
	
	public void setEventField(ActionRequest request, ActionResponse response) {
		if (request.getContext().getParent() != null) {
			response.setValue("event", request.getContext().getParent().asType(Event.class));
			response.setAttr("eventDetails", "hidden", true);
		}
	}
	
	public void setAmountField(ActionRequest request, ActionResponse response) {
		//EventRegistration eventRegistration = request.getContext().asType(EventRegistration.class);
		
	}
}
