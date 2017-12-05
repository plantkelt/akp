package net.plantkelt.akp.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AkpUserRoles {

	// Not really a right, used for backward-compatibility
	// TODO Remove this role
	public static final String ROLE_USER = "USER";
	public static final String ROLE_EDIT_TAXON = "EDIT_TAXON";
	public static final String ROLE_EDIT_VERNA_PREFIX = "EDIT_VERNA_";
	public static final String ROLE_EDIT_VERNA_ALL = "EDIT_VERNA_*";
	public static final String ROLE_ADMIN = "ADMIN";

	private static final List<String> ALL_ROLES;

	static {
		// Do not include ADMIN in the list
		ALL_ROLES = Arrays.asList(ROLE_USER, ROLE_EDIT_TAXON,
				ROLE_EDIT_VERNA_ALL);
	}

	public static List<String> allRoles() {
		return ALL_ROLES;
	}

	public static List<String> allRoles(List<String> langXids) {
		List<String> ret = new ArrayList<>(ALL_ROLES.size() + langXids.size());
		ret.addAll(ALL_ROLES);
		for (String langXid : langXids) {
			ret.add(ROLE_EDIT_VERNA_PREFIX + langXid);
		}
		return ret;
	}
}
