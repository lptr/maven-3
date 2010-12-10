package org.apache.maven.execution;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.logging.Logger;

public class ExecutionTimeListener extends AbstractExecutionListener {

    private final Logger logger;
    
    private ConcurrentMap<MojoExecution, Long> mojoExecutionStartTimes = new ConcurrentHashMap<MojoExecution, Long>();
    
    public ExecutionTimeListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void mojoStarted(ExecutionEvent event) {
        MojoExecution execution = event.getMojoExecution();
        Long previousExecution = mojoExecutionStartTimes.putIfAbsent(execution, System.currentTimeMillis());
        if (logger.isWarnEnabled()) {
            logger.warn("Execution happens twice at the same time: " + previousExecution);
        }
    }

    @Override
    public void mojoSucceeded(ExecutionEvent event) {
        mojoFinished(event);
    }
    
    @Override
    public void mojoFailed(ExecutionEvent event) {
        mojoFinished(event);
    }
    
    private void mojoFinished(ExecutionEvent event) {
        MojoExecution execution = event.getMojoExecution();
        Long startTime = mojoExecutionStartTimes.remove(execution);
        if (startTime == null && logger.isWarnEnabled()) {
            logger.warn("Execution finished without having started: " + execution);
        }
        else {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("=== " + executionTime + " ms for " + execution);
        }
    }
}
