package processor;

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

/*
*	This class implemented to get input file of molecule, then process the file line-by-line and return an data structure of all input
*   Input file format:
*		- Every of a molecule should have a line starting with forward arrow (>) to provide information about it
*		- A molecule information should provide accession number
*			+ ie: > DQ265095.1 Dengue virus type 1 isolate D1.Myanmar.37045.23/00 polyprotein gene, partial cds
*					The accession number: DQ265095.1
*		- A whole molecule sequence must be written in one line right after molecule information
* 	Process steps:
*		- After finish reading one molecule (2 lines), the data will be stored in ArrayList<Molecule>
*			+ Molecule is a class containing accessionNumber and sequence
*/
public class FileProcessor {
	ArrayList<Molecule> inputData;
	File inputFile;

	// validate inputFile before passing to constructor
	public FileProcessor(File inputFile) throws Exception{
		this.inputFile = inputFile;
		inputData = new ArrayList<>();
		runFileProcessor();
	}

	private void runFileProcessor() throws FileNotFoundException{
		Scanner sc = new Scanner(inputFile);
		int expectedSeqIndex = 0; // to identify Molecule sequence after its start (>)
		while(sc.hasNextLine()){
			String line = sc.nextLine();
			if(!"".equals(line)){ // not a blank line
				if(line.charAt(0) == '>'){
					String[] splitLine = line.split(" ");
					String accessionNumber = splitLine[1];
					inputData.add(new Molecule(accessionNumber));
					expectedSeqIndex += 1;
				}
				if(expectedSeqIndex == inputData.size())
					inputData.get(expectedSeqIndex - 1).setSequence(line);
				else {
					System.out.println("Wrong input format. At: " + inputData.get(expectedSeqIndex - 1).getAccessionNumber());
					System.exit(0);
				}
			}
		}
	}

	public ArrayList<Molecule> getInputData() { return inputData; }

	public class Molecule{
		private String accessionNumber;
		private char[] sequence;

		Molecule(String accessionNumber){
			this.accessionNumber = accessionNumber;
		}

		void setSequence(String sequence){
			this.sequence = sequence.toCharArray();
		}

		String getAccessionNumber() { return accessionNumber; }
		char[] getSequence() { return sequence; }
		int getSequenceLength() { return sequence.length; }
	}
}