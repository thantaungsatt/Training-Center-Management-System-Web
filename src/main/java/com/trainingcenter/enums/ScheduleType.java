package com.trainingcenter.enums;

import java.util.ArrayList;
import java.util.List;

public enum ScheduleType {
	WEEKDAY("Weekday"), WEEKEND("Weekend");

	private final String label;

	ScheduleType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static ScheduleType formLabel(String label) {
		for (ScheduleType type : ScheduleType.values()) {
			if (type.getLabel().equalsIgnoreCase(label)) {
				return type;
			}
		}
		throw new IllegalArgumentException("No constant with label" + label);
	}

	public static List<String> getAll() {
		List<String> result = new ArrayList<String>();
		for (ScheduleType s : values()) {
			result.add(s.label);
		}
		return result;
	}

}
