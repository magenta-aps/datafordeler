package dk.magenta.datafordeler.eskat.output;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PunitEntity {
    private String pNummer;
    private String name;
    private String land;

    public PunitEntity(String pNummer, String name, String land) {
        this.pNummer = pNummer;
        this.name = name;
        this.land = land;

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("pnummer")
    public String getpNummer() {
        return pNummer;
    }

    public void setpNummer(String pNummer) {
        this.pNummer = pNummer;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("firmaNavn")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("landekode")
    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }
}
