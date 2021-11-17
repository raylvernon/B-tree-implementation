import java.util.ArrayList;

public class Nodee implements Comparable<String> {
	protected boolean isLeafNode;
	protected ArrayList<String> keys;

	public boolean isOverflowed() {
		return keys.size() > 2 * BPlussTree.D;
	}

	public boolean isUnderflowed() {
		return keys.size() < BPlussTree.D;
	}

    @Override
    public int compareTo(String o) {
        // TODO Auto-generated method stub
        return 0;
    }

}