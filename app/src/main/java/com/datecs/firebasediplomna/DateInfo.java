package com.datecs.firebasediplomna;

public class DateInfo {
    private String temperature;
    private String inputVoltage;
    private String outputVoltage;

    public DateInfo(){

    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getInputVoltage() {
        return inputVoltage;
    }

    public void setInputVoltage(String inputVoltage) {
        this.inputVoltage = inputVoltage;
    }

    public String getOutputVoltage() {
        return outputVoltage;
    }

    public void setOutputVoltage(String outputVoltage) {
        this.outputVoltage = outputVoltage;
    }
}
