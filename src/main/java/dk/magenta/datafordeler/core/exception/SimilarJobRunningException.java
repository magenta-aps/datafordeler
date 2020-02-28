package dk.magenta.datafordeler.core.exception;

public class SimilarJobRunningException extends DataFordelerException {

    public SimilarJobRunningException(String message) {
        super(message);
    }

    @Override
    public String getCode() {
        return "datafordeler.import.similar_job_running";
    }
}
