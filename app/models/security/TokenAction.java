package models.security;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.format.Formats;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.QueryIterator;
import com.avaje.ebean.annotation.EnumValue;

@Entity
public class TokenAction extends Model {

	public static final Finder<Long, TokenAction> find = new Finder<Long, TokenAction>(
			Long.class, TokenAction.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds Defaults to one week
	 */
	private final static long VERIFICATION_TIME = 7 * 24 * 3600;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date created;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date expires;

	@Id
	public Long id;

	@ManyToOne
	public User targetUser;

	@Column(unique = true)
	public String token;

	public Type type;

	public boolean isValid() {
		return this.expires.after(new Date());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static TokenAction create(final Type type, final String token,
			final User targetUser) {
		final TokenAction ua = new TokenAction();
		ua.targetUser = targetUser;
		ua.token = token;
		ua.type = type;
		final Date created = new Date();
		ua.created = created;
		ua.expires = new Date(created.getTime() + (VERIFICATION_TIME * 1000));
		ua.save();
		return ua;
	}

	public static void deleteByUser(final User u, final Type type) {
		final QueryIterator<TokenAction> iterator = find.where()
				.eq("targetUser.id", u.id).eq("type", type).findIterate();
		Ebean.delete(iterator);
		iterator.close();
	}

	public static TokenAction findByToken(final String token, final Type type) {
		return find.where().eq("token", token).eq("type", type).findUnique();
	}

	public enum Type {
		@EnumValue("EV")
		EMAIL_VERIFICATION,

		@EnumValue("PR")
		PASSWORD_RESET
	}
}
