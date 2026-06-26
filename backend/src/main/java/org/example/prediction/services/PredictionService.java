package org.example.prediction.services;

import org.example.prediction.dto.form.AddPredictionDto;

public interface PredictionService {
    void makePrediction(Long id, AddPredictionDto form);
}