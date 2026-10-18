package com.trainingcenter.services.dashboard;

import com.trainingcenter.dto.DashboardDTO;

/** Provides the complete read-only dashboard view model. */
public interface DashboardService {

	DashboardDTO getDashboard();
}
