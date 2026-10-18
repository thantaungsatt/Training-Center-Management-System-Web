package com.trainingcenter.enums;

import java.util.ArrayList;
import java.util.List;

public enum PaymentStatus {
	PARTIAL("Partial"), PAID("Paid"), REFUNDED("Refunded"),
	WAIVED("Waived");

	private final String label;

	PaymentStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static PaymentStatus formLabel(String label) {
		for (PaymentStatus a : PaymentStatus.values()) {
			if (a.getLabel().equalsIgnoreCase(label)) {
				return a;
			}
		}
		throw new IllegalArgumentException("No constant with label" + label);
	}

	public static List<String> getAll() {
		List<String> result = new ArrayList<String>();
		for (PaymentStatus s : values()) {
			result.add(s.label);
		}
		return result;
	}
}
