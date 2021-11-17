import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class WordFrequency {
    
private static Scanner kb = new Scanner(System.in);
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File(args[0]);
        BPlusTree tree = new BPlusTree();
        Scanner scan = new Scanner(file);
        while(scan.hasNext()) {
            String data = scan.next();
            tree.insert(data, data);
        }
        scan.close();
        displayOptions(tree);
    }

    public static void displayOptions(BPlusTree tree) {
        int choice = 0;
        while(choice != -1) {
            System.out.println("\nPlease select from the following:\n" +
            "1) Print out all words in the B+ tree in alphabetical order\n" +
            "2) Display the B+ tree\n" +
            "3) Select a node to display\n" +
            "4) Insert a word\n" +
            "5) Search a word\n" +
            "6) Range search\n" +
            "-1) Quit\n");

            choice = kb.nextInt();

            switch(choice) {
                case 1:
                    tree.printTree();
                    break;
                case 2:
                    tree.displayTree();
                    break;
                case 3:
                    goSelectNode(tree);
                    break;
                case 4:
                    goInsert(tree);
                    break;
                case 5:
                    goFind(tree);
                    break;
                case 6:
                    goRangeSearch(tree);
                    break;
            }
        }
    }

    public static void goInsert(BPlusTree tree) {
        System.out.println("Enter the word you want to insert: ");
        String word = kb.next();
        tree.insert(word, word);
    }

    public static void goFind(BPlusTree tree) {
        System.out.println("Enter the word you want to search: ");
        String word = kb.next();
        int frequency = tree.find(word);
        System.out.println("word: " + word + "\nfrequency: " + frequency);
    }

    public static void goRangeSearch(BPlusTree tree) {
        System.out.println("Enter the first word of the range search: ");;
        String word1 = kb.next();
        System.out.println("Enter the second word of the range search: ");
        String word2 = kb.next();
        System.out.println("\n");
        tree.rangeSearch(word1, word2);
    }

    public static void goSelectNode(BPlusTree tree) {
        System.out.println("Enter the ID of the node you want to display: ");
        int ID = kb.nextInt();
        tree.selectNode(ID);
    }
}