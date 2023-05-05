package com.mycompany.app;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.*;
import java.util.*;

import static com.mycompany.app.getReleaseInfo.relNames;
import static com.mycompany.app.getReleaseInfo.retrieveReleases;

public class FIlesRet {

    public static ArrayList<RepoFile> files = new ArrayList<>();
    public static String repo_path = "/home/alessandrodea/Scrivania/uni/Magistrale/isw2/isw 22-23/projects/syncope/.git";
    public static String projName = "SYNCOPE";
    public static List<Ref> branches = new ArrayList<>();
    public static List<Ref> tags = new ArrayList<>();
    public static Repository repository;

    public static ArrayList<Release> releases = new ArrayList<>();


    /*
    * Le release sono quelle i cui commit hanno un tag. Notare che quelli validi sono quelli dalla release 1.2.1, quelli
    * precedenti non fanno parte della repository principale ma di un fork probabilmente.
    *
    * */
    /*public static void retrieveTags() throws GitAPIException, IOException {
        Git jGit = new Git(repository);
        int len;
        List<Ref> call = jGit.tagList().call();

        RevWalk walk = new RevWalk(repository);

        for (Ref ref : call) {
            RevCommit commit = repository.parseCommit(ref.getObjectId());

            //System.out.println("Tag: " + ref.getName() + " Commit: " + ref.getObjectId().getName());// + " Msg: " + commit.getFullMessage());

            RevTag tag = walk.parseTag(ref.getObjectId());

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(tag.getTaggerIdent().getWhen().getTime());

            releases.add(new Release(ref, calendar));


        }

        releases.sort(new Comparator<Release>() {
            @Override
            public int compare(Release o1, Release o2) {
                return o1.getDate().getTime().compareTo(o2.getDate().getTime());
            }
        });

        for(Release r : releases){
            tags.add(r.getRef()); //li inserisco in tag in modo ordinato

            *//*int year = r.getDate().get(Calendar.YEAR);
            int month = r.getDate().get(Calendar.MONTH) + 1;
            int day = r.getDate().get(Calendar.DAY_OF_MONTH);
            System.out.println("Date: " + day + "/" + month + "/"+ year);*//*
        }


        // scarta l'ultimo 50% delle release
        len = tags.size();
        System.out.println(len);
        for(int i =  len - 1; i > len/2; i--){
            tags.remove(i);
            releases.remove(i);
        }
    }*/


    /*
    * Una volta prese tutte le release vado a vedere a quei commit tutti i cambiamenti fatti ad ogni file
    * */
    /*public static List<Ref> retrieveBranches() throws GitAPIException {
        Git jGit = new Git(repository);
        List<Ref> call = jGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        int n = 0;
        for (Ref ref : call) {
            if(!ref.getName().contains("HEAD") && !ref.getName().contains("master")){
                System.out.println("Branch: " + ref.getName() + " " + ref.getObjectId().getName());
                n++;
            }
        }
        System.out.println(n);
        return call;
    }*/


    public static void writeOnFile(){
        FileWriter fileWriter = null;
        int numVersions;
        int j;
        try {
            String outname = projName + "FilesInfo.csv";
            //Name of CSV for output
            fileWriter = new FileWriter(outname);
            fileWriter.append("Version, Version Name, Name, LOCs, Churn, Age, Number of Authors");
            fileWriter.append("\n");
            numVersions = relNames.size();
            for (int i = 0; i < numVersions; i++) {

                for (RepoFile file : files) {

                    if ((i >= file.getRevisionFirstAppearance() - 1) && (file.getAppearances() > 0)) {
                        fileWriter.append(Integer.toString(i+1));
                        fileWriter.append(",");

                        fileWriter.append(relNames.get(i));
                        fileWriter.append(",");

                        fileWriter.append(file.getPaths().get(0));
                        fileWriter.append(",");

                        fileWriter.append(file.getLOCs().get(0).toString());
                        fileWriter.append(",");

                        fileWriter.append(file.getChurn().get(0).toString());
                        fileWriter.append(",");

                        fileWriter.append((Integer.toString(i - file.getRevisionFirstAppearance() + 1)));
                        /*fileWriter.append(",");

                        fileWriter.append((file.getnAuth().get(0).toString()));*/
                        fileWriter.append("\n");

                        file.getPaths().remove(0);
                        file.getLOCs().remove(0);
                        file.getChurn().remove(0);
                        //file.getnAuth().remove(0);

                        file.decAppearances();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Error in csv writer");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
        System.out.println("File correctly written");
    }

    public static int iterateAndCompareFiles(String name, String path, int relNum){
        for(RepoFile f : files){
            if(f.equals(name)){
                if(f.getPaths().size() > 0) {
                    if (path.equals(f.getPaths().get(f.getPaths().size()-1))) {
                        return files.indexOf(f);
                    }
                }
            }
        }
        return -1;
    }

    public static void listRepositoryContents(String rel, int releaseNumber) throws IOException, GitAPIException {
        ObjectId head = repository.resolve(rel);

        //System.out.println(head.getName());
        // a RevWalk allows to walk over commits based on some filtering that is defined
        RevWalk walk = new RevWalk(repository);

        RevCommit commit = walk.parseCommit(head.toObjectId());

        RevTree tree = commit.getTree();


        // now use a TreeWalk to iterate over all files in the Tree recursively
        // you can set Filters to narrow down the results if needed
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        int ret;
        String[] tkns;

        while (treeWalk.next()) {
            if(treeWalk.getPathString().contains(".java")) {

                tkns = treeWalk.getPathString().split("/");
                if(!treeWalk.getPathString().contains("/test")){
                    //System.out.println("Release Number: " + releaseNumber + " name: " + treeWalk.getPathString());
                    ret = iterateAndCompareFiles(tkns[tkns.length - 1], treeWalk.getPathString(), releaseNumber-1);

                    if (ret >= 0) {

                        files.get(ret).incAppearances();
                        files.get(ret).insertRelease(rel);
                        files.get(ret).insertPath(treeWalk.getPathString());
                        files.get(ret).insertLOCs(countLOCs(treeWalk.getPathString(), rel));
                        files.get(ret).insertChurn(files.get(ret).getReleases().size() - 1);
                        //System.out.println("if: Release Number: " + releaseNumber + " name: " + treeWalk.getPathString());
                        //files.get(ret).insertAuth(countAuthorsInFile(treeWalk.getPathString(), relNames.get(releaseNumber-1)));
                        files.get(ret).insertRevisions(countCommits(repository, treeWalk.getPathString(), rel));

                    } else {
                        //System.out.println("else: Release Number: " + releaseNumber + " name: " + treeWalk.getPathString());
                        RepoFile rf = new RepoFile(tkns[tkns.length - 1]);
                        rf.insertRelease(rel);
                        rf.insertPath(treeWalk.getPathString());
                        rf.insertLOCs(countLOCs(treeWalk.getPathString(), rel));
                        rf.insertChurn(0);
                        rf.setRevisionFirstAppearance(releaseNumber);
                        //rf.insertAuth(countAuthorsInFile(treeWalk.getPathString(), relNames.get(releaseNumber-1)));
                        rf.insertRevisions(countCommits(repository, treeWalk.getPathString(), rel));
                        files.add(rf);
                    }
                }
            }
        }
    }


    public static int countLOCs(String filePath, String release) throws IOException, GitAPIException {
        RevWalk walk = new RevWalk(repository);
        ObjectId headId = repository.resolve(release);
        RevCommit commit = walk.parseCommit(headId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        int lines = 0;
        while (treeWalk.next()) {
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines++;
                }
            }
        }
        return lines;
    }


    public static int countAuthorsInFile(String filePath, String toCommit) throws IOException, GitAPIException {
        int authorsCount = 0;


        ObjectId to = repository.resolve(toCommit); //resolve the commit id from the tag name


        BlameResult blameResult = new Git(repository).blame()
                .setFilePath(filePath)
                .setStartCommit(to)
                .call();

        Set<String> authors = new HashSet<>();
        for (int i = 0; i < blameResult.getResultContents().size(); i++) {
            authors.add(blameResult.getSourceAuthor(i).getName());
            //System.out.println(blameResult.getSourceAuthor(i).getName());
        }

        authorsCount = authors.size();
        return authorsCount;
    }


    public static int countCommits(Repository repository, String file, String currentRelease) throws IOException, GitAPIException {
        RevWalk walk = new RevWalk(repository);
        Git git = new Git(repository);
        int c = 0;

        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();

        // prendi gli object id delle release
        ObjectId objNewId = repository.resolve(currentRelease);

        RevCommit curRelCommit = walk.parseCommit(objNewId);

        // fai il log dell'ultima release e prendi tutti i commit
        LogCommand log = git.log().add(curRelCommit);

        Iterable<RevCommit> commits = log.call();

        ArrayList<RevCommit> relCommits = new ArrayList<>();
        for(RevCommit com : commits){
            relCommits.add(com);
        }


        for(RevCommit commit : relCommits) {
            if(relCommits.indexOf(commit) == relCommits.size()-1)
                break;

            RevTree tree1 = commit.getTree();
            newTreeIter.reset(reader, tree1);

            RevTree tree2 = relCommits.get(relCommits.indexOf(commit) + 1).getTree();
            oldTreeIter.reset(reader, tree2);

            DiffFormatter diffFormatter = new DiffFormatter( DisabledOutputStream.INSTANCE );
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            //System.out.println(commit + "     c: " + c);
            for( DiffEntry entry : entries ) {
                if(entry.getNewPath().equals(file)){
                    //System.out.println(entry.getNewPath());
                    c++;
                }

            }
        }

        System.out.println(c);

        return c;
    }


    public static void main(String[] args) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        int relNum = 0;
        repository = builder
                .setGitDir(new File(repo_path)).readEnvironment()
                .findGitDir().build();


        retrieveReleases();


        /* per ogni release (tag) lista i file */
        for(String releaseName : relNames) {
            //per ogni branch cerca tutti i file - excludi HEAD e master
            relNum++;
            listRepositoryContents(releaseName, relNum);
        }

        // Scrivi il file
        writeOnFile();


        repository.close();

    }

}
