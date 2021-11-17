import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class IndexNode extends Nodee {

	// m nodes
	protected ArrayList<Nodee> children; // m+1 children

	public IndexNode(String key, Nodee child0, Nodee child1) {
		isLeafNode = false;
		keys = new ArrayList<String>();
		keys.add(key);
		children = new ArrayList<Nodee>();
		children.add(child0);
		children.add(child1);
	}

	public IndexNode(List<String> newKeys, List<Nodee> newChildren) {
		isLeafNode = false;

		keys = new ArrayList<String>(newKeys);
		children = new ArrayList<Nodee>(newChildren);
	}

    public ArrayList<Nodee> getChildren() {
        return this.children;
    }

	/**
	 * insert the entry into this node at the specified index so that it still
	 * remains sorted
	 * 
	 * @param e
	 * @param index
	 */
	public void insertSorted(Entry<String, Nodee> e, int index) {
		String key = e.getKey();
		Nodee child = e.getValue();
		if (index >= keys.size()) {
			keys.add(key);
			children.add(child);
		} else {
			keys.add(index, key);
			children.add(index+1, child);
		}
	}

}
