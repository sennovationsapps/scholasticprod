import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

import models.Donation;
import models.Event;
import models.EventPages;
import models.Pfp;
import models.Team;
import models.security.RootEmailAuthUser;
import models.security.SecurityRole;
import models.security.User;
import models.security.UserPermission;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.h2.tools.Script;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.api.libs.concurrent.Akka;
import play.db.jpa.Transactional;
import play.libs.F.Promise;
import play.libs.Time.CronExpression;
import play.mvc.Call;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import providers.email.EmailAuthUser;
import providers.taxid.TaxidAuthUser;
import play.mvc.*;
import play.mvc.Http.*;
import play.api.mvc.EssentialFilter;
import play.api.mvc.Handler;

import com.mohiva.play.htmlcompressor.java.HTMLCompressorFilter;
//import com.mohiva.play.xmlcompressor.java.XMLCompressorFilter;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import controllers.routes;

/**
 * The Class Global. This is run when the application starts up. This is used
 * for loading configurations, default records, and is the setup for starting
 * PlayAuthenticate.
 */
public class Global extends GlobalSettings {

//	@Override
//	public Handler onRouteRequest(RequestHeader request) {
//	    String[] x = request.headers().get("X-Forwarded-Proto");
//	    if (Play.isProd() && (x == null || x.length == 0 || x[0] == null || !x[0].contains("https")))
//	        return controllers.Default.redirect("https://" + request.host() + request.uri());
//	    return super.onRouteRequest(request);
//	}

	 public Promise<Result> onError(RequestHeader request, Throwable t) {
		 Logger.error("RequestHeader during error - {}", ToStringBuilder.reflectionToString(request));
			Logger.error("An error occurred - ", t);
	        return Promise.<Result>pure(Results.internalServerError(
	        				views.html.error_page.render(t)
	        ));
	    }


    /**
     * Get the filters that should be used to handle each request.
     */
    @SuppressWarnings("unchecked")
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[] {
            HTMLCompressorFilter.class,
//            XMLCompressorFilter.class
        };
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see play.GlobalSettings#onStart(play.Application)
	 */
	@Override
	public void onStart(Application app) {

		Logger.debug("Starting the application up");

		PlayAuthenticate.setResolver(new Resolver() {

			@Override
			public Call afterAuth() {
				Logger.debug("Play Authenticate afterAuth routing to the profile");
				// The user will be redirected to this page after authentication
				// if no original URL was saved
				return routes.Application.profile();
			}

			@Override
			public Call afterLogout() {
				Logger.debug("Play Authenticate afterLogout routing to the index");
				return routes.Application.index();
			}

			@Override
			public Call askLink() {
				Logger.debug("Play Authenticate askLink routing to the askLink");
				return routes.Account.askLink();
			}

			@Override
			public Call askMerge() {
				Logger.debug("Play Authenticate askMerge routing to the askMerge");
				return routes.Account.askMerge();
			}

			@Override
			public Call auth(final String provider) {
				Logger.debug("Play Authenticate auth routing to the authenticate");
				// You can provide your own authentication implementation,
				// however the default should be sufficient for most cases
				return com.feth.play.module.pa.controllers.routes.Authenticate
						.authenticate(provider);
			}

			@Override
			public Call login() {
				Logger.debug("Play Authenticate routing to the login");
				// Your login page
				return routes.Signup.login();
			}

			@Override
			public Call onException(final AuthException e) {
				Logger.warn("An Exception occurred on application startup", e);
				if (e instanceof AccessDeniedException) {
					return routes.Signup
							.oAuthDenied(((AccessDeniedException) e)
									.getProviderKey());
				}

				// more custom problem handling here...
				return super.onException(e);
			}
		});
//		dumpDataFromH2();
		initialData();
	}

	/**
	 * Initial data. This will run and load initial default users for testing.
	 */
	@Transactional
	private void initialData() {
		if (!Play.isProd()) {
			loadAllData();
			return;
		}
		loadRoleData();

		if (User.find.findRowCount() == 0) {
		loadSystemUserData();

			if (!Play.isProd()) {
				Logger.debug("We are in Dev");
//				loadUserData();
			} else {
				Logger.debug("We are in Prod");
			}
		}
			
		if (!Play.isProd()) {
			// loadEventData();
		}
		else {
			System.out.println("We are in Prod");
		}
	}
	
	
	private void loadAllData() {
		try {
			if (Play.application().isDev() && SecurityRole.find.findRowCount() == 0) {
				loadSqlFile("conf/loads/default/inmemory-mysql.sql");
			}
		} catch(Exception e) {
			if (Play.application().isDev()) {
				loadSqlFile("conf/loads/default/inmemory-mysql.sql");
			}
		}
	}
	/**
	 * Load event data.
	 */
	private void loadEventData() {
		if (Play.application().isDev()) {
			if (Event.find.all().isEmpty()) {
				loadSqlFile("conf/loads/default/scdb_event.sql");
			}
			if (Team.find.all().isEmpty()) {
				loadSqlFile("conf/loads/default/scdb_team.sql");
			}
			if (EventPages.find.all().isEmpty()) {
				loadSqlFile("conf/loads/default/scdb_event_pages.sql");
			}
			if (Pfp.find.all().isEmpty()) {
				loadSqlFile("conf/loads/default/scdb_pfp.sql");
			}
			if (Donation.find.all().isEmpty()) {
				loadSqlFile("conf/loads/default/scdb_donation.sql");
			}

		}
	}

	/**
	 * Load role data.
	 */
	private void loadRoleData() {
		if (SecurityRole.find.findRowCount() == 0) {
			for (final String name : SecurityRole.getSecurityRoleValues()) {
				final SecurityRole role = new SecurityRole();
				role.roleName = name;
				role.save();
			}
		}
		if (UserPermission.find.findRowCount() == 0) {
			for (final String name : UserPermission.getUserPermissionValues()) {
				final UserPermission permission = new UserPermission();
				permission.value = name;
				permission.save();
			}
		}
	}

	/**
	 * Load sql file.
	 * 
	 * @param name
	 *            the name
	 */
	private void loadSqlFile(String name) {
		Logger.debug("Loading the sql file {}", name);
		final Transaction t = Ebean.beginTransaction();
		// Reading the evolution file
		String evolutionContent;
		try {
			evolutionContent = FileUtils.readFileToString(new File(name));
			Ebean.execute(Ebean.createSqlUpdate(evolutionContent));
			Ebean.commitTransaction();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finally {
			Ebean.endTransaction();
		}
	}

	/**
	 * Load user data.
	 */
	private void loadSystemUserData() {
//		if (User.find.findRowCount() == 0) {
			Logger.debug("Creating {} with the role of {}",
					"jay@scholasticchallenge.com", SecurityRole.ROOT_ADMIN);
			final RootEmailAuthUser authUser1 = new RootEmailAuthUser(
					"SCp@ssw0rd!", "jay@scholasticchallenge.com", "Jay", "Blanton");
			User.create(authUser1);
			final User user1 = User.findByAuthUserIdentity(authUser1);
			User.verify(user1);
			user1.changePassword(authUser1, true);
			
			Logger.debug("Creating {} with the role of {}",
					"scott@scholasticchallenge.com", SecurityRole.ROOT_ADMIN);
			final RootEmailAuthUser authUser2 = new RootEmailAuthUser(
					"SCp@ssw0rd!", "scott@scholasticchallenge.com", "Scott", "Ciampi");
			User.create(authUser2);
			final User user2 = User.findByAuthUserIdentity(authUser2);
			User.verify(user2);
			user2.changePassword(authUser2, true);
//		}
	}

	/**
	 * Load user data.
	 */
	private void loadUserData() {
//		if (User.find.findRowCount() == 0) {
			Logger.debug("Load user data");
			Logger.debug("Creating {} with the role of {}",
					SecurityRole.EVENT_ADMIN, "jay.blanton@sbcglobal.net");
			final TaxidAuthUser authUser2 = new TaxidAuthUser("password",
					"jay.blanton@sbcglobal.net", 111222333L, "1111 Fork Avenue", "Help the Kids",
					"9169991111", "Folsom", "CA", "95630", "Jay", "Event",
					"Folsom", "9167551111", true, true);
			User.create(authUser2);
			final User user2 = User.findByAuthUserIdentity(authUser2);
			// user2.addRoles(SecurityRole.EVENT_ADMIN);
			User.verify(user2);
			user2.changePassword(authUser2, true);
			// Logger.debug("This is the user - " +
			// ToStringBuilder.reflectionToString(user2));

			Logger.debug("Creating {} with the role of {}",
					SecurityRole.PFP_ADMIN, "theblantons@sbcglobal.net");
			final EmailAuthUser authUser3 = new EmailAuthUser("password",
					"theblantons@sbcglobal.net", "PFP", "Bmail");
			User.create(authUser3);
			final User user3 = User.findByAuthUserIdentity(authUser3);
			// user3.addRoles(SecurityRole.PFP_ADMIN);
			User.verify(user3);
			user3.changePassword(authUser3, true);
			// Logger.debug("This is the user - " +
			// ToStringBuilder.reflectionToString(user3));

//			Logger.debug("Creating {} with the role of {}",
//					SecurityRole.PFP_ADMIN, "jntblanton@gmail.com");
//			final EmailAuthUser authUser4 = new EmailAuthUser("password",
//					"jntblanton@gmail.com", "PFP", "Gmail");
//			User.create(authUser4);
//			final User user4 = User.findByAuthUserIdentity(authUser4);
//			// user4.addRoles(SecurityRole.PFP_ADMIN);
//			User.verify(user4);
//			user4.changePassword(authUser4, true);
//			// Logger.debug("This is the user - " +
//			// ToStringBuilder.reflectionToString(user4));
//		}
	}
	private void dumpDataFromH2() {
		try {
			Class.forName("org.h2.Driver");
			DriverManager.registerDriver(new org.h2.Driver());
			Script.execute("jdbc:h2:mem:play;MODE=MYSQL", "", "", "inmemory-mysql.sql");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
//	@Override
//	public void onStart(Application application) {
//		super.onStart(application); 
//		    schedule(); 
//		}
//
//		private void schedule() {
//		try {
//		    CronExpression e = new CronExpression("0 00 10 ? * *");
//		    Date nextValidTimeAfter = e.getNextValidTimeAfter(new Date());
//		    FiniteDuration d = Duration.create(
//		        nextValidTimeAfter.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
//
//		    Logger.debug("Scheduling to run at "+nextValidTimeAfter);
//
//		    Akka.system().scheduler().scheduleOnce(d, new Runnable() {
//
//		    @Override
//		    public void run() {
//		        Logger.debug("Ruuning scheduler");
//		        //Do your tasks here
//
//		        schedule(); //Schedule for next time
//
//		    }
//		    }, Akka.system().dispatcher());
//		} catch (Exception e) {
//		    Logger.error("", e);
//
//		}
//		}
}