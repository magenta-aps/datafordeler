package dk.magenta.datafordeler.ger.configuration;

import dk.magenta.datafordeler.core.configuration.Configuration;
import dk.magenta.datafordeler.ger.GerPlugin;
import dk.magenta.datafordeler.ger.data.company.CompanyEntity;
import dk.magenta.datafordeler.ger.data.responsible.ResponsibleEntity;
import dk.magenta.datafordeler.ger.data.unit.UnitEntity;

import javax.persistence.*;

@Entity
@Table(name = "ger_config")
public class GerConfiguration implements Configuration {

    public enum RegisterType {
        DISABLED(0),
        LOCAL_FILE(1);

        private final int value;

        RegisterType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    @Id
    @Column(name = "id")
    private final String plugin = GerPlugin.class.getName();


    @Column
    private String pullCronSchedule = null;

    public String getPullCronSchedule() {
        return this.pullCronSchedule;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType companyRegisterType = RegisterType.LOCAL_FILE;

    public RegisterType getCompanyRegisterType() {
        return this.companyRegisterType;
    }

    public void setCompanyRegisterType(RegisterType companyRegisterType) {
        this.companyRegisterType = companyRegisterType;
    }

/*
    @Column(length = 64)
    private String companySheetName = "JE";

    public String getCompanySheetName() {
        return this.companySheetName;
    }
*/

    @Column(length = 1024)
    private String companyRegisterURL = "";

    public String getCompanyRegisterURL() {
        return this.companyRegisterURL;
    }


    public void setCompanyRegisterURL(String companyRegisterURL) {
        this.companyRegisterURL = companyRegisterURL;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType unitRegisterType = RegisterType.LOCAL_FILE;

    public RegisterType getUnitRegisterType() {
        return this.unitRegisterType;
    }


    public void setUnitRegisterType(RegisterType unitRegisterType) {
        this.unitRegisterType = unitRegisterType;
    }
/*
    @Column(length = 64)
    private String unitSheetName = "PE";

    public String getUnitSheetName() {
        return this.unitSheetName;
    }
*/

    @Column(length = 1024)
    private String unitRegisterURL = "";

    public String getUnitRegisterURL() {
        return this.unitRegisterURL;
    }


    public void setUnitRegisterURL(String unitRegisterURL) {
        this.unitRegisterURL = unitRegisterURL;
    }


    @Column
    @Enumerated(EnumType.ORDINAL)
    private RegisterType responsibleRegisterType = RegisterType.LOCAL_FILE;

    public RegisterType getResponsibleRegisterType() {
        return this.responsibleRegisterType;
    }

    public void setResponsibleRegisterType(RegisterType responsibleRegisterType) {
        this.responsibleRegisterType = responsibleRegisterType;
    }

    /*
        @Column(length = 64)
        private String responsibleSheetName = "";

        public String getResponsibleSheetName() {
            return this.responsibleSheetName;
        }
    */
    @Column(length = 1024)
    private String responsibleRegisterURL = "";

    public String getResponsibleRegisterURL() {
        return this.responsibleRegisterURL;
    }

    public void setResponsibleRegisterURL(String responsibleRegisterURL) {
        this.responsibleRegisterURL = responsibleRegisterURL;
    }


    public RegisterType getRegisterType(String schema) {
        switch (schema) {
            case CompanyEntity.schema:
                return this.companyRegisterType;
            case UnitEntity.schema:
                return this.unitRegisterType;
            case ResponsibleEntity.schema:
                return this.responsibleRegisterType;
        }
        return null;
    }

    public String getURL(String schema) {
        switch (schema) {
            case CompanyEntity.schema:
                return this.companyRegisterURL;
            case UnitEntity.schema:
                return this.unitRegisterURL;
            case ResponsibleEntity.schema:
                return this.responsibleRegisterURL;
        }
        return null;
    }
}
