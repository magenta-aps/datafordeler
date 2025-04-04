package dk.magenta.datafordeler.core.fapi;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import dk.magenta.datafordeler.core.database.Effect;
import dk.magenta.datafordeler.core.database.Entity;
import dk.magenta.datafordeler.core.database.Registration;
import dk.magenta.datafordeler.core.exception.HttpNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Service container to be subclassed for each Entity class, serving REST
 */
@RequestMapping("/fapi_service_with_no_requestmapping")
public abstract class FapiService<E extends Entity, Q extends Query> extends FapiBaseService<E, Q> {

    @Autowired
    private CsvMapper csvMapper;


    protected void sendAsCSV(Stream<E> entities, HttpServletRequest request,
                             HttpServletResponse response)
            throws IOException, HttpNotFoundException {
        List<MediaType> acceptedTypes = MediaType.parseMediaTypes
                (request.getHeader("Accept"));

        Iterator<Map<String, Object>> dataIter =
                entities.map(Entity::getRegistrations).flatMap(
                        List::stream
                ).flatMap(
                        r -> ((Registration) r).getEffects().stream()
                ).map(
                        obj -> {
                            Effect e = (Effect) obj;
                            Registration r = e.getRegistration();
                            Map<String, Object> data = e.getData();

                            data.put("effectFrom",
                                    e.getEffectFrom());
                            data.put("effectTo",
                                    e.getEffectTo());
                            data.put("registrationFrom",
                                    r.getRegistrationFrom());
                            data.put("registrationTo",
                                    r.getRegistrationFrom());
                            data.put("sequenceNumber",
                                    r.getSequenceNumber());
                            data.put("uuid", r.getEntity().getUUID());

                            return data;
                        }
                ).iterator();

        if (!dataIter.hasNext()) {
            response.sendError(HttpStatus.NO_CONTENT.value());
            return;
        }

        CsvSchema.Builder builder =
                new CsvSchema.Builder();

        Map<String, Object> first = dataIter.next();
        ArrayList<String> keys =
                new ArrayList<>(first.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            builder.addColumn(new CsvSchema.Column(
                    i, keys.get(i),
                    CsvSchema.ColumnType.NUMBER_OR_STRING
            ));
        }

        CsvSchema schema =
                builder.build().withHeader();

        if (acceptedTypes.contains(new MediaType("text", "tsv"))) {
            schema = schema.withColumnSeparator('\t');
            response.setContentType("text/tsv");
        } else {
            response.setContentType("text/csv");
        }

        SequenceWriter writer =
                csvMapper.writer(schema).writeValues(response.getOutputStream());

        writer.write(first);

        while (dataIter.hasNext()) {
            writer.write(dataIter.next());
        }
    }

}
