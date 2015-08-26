package models.security;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang.builder.ToStringBuilder;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class MergedAccount extends Model {

	public static final Finder<Long, MergedAccount> find = new Finder<Long, MergedAccount>(
			Long.class, MergedAccount.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Required
	public Long authUserId;

	@Required
	public String email;

	@Id
	public Long id;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static MergedAccount create(final User authUser) {
		Logger.debug("Creating a merged account - {}", authUser.email);
		final MergedAccount ret = new MergedAccount();
		ret.authUserId = authUser.id;
		ret.email = authUser.email;
		return ret;
	}
}