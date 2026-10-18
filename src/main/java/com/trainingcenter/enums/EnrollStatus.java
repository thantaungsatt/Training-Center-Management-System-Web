package com.trainingcenter.enums;

import java.util.ArrayList;
import java.util.List;

public enum EnrollStatus {
	ACTIVE("Active"), INACTIVE("Inactive"), COMPLETED("Completed");

	private final String label;

	EnrollStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static EnrollStatus formLabel(String label) {
		for (EnrollStatus a : EnrollStatus.values()) {
			if (a.getLabel().equalsIgnoreCase(label)) {
				return a;
			}
		}
		throw new IllegalArgumentException("No constant with label" + label);
	}

	public static List<String> getAll() {
		List<String> result = new ArrayList<String>();
		for (EnrollStatus s : values()) {
			result.add(s.label);
		}
		return result;
	}
}
