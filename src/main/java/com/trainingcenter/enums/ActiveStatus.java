package com.trainingcenter.enums;

import java.util.ArrayList;
import java.util.List;

public enum ActiveStatus {
	ACTIVE("Active"), INACTIVE("Inactive");

	private final String label;

	ActiveStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static ActiveStatus formLabel(String label) {
		for (ActiveStatus a : ActiveStatus.values()) {
			if (a.getLabel().equalsIgnoreCase(label)) {
				return a;
			}
		}
		throw new IllegalArgumentException("No constant with label" + label);
	}

	public static List<String> getAll() {
		List<String> result = new ArrayList<String>();
		for (ActiveStatus s : values()) {
			result.add(s.label);
		}
		return result;
	}

}
