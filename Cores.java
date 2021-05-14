import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.lang.Math;

public class Cores {
    /**
     * @param Clients is an ArrayList Clients[Number of Cores Client wants][Price offered for 1 core]
     * @param cores maximum available number of cores (first line of input data)
     * @param VM available packages of Virtual Machines (in this implementation we have 11, 7, 2 and 1)
     */
    private static ArrayList<ArrayList<Float>> Clients = new ArrayList<ArrayList<Float>>();
    private static int cores;
    private static ArrayList<Integer> VM = new ArrayList<Integer>(Arrays.asList(11, 7, 2, 1)); // Virtual Machines available with 11, 7, 2 or 1 core(s)

    /**
     * @param args in the first cell of args matrix we have the input data
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        /**
         * @param maxNumOfCores is the maximum number of cores a client is requesting
         */
        //Read Data from file
        //File name is given from command line java Mines filename
        String[] tempArray;//use to Parse String
        BufferedReader reader;
        String filename = args[0];//"data.txt";
        int maxNumOfCores = Integer.MIN_VALUE;
        try {
            reader = new BufferedReader(new FileReader(filename));
            cores = Integer.parseInt(reader.readLine());
            String line = reader.readLine();
            while (line != null) {
                tempArray = line.split(" ");
                float numOfCores = Float.parseFloat(tempArray[0]);
                float pricePerCore = Float.parseFloat(tempArray[1]);
                ArrayList<Float> tempClient = new ArrayList<Float>();
                tempClient.add(numOfCores);
                tempClient.add(pricePerCore);
                Clients.add(tempClient);
                if (numOfCores > maxNumOfCores) {
                    maxNumOfCores = (int) numOfCores;
                }

                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println(Clients);
        //System.out.println(maxNumOfCores);

        /**
         * @param DPTable is the Dynamic Programming Table we use to find the minimum number
         * of Virtual Machines (VMs) we need to cover each clients request
         */
        ArrayList<Integer> DPTable = new ArrayList<Integer>(maxNumOfCores); //Dynamic Programming Table
        DPTable.add(0);
        for (int i = 1; i <= maxNumOfCores; i++) DPTable.add(Integer.MAX_VALUE);
        for (int i = 1; i <= maxNumOfCores; i++) {
            for (int j = 0; j < VM.size(); j++) {
                if (VM.get(j) <= i) {
                    int vms_min = DPTable.get(i - VM.get(j));
                    if (vms_min + 1 < DPTable.get(i)) {
                        DPTable.set(i, vms_min + 1);
                    }
                }
            }
        }
        int clientPos = 1; //to print client's id on output
        for (ArrayList<Float> client : Clients) {
            int vms = Math.round(client.get(0));
            System.out.println("Client " + clientPos + ": " + DPTable.get(vms) + " VMs");
            clientPos++;
        }
        //System.out.println(DPTable);

        /**
         * @param Weights[1...n] has the weight of each value
         */
        ArrayList<Integer> Weights = new ArrayList<>();
        for (int i = 0; i < Clients.size(); i++) {
            Weights.add(Math.round(Clients.get(i).get(0)));
        }
        //System.out.println(Weights);

        /**
         * @param Values[1...n] has the value of each request value=weight*offer
         */
        ArrayList<Float> Values = new ArrayList<>();
        for (int i = 0; i < Clients.size(); i++) {
            Values.add(roundFunction(Clients.get(i).get(1) * Weights.get(i)));
        }
        //System.out.println(Values);

        /**
         * @param Knapsack-->V[0...n][0...W] is a DP table
         */
        ArrayList<ArrayList<Float>> Knapsack = new ArrayList<>();
        Knapsack.add(new ArrayList<>());
        for (int j = 0; j <= cores; j++) {
            Knapsack.get(0).add(j, (float) 0);
        }
        for (int i = 1; i <= Clients.size(); i++) {
            Knapsack.add(new ArrayList<Float>());
            Knapsack.get(i).add(0, (float) 0);
            for (int j = 1; j <= cores; j++) {
                Knapsack.get(i).add(j, (float) -1);
            }
        }
        //System.out.println(Knapsack);

        /**
         * Knapsack Algorithm Implementation
         */
        float value;
        for (int i = 1; i <= Clients.size(); i++) {
            for (int j = 1; j <= cores; j++) {
                if (Knapsack.get(i).get(j) < 0) {
                    if (j < Weights.get(i - 1)) {
                        value = Knapsack.get(i - 1).get(j);
                    } else {
                        value = Math.max(Knapsack.get(i - 1).get(j),
                                Values.get(i - 1) + Knapsack.get(i - 1).get(j - Weights.get(i - 1)));
                    }
                    Knapsack.get(i).set(j, value);
                }
            }
        }
        //System.out.println(Knapsack);
        System.out.println("Total amount: " + Knapsack.get(Clients.size()).get(cores));

    }

    /**
     *
     * @param aNum number to be rounded
     * @return rounded number to 3 decimals places
     */
    static float roundFunction(Float aNum) {
        float toReturn = 0;
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);
        float d = aNum.floatValue();
        toReturn = Float.parseFloat(df.format(d));

        return toReturn;
    }

}
