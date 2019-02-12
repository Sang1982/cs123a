package processor;

import processor.FileProcessor.Molecule;

import java.io.PrintStream;

public class PairwiseAlignmentThread implements Runnable {
    private Molecule molecule1, molecule2;
    // using advanced scoring scheme
    private final int GapPenalty = -2,
                            MatchScore = 2,
                                MismatchScore = -1;

    PrintStream outToFile, outToConsole;

    public PairwiseAlignmentThread(Molecule molecule1, Molecule molecule2, PrintStream outToFile, PrintStream outToConsole){
        this.molecule1 = molecule1;
        this.molecule2 = molecule2;
        this.outToFile = outToFile;
        this.outToConsole = outToConsole;
    }

    @Override
    public void run() {
        pairwiseAlignment();
    }

    private int findMax(int num1, int num2, int num3){
        int max = num1; //diagonal
        if(num2 > max) max = num2; // top
        else if(num3 > max) max = num3; // left
        return max;
    }

    // class MatrixCell is used for alignment
    private class MatrixCell {
        int score;
        boolean top, left, diagonal; // to determine which neighbor gives score, later used for traceback steps
        private MatrixCell(int score){
            this.score = score;
        }
        private void setPointer(boolean diagonal, boolean top, boolean left){
            this.diagonal = diagonal;
            this.top = top;
            this.left = left;
        }
        private int getScore(){ return score; }
        private boolean isPointerDiagonal(){ return diagonal;}
        private boolean isPointerTop(){ return top; }
        private boolean isPointerLeft(){ return left;}
    }

    int gapCounter = 0;

    private char[] traceBack( MatrixCell[][] scoringMatrix, char[] alignmentSequence, int alignmentIndex,
                                                int colIndex, int rowIndex, char[] newSequenceWithGaps){
        MatrixCell currCell = scoringMatrix[colIndex][rowIndex];
        if(alignmentIndex == -1 || currCell == null || (!currCell.isPointerDiagonal() && !currCell.isPointerLeft())) //base case
            return newSequenceWithGaps;

        int diagonalScore = scoringMatrix[colIndex -1][rowIndex - 1].getScore();
        int leftScore = scoringMatrix[colIndex -1][rowIndex].getScore();
        int topScore = scoringMatrix[colIndex][rowIndex -1].getScore();
        int max = diagonalScore;
        int nextColIndex = -1;
        int nextRowIndex = -1;

        // looking for pointers, following priority of diagonal > left
        if(currCell.isPointerDiagonal()){
            nextColIndex = colIndex -1; nextRowIndex = rowIndex -1;
            newSequenceWithGaps[alignmentIndex] = alignmentSequence[alignmentIndex];
            alignmentIndex --;
        }
        if(currCell.isPointerLeft() && leftScore > max) {
            max = leftScore; nextColIndex = colIndex -1; nextRowIndex = rowIndex;
            newSequenceWithGaps[alignmentIndex] = ' ';
            gapCounter ++;
        }
        if(currCell.isPointerTop() && topScore > max){
            nextColIndex = colIndex; nextRowIndex = rowIndex -1;
            gapCounter ++;
        }

        return traceBack(scoringMatrix, alignmentSequence, alignmentIndex, nextColIndex, nextRowIndex, newSequenceWithGaps);
    }

    private void pairwiseAlignment() {
        System.setOut(outToFile);
        System.out.println("\n> " + molecule1.getAccessionNumber() + " || " + molecule2.getAccessionNumber());
        System.setOut(outToConsole);
        char[] sequence1 = molecule1.getSequence(),
                sequence2 = molecule2.getSequence();

        // determine longer sequence; longer sequence as Cols, other sequence as Rows
        int length, numCol, numRow;
        char[] hSequence, vSequence; // horizontal sequence (columns), vertical sequence (rows), set as Default
        if(sequence1.length > sequence2.length) { length = numCol = sequence1.length; numRow = sequence2.length; hSequence = sequence1; vSequence = sequence2; }
        else { length = numCol = sequence2.length; numRow = sequence1.length; hSequence = sequence2; vSequence = sequence1; }

    //== initialize Scoring matrix
        MatrixCell[][] scoringMatrix = new MatrixCell[numCol +1][numRow +1];
        //initialize first row and first column with 0
        for(int i = 0; i < numCol + 1; i++) scoringMatrix[i][0] = new MatrixCell(0);
        for(int j = 0; j < numRow + 1; j++) scoringMatrix[0][j] = new MatrixCell(0);

    //== fill in scoring matrix
        for(int i = 1; i < numCol +1; i++){
            for(int j = 1; j < numRow +1; j++){
                int score = (hSequence[i-1] == vSequence[j-1])? MatchScore : MismatchScore;
                int diagonal = scoringMatrix[i-1][j-1].getScore() + score;
                int top = scoringMatrix[i][j-1].getScore() + GapPenalty;
                int left = scoringMatrix[i-1][j].getScore() + GapPenalty;

                //set Score
                scoringMatrix[i][j] = new MatrixCell(findMax(diagonal, top, left));
                // set Pointer
                int matrixCellScore = scoringMatrix[i][j].getScore();
                scoringMatrix[i][j].setPointer(matrixCellScore == diagonal, matrixCellScore == top, matrixCellScore == left);
            }
        }

    //== traceback (alignment)
        char[] newSequenceWithGaps = new char[hSequence.length];
        newSequenceWithGaps = traceBack(scoringMatrix, vSequence, vSequence.length -1, numCol, numRow, newSequenceWithGaps);

    //== get Score and Identity
        int alignmentScore = 0, matchCounter = 0;
        for(int i = 0; i < hSequence.length; i++){
            if(hSequence[i] == newSequenceWithGaps[i]) { alignmentScore += MatchScore; matchCounter++; }
            else if(hSequence[i] == ' ' || newSequenceWithGaps[i] == ' ') alignmentScore += GapPenalty;
            else alignmentScore += MismatchScore;
        }

        //if 2 sequences have the same length, check whether gapping gives alignment higher score
        int alignmentScore2 = 0, matchCounter2 = 0;
        if(sequence1.length == sequence2.length){
            for(int i = 0; i < sequence1.length; i++){
                if(sequence1[i] == sequence2[i]) { alignmentScore2 += MatchScore; matchCounter2++; }
                else alignmentScore2 += MismatchScore;
            }
            if(alignmentScore2 > alignmentScore){
                alignmentScore = alignmentScore2;
                matchCounter = matchCounter2;
                gapCounter = 0;
            }
        }

        double percentage = (double)matchCounter/length*100;
        new Report(alignmentScore, matchCounter, length, percentage).printReport();
    }

    private class Report{
        int alignmentScore; int matchCounter; double percentage; int length;
        private Report(int alignmentScore, int matchCounter, int length, double percentage){
            this.alignmentScore = alignmentScore;
            this.matchCounter = matchCounter;
            this.length = length;
            this.percentage = percentage;
        }

        public void printReport(){
            // Display alignment score
            System.setOut(outToFile);
            System.out.println("Score of alignment: " + alignmentScore);
            //Display identical percentage
            System.out.println("Number of matches: " + matchCounter);
            System.out.println("Number of gaps: " + gapCounter);
            System.out.println("Length of alignment: " + length);
            System.out.println("Identity: " + String.format("%.2f", percentage) + "%");
            System.out.println("\n");
        }
    }
}