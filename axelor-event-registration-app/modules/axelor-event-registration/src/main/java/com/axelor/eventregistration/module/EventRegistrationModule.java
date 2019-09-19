package com.axelor.eventregistration.module;

import com.axelor.app.AxelorModule;
import com.axelor.event.registration.db.repo.EventRegistrationRepository;
import com.axelor.eventregistration.db.repo.EventRegistrationRepo;
import com.axelor.eventregistration.service.EventRegistrationService;
import com.axelor.eventregistration.service.EventRegistrationServiceImpl;
import com.axelor.eventregistration.service.EventService;
import com.axelor.eventregistration.service.EventServiceImpl;

public class EventRegistrationModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(EventService.class).to(EventServiceImpl.class);
    bind(EventRegistrationService.class).to(EventRegistrationServiceImpl.class);
    bind(EventRegistrationRepository.class).to(EventRegistrationRepo.class);
  }
}
