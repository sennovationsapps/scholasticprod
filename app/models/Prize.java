package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.builder.ToStringBuilder;

import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Prize extends Model {

	public static final Finder<Long, Prize> find = new Finder<Long, Prize>(
			Long.class, Prize.class);

//	@Required
	public String description;

	@ManyToOne
	public Awards awards;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

//	@Required
//	public String imgUrl;
	
//	@Constraints.Required
	@Column(columnDefinition = "TEXT")
	public String info;

//	@Required
	public String name;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static Prize findById(Long id) {
		return find.byId(id);
	}
}