package dk.magenta.datafordeler.core.event;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lars on 13-01-17.
 */
public class EventBody {

  @JsonProperty(value = "Hændelsesbesked")
  public EventMessage eventMessage;

}