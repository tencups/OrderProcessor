package processor;

public class Item {
	private String name;
	private double cost;

	public Item(String name, double cost) {
		this.name = name;
		this.cost = cost;
	}

	public String getName() {
		return name;
	}

	public double getCost() {
		return cost;
	}
}
