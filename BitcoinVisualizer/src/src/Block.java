package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Block {
	
	// Fields
	private String hash, parentBlockHash, maxSentAddress;
	private Block childBlock, parentBlock;
	private int blockNumber, numTX;
	private double storageSize, totalVolumeBTC, reward, feesCollected;
	private double maxTX = 0;
	private ArrayList<String> jsonList = new ArrayList<>();
	private final String APISTRING = "https://blockchain.info/rawblock/";

	// Constructor
	public Block(String bHash, Block child) {
		this.hash = bHash;
		this.childBlock = child;
		this.fetchJSONData();
		this.parseData();
	}
	
	/** 
	 * Parse Data accesses the JSON list created when the data is fetched. It first gets the data that is always
	 * returned at the same line of the block, then scans line prefixes for keywords to get the block reward and each
	 * transaction. The transactions are each added to a transaction total, and the max transaction is found
	 * while scanning through each transaction.
	 */
	public void parseData() {
		// Get static block data
		this.parentBlockHash = jsonList.get(2).substring(11);
		this.feesCollected = Double.parseDouble(jsonList.get(6).substring(4)) * 0.00000001;
		this.numTX = Integer.parseInt(jsonList.get(8).substring(5));
		this.storageSize = Double.parseDouble(jsonList.get(9).substring(5));
		this.blockNumber = Integer.parseInt(jsonList.get(12).substring(7));
		
		// Get reward amount
		int lineIndex = 15;
		while(reward == 0) {
			if(jsonList.get(lineIndex).length() > 5 && jsonList.get(lineIndex).substring(0,5).equals("value")) {
				this.reward = Double.parseDouble(jsonList.get(lineIndex).substring(6)) * 0.00000001;
			}
			lineIndex++;
		}

		// Parse through transactions, add to total, and find biggest transaction
		boolean newTX = false;
		String currentAddr = "";
		for (int i = lineIndex; i < jsonList.size(); i++) {
			if (jsonList.get(i).length() > 6 && jsonList.get(i).substring(0,6).equals("inputs")) {
				newTX = true;
			}
			if (newTX && jsonList.get(i).length() > 10 && jsonList.get(i).substring(0,4).equals("addr")) {
				currentAddr = jsonList.get(i).substring(5);
			}
			if (newTX && jsonList.get(i).length() > 10 && jsonList.get(i).substring(0,5).equals("value")) {
				double tvalue = Double.parseDouble(jsonList.get(i).substring(6)) * 0.00000001;
				if (tvalue > this.maxTX) {
					this.maxTX = tvalue;
					this.maxSentAddress = currentAddr;
				}
				this.totalVolumeBTC += tvalue;
				newTX = false;
			}
		}
	}

	/** 
	 * Fetch JSON Data accesses the given hash in the constructor and sends a request to the API.
	 * A buffered reader reads in each line, which is then stripped and saved in a list of Strings.
	 */
	public void fetchJSONData() {
		try {
			URL addr = new URL(this.APISTRING + this.hash);
			URLConnection conn = addr.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				// Checks if line contains a letter and adds it to list if it does
				if (inputLine.matches(".*[a-z].*")) { 
					this.jsonList.add(inputLine.replaceAll(" ", "").replaceAll("\"", "").replaceAll(",", ""));
				}
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/** 
	 * Is First Block returns true if the child block is not Null.
	 */
	public boolean isFirstBlock() { 
		if (this.childBlock == null) {
			return true;
		}
		return false;
	}
	
	// Getters and Setters
	public int getBlockNumber() {
		return this.blockNumber;
	}
	
	public double getFeesCollected() {
		return this.feesCollected;
	}
	
	public int getNumTxs() {
		return this.numTX;
	}
	
	public double getStorageSizeInMB() {
		return this.storageSize * .000001;
	}
	
	public double getReward() {
		return this.reward;
	}
	
	public double getBiggestTx() {
		return this.maxTX;
	}
	
	public double getTotalBTC() {
		return this.totalVolumeBTC;
	}
	
	public String getParentBlockHash() {
		return this.parentBlockHash;
	}
	
	public Block getChildBlock() {
		return this.childBlock;
	}
	
	public void setParentBlock(Block p) {
		this.parentBlock = p;
	}
	
	public Block getParentBlock() {
		return this.parentBlock;
	}
	
	public String getMaxAddress() {
		return this.maxSentAddress;
	}
	
	// For testing
	public void printBlock() {
		System.out.println("Block Number: " + this.blockNumber);
		System.out.println("Prev hash: " + this.parentBlockHash);
		System.out.println("Fees Collected: " + this.feesCollected);
		System.out.println("Num TX: " + this.numTX);
		System.out.println("Storage Size: " + this.storageSize);
		System.out.println("Reward: " + this.reward);
		System.out.println("Max TX: " + this.maxTX);
		System.out.println("Total: " + this.totalVolumeBTC);	
	}
	
	public static void main(String[] args) {
		Block test = new Block("00000000000000000002fa1477edc2ecc9b6a8e4afa5a09d53725fc00beaaa3f", null);
		test.printBlock();
	}
}

