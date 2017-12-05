package net.plantkelt.akp.domain;

public class AkpUserRoles {

	// Not really a right, used for backward-compatibility
	public static final String ROLE_USER = "USER";

	public static final String ROLE_EDIT_TAXON = "EDIT_TAXON";

	public static final String ROLE_ADMIN = "ADMIN";

	private static final String[] ALL_ROLES = { ROLE_USER, ROLE_EDIT_TAXON,
			ROLE_ADMIN };

	public static String[] allRoles() {
		return ALL_ROLES;
	}
}
