package com.Strategies;

public class HeartRateStrategy implements AlertStrategy {

    @Override
    public boolean checkAlert(double value) {
        return value > 120 || value < 50;
    }
}