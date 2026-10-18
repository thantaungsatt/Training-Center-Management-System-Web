package com.trainingcenter.enums;

import java.util.ArrayList;
import java.util.List;

public enum StudentStatus {
	ACTIVE("Active"), INACTIVE("Inactive"), CERTIFICATED("Certificated"), ABSENCE("Leave of Absence"),
	SUSPENDED("Suspended");

	private final String label;

	StudentStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static StudentStatus formLabel(String label) {
		for (StudentStatus a : StudentStatus.values()) {
			if (a.getLabel().equalsIgnoreCase(label)) {
				return a;
			}
		}
		throw new IllegalArgumentException("No constant with label" + label);
	}

	public static List<String> getAll() {
		List<String> result = new ArrayList<String>();
		for (StudentStatus s : values()) {
			result.add(s.label);
		}
		return result;
	}
}
