package org.mephi.finance.manager.rest.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mephi.finance.manager.api.StatisticsApi;
import org.mephi.finance.manager.domain.Statistics;
import org.mephi.finance.manager.mapper.StatisticsMapper;
import org.mephi.finance.manager.model.StatisticsResponse;
import org.mephi.finance.manager.service.CurrentUserService;
import org.mephi.finance.manager.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatisticsController implements StatisticsApi {

    private final StatisticsService statisticsService;
    private final StatisticsMapper statisticsMapper;
    private final CurrentUserService currentUserService;

    @Override
    public ResponseEntity<StatisticsResponse> getOverview() {
        log.info("Получение общей статистики");

        UUID userId = currentUserService.getCurrentUserId();
        Statistics statistics = statisticsService.getOverviewStatistics(userId);

        StatisticsResponse response = statisticsMapper.toApiResponse(statistics);
        return ResponseEntity.ok(response);
    }
}
