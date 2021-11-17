import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.ArrayList;


/**
 * BPlusTree Class Assumptions: 1. No duplicate keys inserted 2. Order D:
 * D<=number of keys in a node <=2*D 3. All keys are non-negative
 * TODO: Rename to BPlusTree
 */
public class BPlussTree {

	public Nodee root;
	public static final int D = 2;

	/**
	 * TODO Search the value for a specific key
	 * 
	 * @param key
	 * @return value
	 */
	public String search(String key) {
		// Return if empty tree or key
		if(key == null || root == null) {
			return null;
		}
		// Look for leaf node that key is pointing to
		LeafNode leaf = (LeafNode)treeSearch(root, key);
					
		// Look for value in the leaf
		for(int i=0; i<leaf.keys.size(); i++) {
			if(key.compareTo(leaf.keys.get(i)) == 0) {
				return leaf.values.get(i);
			}
		}
					
		return null;
	}
	
	private Nodee treeSearch(Nodee node, String key) {
		if(node.isLeafNode) {
			return node;
		} 
		// The node is index node
		else {
			IndexNode index = (IndexNode)node;
			
			// K < K1, return treeSearch(P0, K)
			if(key.compareTo(index.keys.get(0)) < 0) {
				return treeSearch((Nodee)index.children.get(0), key);
			}
			// K >= Km, return treeSearch(Pm, K), m = #entries
			else if(key.compareTo(index.keys.get(node.keys.size()-1)) >= 0) {
				return treeSearch((Nodee)index.children.get(index.children.size()-1), key);
			}
			// Find i such that Ki <= K < K(i+1), return treeSearch(Pi,K)
			else {
				// Linear searching
				for(int i=0; i<index.keys.size()-1; i++) {
					if(key.compareTo(index.keys.get(i)) >= 0 && key.compareTo(index.keys.get(i+1)) < 0) {
						return treeSearch((Nodee)index.children.get(i+1), key);
					}
				}
 			}
			return null;
		}
	} 
	
	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public void insert(String key, String value) {
		LeafNode newLeaf = new LeafNode(key, value);
		Entry<String, Nodee> entry = new AbstractMap.SimpleEntry<String, Nodee>(key, newLeaf);
		
		// Insert entry into subtree with root node pointer
		if(root == null || root.keys.size() == 0) {
			root = entry.getValue();
		}
		
		// newChildEntry null initially, and null on return unless child is split
		Entry<String, Nodee> newChildEntry = getChildEntry(root, entry, null);
		
		if(newChildEntry == null) {
			return;
		} else {
			IndexNode newRoot = new IndexNode(newChildEntry.getKey(), root, 
					newChildEntry.getValue());
			root = newRoot;
			return;
		}
	}
	
	private Entry<String, Nodee> getChildEntry(Nodee node, Entry<String, Nodee> entry, 
			Entry<String, Nodee> newChildEntry) {
		if(!node.isLeafNode) {
			// Choose subtree, find i such that Ki <= entry's key value < J(i+1)
			IndexNode index = (IndexNode) node;
			int i = 0;
			while(i < index.keys.size()) {
				if(entry.getKey().compareTo(index.keys.get(i)) < 0) {
					break;
				}
				i++;
			}
			// Recursively, insert entry
			newChildEntry = getChildEntry((Nodee) index.children.get(i), entry, newChildEntry);
			
			// Usual case, didn't split child
			if(newChildEntry == null) {
				return null;
			} 
			// Split child case, must insert newChildEntry in node
			else {
				int j = 0;
				while (j < index.keys.size()) {
					if(newChildEntry.getKey().compareTo(index.keys.get(j)) < 0) {
						break;
					}
					j++;
				}
				
				index.insertSorted(newChildEntry, j);
				
				// Usual case, put newChildEntry on it, set newChildEntry to null, return
				if(!index.isOverflowed()) {
					return null;
				} 
				else{
					newChildEntry = splitIndexNode(index);
					
					// Root was just split
					if(index == root) {
						// Create new node and make tree's root-node pointer point to newRoot
						IndexNode newRoot = new IndexNode(newChildEntry.getKey(), root, 
								newChildEntry.getValue());
						root = newRoot;
						return null;
					}
					return newChildEntry;
				}
			}
		}
		// Node pointer is a leaf node
		else {
			LeafNode leaf = (LeafNode)node;
			LeafNode newLeaf = (LeafNode)entry.getValue();
			
			leaf.insertSorted(entry.getKey(), newLeaf.values.get(0));
			
			// Usual case: leaf has space, put entry and set newChildEntry to null and return
			if(!leaf.isOverflowed()) {
				return null;
			}
			// Once in a while, the leaf is full
			else {
				newChildEntry = splitLeafNode(leaf);
				if(leaf == root) {
					IndexNode newRoot = new IndexNode(newChildEntry.getKey(), leaf, 
							newChildEntry.getValue());
					root = newRoot;
					return null;
				}
				return newChildEntry;
			}
		}
	}

	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param leaf, any other relevant data
	 * @return the key/node pair as an Entry
	 */
	public Entry<String, Nodee> splitLeafNode(LeafNode leaf) {
		ArrayList<String> newKeys = new ArrayList<String>();
		ArrayList<String> newValues = new ArrayList<String>();
		
		// The rest D entries move to brand new node
		for(int i=D; i<=2*D; i++) {
			newKeys.add(leaf.keys.get(i));
			newValues.add(leaf.values.get(i));
		}
		
		// First D entries stay
		for(int i=D; i<=2*D; i++) {
			leaf.keys.remove(leaf.keys.size()-1);
			leaf.values.remove(leaf.values.size()-1);
		}
		
		String splitKey = newKeys.get(0);
		LeafNode rightNode = new LeafNode(newKeys, newValues);
		
		// Set sibling pointers
		LeafNode tmp = leaf.nextLeaf;
		leaf.nextLeaf = rightNode;
		leaf.nextLeaf.previousLeaf = rightNode;
		rightNode.previousLeaf = leaf;
		rightNode.nextLeaf = tmp;
        
		Entry<String, Nodee> newChildEntry = new AbstractMap.SimpleEntry<String, Nodee>(splitKey, rightNode);
		
		return newChildEntry;
	}

	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index, any other relevant data
	 * @return new key/node pair as an Entry
	 */
	public Entry<String, Nodee> splitIndexNode(IndexNode index) {
		ArrayList<String> newKeys = new ArrayList<String>();
		ArrayList<Nodee> newChildren = new ArrayList<Nodee>();
		
		// Note difference with splitting leaf page, 2D+1 key values and 2D+2 node pointers
		String splitKey = index.keys.get(D);
		index.keys.remove(D);
		
		// First D key values and D+1 node pointers stay
		// Last D keys and D+1 pointers move to new node
		newChildren.add(index.children.get(D+1));
		index.children.remove(D+1);
		
		while(index.keys.size() > D) {
			newKeys.add(index.keys.get(D));
			index.keys.remove(D);
			newChildren.add(index.children.get(D+1));
			index.children.remove(D+1);
		}

		IndexNode rightNode = new IndexNode(newKeys, newChildren);
		Entry<String, Nodee> newChildEntry = new AbstractMap.SimpleEntry<String, Nodee>(splitKey, rightNode);

		return newChildEntry;
	}
	
	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(String key) {
		if(key == null || root == null) {
			return;
		}

		// Check if entry key exist in the leaf node
		LeafNode leaf = (LeafNode)treeSearch(root, key);
		if(leaf == null) {
			return;
		}
		
		// Delete entry from subtree with root node pointer
		Entry<String, Nodee> entry = new AbstractMap.SimpleEntry<String, Nodee>(key, leaf);
		
		// oldChildEntry null initially, and null upon return unless child deleted
		Entry<String, Nodee> oldChildEntry = deleteChildEntry(root, root, entry, null);
		
		// Readjust the root, no child is deleted
		if(oldChildEntry == null) {
			if(root.keys.size() == 0) {
				if(!root.isLeafNode) {
					root = ((IndexNode) root).children.get(0);
				}
			}
			return;
		}
		// Child is deleted
		else {
			// Find empty node
			int i = 0;
			String oldKey = oldChildEntry.getKey();
			while(i < root.keys.size()) {
				if(oldKey.compareTo(root.keys.get(i)) == 0) {
					break;
				}
				i++;
			}
			// Return if empty node already discarded
			if(i == root.keys.size()) {
				return;
			}
			// Discard empty node
			root.keys.remove(i);
			((IndexNode)root).children.remove(i+1);
			return;
		}
	}
	
	private Entry<String, Nodee> deleteChildEntry(Nodee parentNode, Nodee node, 
			Entry<String, Nodee> entry, Entry<String, Nodee> oldChildEntry) {
		if(!node.isLeafNode) {
			// Choose subtree, find i such that Ki <= entry's key value < K(i+1)
			IndexNode index = (IndexNode)node;
			int i = 0;
			String entryKey = entry.getKey();
			while(i < index.keys.size()) {
				if(entryKey.compareTo(index.keys.get(i)) < 0) {
					break;
				}
				i++;
			}
			// Recursive delete
			oldChildEntry = deleteChildEntry(index, index.children.get(i), entry, oldChildEntry);
			
			// Usual case: child not deleted
			if(oldChildEntry == null) {
				return null;
			}
			// Discarded child node case
			else {
				int j = 0;
				String oldKey = oldChildEntry.getKey();
				while(j < index.keys.size()) {
					if(oldKey.compareTo(index.keys.get(j)) == 0) {
						break;
					}
					j++;
				}
				// Remove oldChildEntry from node
				index.keys.remove(j);
				index.children.remove(j+1);
				
				// Check for underflow, return null if empty
				if(!index.isUnderflowed() || index.keys.size() == 0) {
					// Node has entries to spare, delete doesn't go further
					return null; 
				}
				else {
					// Return if root
					if(index == root) {
						return oldChildEntry;
					}
					// Get sibling S using parent pointer
					int s = 0;
					String firstKey = index.keys.get(0);
					while(s < parentNode.keys.size()) {
						if(firstKey.compareTo(parentNode.keys.get(s)) < 0) {
							break;
						}
						s++;
					}
					// Handle index underflow
					int splitKeyPos;
					IndexNode parent = (IndexNode)parentNode;
					
					if(s > 0 && parent.children.get(s-1) != null) {
						splitKeyPos = handleIndexNodeUnderflow(
								(IndexNode)parent.children.get(s-1), index, parent);
					} else {
						splitKeyPos = handleIndexNodeUnderflow(
								index, (IndexNode)parent.children.get(s+1), parent);
					}
					// S has extra entries, set oldChildentry to null, return
					if(splitKeyPos == -1) {
						return null;
					}
					// Merge indexNode and S
					else {
						String parentKey = parentNode.keys.get(splitKeyPos);
						oldChildEntry = new AbstractMap.SimpleEntry<String, Nodee>(parentKey, parentNode);
						return oldChildEntry;
					}
				}
			}
		}
		// The node is a leaf node
		else {
			LeafNode leaf = (LeafNode)node;
			// Look for value to delete
			for(int i=0; i<leaf.keys.size(); i++) {
				if(leaf.keys.get(i) == entry.getKey()) {
					leaf.keys.remove(i);
					leaf.values.remove(i);
					break;
				}
			}
			// Usual case: no underflow
			if(!leaf.isUnderflowed()) {
				return null;
			}
			// Once in a while, the leaf becomes underflow
			else {
				// Return if root
				if(leaf == root || leaf.keys.size() == 0) {
					return oldChildEntry;
				}
				// Handle leaf underflow
				int splitKeyPos;
				String firstKey = leaf.keys.get(0);
				String parentKey = parentNode.keys.get(0);
				
				if(leaf.previousLeaf != null && firstKey.compareTo(parentKey) >= 0) {
					splitKeyPos = handleLeafNodeUnderflow(leaf.previousLeaf, leaf, (IndexNode)parentNode);
				} else {
					splitKeyPos = handleLeafNodeUnderflow(leaf, leaf.nextLeaf, (IndexNode)parentNode);
				}
				// S has extra entries, set oldChildEntry to null, return
				if(splitKeyPos == -1) {
					return null;
				} 
				// Merge leaf and S
				else {
					parentKey = parentNode.keys.get(splitKeyPos);
					oldChildEntry = new AbstractMap.SimpleEntry<String, Nodee>(parentKey, parentNode);
					return oldChildEntry;
				}	
			}
		}
	}

	/**
	 * TODO Handle LeafNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleLeafNodeUnderflow(LeafNode left, LeafNode right,
			IndexNode parent) {
		// Find entry in parent for node on right
		int i = 0;
		String rKey = right.keys.get(0);
		while(i < parent.keys.size()) {
			if(rKey.compareTo(parent.keys.get(i)) < 0) {
				break;
			}
			i++;
		}	
		// Redistribute evenly between right and left nodes
		// If S has extra entries
		if(left.keys.size() + right.keys.size() >= 2*D) {
			// Left node has more entries
			if(left.keys.size() > right.keys.size()) {
				while(left.keys.size() > D) {
					right.keys.add(0, left.keys.get(left.keys.size()-1));
					right.values.add(0, left.values.get(left.keys.size()-1));
					left.keys.remove(left.keys.size()-1);
					left.values.remove(left.values.size()-1);
				}
			}
			// Right node has more entries
			else {
				while(left.keys.size() < D) {
					left.keys.add(right.keys.get(0));
					left.values.add(right.values.get(0));
					right.keys.remove(0);
					right.values.remove(0);
				}
			}
			// Replace key value in parent entry by low-key in right node
			parent.keys.set(i-1, right.keys.get(0));
			
			return -1;
		}
		// No extra entries, return splitKeyPos
		else {
			// Move all entries from right to left node
			while(right.keys.size() > 0) {
				left.keys.add(right.keys.get(0));
				left.values.add(right.values.get(0));
				right.keys.remove(0);
				right.values.remove(0);
			}
			// Adjust sibling pointers
			if(right.nextLeaf != null) {
				right.nextLeaf.previousLeaf = left;
			}
			left.nextLeaf = right.nextLeaf;
			
			return i-1;
		}
	}
	
	/**
	 * TODO Handle IndexNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleIndexNodeUnderflow(IndexNode leftIndex,
			IndexNode rightIndex, IndexNode parent) {
		// Find entry in parent for node on right
		int i = 0;
		String rKey = rightIndex.keys.get(0);
		while(i < parent.keys.size()) {
			if(rKey.compareTo(parent.keys.get(i)) < 0) {
				break;
			}
			i++;
		}
		// Redistribute evenly between node and S through parent
		// If S has extra entries
		if(leftIndex.keys.size() + rightIndex.keys.size() >= 2*D) {
			// Left node has more entries
			if(leftIndex.keys.size() > rightIndex.keys.size()) {
				while(leftIndex.keys.size() > D) {
					rightIndex.keys.add(0, parent.keys.get(i-1));
					rightIndex.children.add(leftIndex.children.get(leftIndex.children.size()-1));
					parent.keys.set(i-1, leftIndex.keys.get(leftIndex.keys.size()-1));
					leftIndex.keys.remove(leftIndex.keys.size()-1);
					leftIndex.children.remove(leftIndex.children.size()-1);
				}
			}
			// Right node has more entries
			else {
				while(leftIndex.keys.size() < D) {
					leftIndex.keys.add(parent.keys.get(i-1));
					leftIndex.children.add(rightIndex.children.get(0));
					parent.keys.set(i-1, rightIndex.keys.get(0));
					rightIndex.keys.remove(0);
					rightIndex.children.remove(0);
				}
			}
			return -1;
		}
		// No extra entries, return spiltKeyPos
		else {
			leftIndex.keys.add(parent.keys.get(i-1));
			// Move all entries from right to left node
			while(rightIndex.keys.size() > 0) {
				leftIndex.keys.add(rightIndex.keys.get(0));
				leftIndex.children.add(rightIndex.children.get(0));
				rightIndex.keys.remove(0);
				rightIndex.children.remove(0);
			}
			leftIndex.children.add(rightIndex.children.get(0));
			rightIndex.children.remove(0);
			
			return i-1;
		}
	}

    public void printTree(Nodee parent) {
        for(Nodee n : ((IndexNode) root).getChildren()) {
            
        }
    }
}