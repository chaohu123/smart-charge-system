package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface EvaluationService {
    Result<?> createEvaluation(String token, com.smartcharge.entity.Evaluation evaluation);
    Result<?> getStationEvaluations(Long stationId, Integer current, Integer size);
    Result<?> uploadEvaluationImage(String token, org.springframework.web.multipart.MultipartFile file);
    Result<?> updateEvaluation(String token, Long id, com.smartcharge.entity.Evaluation evaluation);
    Result<?> deleteEvaluation(String token, Long id);
    Result<?> likeEvaluation(String token, Long evaluationId);
    Result<?> unlikeEvaluation(String token, Long evaluationId);
    Result<?> checkLikeStatus(String token, Long evaluationId);
    Result<?> getEvaluationLikeCount(Long evaluationId);
}

