package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.EvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/evaluation")
public class EvaluationController {
    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/create")
    public Result<?> createEvaluation(@RequestHeader("Authorization") String token,
                                       @RequestBody com.smartcharge.entity.Evaluation evaluation) {
        return evaluationService.createEvaluation(token, evaluation);
    }

    @PostMapping("/upload")
    public Result<?> uploadEvaluationImage(@RequestHeader("Authorization") String token,
                                           @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return evaluationService.uploadEvaluationImage(token, file);
    }

    @GetMapping("/station/{stationId}")
    public Result<?> getStationEvaluations(@PathVariable Long stationId,
                                           @RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return evaluationService.getStationEvaluations(stationId, current, size);
    }

    @PutMapping("/{id}")
    public Result<?> updateEvaluation(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id,
                                      @RequestBody com.smartcharge.entity.Evaluation evaluation) {
        return evaluationService.updateEvaluation(token, id, evaluation);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteEvaluation(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id) {
        return evaluationService.deleteEvaluation(token, id);
    }

    @PostMapping("/{id}/like")
    public Result<?> likeEvaluation(@RequestHeader("Authorization") String token,
                                   @PathVariable Long id) {
        return evaluationService.likeEvaluation(token, id);
    }

    @PostMapping("/{id}/unlike")
    public Result<?> unlikeEvaluation(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id) {
        return evaluationService.unlikeEvaluation(token, id);
    }

    @GetMapping("/{id}/like/status")
    public Result<?> checkLikeStatus(@RequestHeader("Authorization") String token,
                                    @PathVariable Long id) {
        return evaluationService.checkLikeStatus(token, id);
    }

    @GetMapping("/{id}/like/count")
    public Result<?> getEvaluationLikeCount(@PathVariable Long id) {
        return evaluationService.getEvaluationLikeCount(id);
    }
}

