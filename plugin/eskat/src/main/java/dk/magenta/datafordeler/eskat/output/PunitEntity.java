package dk.magenta.datafordeler.eskat.output;


public class PunitEntity {
    private String pNummer;
    private String name;
    private String land;

    public PunitEntity(String pNummer, String name, String land) {
        this.pNummer = pNummer;
        this.name = name;
        this.land = land;

    }

    public String getpNummer() {
        return pNummer;
    }

    public void setpNummer(String pNummer) {
        this.pNummer = pNummer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }
}
