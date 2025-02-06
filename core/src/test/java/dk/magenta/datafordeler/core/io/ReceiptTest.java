package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.UUID;


@ContextConfiguration(classes = Application.class)
public class ReceiptTest {

    private static final String errorcode = "receipttest";
    private static final String errormessage = "this is a test of the Receipt class";

    @Test
    public void testGetObjectID() {
        String objectId = UUID.randomUUID().toString();
        Receipt receipt = new Receipt(objectId, OffsetDateTime.now());
        Assertions.assertEquals(objectId, receipt.getEventID());
    }

    @Test
    public void testGetStatus() {
        Receipt receipt = new Receipt(UUID.randomUUID().toString(), OffsetDateTime.now());
        Assertions.assertEquals(Receipt.Status.ok, receipt.getStatus());
        receipt = new Receipt(UUID.randomUUID().toString(), OffsetDateTime.now(), this.generateDatafordelerException());
        Assertions.assertEquals(Receipt.Status.failed, receipt.getStatus());
    }

    @Test
    public void testGetReceived() {
        OffsetDateTime now = OffsetDateTime.now();
        Receipt receipt = new Receipt(UUID.randomUUID().toString(), now);
        Assertions.assertEquals(now, receipt.getReceived());

    }

    @Test
    public void testGetErrorCode() {
        Receipt receipt = new Receipt(UUID.randomUUID().toString(), OffsetDateTime.now(), this.generateDatafordelerException());
        Assertions.assertEquals(errorcode, receipt.getErrorCode());
    }

    @Test
    public void testGetErrorMessage() {
        Receipt receipt = new Receipt(UUID.randomUUID().toString(), OffsetDateTime.now(), this.generateDatafordelerException());
        Assertions.assertEquals(errormessage, receipt.getErrorMessage());
    }

    private DataFordelerException generateDatafordelerException() {
        return new DataFordelerException() {
            @Override
            public String getCode() {
                return errorcode;
            }

            @Override
            public String getMessage() {
                return errormessage;
            }
        };
    }
}
