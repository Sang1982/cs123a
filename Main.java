import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Scanner;	// Need for scanner class
import processor.FileProcessor;
import processor.FileProcessor.Molecule;
import processor.PairwiseAlignmentThread;

/**
	This class calculates the aligment score
*/
public class Main {
	private static String defaultFileDirectory = ".\\";
	//private static final String testLink =
            //"C:\\Users\\Thong Le\\Desktop\\courses\\cs123A-Bioinformatics\\project\\PA\\out\\production\\PA\\";

	public static void main(String[] args)
	{
		System.out.println(" ================================================================= ");
		System.out.println("|Pairwise Alignment mini-tool @ Created by: Thong Le - Sang Nguyen|");
		System.out.println(" ================================================================= ");
		// Create scanner object for keyboard input
		Scanner input = new Scanner(System.in);
		boolean fileFound = false;
		FileProcessor fileProcessor;
		ArrayList<Molecule> data = new ArrayList<>();
		PrintStream outToFile = null, outToConsole = null;
		do {
			System.out.print("Enter filename (file should be placed in same folder with program): " + System.getProperty("user.dir") + "\\");
			String fileName = input.nextLine();
			try {
				fileProcessor = new FileProcessor(new File(defaultFileDirectory + fileName));
                //fileProcessor = new FileProcessor(new File(testLink + fileName));
				//SAVE REPORT TO FILE
				fileFound = true;
				boolean fileExist = true;
				do {
					System.out.print("Enter name with file extension (.txt) to save report to: " + System.getProperty("user.dir")  + "\\");
					fileName = input.nextLine();
					try {
						File outputFile = new File(defaultFileDirectory + fileName);
						//File outputFile = new File(testLink + fileName);
						if(outputFile.exists()) throw new FileAlreadyExistsException("");
						else {
							// Creating a File object that represents the disk file.
							 outToFile = new PrintStream(outputFile);
							// Creating a File object that represents the disk file.
							outToConsole = System.out;
							fileExist = false;
						}
					} catch (FileAlreadyExistsException e) {
						System.out.println("File name " + fileName + " has been used. Try another name");
					} catch (IOException e){
						e.printStackTrace();
						System.exit(0);
					}
				} while (fileExist);

				data = (fileProcessor.getInputData());
			} catch (Exception e){
				System.out.println("File: " + fileName + " not found. Make sure you have file in same directory and included file extension .txt");
				//e.printStackTrace();
			}
		} while (!fileFound);


		if(data.size() == 0) { System.out.println("File is empty. Terminating..."); System.exit(0); }

//===========================================Process Reporting================================================
		//System.out.println("\n>   Query    ||  Subject");
		System.setOut(outToConsole);
		System.out.print("\nTool is running. Don't close.");

        int numOfPA = 0; // number of pairwise alignments proceeded
        long start = System.currentTimeMillis();
		for(int i = 0; i < data.size() - 1; i++){
		    for(int j = i + 1; j < data.size(); j++){
		    	System.setOut(outToConsole);
				if(j % 4 == 0) System.out.print(".");

		        numOfPA ++;
		        Molecule molecule1 = data.get(i);
		        Molecule molecule2 = data.get(j);
                PairwiseAlignmentThread paThread = new PairwiseAlignmentThread(molecule1, molecule2, outToFile, outToConsole);
                paThread.run();
                /*try{
                    Thread.sleep(100);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }*/
            }
        }
        long alignmentTime = System.currentTimeMillis() - start;
		System.setOut(outToFile);
		System.out.println("\n=====================");
		System.out.println("Number of pairwise alignment made: " + numOfPA);
        System.out.println("Elapsed time: " + alignmentTime + " ms");
        System.setOut(outToConsole);
		System.out.println("\n======== DONE! See file for results ==========");
		System.out.println("Number of pairwise alignment made: " + numOfPA);
		System.out.println("Elapsed time: " + alignmentTime + " ms");

	}


}