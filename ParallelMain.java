
import java.io.*;
import java.util.*;
import java.util.concurrent.*;


class Node {
    int freq;
    char letter;
    Node left;
    Node right;
}

class MyComparator implements Comparator<Node> {
    public int compare(Node x, Node y) {
        return x.freq - y.freq;
    }
}

public class ParallelMain {

    public static Map<Character, Integer> frequencyTable(String fileName) throws FileNotFoundException, IOException {
        Map<Character, Integer> freqTable = new HashMap<Character, Integer>();
        FileReader fr = new FileReader(new File(fileName));
        BufferedReader br = new BufferedReader(fr);
        List<Character> entryString = new ArrayList<Character>();
        
        int a = 0;
        while ((a = br.read()) != -1) {
            char character = (char) a;          //converting integer to char
            freqTable.putIfAbsent(character, 0);
            freqTable.put(character, freqTable.get(character) + 1);
        }
        fr.close();
        br.close();

        List<Map.Entry<Character, Integer>> list = new LinkedList<Map.Entry<Character, Integer>>(freqTable.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Character, Integer>>() {
            public int compare(Map.Entry<Character, Integer> o1, Map.Entry<Character, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        HashMap<Character, Integer> temp = new LinkedHashMap<Character, Integer>();
        for (Map.Entry<Character, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;

    }

    public static Map<Character, String> storeCode(Node root, String s, Map<Character, String> binTable) {

        if (root.left == null && root.right == null && root.letter != '*') {
            binTable.put(root.letter, s);
            return binTable;
        }
        storeCode(root.left, s + "0", binTable);
        storeCode(root.right, s + "1", binTable);
        return binTable;
    }


    public static Map<Character, String> codingTable(Map<Character, Integer> map) {

        int n = map.keySet().toArray().length;
        Map<Character, String> binaryTable = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<Node>(n, new MyComparator());

        for (char let : map.keySet()) {
            int num = map.get(let);
            Node node = new Node();
            node.letter = let;
            node.freq = num;
            node.left = null;
            node.right = null;
            pq.add(node);
        }
        Node root = null;
        while (pq.size() > 1) {
            Node first = pq.peek();
            pq.poll();
            Node second = pq.peek();
            pq.poll();
            Node comb = new Node();
            comb.freq = first.freq + second.freq;
            comb.letter = '*';
            comb.left = first;
            comb.right = second;
            root = comb;
            pq.add(comb);
        }
        binaryTable = storeCode(root, "", binaryTable);
        return binaryTable;
    }

    static class CompressTask implements Callable<String> {

        private Map<Character,String> binaryMap;
        private char c;
        public String binaryValue;

        public CompressTask(char c, Map<Character, String> binaryMap) {
            this.c = c;
            this.binaryMap = binaryMap;
        }

        @Override
        public String call() throws Exception {
            return binaryMap.get(c);
        }
    }


    public static void huffmanCompress(String fileName, Map<Character, String> binaryMap) throws IOException, Exception {

        FileReader fr = new FileReader(new File(fileName));
        BufferedReader br = new BufferedReader(fr);
        ArrayList<Character> compressedBinary = new ArrayList<Character>();
        ArrayList<String> stringArray = new ArrayList<>(); //binary
        ArrayList<Character> strList = new ArrayList<Character>();
        ArrayList<Character> storeList = new ArrayList<Character>();

        int c = 0;
        while ((c = br.read()) != -1) {
            char character = (char) c;          //converting integer to char
            storeList.add(character);
        }

        ExecutorService service = Executors.newCachedThreadPool();
        Future[] allFuture = new Future[storeList.size()];

        for (int i=0; i < storeList.size(); i++) {
            Future<String> future = service.submit(new CompressTask(storeList.get(i),binaryMap));
            allFuture[i] = future;
        }

        for (int i=0; i < allFuture.length; i++) {
            stringArray.add((String) allFuture[i].get());
        }

        

        stringArray.removeAll(Collections.singleton(null));
        for(String i : stringArray) {
            char[] temporary = i.toCharArray();
            for(char j :temporary) {
                compressedBinary.add(j);
            }
        }
        String binString = "";
        int counter = 0;
        for(char i : compressedBinary) {
            if(counter < 8) {
                String s = String.valueOf(i);
                binString += s;
                counter++;
            }
            else {
                int decimal=Integer.parseInt(binString,2);
                char str = (char)decimal;
                strList.add(str);
                binString = String.valueOf(i);
                counter=1;
            }
        }

        //writing to text file
        File file = new File("compressedParallel.txt");
        if (file.createNewFile())
        {
            System.out.println("File is created!");
        } else {
            System.out.println("File already exists.");
        }
        FileWriter writer = new FileWriter(file);

        for(char i : strList) {
            writer.write(i);
        }
        writer.close();
        }

        public static void main (String[]args) throws IOException, Exception {

            long start = System.currentTimeMillis();
            String fileToComp = "constitution.txt";
            Map<Character, Integer> map = frequencyTable(fileToComp);
            Map<Character, String> binaryCoding = codingTable(map);
            huffmanCompress(fileToComp, binaryCoding);
            long end = System.currentTimeMillis();
            System.out.println("Time of execution: " + (end-start) + " millisecond");

        }

}


