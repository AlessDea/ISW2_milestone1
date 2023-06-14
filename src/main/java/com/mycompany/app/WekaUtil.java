package com.mycompany.app;

import java.io.*;

public class WekaUtil {

    static final int BK_RELEASES = 8; //7+1
    static final int SY_RELEASES = 27; //26+1

    private static int releases;
    private static String projName;

    public static void arffInit(FileWriter wr, String name) throws IOException {
        wr.write("@RELATION " + name + "\n\n");
        wr.write("@ATTRIBUTE LOCs numeric\n");
        wr.write("@ATTRIBUTE Churn numeric\n");
        wr.write("@ATTRIBUTE Age numeric\n");
        wr.write("@ATTRIBUTE WeightedAge numeric\n");
        wr.write("@ATTRIBUTE NumOfAuthors numeric\n");
        wr.write("@ATTRIBUTE Revisions numeric\n");
        wr.write("@ATTRIBUTE LOCsTouched numeric\n");
        wr.write("@ATTRIBUTE LOCsAdded numeric\n");
        wr.write("@ATTRIBUTE AvgSetSize numeric\n");
        wr.write("@ATTRIBUTE NumOfFixes numeric\n");
        wr.write("@ATTRIBUTE Buggy {false,true}\n\n");
        wr.write("@DATA\n");
    }
    public static void writeArffLine(FileWriter fileWriter, String[] val) throws IOException {
        for(int i = 3; i < val.length; i++){
            fileWriter.append(val[i]);
            if(i != val.length-1){
                fileWriter.append(",");
            }
        }
        fileWriter.append("\n");
    }



    public static void walkForward(String outputDirectoryPath, String inputFilePath){

        try {
            String outputFilePathTrain;
            String outputFilePathTest;
            String line;

            String dir_path = "WalkForward-" + projName;
            new File("Output").mkdir();
            new File(outputDirectoryPath + dir_path).mkdir();

            for(int index = 2; index < releases; index++){
                outputFilePathTrain = outputDirectoryPath + dir_path + "/" + index + "/" + "Train.arff";
                outputFilePathTest = outputDirectoryPath + dir_path + "/" + index + "/" + "Test.arff";
                new File(outputDirectoryPath + dir_path + "/" + index).mkdir();

                FileWriter fileWriterTrain = new FileWriter(outputFilePathTrain);
                arffInit(fileWriterTrain, "Train");
                FileWriter fileWriterTest = new FileWriter(outputFilePathTest);
                arffInit(fileWriterTest, "Test");

                BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));

                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if(!values[0].equals("Version")){
                        if(Integer.parseInt(values[0]) == index){
                            // csv di testing
                            writeArffLine(fileWriterTest, values);
                        } else if (Integer.parseInt(values[0]) < index) {
                            // csv di training
                            writeArffLine(fileWriterTrain, values);
                        } else {
                            break;
                        }
                    }
                }
                reader.close();
                fileWriterTrain.close();
                fileWriterTest.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String outputDirectoryPath = "Output/";
        String inputFilePath = "/home/alessandrodea/Scrivania/uni/Magistrale/isw2/isw_22-23/projects/milestone1/milestone1/BOOKKEEPERFilesInfo.csv";
        projName = "BK";
        releases = BK_RELEASES;
        walkForward(outputDirectoryPath, inputFilePath);

        projName = "SY";
        releases = SY_RELEASES;
        inputFilePath = "/home/alessandrodea/Scrivania/uni/Magistrale/isw2/isw_22-23/projects/milestone1/milestone1/SYNCOPEFilesInfo.csv";
        walkForward(outputDirectoryPath, inputFilePath);
    }
}