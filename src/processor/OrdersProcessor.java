package processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class OrdersProcessor {

	private HashMap<String, Item> items;
	private ArrayList<Order> orders;
	private TreeMap<String, Integer> sumPurchases;
	private long startTime, endTime;

	public static void main(String[] args) {

		OrdersProcessor op = new OrdersProcessor();

		Scanner sc = new Scanner(System.in);

		System.out.print("Enter item's data file name: ");
		String itemData = sc.nextLine();

		System.out.print("Enter 'y' for multiple threads, any other character otherwise: ");
		String threadsInput = sc.nextLine();

		System.out.print("Enter number of orders to process: ");
		int numOfOrders = Integer.parseInt(sc.nextLine());

		System.out.print("Enter order's base filename: ");
		String base = sc.nextLine();

		System.out.print("Enter result's base filename: ");
		String result = sc.nextLine();

		op.readOrders(threadsInput, numOfOrders, base, result, itemData);

	}

	public OrdersProcessor() {
		this.items = new HashMap<String, Item>();
		this.startTime = 0;
		this.endTime = 0;
		this.orders = new ArrayList<Order>();
		this.sumPurchases = new TreeMap<String, Integer>();

	}

	public void readOrders(String threadsInput, int numOfOrders, String base, String result, String itemData) {

		startTime = System.currentTimeMillis();

		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(itemData));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] splited = line.split(" ");
				items.put(splited[0], new Item(splited[0], Double.parseDouble(splited[1])));
			}
			bufferedReader.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		for (int i = 1; i <= numOfOrders; i++) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(base + i + ".txt"));
				String line;
				String[] first = bufferedReader.readLine().split(" ");
				int clientID = Integer.parseInt(first[1]);
				TreeMap<String, Integer> purchases = new TreeMap<String, Integer>();
				while ((line = bufferedReader.readLine()) != null) {
					String[] splited = line.split(" ");

					if (purchases.containsKey(splited[0])) {
						purchases.put(splited[0], purchases.get(splited[0]) + 1);
					} else {
						purchases.put(splited[0], 1);
					}

				}
				orders.add(new Order(clientID, purchases));
				bufferedReader.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

		if (!threadsInput.equals("y")) {
			writeOrders(result, calcOrdersOneThread(result));

		} else {

			Object lockObj = new Object();
			Thread[] threads = new Thread[numOfOrders];
			TreeMap<Integer, String> allOrders = new TreeMap<Integer, String>();

			for (int i = 0; i < orders.size(); i++) {
				threads[i] = new Thread(new ProcessOrderWorker(lockObj, items, orders.get(i), sumPurchases, allOrders));
			}

			for (int i = 0; i < threads.length; i++) {
				threads[i].start();
			}

			for (int i = 0; i < threads.length; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			String allOrdersStringOutput = "";
			Collection<String> c = allOrders.values();
			Iterator<String> itr = c.iterator();

			// iterate through TreeMap values iterator
			while (itr.hasNext()) {
				allOrdersStringOutput += itr.next();
			}

			writeOrders(result, allOrdersStringOutput + getSummary());

		}

	}

	public String calcOrdersOneThread(String result) {
		String orderOutput = "";

		for (int i = 0; i < orders.size(); i++) {
			double orderTotal = 0;
			System.out.println("Reading order for client with id: " + orders.get(i).getID());
			orderOutput += "----- Order details for client with Id: " + orders.get(i).getID() + " -----\n";
			TreeMap<String, Integer> purchases = orders.get(i).getPurchases();
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

		}
		orderOutput += getSummary();
		return orderOutput;
	}

	public String getSummary() {
		String result = "***** Summary of all orders *****\n";

		Set<String> sumKeys = sumPurchases.keySet();
		Iterator<String> sumIterator = sumKeys.iterator();
		double sumTotal = 0;

		while (sumIterator.hasNext()) {
			String name = sumIterator.next();
			double costPerItem = items.get(name).getCost();
			double cost = (sumPurchases.get(name) * costPerItem);
			result += "Summary - Item's name: " + name + ", Cost per item: "
					+ NumberFormat.getCurrencyInstance().format(costPerItem) + ", Number sold: "
					+ sumPurchases.get(name) + ", Item's Total: " + NumberFormat.getCurrencyInstance().format(cost)
					+ "\n";
			sumTotal += cost;
		}
		result += "Summary Grand Total: " + NumberFormat.getCurrencyInstance().format(sumTotal);
		endTime = System.currentTimeMillis();
		System.out.println("Processing time (msec): " + (endTime - startTime));
		return result;
	}

	public void writeOrders(String fileName, String result) {

		boolean append = false;

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, append));
			bufferedWriter.write(result);
			bufferedWriter.flush(); // Forces flushing of buffer
			bufferedWriter.close(); // Also flushes the buffer so previous flush unnecessary
			System.out.println("Results can be found at: " + fileName);
		} catch (IOException e) {
			System.err.println(e.getMessage());

		}
	}
}