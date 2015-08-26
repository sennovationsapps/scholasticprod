package controllers;

import models.Event;
import models.security.SecurityRole;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.events.eventWorkflow;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

// TODO: Auto-generated Javadoc
/**
 * The Class EventWorkflowMgmt.
 */
public class EventWorkflowMgmt extends Controller {

	/**
	 * Creates the.
	 * 
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/loginTaxid")
	public static Result create() {
		ControllerUtil.convertSessionMessages();
		// return ok(viewEventWorkflow.render(null));
		return ok(eventWorkflow.render(null));
	}

	/**
	 * Display the 'new event form'.
	 * 
	 * @param id
	 *            the id
	 * @return the result
	 */
	@Restrict({ @Group(SecurityRole.ROOT_ADMIN),
			@Group(SecurityRole.SYS_ADMIN), @Group(SecurityRole.EVENT_ADMIN),
			@Group(SecurityRole.EVENT_ASSIST) })
	@SubjectPresent(content = "/loginTaxid")
	public static Result update(Event event) {
		if (event.isIdOnly()) {
			event = Event.findByIdWithMin(event.id);
		}
		if (!Event.canManage(ControllerUtil.getLocalUser(session()), event)) {
			flash(ControllerUtil.FLASH_DANGER_KEY,
					"The requested event action cannot be completed by the logged in user.");
			return redirect(routes.Application.index());
		}
		ControllerUtil.convertSessionMessages();
		// User user = ControllerUtil.getLocalUser(session());
		// Logger.debug(ToStringBuilder.reflectionToString(user));
		// Event event = Event.findById(id);
		// return ok(viewEventWorkflow.render(event));
		return ok(eventWorkflow.render(event));
	}

}