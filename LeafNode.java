import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class LeafNode extends Nodee {
	protected ArrayList<String> values;
	protected LeafNode nextLeaf;
	protected LeafNode previousLeaf;

	public LeafNode(String firstKey, String firstValue) {
		isLeafNode = true;
		keys = new ArrayList<String>();
		values = new ArrayList<String>();
		keys.add(firstKey);
		values.add(firstValue);

	}

	public LeafNode(List<String> newKeys, List<String> newValues) {
		isLeafNode = true;
		keys = new ArrayList<String>(newKeys);
		values = new ArrayList<String>(newValues);

	}

	/**
	 * insert key/value into this node so that it still remains sorted
	 * 
	 * @param key
	 * @param value
	 */
	public void insertSorted(String key, String value) {
		if (key.compareTo(keys.get(0)) < 0) {
			keys.add(0, key);
			values.add(0, value);
		} else if (key.compareTo(keys.get(keys.size() - 1)) > 0) {
			keys.add(key);
			values.add(value);
		} else {
			ListIterator<String> iterator = keys.listIterator();
			while (iterator.hasNext()) {
				if (iterator.next().compareTo(key) > 0) {
					int position = iterator.previousIndex();
					keys.add(position, key);
					values.add(position, value);
					break;
				}
			}

		}
	}

    public void printValues() {
        for (int i = 0; i < values.size(); i++) {
            System.out.println(values.get(i));
        }
    }

}