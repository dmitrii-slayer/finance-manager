package org.mephi.finance.manager.exception;

public class ActionForbiddenException extends RuntimeException {
    public ActionForbiddenException(String message) {
        super(message);
    }
}
