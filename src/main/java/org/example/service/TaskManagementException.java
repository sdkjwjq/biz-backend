package org.example.service;

/** 管理操作的可预期业务错误。 */
public class TaskManagementException extends RuntimeException {
    private final int code;
    public TaskManagementException(int code, String message) { super(message); this.code = code; }
    public int getCode() { return code; }
}
