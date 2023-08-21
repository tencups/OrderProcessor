package processor;

import java.util.TreeMap;

public class Order {

	private int id;
	private TreeMap<String, Integer> purchases;

	public Order(int id, TreeMap<String, Integer> purchases) {

		this.id = id;
		this.purchases = purchases;
	}

	public int getID() {
		return id;
	}

	public TreeMap<String, Integer> getPurchases() {

		return purchases;
	}

}
