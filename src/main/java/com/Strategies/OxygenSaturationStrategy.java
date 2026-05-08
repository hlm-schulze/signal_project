package com.Strategies;

public class OxygenSaturationStrategy implements AlertStrategy {

    @Override
    public boolean checkAlert(double value) {
        return value < 92;
    }
}