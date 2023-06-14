package com.mycompany.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.mycompany.app.Proportion.defectProportionInc;

public class Version {
    private String name;
    private String extendedName; //for those releases which have the name like '4.0.0-something'
    private LocalDateTime date;
    private int verNum;
    private List<Tickets> fixedDefects;
    private Double propIncremental;
    private Double defectProp;

    public Version(String name, LocalDateTime date, int vn) {

        String[] tkns = name.split("-");
        if(tkns.length > 1){
            this.name = tkns[0];
            this.extendedName = name;
        }else {
            this.name = name;
            this.extendedName = null;
        }
        this.date = date;
        this.verNum = vn;
        this.defectProp = 0.0;
        this.propIncremental = 0.0;
    }

    public Version(String name, LocalDateTime date) {

        String[] tkns = name.split("-");
        if(tkns.length > 1){
            this.name = tkns[0];
            this.extendedName = name;
        }else {
            this.name = name;
            this.extendedName = null;
        }
        this.date = date;
        this.defectProp = 0.0;
        this.propIncremental = 0.0;
    }

    public String getName() {
        return name;
    }

    public String getExtendedName() {
        return extendedName;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Double getPropIncremental() {
        return propIncremental;
    }

    public Double getDefectProp() {
        return defectProp;
    }

    public int getVerNum() {
        return verNum;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compare(Version otherVer){
        if(this.getDate().compareTo(otherVer.getDate()) >= 0)
            return 0;
        return 1;
    }

    public void setPropIncremental(Double propIncremental) {
        this.propIncremental = propIncremental;
    }

    public void setFixedDefects(List<Tickets> fixedDefects) {
        int c = 0;
        this.fixedDefects = fixedDefects;

        //for each complete defect (containing the iv) calculate the proportion P of defects
        for(Tickets t : fixedDefects){
            if(t.getIv() != null){
                c++;
                this.defectProp += defectProportionInc(t);
            }
        }
        if(this.defectProp != 0 && c != 0)
            this.defectProp = this.defectProp / c;
        else
            this.defectProp = 1.0; //non può essere 0

    }

    public void calcMissingIV(List<Version> releases){
        for(Tickets t : fixedDefects){
            if(t.getIv() == null){
                t.calIvAndSetAv(releases);
            }
        }
    }
}
