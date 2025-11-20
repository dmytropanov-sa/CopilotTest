package com.example.forgetpass.services;

import java.util.List;

public class ReCaptchaResponse {
    private boolean success;
    private double score;
    private String action;
    private List<String> errorCodes;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public List<String> getErrorCodes() { return errorCodes; }
    public void setErrorCodes(List<String> errorCodes) { this.errorCodes = errorCodes; }
}
