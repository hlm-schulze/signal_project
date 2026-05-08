package Patterns;

import com.Strategies.AlertStrategy;
import com.Strategies.HeartRateStrategy;
import org.junit.jupiter.api.Test;
import com.alerts.BaseAlert;
import static org.junit.jupiter.api.Assertions.*;

public class StrategyPatternTest {

    @Test
    void testHeartRateStrategy() {
        AlertStrategy strategy = new HeartRateStrategy();

        assertTrue(strategy.checkAlert(140));
        assertFalse(strategy.checkAlert(75));
    }
}