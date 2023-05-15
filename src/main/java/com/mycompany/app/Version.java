package com.mycompany.app;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.mycompany.app.Proportion.defectProportionInc;

public class Version {
    private String name;
    private String extendedName; //for those releases which have the name like '4.0.0-something'
    private LocalDateTime date;
    private int verNum;
    private ArrayList<Tickets> fixedDefects;
    private Double prop_incremental;
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
        this.prop_incremental = 0.0;
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
        this.prop_incremental = 0.0;
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

    public Double getProp_incremental() {
        return prop_incremental;
    }

    public Double getDefectProp() {
        return defectProp;
    }

    public int getVerNum() {
        return verNum;
    }

    public ArrayList<Tickets> getFixedDefects() {
        return fixedDefects;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExtendedName(String extendedName) {
        this.extendedName = extendedName;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int compare(Version otherVer){
        if(this.getDate().compareTo(otherVer.getDate()) >= 0)
            return 0;
        return 1;
    }

    public void setProp_incremental(Double prop_incremental) {
        this.prop_incremental = prop_incremental;
    }

    public void setDefectProp(Double defectProp) {
        this.defectProp = defectProp;
    }

    public void setVerNum(int verNum) {
        this.verNum = verNum;
    }

    public void setFixedDefects(ArrayList<Tickets> fixedDefects) {
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

    public void calcMissingIV(ArrayList<Version> releases){
        for(Tickets t : fixedDefects){
            if(t.getIv() == null){
                t.calIvAndSetAv(releases);
            }
        }
    }
}
