package processor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class ProcessOrderWorker implements Runnable {

	private Object lockObj;
	private HashMap<String, Item> items;
	private Order order;
	private TreeMap<String, Integer> sumPurchases;
	private String orderOutput;
	private TreeMap<Integer, String> allOrders;

	public ProcessOrderWorker(Object lockObj, HashMap<String, Item> items, Order order,
			TreeMap<String, Integer> sumPurchases, TreeMap<Integer, String> allOrders) {
		this.lockObj = lockObj;
		this.items = items;
		this.order = order;
		this.sumPurchases = sumPurchases;
		this.orderOutput = "";
		this.allOrders = allOrders;
	}

	@Override
	public void run() {
		synchronized (lockObj) {
			double orderTotal = 0;
			System.out.println("Reading order for client with id: " + order.getID());
			orderOutput += "----- Order details for client with Id: " + order.getID() + " -----\n";
			TreeMap<String, Integer> purchases = order.getPurchases();
			Set<String> keys = purchases.keySet();
			Iterator<String> iterator = keys.iterator();
			while (iterator.hasNext()) {

				String name = iterator.next();
				double costPerItem = items.get(name).getCost();
				double cost = (purchases.get(name) * costPerItem);
				orderOutput += "Item's name: " + name + ", Cost per item: "
						+ NumberFormat.getCurrencyInstance().format(costPerItem) + ", Quantity: " + purchases.get(name)
						+ ", Cost: " + NumberFormat.getCurrencyInstance().format(cost) + "\n";
				orderTotal += purchases.get(name) * costPerItem;

				if (sumPurchases.containsKey(name)) {
					sumPurchases.put(name, sumPurchases.get(name) + purchases.get(name));
				} else {
					sumPurchases.put(name, purchases.get(name));
				}

			}
			orderOutput += "Order Total: " + NumberFormat.getCurrencyInstance().format(orderTotal) + "\n";
			allOrders.put(order.getID(), orderOutput);
		}

	}

}
