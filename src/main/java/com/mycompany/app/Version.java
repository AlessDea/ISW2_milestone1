package com.mycompany.app;

import java.time.LocalDateTime;

public class Version {
    private String name;
    private String extendedName; //for those releases which have the name like '4.0.0-something'
    private LocalDateTime date;

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

}
