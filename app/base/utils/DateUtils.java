package base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import play.Logger;
import play.libs.F.Option;

public class DateUtils {
	
	private DateUtils() {}
	
	public static String formatExpDate(final Date t) {
		return new SimpleDateFormat("MMyy").format(t);
	}
	
	public static String formatReconcileDate(final Date t) {
		return new SimpleDateFormat("MM/dd/yyyy").format(t);
	}
	
	public static Option<Date> parseDate(final String s) {
		try {
			return Option.Some(org.apache.commons.lang.time.DateUtils.parseDate(s, new String[] {"MM/dd/yyyy"}));
		} catch (ParseException e) {
			Logger.warn("An error occurred parsing date based on format", e);
		}
		return Option.None();
	}
}
