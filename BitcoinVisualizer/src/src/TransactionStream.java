package src;

import java.net.URI;
import java.net.URISyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class TransactionStream extends WebSocketClient{

	@SuppressWarnings("unused")
	private int numTX = 0;
	private BitExplorer app;
	
	// Constructors
	public TransactionStream(URI serverURI) {
		super(serverURI);
	}

	public TransactionStream(URI serverURI, BitExplorer app) {
		super(serverURI);
		this.app = app;
	}
	
	@Override
	public void onClose(int arg0, String closeString, boolean arg2) {
		//System.out.println("Websocket Closed");
	}

	@Override
	public void onError(Exception ex) {
		//ex.printStackTrace();
	}

	@Override
	/** 
	 * onMessage is called whenever the Websocket Client receives a message from the API that
	 * a new transaction has occured. The JSON message is passed as an argument. The Message is
	 * then parse to get the from address, the to address, and the value of the transaction.
	 * Then, the data is sent to the main application via an addBall call.
	 */
	public void onMessage(String message) {
		this.numTX++;
		String[] info = parseMessage(message);
		String fAddress = info[0];
		String tAddress = info[1];
		double amtBTC = Double.parseDouble(info[2]) * 0.00000001;
		this.app.addBall(fAddress, tAddress, amtBTC);
		//printTx(fAddress, tAddress, amtBTC, this.numTX);
	}

	@Override
	/** 
	 * On Open is called when the websocket is instantiated.
	 */
	public void onOpen(ServerHandshake arg0) {
		//System.out.println("Websocket Opened");
		this.startStream();
	}

	/** 
	 * Start Stream sends a message to the server to subscribe to new transactions.
	 */
	public void startStream() {
		String newTxSubscription = "{\"op\": \"unconfirmed_sub\"}";
		this.send(newTxSubscription);
	}

	/** 
	 * Stop Stream sends a message to the server to unsubscribe from new transactions.
	 * It is not currently in use, as we decided to just close the websocket instead.
	 */
	public void stopStream() {
		String txUnSubscribe = "{\"op\": \"unconfirmed_unsub\"}";
		this.send(txUnSubscribe);
	}

	/** 
	 * Parse Message is called on each new message. First it strips all spaces, quotes, and commas.
	 * Then it makes a string array where each line is an element in the array.
	 * By looking at the start of the line, data can then be extracted.
	 */
	public static String[] parseMessage(String message) {
		String[] relevantInfo = {"", "", ""};
		String spacesStripped = message.replaceAll(" ", "");
		String quotesStripped = spacesStripped.replaceAll("\"", "");
		String commasStripped = quotesStripped.replaceAll(",", "");
		String[] jsonArray = commasStripped.split("\n");
		for(String line : jsonArray) {
			if (line.length() > 4 && line.substring(0, 4).equals("addr")) {
				if (relevantInfo[0].equals("")) {
					relevantInfo[0] = line.substring(5);
				} else {
					relevantInfo[1] = line.substring(5);
				}
			} else if (line.length() > 5 && line.substring(0, 5).equals("value") && relevantInfo[2].equals("")) {
				relevantInfo[2] = line.substring(6);
			}
		}
		return relevantInfo;
	}
	
	// For testing
	public static void printTx(String from, String to, double val, int numberTrans) {
		System.out.println("New Transaction:");
		System.out.println("------------------------------------------------");
		System.out.println("From Address: " + from);
		System.out.println("To Address: " + to);
		System.out.println("Value Sent: " +  val + " BTC");
		System.out.println("TX Number: " + numberTrans);
		System.out.println();
	}
	
	public static void main(String[] args) throws URISyntaxException {
		TransactionStream btc = new TransactionStream(new URI("ws://ws.blockchain.info/inv"));
		btc.connect();
	}

}
