package org.mephi.finance.manager.mapper;

import org.mapstruct.Mapper;
import org.mephi.finance.manager.domain.Statistics;
import org.mephi.finance.manager.model.StatisticsResponse;

@Mapper
public interface StatisticsMapper {
    StatisticsResponse toApiResponse(Statistics statistics);
}
