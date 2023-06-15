package com.mycompany.app;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.mycompany.app.FIlesRet.projName;
import static com.mycompany.app.FIlesRet.repoPath;

public class GetReleaseInfo {

    static Map<LocalDateTime, String> releaseNames;
    static Map<LocalDateTime, String> releaseID;
    static Map<String, LocalDateTime> releasesInfo = new HashMap<>();
    static List<LocalDateTime> jiraReleases;
    static List<Version> relNames = new ArrayList<>(); //lista dei nomi delle release ordinate, la uso in FilesRet.java per ordinarmi quelle di git
    static List<String> gitReleases = new ArrayList<>();


    /**
     * Le release sono quelle i cui commit hanno un tag.
     * */
    public static void retrieveTags() throws GitAPIException, IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder
                .setGitDir(new File(repoPath)).readEnvironment()
                .findGitDir().build();


        Git jGit = new Git(repository);
        List<Ref> call = jGit.tagList().call();


        for (Ref ref : call) {

            gitReleases.add(ref.getName());

        }

    }


    public static void retrieveReleases() throws IOException, JSONException, GitAPIException {

        //Fills the arraylist with releases dates and orders them
        //Ignores releases with missing dates
        jiraReleases = new ArrayList<LocalDateTime>();
        Integer i;
        int vernum = 1;
        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
        JSONObject json = readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");
        releaseNames = new HashMap<LocalDateTime, String>();
        releaseID = new HashMap<LocalDateTime, String>();
        for (i = 0; i < versions.length(); i++) {
            String name = "";
            String id = "";
            if (versions.getJSONObject(i).has("releaseDate")) {
                if (versions.getJSONObject(i).has("name"))
                    name = versions.getJSONObject(i).get("name").toString();
                if (versions.getJSONObject(i).has("id"))
                    id = versions.getJSONObject(i).get("id").toString();

                addRelease(versions.getJSONObject(i).get("releaseDate").toString(), name, id);
            }
        }
        // order releases by date
        Collections.sort(jiraReleases, new Comparator<LocalDateTime>() {
            public int compare(LocalDateTime o1, LocalDateTime o2) {
                return o1.compareTo(o2);
            }
        });

        ArrayList<String> tmp = new ArrayList<>();
        for(LocalDateTime ldt : jiraReleases){
            for(LocalDateTime l : releaseNames.keySet()) {
                if(l.equals(ldt))
                    if(projName.equals("SYNCOPE"))
                        tmp.add("refs/tags/syncope-" + releaseNames.get(l)); //per syncope
                    else
                        tmp.add("refs/tags/release-" + releaseNames.get(l)); //per bookkeeper
            }
        }

        /* se il progetto è BOOKKEEPER bisogna prende i tag anche da github e togliere, da quelli presi su jira,
        * quelli non presenti da github altrimenti non è possibile fare il resolve del tag. Di conseguenza, dato che le releas
        * sono poche, lo faccio prima di dimezzarle */

        retrieveTags();


        /* togli le release che non si trovano su git */
        for(String rel : tmp){

            if(rel.equals("refs/tags/syncope-2.0.0-M1")) //exclude it because causes problems
                continue;

            if(gitReleases.contains(rel)) {
                if(releasesInfo.get(rel) != null) {
                    Version v = new Version(rel, releasesInfo.get(rel), vernum);
                    relNames.add(v);
                    vernum++;
                }
            }
        }



        /*// scarta l'ultimo 50% delle release
        int len = relNames.size();
        for(int j =  len - 1; j > len/2; j--){
            relNames.remove(j);
        }*/


    }


    public static void addRelease(String strDate, String name, String id) {
        LocalDate date = LocalDate.parse(strDate);
        LocalDateTime dateTime = date.atStartOfDay();


        if (!jiraReleases.contains(dateTime)) {
            jiraReleases.add(dateTime);
            if(projName.equals("SYNCOPE")) {
                releasesInfo.put("refs/tags/syncope-" + name, dateTime);
            }else {
                releasesInfo.put("refs/tags/release-" + name, dateTime);
            }
        }
        releaseNames.put(dateTime, name);
        releaseID.put(dateTime, id);
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


    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }




}