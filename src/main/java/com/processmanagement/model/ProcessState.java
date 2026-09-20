package com.processmanagement.model;

/**
 * The states a simulated process can be in, following the classic
 * operating-system process lifecycle.
 */
public enum ProcessState {
    NEW,
    READY,
    RUNNING,
    WAITING,
    SWAPPED_OUT,
    TERMINATED
}