package dk.magenta.dafosts.library.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InactiveAccessAccountException extends Exception {
    public InactiveAccessAccountException(String message) {
        super(message);
    }
}
