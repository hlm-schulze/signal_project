package Patterns;

import com.alerts.Alert;
import com.alerts.AlertFactory;
import com.alerts.BloodPressureAlert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FactoryPatternTest {

    @Test
    void testBloodPressureFactory() {
        AlertFactory factory = new BloodPressureAlert();
        Alert alert = factory.createAlert("1", "High", 1000L);

        assertEquals("1", alert.getPatientId());
        assertTrue(alert.getMessage().contains("Blood Pressure"));
    }
}