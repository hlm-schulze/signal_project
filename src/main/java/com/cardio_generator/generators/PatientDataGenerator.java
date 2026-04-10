package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/*
 * Interface for creating patient data
 * Used in the CardioGenerator to generate different types of patient data
 * All data generator classes must implement this interface
 */

public interface PatientDataGenerator {
    /*
     * Generates patient data for the specified patient ID

     * @param patientId  ID of the patient for who to generate data
     * @param outputStrategy  strategy to use for outputting the generated data
     */

    void generate(int patientId, OutputStrategy outputStrategy);
}
