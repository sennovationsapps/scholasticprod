package providers.uniqueid;

import providers.email.TermsOfServiceIdentity;

public interface RepresentOrgIdentity extends TermsOfServiceIdentity {

	Boolean getAgreeToRepresent();

}