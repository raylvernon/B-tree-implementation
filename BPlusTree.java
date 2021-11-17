import java.util.ArrayList;
import java.util.Collections;

public class BPlusTree
{
    public static final int D = 3;
    private Node root;
    private Node nodeFound = null;
    private ArrayList<String> allKeys = new ArrayList();
    private int ID_num = 1;

    public BPlusTree() {
        root = new LeafNode();
        root.ID = ID_num;
        ID_num++;
    }

    public void insert(String key, String value) {
    allKeys.add(key);
	Split result = root.insert(key, value);
        if (result != null) {
	    //The old root was split into two parts.
	    //create a new root pointing to them
            IndexNode _root = new IndexNode();
            _root.num = 1;
            _root.keys[0] = result.key;
            _root.children[0] = result.left;
            _root.children[1] = result.right;
            root = _root;
        }
    }


    //search for a key and if found then return the frequency
    public int find(String key) {
        Node node = root;
        while (node instanceof BPlusTree.IndexNode) { //need to traverse down to the leaf
            IndexNode inner = (IndexNode) node;
            int idx = inner.getLoc(key);
            node = inner.children[idx];
        }

        //after the while loop gets us the leaf
        LeafNode leaf = (LeafNode) node;
        int idx = leaf.getLoc(key);
        if (idx < leaf.num && leaf.keys[idx].equals(key)) {
	    return leaf.countFrequency(key);
        } else {
	    return 0;
        }
    }

    public void displayTree() {
        displayTree(root, "");
    }

    public void displayTree(Node node, String indentation) {
        System.out.println(indentation + node.ID);
        if(node instanceof BPlusTree.IndexNode) {
            IndexNode newNode = (IndexNode)node;
            ArrayList<Node> LChildren = getLChildren(newNode);
            for(Node n : LChildren) {
                displayTree(n, indentation + "\t");
            }
        }
    }

    public void printTree() {
        Collections.sort(allKeys);
        for(String key : allKeys) {
            System.out.println(key);
        }
    }

    public void rangeSearch(String word1, String word2) {
        Collections.sort(allKeys);
        for (int i = 0; i < allKeys.size(); i++) {
            if(word1.equals(allKeys.get(i))) {
                do {
                    System.out.println(allKeys.get(i));
                    i++;
                } while(!allKeys.get(i).equals(word2));
                System.out.println(allKeys.get(i));
                break;
            }
        }
    }

    public void selectNode(int ID) {
        Node node = selectNode(ID, root);
        printNode(node);
    }

    public Node selectNode(int ID, Node node) {
        if(ID == node.ID) {
            nodeFound = node;
        }
        if(node instanceof BPlusTree.IndexNode) {
            IndexNode newNode = (IndexNode) node;
            ArrayList<Node> LChildren = getLChildren(newNode);
            LChildren.trimToSize();
            for (int i = 0; i < LChildren.size(); i++) {
                if(nodeFound != null)
                    break;
                Node child = LChildren.get(i);
                selectNode(ID, child);
            }
        }
        return nodeFound;
    }

    public ArrayList<Node> getLChildren(IndexNode node) {
        ArrayList<Node> LChildren = new ArrayList<>();
        for (int i = 0; i < node.children.length; i++) {
            if(node.children[i] != null) {
                LChildren.add(node.children[i]);
            }
        }
        return LChildren;
    }

    public void printNode(Node node) {
        if(node != null) {
            System.out.println("\nContents for node with ID " + node.ID + ":");
            for (int i = 0; i < node.keys.length; i++) {
                if(node.keys[i] != null) {
                    System.out.println(node.keys[i]);
                }
            }
        }
        nodeFound = null;
    }

    abstract class Node {
    protected int ID;
	protected int num; //number of keys
	protected String[] keys;

	abstract public int getLoc(String key);
	abstract public Split insert(String key, String value);
    }

    class LeafNode extends Node {
	private String[] values = new String[D];
	{ keys = new String[D]; }

	
	// Returns the position where 'key' should be inserted in a leaf node
	// that has the given keys.
	public int getLoc(String key) {
	    for (int i = 0; i < num; i++) {
		if (keys[i].compareTo(key) >= 0) {
		    return i;
		}
	    }
	    return num;
	}

    public int countFrequency(String key) {
        int count = 0;
        for (int i = 0; i < allKeys.size(); i++) {
            if(allKeys.get(i).equals(key)) {
                count++;
            }
        }
        return count;
    }

	public Split insert(String key, String value) {
	    int i = getLoc(key);
	    if (this.num == D) { // The node was full. We must split it
            int mid = (D+1)/2;
            int sNum = this.num - mid;
            LeafNode sibling = new LeafNode();
            sibling.ID = ID_num;
            ID_num++;
            sibling.num = sNum;
            System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
            System.arraycopy(this.values, mid, sibling.values, 0, sNum);
            this.num = mid;
            if (i < mid) {
                this.insertNonfull(key, value, i);
            } else {
                sibling.insertNonfull(key, value, i-mid);
            }
            Split result = new Split(sibling.keys[0],
                        this,
                        sibling);
            return result;
	    } else {
		// The node was not full
		this.insertNonfull(key, value, i);
		return null;
	    }
	}

	private void insertNonfull(String key, String value, int idx) {
	    if (idx < num && keys[idx].equals(key)) {
		values[idx] = value;
	    } else {
		// The key we are inserting is unique
		System.arraycopy(keys, idx, keys, idx+1, num-idx);
		System.arraycopy(values, idx, values, idx+1, num-idx);

		keys[idx] = key;
		values[idx] = value;
		num++;
	    }
	}
    }

    class IndexNode extends Node {
	final Node[] children = new BPlusTree.Node[D+1];
	{ keys = new String[D]; }

	public int getLoc(String key) {
	    for (int i = 0; i < num; i++) {
		if (keys[i].compareTo(key) > 0) {
		    return i;
		}
	    }
	    return num;
	}

	public Split insert(String key, String value) {
	    if (this.num == D) {
		int mid = (D+1)/2;
		int sNum = this.num - mid;
		IndexNode sibling = new IndexNode();
		sibling.num = sNum;
        sibling.ID = ID_num;
        ID_num++;
		System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
		System.arraycopy(this.children, mid, sibling.children, 0, sNum+1);

		this.num = mid-1;

		// Set up the return variable
		Split result = new Split(this.keys[mid-1],
					 this,
					 sibling);

		// Now insert in the appropriate sibling
		if (key.compareTo(result.key) < 0) {
		    this.insertNonfull(key, value);
		} else {
		    sibling.insertNonfull(key, value);
		}
		return result;

	    } else {
		this.insertNonfull(key, value);
		return null;
	    }
	}

	private void insertNonfull(String key, String value) {
	    int idx = getLoc(key);
	    Split result = children[idx].insert(key, value);

	    if (result != null) {
		if (idx == num) {
		    keys[idx] = result.key;
		    children[idx] = result.left;
		    children[idx+1] = result.right;
		    num++;
		} else {
		    System.arraycopy(keys, idx, keys, idx+1, num-idx);
		    System.arraycopy(children, idx, children, idx+1, num-idx+1);

		    children[idx] = result.left;
		    children[idx+1] = result.right;
		    keys[idx] = result.key;
		    num++;
		    }
	    } // else the current node is not affected
	}
    }

    class Split {
	public final String key;
	public final Node left;
	public final Node right;

        public Split(String k, Node l, Node r) {
            key = k; left = l; right = r;
        }
    }
}