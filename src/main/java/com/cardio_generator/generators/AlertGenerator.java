package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Implementation of PatientDataGenerator that simulates alerts for patients
 * Each patient has a boolean state indicating whether an alert is currently active (pressed) or not (resolved)
 * Models real-world alarm systems
 */
public class AlertGenerator implements PatientDataGenerator {

    //made private since only AlertGenerator uses it
    private static final Random randomGenerator = new Random(); 

    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Constructs new AlertGenerator with the specified number of patients
     * All patients start with no active alerts (all states initialized to false)
     * 
     * @param patientCount  number of patients for whom to generate alert data
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1]; //changed to camelCase (all the following instances as well): AlertStates -> alertStates
    }

    /**
     * Generates alert data for the specified patient
     * If an alert is currently active for the patient, there is a 90% chance that it will be resolved in the next generation cycle
     * If no alert is currently active, one may be triggered based on a Poisson process with a specified average rate (lambda)
     * 
     * @param patientId ID of the patient for whom to generate data
     * @param outputStrategy Strategy to use for outputting generated data
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                    //changed to camelCase (all the following instances as well): Lambda -> lambda
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
