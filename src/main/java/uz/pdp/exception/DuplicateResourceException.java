package uz.pdp.exception;

import jakarta.validation.constraints.NotBlank;

public class DuplicateResourceException extends Throwable {
    public DuplicateResourceException(@NotBlank(message = "Category name cannot be empty") String s) {
        super(s);
    }
}
