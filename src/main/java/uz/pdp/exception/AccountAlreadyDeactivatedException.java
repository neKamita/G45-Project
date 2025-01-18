package uz.pdp.exception;

public class AccountAlreadyDeactivatedException extends RuntimeException {
    public AccountAlreadyDeactivatedException(String message) {
        super(message);
    }
}
