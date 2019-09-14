package com.axelor.eventregistration.service;

import com.axelor.event.registration.db.Event;

public interface EventService {
	public boolean checkEndDate(Event event);
	public boolean checkRegOpenDate(Event event);
	public boolean checkRegCloseDate(Event event);
	public int calculateAmount(Event event);
}
