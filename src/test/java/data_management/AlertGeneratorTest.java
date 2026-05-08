package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.alerts.AlertManager;
import com.data_management.DataStorage;
import com.data_management.Patient;

class AlertGeneratorTest {

    private DataStorage storage;
    private AlertManager alertManager;
    private AlertGenerator alertGenerator;

    private static final long NOW = System.currentTimeMillis();
    private static final long T1 = NOW - 50_000L;
    private static final long T2 = NOW - 40_000L;
    private static final long T3 = NOW - 30_000L;

    @BeforeEach
    void setUp() {
        storage = new DataStorage();
        alertManager = new AlertManager();
        alertGenerator = new AlertGenerator(storage, alertManager);
    }

    private Alert findAlert(String conditionSubstring) {
        return alertManager.getActiveAlerts().stream()
                .filter(a -> a.getCondition().contains(conditionSubstring))
                .findFirst()
                .orElse(null);
    }

    @Test
    void testSystolicHigh_alertTriggered() {
        storage.addPatientData(1, 185.0, "SystolicPressure", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("high Systolic"));
        assertEquals("1", findAlert("high Systolic").getPatientId());
    }

    @Test
    void testSystolicLow_alertTriggered() {
        storage.addPatientData(1, 85.0, "SystolicPressure", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("low Systolic"));
    }

    @Test
    void testSystolicNormal_noAlert() {
        storage.addPatientData(1, 120.0, "SystolicPressure", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertEquals(0, alertManager.getActiveAlertCount());
    }

    @Test
    void testDiastolicHigh_alertTriggered() {
        storage.addPatientData(1, 125.0, "DiastolicPressure", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("high Diastolic"));
    }

    @Test
    void testDiastolicLow_alertTriggered() {
        storage.addPatientData(1, 55.0, "DiastolicPressure", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("low Diastolic"));
    }

    @Test
    void testSystolicIncreasingTrend_alertTriggered() {
        storage.addPatientData(1, 110.0, "SystolicPressure", T1);
        storage.addPatientData(1, 125.0, "SystolicPressure", T2);
        storage.addPatientData(1, 140.0, "SystolicPressure", T3);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("increasing trend"));
    }

    @Test
    void testSystolicDecreasingTrend_alertTriggered() {
        storage.addPatientData(1, 150.0, "SystolicPressure", T1);
        storage.addPatientData(1, 135.0, "SystolicPressure", T2);
        storage.addPatientData(1, 120.0, "SystolicPressure", T3);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("decreasing trend"));
    }

    @Test
    void testSystolicSmallChanges_noTrendAlert() {
        //changes of only 5 mmHg should not trigger a trend alert
        storage.addPatientData(1, 110.0, "SystolicPressure", T1);
        storage.addPatientData(1, 115.0, "SystolicPressure", T2);
        storage.addPatientData(1, 120.0, "SystolicPressure", T3);
        alertGenerator.evaluateData(new Patient(1));

        assertNull(findAlert("trend"));
    }

    @Test
    void testSystolicMixedDirection_noTrendAlert() {
        storage.addPatientData(1, 110.0, "SystolicPressure", T1);
        storage.addPatientData(1, 125.0, "SystolicPressure", T2);
        storage.addPatientData(1, 110.0, "SystolicPressure", T3);
        alertGenerator.evaluateData(new Patient(1));

        assertNull(findAlert("trend"));
    }

    @Test
    void testTrendRequiresThreeReadings_noAlert() {
        storage.addPatientData(1, 110.0, "SystolicPressure", T1);
        storage.addPatientData(1, 125.0, "SystolicPressure", T2);
        alertGenerator.evaluateData(new Patient(1));

        assertNull(findAlert("trend"));
    }

    @Test
    void testLowSaturation_alertTriggered() {
        storage.addPatientData(1, 90.0, "Saturation", T1);
        alertGenerator.evaluateData(new Patient(1));

        //search for "Saturation" to be flexible with exact wording in AlertGenerator
        assertNotNull(findAlert("Saturation"));
    }

    @Test
    void testSaturationAtThreshold_noAlert() {
        //exactly 92% should not trigger (condition is strictly less than)
        storage.addPatientData(1, 92.0, "Saturation", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertEquals(0, alertManager.getActiveAlertCount());
    }

    @Test
    void testNormalSaturation_noAlert() {
        storage.addPatientData(1, 98.0, "Saturation", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertEquals(0, alertManager.getActiveAlertCount());
    }

    @Test
    @org.junit.jupiter.api.Disabled("Enable once rapid saturation drop is implemented in AlertGenerator")
    void testRapidSaturationDrop_alertTriggered() {
        //drop of 6% within 5 minutes, well within the 10-minute window
        long base = NOW - 55_000L;
        storage.addPatientData(1, 98.0, "Saturation", base);
        storage.addPatientData(1, 92.0, "Saturation", base + 300_000L);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("Rapid saturation drop"));
    }

    @Test
    @org.junit.jupiter.api.Disabled("Enable once rapid saturation drop is implemented in AlertGenerator")
    void testSmallSaturationDrop_noRapidDropAlert() {
        long base = NOW - 55_000L;
        storage.addPatientData(1, 97.0, "Saturation", base);
        storage.addPatientData(1, 94.0, "Saturation", base + 60_000L);
        alertGenerator.evaluateData(new Patient(1));

        assertNull(findAlert("Rapid saturation drop"));
    }

    @Test
    void testHypotensiveHypoxemia_alertTriggered() {
        storage.addPatientData(1, 85.0, "SystolicPressure", T1);
        storage.addPatientData(1, 90.0, "Saturation", T2);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("Hypotensive Hypoxemia"));
    }

    @Test
    void testLowBpNormalSat_noCombinedAlert() {
        storage.addPatientData(1, 85.0, "SystolicPressure", T1);
        storage.addPatientData(1, 97.0, "Saturation", T2);
        alertGenerator.evaluateData(new Patient(1));

        assertNull(findAlert("Hypotensive Hypoxemia"));
    }

    @Test
    void testNormalBpLowSat_noCombinedAlert() {
        storage.addPatientData(1, 120.0, "SystolicPressure", T1);
        storage.addPatientData(1, 90.0, "Saturation", T2);
        alertGenerator.evaluateData(new Patient(1));

        assertNull(findAlert("Hypotensive Hypoxemia"));
    }

    @Test
    void testEcgAbnormalPeak_alertTriggered() {
        long t = T1;
        for (int i = 0; i < 20; i++) {
            storage.addPatientData(1, 0.3, "ECG", t + i * 1000L);
        }
        //large spike far above the window average
        storage.addPatientData(1, 5.0, "ECG", T2);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("Abnormal ECG peak"));
    }

    @Test
    void testEcgNormalValues_noAlert() {
        long t = T1;
        for (int i = 0; i < 21; i++) {
            storage.addPatientData(1, 0.3 + (i % 2) * 0.05, "ECG", t + i * 1000L);
        }
        alertGenerator.evaluateData(new Patient(1));

        assertNull(findAlert("Abnormal ECG peak"));
    }

    @Test
    void testManualTriggeredAlert_alertTriggered() {
        //non-zero value represents a triggered state
        storage.addPatientData(1, 1.0, "Alert", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertNotNull(findAlert("Manual alert triggered"));
    }

    @Test
    void testManualResolvedAlert_noAlert() {
        //zero value represents resolved state
        storage.addPatientData(1, 0.0, "Alert", T1);
        alertGenerator.evaluateData(new Patient(1));

        assertNull(findAlert("Manual alert triggered"));
    }

    @Test
    void testAlertsArePatientSpecific() {
        storage.addPatientData(1, 120.0, "SystolicPressure", T1);
        storage.addPatientData(2, 185.0, "SystolicPressure", T1);

        alertGenerator.evaluateData(new Patient(1));
        alertGenerator.evaluateData(new Patient(2));

        long patient1Alerts = alertManager.getActiveAlerts().stream()
                .filter(a -> a.getPatientId().equals("1")).count();
        long patient2Alerts = alertManager.getActiveAlerts().stream()
                .filter(a -> a.getPatientId().equals("2")).count();

        assertEquals(0, patient1Alerts);
        assertTrue(patient2Alerts > 0);
    }
}