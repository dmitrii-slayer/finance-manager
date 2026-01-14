package org.mephi.finance.manager.service;

import org.mephi.finance.manager.domain.Statistics;

import java.util.UUID;

public interface StatisticsService {

    Statistics getOverviewStatistics(UUID userId);
}
