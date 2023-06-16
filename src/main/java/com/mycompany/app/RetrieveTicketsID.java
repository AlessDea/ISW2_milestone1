package com.mycompany.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.mycompany.app.FIlesRet.projName;
import static com.mycompany.app.Proportion.revisionProportionInc;
import static com.mycompany.app.GetReleaseInfo.relNames;


public class RetrieveTicketsID {

    static List<Tickets> tickets;

    private RetrieveTicketsID() {}

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONArray(jsonText);
        } finally {
            is.close();
        }
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }


    public static Version getTheOpeningVerName(LocalDateTime d){

        //check if it is the first one
        if(d.isBefore(relNames.get(0).getDate()) || d.isEqual(relNames.get(0).getDate())){
            //It's the first release
            return relNames.get(0);
        }

        //iterate in couple the releases and check if the date of the opening version is between the two considered.
        for(int i = 0; i < relNames.size() - 1; i++) {
            if(d.isAfter(relNames.get(i).getDate()) && d.isBefore(relNames.get(i + 1).getDate())){
                //se è compresa fra queste due vuol dire che è la seconda
                return relNames.get(i + 1);
            }
        }

        return null; //it comes from the last half of the releases

    }

    public static Version getTheInjectedVer(List<Version> ivs){
        if(!ivs.isEmpty()) {
            ivs.sort(new Comparator<Version>() {
                @Override
                public int compare(Version o1, Version o2) {
                    return o1.compare(o2);
                }
            });

            for (Version v : relNames) {
                //faccio così perchè in questo modo ho proprio l'oggetto versione presente in relNames ed è più facile calcolarmi le affected versions
                if (v.getExtendedName().equals(ivs.get(0).getExtendedName())) //ivs.get(0)  ->  la prima sarà la più vecchia
                    return v;
            }
        }

        //it comes from the last half of the releases
        return null;
    }


    public static Version getTheFixedVer(List<Version> fvs){
        if(!fvs.isEmpty()) {
            fvs.sort(new Comparator<Version>() {
                @Override
                public int compare(Version o1, Version o2) {
                    return o1.compare(o2);
                }
            });

            for (Version v : relNames) {
                //faccio così perchè in questo modo ho proprio l'oggetto version presente in relNames ed è più facile calcolarmi le affected versions
                if (v.getExtendedName().equals(fvs.get(fvs.size() - 1).getExtendedName())) //ivs.size() - 1  ->  l'ultima sarà la più nuova, e quindi l'ultima in cui è stata fixata
                    return v;
            }
        }

        //it comes from the last half of the releases
        return null;
    }

    public static void addTicket(String key, Version iv, Version fv, Version ov){
        Tickets newTicket;
        if (iv != null) {
            newTicket = new Tickets(key, iv, fv, ov, relNames);
        } else {
            newTicket = new Tickets(key, fv, ov);
        }

        tickets.add(newTicket);
    }


    public static void retrieveTickets(String projName) throws IOException, JSONException {
        Version iv;
        Version fv;
        Version ov;
        int j = 0;
        int i = 0;
        int total = 1;

        tickets = new ArrayList<>();

        DateTimeFormatter onlyDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //Get JSON API for closed bugs w/ AV in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created,fixVersions&startAt="
                    + i + "&maxResults=" + j;
            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");

            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug
                JSONObject row = issues.getJSONObject(i%1000);
                String key = row.get("key").toString();

                JSONObject fields = (JSONObject) row.get("fields");
                LocalDateTime ovDate = LocalDateTime.parse(fields.get("created").toString().split("\\.")[0]); //opening version date


                //prendi le opening versions
                JSONArray injVer = fields.getJSONArray("versions");
                ArrayList<Version> injectedVersions = new ArrayList<>();

                JSONArray fixVer = fields.getJSONArray("fixVersions");
                ArrayList<Version> fixedVersions = new ArrayList<>();

                try {
                    prepareVersions(injVer, fixVer, injectedVersions, fixedVersions, onlyDateFormatter);
                } catch (JSONException e) {
                    continue;
                }

                /* conoscendo la data della opening devo prendermi il nome della release corrispondente */
                ov = getTheOpeningVerName(ovDate);
                if(ov != null) {

                    /* ora il problema è che le injected e le fixed possono essere più di una, quindi dato che ho considerato le release sequenziali (non parallele come sono effettivamente mantenute)
                     * devo prendere la più piccola delle injected e la più grande delle fixed.
                     * */
                    iv = null; //injected version not present, must use proportion
                    if (!injectedVersions.isEmpty()) {
                        //get the oldest
                        iv = getTheInjectedVer(injectedVersions);
                    }

                    fv = getTheFixedVer(fixedVersions);
                    if (fv != null && ov.getVerNum() <= fv.getVerNum()) { //without the fixed version the ticket is useless

                        addTicket(key, iv, fv, ov);

                    }
                }
            }
        } while (i < total);


        bindRelsAndDefects();

        setIncrementalProportion();

    }

    public static void setIncrementalProportion(){
        for (Version v : relNames){

            if(v.getVerNum() == 1) {
                v.setPropIncremental(v.getDefectProp()); //set the value of prop_incr as P (defectProp) because it's the first release
                continue; //for the first release the IVs are always 1, it's useless to calculate them and also it's useless to use them
            }else {
                v.setPropIncremental(revisionProportionInc(v)); //calculate prop_incremental
            }
            v.calcMissingIV(relNames); //calculate the missing IVs

        }
    }


    public static void bindRelsAndDefects(){
        //assign at each version its defects
        for(Version v : relNames) {
            ArrayList<Tickets> defects = new ArrayList<>();
            for(Tickets t : tickets){
                if(t.getFv().equals(v)){
                    defects.add(t);
                }
            }
            v.setFixedDefects(defects);
        }
    }


    public static void prepareVersions(JSONArray injVer, JSONArray fixVer, List<Version> injectedVersions, ArrayList<Version> fixedVersions, DateTimeFormatter onlyDateFormatter){
        for(int z = 0; z < injVer.length(); z++){
            String name = injVer.getJSONObject(z).get("name").toString();
            LocalDateTime date = LocalDate.parse(injVer.getJSONObject(z).get("releaseDate").toString(), onlyDateFormatter).atStartOfDay();
            Version v = null;
            if(projName.equals("BOOKKEEPER")) {
                v = new Version("refs/tags/release-" + name, date);
            }else {
                v = new Version("refs/tags/syncope-" + name, date);
            }
            injectedVersions.add(v);
        }


        for(int z = 0; z < fixVer.length(); z++){
            String name = fixVer.getJSONObject(z).get("name").toString();
            LocalDateTime date = LocalDate.parse(fixVer.getJSONObject(z).get("releaseDate").toString(), onlyDateFormatter).atStartOfDay();
            Version v = null;
            if(projName.equals("BOOKKEEPER"))
                v = new Version("refs/tags/release-" + name, date);
            else
                v = new Version("refs/tags/syncope-" + name, date);

            fixedVersions.add(v);
        }
    }
}
