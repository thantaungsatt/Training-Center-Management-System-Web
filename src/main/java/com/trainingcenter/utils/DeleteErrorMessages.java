package com.trainingcenter.utils;

import org.springframework.dao.DataIntegrityViolationException;

public final class DeleteErrorMessages {
	private DeleteErrorMessages() { }

	public static String forDeleteFailure(Exception exception, String recordName, String relatedRecords) {
		if (isReferenceConstraintFailure(exception)) {
			return "This " + recordName + " cannot be deleted because it is still linked to " + relatedRecords
					+ ". Remove or reassign those records first.";
		}
		String message = exception.getMessage();
		return message == null || message.isBlank()
				? "Unable to delete this " + recordName + ". Please refresh the page and try again."
				: message;
	}

	private static boolean isReferenceConstraintFailure(Throwable failure) {
		for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
			if (cause instanceof DataIntegrityViolationException) {
				return true;
			}
			String message = cause.getMessage();
			if (message != null) {
				String normalized = message.toLowerCase();
				if (normalized.contains("foreign key") || normalized.contains("constraint")
						|| normalized.contains("could not execute statement")) {
					return true;
				}
			}
		}
		return false;
	}
}
