package com.mycompany.app;


import static com.mycompany.app.GetReleaseInfo.relNames;

/**
 * step-by-step explaination
 *
 * Step 1: retrieve releases
 * Setp 2: retrieve Jira tags, for each tag:
 *      Step 2.1: get FV, OV, IV and create a Tickets Object:
 *          Step 2.1.1: if IV is not null: calculate all the AV
 *          Step 2.1.2: else: can't calculate it, do it later
 *      Step 2.2: For each Release assign it its defects
 *          Step 2.2.1: for each defect with the IV calculate the proportion P (defectProp in Version class) value and average it on the number of defects
 *      Step 2.3: For each Release calculate the proportion_incremental (prop_incremental in Version class)
 *      Step 2.4: having the proportion_incremental for each Version, calculate the IV for the tickets which miss it
 */

public class Proportion {

    private Proportion() {}

    /**
     * La proportion di ogni release ce l'ho dentro all'oggetto Version ed inoltre nell'oggetto Version c'è anche
     * la lista dei defect
     */
    public static Double revisionProportionInc(Version v){
        double p = 0.0;
        for(Version ver : relNames) {
            if(v.equals(ver)){
                for(int i = 0; i < relNames.indexOf(ver); i++){
                    p += relNames.get(i).getDefectProp();
                }
                p = p / relNames.indexOf(ver);
            }
        }
        return p;
    }

    /**
     * For each defect which has the IV calculate the proportion value as (FV − IV)/(FV − OV)
     * @param t Defect's ticket
     * @return p the proportion value
     */

    public static double defectProportionInc(Tickets t){
        double p;
        if(t.getFv().getVerNum() == t.getOv().getVerNum())
            p = (t.getFv().getVerNum() - t.getIv().getVerNum());
        else
            p = (double) (t.getFv().getVerNum() - t.getIv().getVerNum())/(t.getFv().getVerNum() - t.getOv().getVerNum());

        return p;
    }

    /**
     * Calculate the injected version of a defect as FV − (FV − OV ) ∗ P
     * @param t
     * @return iv integer value of the injected version
     */
    public static int calcInjectedVersion(Tickets t){
        double iv;
        if(t.getFv().getVerNum() == t.getOv().getVerNum())
            iv = t.getFv().getVerNum() - 1 * t.getFv().getPropIncremental(); // fv - ov setted as default as 1, otherwise iv equals to fv that is the case we excluded
        else
            iv = t.getFv().getVerNum() - (t.getFv().getVerNum() - t.getOv().getVerNum()) * t.getFv().getPropIncremental();

        return (int) Math.floor(iv);
    }


}
