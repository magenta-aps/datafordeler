package dk.magenta.datafordeler.cvr.service;

import dk.magenta.datafordeler.cvr.output.ParticipantPnrRecordOutputWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/cvr/participant_pnr/1/rest")
public class ParticipantPnrRecordService extends ParticipantRecordService {

    @Autowired
    private ParticipantPnrRecordOutputWrapper participantPnrRecordOutputWrapper;

    @PostConstruct
    public void init() {
        super.init();
        this.setOutputWrapper(this.participantPnrRecordOutputWrapper);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getServiceName() {
        return "participant_pnr";
    }

    public static String getDomain() {
        return "https://data.gl/cvr/participant_pnr/1/rest/";
    }

}
