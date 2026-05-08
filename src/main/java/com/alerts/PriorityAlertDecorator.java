package com.alerts;

public class PriorityAlertDecorator extends AlertDecorator {

    public PriorityAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    @Override
    public String getMessage() {
        return "[HIGH PRIORITY] " + decoratedAlert.getMessage();
    }
}