package com.trainingcenter.enums;

import java.util.ArrayList;
import java.util.List;

public enum PaymentType {
	CASH("Cash"),
	KBZ("KBZ Pay"),
	AYA("AYA Pay"),
	CB("CB Pay"),
	UAB("UAB Pay"),
	WAVE("Wave Pay"),
	MMQR("MMQR");
	
	private final String label;

	PaymentType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static PaymentType formLabel(String label) {
		for (PaymentType a : PaymentType.values()) {
			if (a.getLabel().equalsIgnoreCase(label)) {
				return a;
			}
		}
		throw new IllegalArgumentException("No constant with label" + label);
	}

	public static List<String> getAll() {
		List<String> result = new ArrayList<String>();
		for (PaymentType s : values()) {
			result.add(s.label);
		}
		return result;
	}
	
}
