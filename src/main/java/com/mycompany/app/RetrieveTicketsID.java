package com.mycompany.app;

import org.eclipse.jgit.api.errors.GitAPIException;
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

import static com.mycompany.app.getReleaseInfo.relNames;
import static com.mycompany.app.getReleaseInfo.retrieveReleases;


public class RetrieveTicketsID {

    public static ArrayList<Tickets> tickets = new ArrayList<>();

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
            JSONArray json = new JSONArray(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    /**
    * Per calcolare l'IV c'è bisogno di calcolare la Proportion, questa può essere fatta in tre modi:
     * - cold start: usando dati di altri progetti
     * - increment
     * - moving window
    * */
    public static int proportion(int ov, int fv){
        int iv;
        int prop = 0;
        //prop = prop_cold_start(); // = (fv - iv)/(fv - ov);
        iv = fv - (fv - ov) * prop;

        return iv;
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

        //in this case it must be the last one
        return relNames.get(relNames.size()-1);

    }

    public static Version getTheInjectedVer(ArrayList<Version> ivs){
        if(ivs.size() > 0) {
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

        //in caso non la trova c'è qualche problema grosso
        return null;
    }


    public static Version getTheFixedVer(ArrayList<Version> fvs){
        if(fvs.size() > 0) {
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

        //in caso non la trova c'è qualche problema grosso
        return null;
    }


    public static void retrieveTickets(String projName) throws IOException, JSONException {
        Version iv;
        Version fv;
        Version ov;
        int j = 0, i = 0, total = 1;

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

            /*for(Object o : issues)
                System.out.println(o);*/

            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug
                JSONObject row = issues.getJSONObject(i%1000);
                String key = row.get("key").toString();

                JSONObject fields = (JSONObject) row.get("fields");
                LocalDateTime resDate = LocalDateTime.parse(fields.get("resolutiondate").toString().split("\\.")[0]); //non mi serve
                LocalDateTime ovDate = LocalDateTime.parse(fields.get("created").toString().split("\\.")[0]); //opening version date


                //prendi le opening versions
                JSONArray injVer = fields.getJSONArray("versions");
                ArrayList<Version> injectedVersions = new ArrayList<>();
                try {
                    for(int z = 0; z < injVer.length(); z++){
                        String name = injVer.getJSONObject(z).get("name").toString();
                        LocalDateTime date = LocalDate.parse(injVer.getJSONObject(z).get("releaseDate").toString(), onlyDateFormatter).atStartOfDay();
                        Version v = null;
                        if(projName.equals("BOOKKEEPER"))
                            v = new Version("refs/tags/release-" + name, date);
                        else
                            v = new Version("refs/tags/syncope-" + name, date);

                        injectedVersions.add(v);
                    }
                } catch (JSONException e) {
                    continue;
                }



                JSONArray fixVer = fields.getJSONArray("fixVersions");
                ArrayList<Version> fixedVersions = new ArrayList<>();
                try {
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
                } catch (JSONException e) {
                    continue;
                }

                /* conoscendo la data della opening devo prendermi il nome della release corrispondente */
                ov = getTheOpeningVerName(ovDate);

                /* ora il problema è che le injected e le fixed possono essere più di una, quindi dato che ho considerato le release sequenziali (non parallele come sono effettivamente mantenute)
                * devo prendere la più piccola delle injected e la più grande delle fixed.
                * */
                if(injectedVersions.size() < 1) {
                    iv = null; //non c'è la injected, bisognerà usare proportion
                }else {
                    //devo prendere la più vecchia
                    iv = getTheInjectedVer(injectedVersions);
                }

                fv = getTheFixedVer(fixedVersions);
                if(fv == null){
                    //without the fixed version the ticket is useless
                    //System.out.println("continuing");
                    continue;
                }

                Tickets newTicket;
                if(iv != null){
                    newTicket = new Tickets(key, iv, fv, ov, (ArrayList<Version>) relNames);
                } else{
                    newTicket = new Tickets(key, fv, ov);
                }

                tickets.add(newTicket);
            }
        } while (i < total);

        System.out.println("num of tickets: " + tickets.size());
        for (Tickets t : tickets) {
            System.out.println(t.getName());
            if(t.getIv() != null)
                System.out.println("\t\tINJECTED VERSION: " + t.getIv().getExtendedName() + " FIXED VERSION: " + t.getFv().getExtendedName());
            else
                System.out.println("\t\tINJECTED VERSION: " + t.getIv() + " FIXED VERSION: " + t.getFv().getExtendedName());
            for(Version tmp : t.getAffectedVersions())
                System.out.println("\t\t\t\tAFFECTED VERSIONS: " + tmp.getExtendedName());
        }
        //Bisogna togliere quelli della seconda metà delle release
    }

    public static void main(String[] argv) throws IOException, GitAPIException {
        retrieveReleases();
        retrieveTickets("SYNCOPE");
    }

}
