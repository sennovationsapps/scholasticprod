package security;

import models.security.User;

import org.apache.commons.lang.builder.ToStringBuilder;

import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

import controllers.ControllerUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class AuthorizeDeadboltHandler.
 */
public class AuthorizeDeadboltHandler extends AbstractDeadboltHandler {

	@Override
	public F.Promise<Result> beforeAuthCheck(final Http.Context context) {
		if (PlayAuthenticate.isLoggedIn(context.session())) {
			// user is logged in
			return F.Promise.pure(null);
		} else {
			// user is not logged in

			// call this if you want to redirect your visitor to the page that
			// was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page
			// defined by your resolver
			final String originalUrl = PlayAuthenticate
					.storeOriginalUrl(context);

			context.flash().put("error",
					"You need to log in first, to view '" + originalUrl + "'");
            return F.Promise.promise(new F.Function0<Result>()
            {
                @Override
                public Result apply() throws Throwable
                {
                    return redirect(PlayAuthenticate.getResolver().login());
                }
            });
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * be.objectify.deadbolt.java.AbstractDeadboltHandler#getDynamicResourceHandler
	 * (play.mvc.Http.Context)
	 */
	@Override
	public DynamicResourceHandler getDynamicResourceHandler(
			final Http.Context context) {
		return null;
	}
	
	@Override	
	   public F.Promise<Subject> getSubject(Http.Context context)
	    {
    		final AuthUserIdentity u = PlayAuthenticate.getUser(context);
    		Logger.debug("Trying to getSubject " + u);
    		if(u != null) {
    			Logger.debug("Trying to getSubject " + ToStringBuilder.reflectionToString(context));
    		}
	        // in a real application, the user name would probably be in the session following a login process
	        return F.Promise.promise(new F.Function0<Subject>()
	        {
	            @Override
	            public Subject apply() throws Throwable {
	        		// Caching might be a good idea here
	        		return User.findByAuthUserIdentity(u);
	            }
	        });
	    }

	
	@Override
	public F.Promise<Result> onAuthFailure(final Http.Context context,
			final String content) {
		// if the user has a cookie with a valid user and the local user has
		// been deactivated/deleted in between, it is possible that this gets
		// shown. You might want to consider to sign the user out in this case.
        return F.Promise.promise(new F.Function0<Result>()
        {
            @Override
            public Result apply() throws Throwable
            {
            	context.flash().put(ControllerUtil.FLASH_DANGER_KEY,
        						"The requested event action cannot be completed by the logged in user.");
        				return forbidden(views.html.index.render());
            }
        });
	}
}
