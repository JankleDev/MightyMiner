package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.utils.Clock;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.helper.Helper;

public abstract class AbstractFeature implements IFeature, Helper {
    protected boolean enabled = false;
    protected boolean forceEnable = false;
    protected boolean failed = false;
    protected boolean succeeded = false;
    protected Clock timer = new Clock(); // General Timer for Everything
    @Override
    public void setSuccessStatus(final boolean succeeded) {
        this.succeeded = succeeded;
        this.failed = !succeeded;
    }

    @Override
    public boolean hasSucceeded() {
        return !this.enabled && this.succeeded;
    }

    @Override
    public boolean hasFailed() {
        return !this.enabled && this.failed;
    }

    @Override
    public void log(final String message){
        String messageToLog = String.format("[%s] - %s", this.getFeatureName(), message);
        LogUtils.debugLog(messageToLog);
    }

    @Override
    public void error(final String errorMessage){
        String errorToLog = String.format("[%s] - %s", this.getFeatureName(), errorMessage);
        LogUtils.logError(errorToLog);
    }

    @Override
    public void note(final String noteMessage){
        String noteToLog = String.format("[%s] - %s", this.getFeatureName(), noteMessage);
        LogUtils.addNote(noteToLog);
    }
}
