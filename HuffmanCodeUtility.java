/* C0ded by Armin Fallah #9444333123
 * Huffman Code implementation + permutation
 * Note that here for simplicity weight = frequency
 */

import java.io.*;
import java.util.*;

public class HuffmanCodeUtility {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String l;
        HuffmanCode hc = new HuffmanCode();

        System.out.print("~Welcome to HuffmanCodeUtility 2.0~\nCommands: buildtree [filepath]\t|\tloadtree [filepath]\t|\tprintcodes\t|\tencode [filepath]\t|\tdecode [filepath]\t|\tpermutate\t|\texit\n");
        while (true){
            l = s.nextLine();
            if(l.equals("exit")) break;
            else if (l.length() > 9 && l.substring(0,9).equals("buildtree")) {
                hc.buildTree(l.substring(10));
                System.out.println("Huffman Tree built successfully");
            }
            else if (l.equals("permutate")) System.out.println("Key String : " + hc.Permutate());
            else if (l.equals("printcodes")) {
                Map<Character,String> m = hc.CodeTable();
                for(Map.Entry<Character,String> en: m.entrySet()) System.out.println(en.getKey() + "\t" + en.getValue());
            }else if(l.length() > 6 && l.substring(0,6).equals("encode")){
                hc.Encode(l.substring(7));
                System.out.println("File Encoded successfully");
            }
            else if(l.length() > 6 && l.substring(0,6).equals("decode")) {
                hc.Decode(l.substring(7));
                System.out.println("File Decoded successfully");
            }
            else if(l.length() > 8 && l.substring(0,8).equals("loadtree")) {
                hc.LoadTree(l.substring(9));
                System.out.println("Huffman tree loaded successfully");
            }
            else System.out.println("Command NOT recognized !");
        }
    }
}

class HuffmanCode implements Serializable{
    private Node root;
    private int iNodes;
    private int lNodes;
    private Map<Character,String> codeTable = new HashMap<>();
    private boolean[] permutation;
    private int counter;

    private class Node implements Serializable {
        int weight;
        transient Node left;
        transient Node right;
        char ch;
        boolean permutated;
        //internal node
        Node(int weight,Node left, Node right){
            this.weight = weight;
            this.left = left ;
            this.right = right;
        }
        //leaf node
        Node(int weight, char ch){
            this.weight = weight;
            this.ch = ch;
        }
    }

    private static class NodeComparator implements Comparator<Node>{
        @Override
        public int compare(Node n1,Node n2){ return n1.weight - n2.weight; }
    }

    void buildTree(String filepath){
        String str = fileContent(filepath);
        Map<Character,Integer> freqTable = new HashMap<>();
        //build frequency table by mapping each char to it's frequency
        for(char ch : str.toCharArray()){
            if(!freqTable.containsKey(ch)) freqTable.put(ch,1);
            else freqTable.replace(ch,freqTable.get(ch) + 1);
        }
        lNodes = freqTable.size();

        Queue<Node> pq = new PriorityQueue<>(new NodeComparator());
        //add each char in table as leaf nodes to priorityqueue for building the tree
        for(Map.Entry<Character,Integer> en : freqTable.entrySet()) pq.add(new Node(en.getValue(),en.getKey()));
        iNodes = 0;
        //build the huffman tree
        while(pq.size() > 1){
            Node t1 = pq.remove();
            Node t2 = pq.remove();
            pq.add(new Node(t1.weight + t2.weight,t1,t2));
            iNodes++; //counting internal nodes
        }
        root = pq.remove();
        permutation = new boolean[iNodes];
        //generate huffman code based on the tree
        codeTable.clear();
        generateCode(root,"");
        saveTree(filepath);
    }

    Map<Character,String> CodeTable() { return codeTable;}

    //traverse the tree to find code for each char
    private void generateCode(Node tree, String code){
        if(lNodes == 1) codeTable.put(root.ch,root.permutated ? "1" : "0");
        else if(lNodes > 0) {
            if(tree != null){
                //complete binary tree so no left/right means it's leaf node
                if(tree.left == null){
                    codeTable.put(tree.ch,code);
                }else{ //internal node
                    //traverse left
                    code+= tree.permutated ? "1" : "0";
                    generateCode(tree.left,code);
                    code = code.substring(0,code.length()-1);
                    //traverse right
                    code+= tree.permutated ? "0" : "1";
                    generateCode(tree.right,code);
                    code = code.substring(0,code.length()-1);
                }
            }
        }
    }

    String Permutate() {
        if(lNodes < 1) return null;
        String p = "";
        Random r = new Random();
        //generate random permutation
        for (int i = 0; i < iNodes; i++) {
            permutation[i] = r.nextBoolean();
            p += permutation[i] ? "1" : "0";
        }
        //set permutations
        counter = 0;
        changeP(root);
        //renew code table
        codeTable.clear();
        generateCode(root, "");

        return p;
    }

    //change permutation
    private void changeP (Node tree){
        if(counter < iNodes) {
            //permutation on internal nodes
            if (tree.left != null) {
                tree.permutated = permutation[counter++];
                changeP(tree.left);
                if(tree.right != null) changeP(tree.right);
            }
        }
    }

    void Encode(String filepath){
        String str = fileContent(filepath),s = "";
        for(char ch : str.toCharArray()) s = s.concat(codeTable.get(ch));
        try {
            FileOutputStream file = new FileOutputStream(filepath);
            ObjectOutputStream f = new ObjectOutputStream(file);
            f.writeObject(permutation);
            f.writeObject(s);
            f.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    void Decode(String filepath){
        String s = "",str = "";
        try {
            FileInputStream file = new FileInputStream(filepath);
            ObjectInputStream f = new ObjectInputStream(file);
            permutation = (boolean[]) f.readObject();
            str = (String) f.readObject();
            f.close();
        }catch (IOException | ClassNotFoundException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        counter = 0;
        changeP(root);

        Node t;
        int last = 0;
        for(int i = 0; i < (str.length() - 1) ;){
            t = root;
            while(t.left != null) { //internal node
                boolean left = str.charAt(i) == '0';
                left = t.permutated != left;
                t = left ? t.left : t.right;
                i++;
            }
            s += t.ch;
        }

        try {
            FileWriter f = new FileWriter(filepath);
            f.write(s);
            f.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void saveTree(String filepath){
        try{
            FileOutputStream o = new FileOutputStream(filepath.substring(0,filepath.length()-4) + ".hmt");
            ObjectOutputStream f = new ObjectOutputStream(o);
            Node[] t = new Node[iNodes + lNodes];
            counter = 0;
            preOrdered(root,t);
            f.writeObject(t);
            t = new Node[iNodes + lNodes];
            counter = 0;
            inOrdered(root, t);
            f.writeObject(t);
            f.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void inOrdered(Node tree, Node[] result){
        if (tree != null) {
            inOrdered(tree.left, result);
            result[counter++] = tree;
            inOrdered(tree.right, result);
        }
    }

    private void preOrdered(Node tree, Node[] result) {
        if (tree != null) {
            result[counter++] = tree;
            preOrdered(tree.left, result);
            preOrdered(tree.right, result);
        }
    }

    void LoadTree(String filepath) {
        if(!filepath.endsWith(".hmt")) return;
        try {
            FileInputStream file = new FileInputStream(filepath);
            ObjectInputStream f = new ObjectInputStream(file);
            Node[] preO = (Node[]) f.readObject();
            Node[] inO = (Node[]) f.readObject();
            f.close();
            counter = 0; //preorder index
            root = reBuildTree(preO,inO,0,inO.length-1);
        }catch (IOException | ClassNotFoundException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        //count iNodes & lNodes
        iNodes = 0;
        lNodes = 0;
        countNodes(root);
        //generate code table
        codeTable.clear();
        generateCode(root,"");
        permutation = new boolean[iNodes];
    }

    private Node reBuildTree(Node[] preO,Node[] inO, int start, int end){
        if(start > end) return null;

        Node tNode = preO[counter++];
        if(start == end) return tNode;

        //the node's index in InOrder
        int inOindex = end;
        for(int i = start; i<=end; i++) if(inO[i].equals(tNode)) inOindex = i;

        tNode.left = reBuildTree(preO,inO,start,inOindex-1);
        tNode.right = reBuildTree(preO,inO,inOindex+1,end);
        return tNode;
    }

    private String fileContent(String filepath){
        String str = "";
        try {
            FileReader f = new FileReader(filepath);
            int ch;
            while ((ch = f.read()) != -1) str += (char)ch;
            f.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        return str;
    }

    private void countNodes(Node tree){
        if(tree == null) return;

        if(tree.left != null){ //internal node
            iNodes++;
            countNodes(tree.left);
            countNodes(tree.right);
        }
        else lNodes++;
    }
}