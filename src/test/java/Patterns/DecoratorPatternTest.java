package Patterns;

import com.alerts.Alert;
import com.alerts.BaseAlert;
import com.alerts.PriorityAlertDecorator;
import com.alerts.RepeatedAlertDecorator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DecoratorPatternTest {

    @Test
    void testPriorityDecorator() {
        Alert alert = new BaseAlert("1", "Critical", 1000L);
        alert = new PriorityAlertDecorator(alert);

        assertTrue(alert.getMessage().contains("HIGH PRIORITY"));
    }

    @Test
    void testRepeatedDecorator() {
        Alert alert = new BaseAlert("1", "Critical", 1000L);
        alert = new RepeatedAlertDecorator(alert);

        assertTrue(alert.getMessage().contains("REPEATED ALERT"));
    }
}