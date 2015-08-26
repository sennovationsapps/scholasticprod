package plugins;

import models.security.User;
import play.Application;
import play.Logger;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class AuthenticationServicePlugin extends UserServicePlugin {

	public AuthenticationServicePlugin(final Application app) {
		super(app);
	}

	@Override
	public Object getLocalIdentity(final AuthUserIdentity identity) {
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get
		// deactivated/deleted
		final User u = User.findByAuthUserIdentity(identity);
		if (u != null) {
			return u.id;
		} else {
			return null;
		}
	}

	@Override
	public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
		Logger.debug(
				"We are now in the Authentication...Plugin.link oldUser={} and newUser{}",
				oldUser.getProvider(), newUser.getProvider());
		User.addLinkedAccount(oldUser, newUser);
		return newUser;
	}

	@Override
	public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
		Logger.debug(
				"We are now in the Authentication...Plugin.merge oldUser={} and newUser{}",
				oldUser.getProvider(), newUser.getProvider());
		if (!oldUser.equals(newUser)) {
			// Remove if not necessary
			User.merge(oldUser, newUser);
		}
		return oldUser;
	}

	@Override
	public Object save(final AuthUser authUser) {
		final boolean isLinked = User.existsByAuthUserIdentity(authUser);
		if (!isLinked) {
			Logger.debug("AuthenticationServicePlugin save user - ");
			return User.create(authUser).id;
		} else {
			// we have this user already, so return null
			return null;
		}
	}

	@Override
	public AuthUser update(final AuthUser knownUser) {
		// User logged in again, bump last login date
		User.setLastLoginDate(knownUser);
		return knownUser;
	}

}
