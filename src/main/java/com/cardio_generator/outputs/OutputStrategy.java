package com.cardio_generator.outputs;

/* 
* Interface for outputting patient data 
* Used in the CardioGenerator to output generated patient data
* All output strategy classes must implement this interface
* Implementing classes determine how the generated data is sent (e.g., to a file, over TCP, etc.)
*/

public interface OutputStrategy {
    /*
     * Outputs the generated patient data
     *
     * @param patientId  ID of the patient for who the data was generated
     * @param timestamp  time when the data was generated
     * @param label  label describing the type of data being output (e.g., "ECG", "Blood Pressure")
     * @param data  actual generated data to be outputted
     */
    void output(int patientId, long timestamp, String label, String data);
}
