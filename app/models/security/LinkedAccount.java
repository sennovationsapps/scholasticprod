package models.security;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.ToStringBuilder;

import play.db.ebean.Model;

import com.feth.play.module.pa.user.AuthUser;

@Entity
public class LinkedAccount extends Model {

	public static final Finder<Long, LinkedAccount> find = new Finder<Long, LinkedAccount>(
			Long.class, LinkedAccount.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String providerKey;
	public String providerUserId;

	@ManyToOne
	public User user;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public void update(final AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

	public static LinkedAccount create(final AuthUser authUser) {
		final LinkedAccount ret = new LinkedAccount();
		ret.update(authUser);
		return ret;
	}

	public static LinkedAccount create(final LinkedAccount acc) {
		final LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;

		return ret;
	}

	public static LinkedAccount findByProviderKey(final User user, String key) {
		return find.where().eq("user", user).eq("providerKey", key)
				.findUnique();
	}
}