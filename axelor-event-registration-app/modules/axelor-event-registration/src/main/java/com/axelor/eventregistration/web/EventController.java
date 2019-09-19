package com.axelor.eventregistration.web;

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.Model;
import com.axelor.event.registration.db.Event;
import com.axelor.event.registration.db.EventRegistration;
import com.axelor.event.registration.db.repo.EventRepository;
import com.axelor.eventregistration.service.EventRegistrationService;
import com.axelor.eventregistration.service.EventService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventController {

  @Inject private TemplateMessageService templateMessageService;

  @Inject private TemplateRepository templateRepository;

  @Inject MessageService messageService;

  @Inject MetaModelRepository metaModelRepository;

  @Inject private EventService eventService;

  @Inject private EventRegistrationService eventRegistrationService;

  @Inject private EmailAccountRepository emailAccountRepository;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void checkDates(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    if (event.getStartDate() != null && event.getEndDate() != null) {
      if (eventService.checkEndDate(event)) {
        response.setError("End Date Must be Greater Than Start Date");
      } else if (event.getRegOpenDate() != null && eventService.checkRegOpenDate(event)) {
        response.setError("Registration Open Date Must be Less Than Event Start Date");
      } else if (event.getRegCloseDate() != null
          && event.getRegOpenDate() != null
          && eventService.checkRegCloseDate(event)) {
        response.setError(
            "Registration Close Date Must be Grater Than Registration Open Date And Less Than Event Start Date");
      }
    } else if (event.getEventRegistrationList() != null
        && !event.getEventRegistrationList().isEmpty()) {
      int totalRegistrations = 0;
      totalRegistrations = event.getEventRegistrationList().size() - 1;

      if (totalRegistrations == event.getCapacity() || event.getCapacity() == 0) {
        response.setError("Total Number Of Registrations Are Exceeds Capacity");
      }
      for (EventRegistration eventRegistration : event.getEventRegistrationList()) {
        if (eventRegistrationService.checkEventRegistrationDate(eventRegistration)) {
          response.setError("Registration Date For event Must Be In Between Registration Period");
          break;
        }
      }
    } else {
      eventService.calculateTotalFields(event);
      response.setValues(event);
    }
  }

  public void calculateDiscount(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    if (event.getEventFees().intValue() == 0) {
      event.setDiscountList(null);
    }
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
    if (totalRegistrations == event.getCapacity() && totalRegistrations != 0) {
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

  @SuppressWarnings("unchecked")
  public void importEventRegistration(ActionRequest request, ActionResponse response) {
    LinkedHashMap<String, Object> map =
        (LinkedHashMap<String, Object>) request.getContext().get("metaFile");
    MetaFile dataFile =
        Beans.get(MetaFileRepository.class).find(((Integer) map.get("id")).longValue());
    Event event =
        Beans.get(EventRepository.class)
            .find(((Integer) request.getContext().get("_event")).longValue());
    try {
      eventRegistrationService.importEventRegistration(dataFile, event);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Object validateEventRegistrationImport(Object bean, Map<String, Object> context) {
    assert bean instanceof EventRegistration;
    EventRegistration eventRegistration = (EventRegistration) bean;
    Event event = (Event) context.get("_event");
    eventRegistration.setEvent(event);
    eventRegistrationService.calculateAmount(event, eventRegistration);
    return bean;
  }

  @SuppressWarnings("unused")
  public void sendEmailFromEvent(ActionRequest request, ActionResponse response) {
    Model context = request.getContext().asType(Model.class);
    String model = request.getModel();
    Event event = request.getContext().asType(Event.class);

    LOG.debug("Call message wizard for model : {} ", model);

    String[] decomposeModel = model.split("\\.");
    String simpleModel = decomposeModel[decomposeModel.length - 1];

    try {
      Template template =
          templateRepository
              .all()
              .filter("self.metaModel.fullName = ?1 AND self.isSystem != true", model)
              .fetchOne();
      EmailAccount emailAccount = emailAccountRepository.all().fetchOne();
      if (template != null && emailAccount != null) {
        Message message = null;
        String toRecipents = "";
        List<EventRegistration> eventRegistrations = event.getEventRegistrationList();
        for (EventRegistration eventRegistration : eventRegistrations) {
          toRecipents = toRecipents + eventRegistration.getEmail() + ";";
        }
        template.setToRecipients(toRecipents);
        message =
            templateMessageService.generateMessage(
                Long.parseLong(context.getId().toString()), model, simpleModel, template);
        message.setSentDateT(LocalDateTime.now());
        message.setSenderUser(AuthUtils.getUser());
        message.setMailAccount(emailAccount);
        if (message == null) {
          response.setError("Email Is Not Sent");
        } else {
          messageService.sendMessage(message);
          response.setReload(true);
          response.setFlash(I18n.get(IExceptionMessage.MESSAGE_4));
        }
      } else {
        response.setError("Set Email Template For Event Or Create Email Account");
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
