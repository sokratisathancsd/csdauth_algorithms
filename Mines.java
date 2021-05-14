import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.lang.Math;

public class Mines {
    private static ArrayList<ArrayList<Integer>> Mines = new ArrayList<ArrayList<Integer>>();
    private static ArrayList<ArrayList<Integer>> UpperHull = new ArrayList<ArrayList<Integer>>();
    private static ArrayList<ArrayList<Integer>> LowerHull = new ArrayList<ArrayList<Integer>>();

    public static void main(String args[]) throws IOException {
        //Read Data from file
        //File name is given from command line java Mines filename
        //Store Data in Mines ArrayList
        String[] tempArray;//use to Parse String
        BufferedReader reader;
        String filename=args[0];//"data.txt";
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                tempArray = line.split(" ");
                int x = Integer.parseInt(tempArray[0]);
                int y = Integer.parseInt(tempArray[1]);
                ArrayList<Integer> mine = new ArrayList<Integer>();
                mine.add(x);
                mine.add(y);
                Mines.add(mine);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Take StartPoint and EndPoint=Treasure
        ArrayList<Integer> StartPoint = Mines.remove(0);
        ArrayList<Integer> Treasure = Mines.remove(0);

        //add P1=leftMostPoint=startPoint and P2=rightMostPoint=treasure to UpperHull and LowerHull
        UpperHull.add(StartPoint);
        UpperHull.add(Treasure);
        LowerHull.add(StartPoint);
        LowerHull.add(Treasure);

        //add Points to S1--> possibleUpperHull and S2--> possibleLowerHull
        ArrayList<ArrayList<Integer>> S1 = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> S2 = new ArrayList<ArrayList<Integer>>();

        //Determinant Calculation
        //If det > 0 means point is in left side, else if det < 0 point is in right side
        //distance = det
        for (ArrayList<Integer> mine : Mines) {
            int distance = GetDeterminant(StartPoint, Treasure, mine);
            if (distance > 0) {
                S1.add(mine);
            } else if (distance < 0) {
                S2.add(mine);
            }
        }
        //ArrayList UpperHull and LowerHull are in the format [[P1x, P1y], [P2x, P2y], [P3x, P3y, detValue], ....
        FindHull(S1, StartPoint, Treasure, "U");
        FindHull(S2, StartPoint, Treasure, "L");
        //System.out.println(UpperHull);
        //System.out.println(LowerHull);

        //Sorting Arraylists UpperHull and LowerHull using the first Element (sorting in x-axis)
        ArrayList<ArrayList<Integer>> sortedUpperHull = new ArrayList<>(UpperHull);
        sortedUpperHull.sort((l1, l2) -> l1.get(0).compareTo(l2.get(0)));
        ArrayList<ArrayList<Integer>> sortedLowerHull = new ArrayList<>(LowerHull);
        sortedLowerHull.sort((l1, l2) -> l1.get(0).compareTo(l2.get(0)));

        //Find Shortest Path
        double upperDistance = 0;
        double lowerDistance = 0;
        for (int i = 0; i < sortedUpperHull.size() - 1; i++) {
            upperDistance += EuclideanDistance(sortedUpperHull.get(i), sortedUpperHull.get(i + 1));
        }
        for (int i = 0; i < sortedLowerHull.size() - 1; i++) {
            lowerDistance += EuclideanDistance(sortedLowerHull.get(i), sortedLowerHull.get(i + 1));
        }
        //Print the output Data
        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.HALF_UP);
        if (upperDistance <= lowerDistance) {
            System.out.print("The shortest distance is ");
            System.out.println(df.format(upperDistance));
            System.out.print("The shortest path is:");
            for (ArrayList<Integer> mine : sortedUpperHull) {
                if(mine==Treasure){
                    System.out.println("("+mine.get(0)+","+mine.get(1)+")");
                    continue;
                }
                System.out.print("("+mine.get(0)+","+mine.get(1)+")-->");
            }
        } else {
            System.out.print("The shortest distance is ");
            System.out.println(df.format(lowerDistance));
            System.out.print("The shortest path is:");
            for (ArrayList<Integer> mine : sortedLowerHull) {
                if(mine==Treasure){
                    System.out.println("("+mine.get(0)+","+mine.get(1)+")");
                    continue;
                }
                System.out.print("("+mine.get(0)+","+mine.get(1)+")-->");
            }
        }

        /*
        //Print the Hull points to file
        BufferedWriter writer = new BufferedWriter(new FileWriter("dataout.txt"));
        for (ArrayList<Integer> tempMine : LowerHull) {
            writer.write(String.valueOf(tempMine.get(0))+" "+String.valueOf(tempMine.get(1)));
            writer.write("\n");
        }
        for (ArrayList<Integer> tempMine : UpperHull) {
            writer.write(String.valueOf(tempMine.get(0))+" "+String.valueOf(tempMine.get(1)));
            writer.write("\n");
        }

        writer.close();*/

    }

    static void FindHull(ArrayList<ArrayList<Integer>> S, ArrayList<Integer> P1, ArrayList<Integer> P2, String h) {
        if (S.isEmpty()) {
            return;
        }
        //From the given set of points in Sk, find farthest point, say C, from segment P1P2
        ArrayList<Integer> C = new ArrayList<>();
        int max = 0;
        for (ArrayList<Integer> mine : S) {
            int distance = GetDeterminant(P1, P2, mine);
            if (Math.abs(distance) > max) {
                max = Math.abs(distance);
                C = mine;
            }
            else if (Math.abs(distance) == max && getAngle(P1, mine, P2) > getAngle(P1, C, P2)) {
                max = Math.abs(distance);
                C = mine;
            }
        }

        //Add point C to Upper or Lower hull
        if (h.equals("U")) {
            UpperHull.add(C);
        } else if (h.equals("L")) {
            LowerHull.add(C);
        }

        //Three points P1, P2, and C partition the remaining points of Sk into 3 subsets: S0, S1, and S2
        //where S0 are points inside triangle PCP2, S1 are points on the right side of the oriented
        //line from P to C, and S2 are points on the right side of the oriented line from C to P2.
        ArrayList<ArrayList<Integer>> S1 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> S2 = new ArrayList<>();
        for (ArrayList<Integer> tempMine : S) {
            int distance1 = GetDeterminant(P1, C, tempMine);
            int distance2 = GetDeterminant(P2, C, tempMine);
            if (h.equals("U")) {
                if (distance1 > 0 && tempMine.get(0) < C.get(0)) {
                    S1.add(tempMine);
                } else if (distance2 < 0 && tempMine.get(0) > C.get(0)) {
                    S2.add(tempMine);
                }
            } else if (h.equals("L")) {
                if (distance1 < 0 && tempMine.get(0) < C.get(0)) {
                    S1.add(tempMine);
                } else if (distance2 > 0 && tempMine.get(0) > C.get(0)) {
                    S2.add(tempMine);
                }
            }
        }
        FindHull(S1, P1, C, h);
        FindHull(S2, C, P2, h);
    }

    static int GetDeterminant(ArrayList<Integer> A, ArrayList<Integer> B, ArrayList<Integer> Point) {
        int distance = A.get(0) * B.get(1) + Point.get(0) * A.get(1) + Point.get(1) * B.get(0) - Point.get(0) * B.get(1) - Point.get(1) * A.get(0) - A.get(1) * B.get(0);
        return distance;
    }

    static double EuclideanDistance(ArrayList<Integer> A, ArrayList<Integer> B) {
        return Math.sqrt((B.get(1) - A.get(1)) * (B.get(1) - A.get(1)) + (B.get(0) - A.get(0)) * (B.get(0) - A.get(0)));
    }
    static double getAngle(ArrayList<Integer> A, ArrayList<Integer> B, ArrayList<Integer> C){
        double a =  EuclideanDistance(A,B);
        double b =  EuclideanDistance(B,C);
        double c =  EuclideanDistance(C,A);
        double betta =  Math.acos((a*a + b*b - c*c)/2*a*b);
        betta =  (betta*180)/Math.PI;
        return  betta;
    }
}


