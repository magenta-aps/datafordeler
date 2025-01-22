package dk.magenta.datafordeler.core.io;

import dk.magenta.datafordeler.core.Application;
import dk.magenta.datafordeler.core.exception.DataFordelerException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.util.UUID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Application.class)
public class ReceiptTest {

    private static final String errorcode = "receipttest";
    private static final String errormessage = "this is a test of the Receipt class";

    @Test
    public void testGetObjectID() {
        String objectId = UUID.randomUUID().toString();
        Receipt receipt = new Receipt(objectId, OffsetDateTime.now());
        Assert.assertEquals(objectId, receipt.getEventID());
    }

    @Test
    public void testGetStatus() {
        Receipt receipt = new Receipt(UUID.randomUUID().toString(), OffsetDateTime.now());
        Assert.assertEquals(Receipt.Status.ok, receipt.getStatus());
        receipt = new Receipt(UUID.randomUUID().toString(), OffsetDateTime.now(), this.generateDatafordelerException());
        Assert.assertEquals(Receipt.Status.failed, receipt.getStatus());
    }

    @Test
    public void testGetReceived() {
        OffsetDateTime now = OffsetDateTime.now();
        Receipt receipt = new Receipt(UUID.randomUUID().toString(), now);
        Assert.assertEquals(now, receipt.getReceived());

    }

    @Test
    public void testGetErrorCode() {
        Receipt receipt = new Receipt(UUID.randomUUID().toString(), OffsetDateTime.now(), this.generateDatafordelerException());
        Assert.assertEquals(errorcode, receipt.getErrorCode());
    }

    @Test
    public void testGetErrorMessage() {
        Receipt receipt = new Receipt(UUID.randomUUID().toString(), OffsetDateTime.now(), this.generateDatafordelerException());
        Assert.assertEquals(errormessage, receipt.getErrorMessage());
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
