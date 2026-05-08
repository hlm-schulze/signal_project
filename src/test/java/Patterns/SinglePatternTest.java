package Patterns;

import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SinglePatternTest {

    @Test
    void testDataStorageSingleton() {
        DataStorage s1 = DataStorage.getInstance();
        DataStorage s2 = DataStorage.getInstance();

        assertSame(s1, s2);
    }

    @Test
    void testHealthDataSimulatorSingleton() {
        HealthDataSimulator h1 = HealthDataSimulator.getInstance();
        HealthDataSimulator h2 = HealthDataSimulator.getInstance();

        assertSame(h1, h2);
    }
}
