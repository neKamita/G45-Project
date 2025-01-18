package uz.pdp.exception;

public class SellerAlreadyApprovedException extends RuntimeException {
    public SellerAlreadyApprovedException(String message) {
        super(message);
    }
}
