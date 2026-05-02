package com.alerts;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsibilities:
 * - Receiving alerts from AlertGenerator
 * - Notifying medical staff (storing/printing for now)
 * - Maintaining list of active alerts
 * - Resolving alerts when conditions are cleared
 */
public class AlertManager {
    private List<Alert> activeAlerts;  

    /**
     * Creates new AlertManager (Constructor) with a so far empty array list
     */
    public AlertManager() {
        this.activeAlerts = new ArrayList<>();
    }

    /**
     * Checks if alert is valid 
     * If it is, it is added to the activeAlerts List and a message is printed out 
     * 
     * @param alert alert that is dispatched
     */
    public void dispatchAlert(Alert alert) {
        if (alert == null)
            return; 

        activeAlerts.add(alert); 
        notifyStaff(alert);  
    }

    /**
     * Simulates notifying staff 
     * In a reald world sceanrio a message would be send to paigers, etc.
     * 
     * @param alert alert that staff is notified about 
     */
    private void notifyStaff(Alert alert) {
        String patient = "Patient ID:" + alert.getPatientId(); 
        String condition = alert.getCondition(); 
        String ts = "Happened at:" + alert.getTimestamp();
        System.out.println("!!!ALERT!!!" + "\n" + patient + "\n" + condition + "\n" + ts);
    }

    /**
     * If an alert is resolved then it is removed from the activeAlerts list 
     * Needs to be called when issue that caused alert to be triggered is resolved 
     * 
     * @param alert alert that needs to be resolved
     * @return if the alert is found and removed then true is returned 
     */
    public boolean resolveAlert(Alert alert) {
        return activeAlerts.remove(alert);
    }

    /**
     * Acces all active alerts 
     * 
     * @return activeAlerts list
     */
    public List<Alert> getActiveAlerts() {
        return activeAlerts; 
    }

    /**
     * Number of still active Alerts 
     * 
     * @return #active alerts
     */
    public int getActiveAlertCount() {
        return activeAlerts.size(); 
    }




}
