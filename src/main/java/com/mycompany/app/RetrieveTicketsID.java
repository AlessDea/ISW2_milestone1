package com.mycompany.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.ArrayList;

import static com.mycompany.app.getReleaseInfo.relNames;

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



    public static void retrieveTickets(String projName) throws IOException, JSONException {

        Integer j = 0, i = 0, total = 1;
        //Get JSON API for closed bugs w/ AV in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + i.toString() + "&maxResults=" + j.toString();
            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");

            /*for(Object o : issues)
                System.out.println(o);*/

            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug
                String key = issues.getJSONObject(i % 1000).get("key").toString();
                JSONObject fields = (JSONObject) issues.getJSONObject(i%1000).get("fields");
                JSONArray versions = fields.getJSONArray("versions");
                String resDate = fields.get("resolutiondate").toString();
                String created = fields.get("created").toString();

                //System.out.println(fields);
                String release = null;
                try {
                    release = versions.getJSONObject(0).get("name").toString();
                } catch (JSONException e) {
                    // alcuni ticket non hanno la release associata
                    continue;
                }

                tickets.add(new Tickets(key, release, created, resDate, (ArrayList<String>) relNames)); // non è giusto, ho bisogno del nome delle release invece che della data
                System.out.println(key + ":\t" + release + "\t\t" + created + "\t" + resDate);
            }
        } while (i < total);

        //Bisogna togliere quelli della seconda metà delle release
    }

   /* public static void main(String[] argv) throws IOException {
        retrieveTickets("BOOKKEEPER");
    }
*/

}
