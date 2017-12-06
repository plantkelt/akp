package net.plantkelt.akp.domain;

import java.util.Arrays;
import java.util.List;

public class AkpUserRoles {

	// Not really a right, used for backward-compatibility
	// TODO Remove this role
	public static final String ROLE_USER = "USER";
	public static final String ROLE_EDIT_TAXON = "EDIT_TAXON";
	public static final String ROLE_EDIT_VERNA = "EDIT_VERNA";
	public static final String ROLE_VIEW_PLANT_HIST = "VIEW_PLANT_HIST";
	public static final String ROLE_ADMIN = "ADMIN";

	private static final List<String> ALL_ROLES;

	static {
		// Do not include ADMIN in the list
		ALL_ROLES = Arrays.asList(ROLE_USER, ROLE_EDIT_TAXON, ROLE_EDIT_VERNA,
				ROLE_VIEW_PLANT_HIST);
	}

	public static List<String> allRoles() {
		return ALL_ROLES;
	}
}
