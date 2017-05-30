package dk.magenta.datafordeler.plugindemo.configuration;

import dk.magenta.datafordeler.core.configuration.Configuration;
import dk.magenta.datafordeler.plugindemo.DemoPlugin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by lars on 06-04-17.
 */
@Entity
@Table(name="demo_config")
public class DemoConfiguration implements Configuration {

    @Id
    @Column(name = "id")
    private final String plugin = DemoPlugin.class.getName();

    @Column
    private String pullCronSchedule = "0 0 0 * * ?";

    public String getPullCronSchedule() {
        return this.pullCronSchedule;
    }

}
