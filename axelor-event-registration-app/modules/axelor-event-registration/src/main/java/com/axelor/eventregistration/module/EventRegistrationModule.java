package com.axelor.eventregistration.module;

import com.axelor.app.AxelorModule;
import com.axelor.eventregistration.service.EventService;
import com.axelor.eventregistration.service.EventServiceImpl;

public class EventRegistrationModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(EventService.class).to(EventServiceImpl.class);
	}

}
