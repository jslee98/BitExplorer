package src;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.net.*;
import java.io.*;
import processing.core.*;

public class BitExplorer extends PApplet{

	// Fields
	private boolean isConnected, isPaused, hasStarted, showKey = false;
	private final int SCREENWIDTH = 800;
	private final int SCREENHEIGHT = 600;
	private int ltPointOne, ltOne, ltFive, gtFive = 0;
	private int currencyTimer = 0;
	private int numTX = 0;
	private double sessionTotalBTC = 0.00;
	private ArrayDeque<String[]> newBallQueue = new ArrayDeque<String[]>(); 
	private ArrayList<Ball> existingBalls = new ArrayList<>();
	private ArrayList<Button> streamButtons = new ArrayList<Button>();
	private ArrayList<Button> explorerButtons = new ArrayList<Button>();
	private ArrayList<Button> startButtons = new ArrayList<Button>();
	private Block currentBlock;
	private Button pauseButton, clearButton, modeButton, parentButton, childButton;
	private Button  streamButton, explorerButton, refreshButton, keyButton;
	private PFont bitFont, typeFont, sansFont;
	private PImage bg;
	private String currentPriceUSD;
	private String modeString = "";
	private String streamString = "Transaction Visualizer";
	private String explorerString = "Blockchain Traveler";
	private TransactionStream tStream;

	// Processing Functions
	public static void main(String[] args) {
		PApplet.main("src.BitExplorer");
	}

	public void settings() {
		size(this.SCREENWIDTH, this.SCREENHEIGHT);
	}

	/** 
	 * Setup runs once when the explorer is booted
	 * Sets instance variables
	 */
	public void setup() {
		noStroke();
		this.modeButton = new Button(730, 5, 60, 20, "MODE");
		this.keyButton = new Button(660, 5, 60, 20, "KEY");
		this.pauseButton = new Button(590, 5, 60, 20, "PAUSE");
		this.clearButton = new Button(520, 5, 60, 20, "CLEAR");
		this.parentButton = new Button(485, 450, 100, 20, "PARENT BLOCK");
		this.childButton = new Button(215, 450, 100, 20, "CHILD BLOCK");
		this.streamButton = new Button(300, 315, 80, 20, "STREAM");
		this.explorerButton = new Button(410, 315, 80, 20, "TRAVELER");
		this.refreshButton = new Button(640, 5, 80, 20, "REFRESH");
		this.streamButtons.add(pauseButton);
		this.streamButtons.add(clearButton);
		this.streamButtons.add(modeButton);
		this.explorerButtons.add(modeButton);
		this.explorerButtons.add(parentButton);
		this.explorerButtons.add(childButton);
		this.startButtons.add(streamButton);
		this.startButtons.add(explorerButton);
		this.explorerButtons.add(refreshButton);
		this.streamButtons.add(keyButton);
		this.bitFont = createFont("Pixeled.ttf", 16, true);
		this.typeFont = createFont("PTMono.ttf", 18, true);
		this.sansFont = createFont("Sansation.ttf", 12, true);
		this.bg = loadImage("background.jpg");
	}

	/** 
	 * Draw runs continuously after the frame is setup
	 * Function is in charge of the GUI
	 * Calls separate draw functions to avoid long blocks of code
	 */
	public void draw() {
		background(bg);
		tint(50);
		if(!this.hasStarted) {
			this.drawStartScreen();
		}
		if (this.modeString.equals(this.streamString)) {
			this.flushQueue();
			this.drawBalls();
			this.drawGraph();
			this.drawSessionTotal();
			this.drawCurrentExchange();
			this.drawNumTransactions();
			this.drawColorKey();
		} else if (this.modeString.equals(this.explorerString)) {
			if (this.currentBlock == null) {
				this.fetchCurrentBlock();
			}
			this.drawBlockInfo();
		}
		this.drawMenuBar();
	}

	// Draw Functions
	/** 
	 * Draw Session Total displays the total amount of Bitcoin transferred
	 * Since the launch of the stream
	 */
	public void drawSessionTotal() {
		String amount = Double.toString(this.sessionTotalBTC);
		int dotIndex = amount.indexOf('.');
		try {
			amount = "Session Total: " + amount.substring(0, dotIndex + 3) + " BTC";
		} catch (Exception ex) {
			amount = "Session Total: " + amount.substring(0, dotIndex + 2) + "0 BTC";
		}
		fill(255);
		textAlign(RIGHT);
		text(amount, 770, 575);
	}

	/** 
	 * Draw Num Transactions displays the total amount of transactions
	 * since the start of the stream
	 */
	public void drawNumTransactions() {
		fill(255);
		textAlign(RIGHT);
		String numTxString = "Total Transactions: " + this.numTX;
		text(numTxString, 770, 560);
	}

	/** 
	 * Draw Current Exchange updates the current BTC/USD exchange rate
	 * once every 10 seconds due to the API call restrictions.
	 * It then displays the exchange rate on the screen.
	 */
	public void drawCurrentExchange(){
		if (millis() - this.currencyTimer > 10000 || this.currencyTimer == 0) {
			try {
				this.currencyTimer = millis();
				URL price = new URL("https://blockchain.info/ticker");
				URLConnection connect = price.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) 
					if(inputLine.contains("USD")) {
						int sIndex = inputLine.indexOf("last");
						String tempString = inputLine.substring(sIndex);
						int eIndex = tempString.indexOf(",");
						String valString = tempString.substring(8, eIndex);
						this.currentPriceUSD = "1 BTC = " + valString + " USD";
					}
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		fill(255);
		textAlign(RIGHT);
		text(this.currentPriceUSD, 770, 545);
	}

	/** 
	 * Draw Graph displays a simple, live-updating bar graph to give a quick visualization
	 * of how big the most popular transactions are. It also prevents screen overflow by capping
	 * the length at 500 but continuing a count.
	 */
	public void drawGraph() {
		fill(15, 150);
		rect(0, 515, this.SCREENWIDTH, 85);
		fill(255);
		int numberShift = 45;
		int numberThreshold = 30;
		textAlign(CENTER);
		text("< .1 BTC", 34, 533);
		text("< 1 BTC", 36, 551);
		text("< 5 BTC", 35, 569);
		text("> 5 BTC", 35, 588);
		// Less than .1
		if(this.ltPointOne > 500) {
			rect(60, 522, 501, 13);
			fill(0);
			text(this.ltPointOne, 501 + numberShift, 534);
			fill(255);
		} else if (this.ltPointOne > numberThreshold) {
			rect(60, 522, this.ltPointOne, 13);
			fill(0);
			text(this.ltPointOne, this.ltPointOne + numberShift, 534);
			fill(255);
		} else {
			rect(60, 522, this.ltPointOne, 13);
		}
		// Less than one
		if(this.ltOne > 500) {
			rect(60, 540, 501, 13);
			fill(0);
			text(this.ltOne, 501 + numberShift, 552);
			fill(255);
		} else if (this.ltOne > numberThreshold) {
			rect(60, 540, this.ltOne, 13);
			fill(0);
			text(this.ltOne, this.ltOne + numberShift, 552);
			fill(255);
		} else {
			rect(60, 540, this.ltOne, 13);
		}
		// Less than five
		if(this.ltFive > 500) {
			rect(60, 558, 501, 13);
			fill(0);
			text(this.ltFive, 501 + numberShift, 570);
			fill(255);
		} else if (this.ltFive > numberThreshold) {
			rect(60, 558, this.ltFive, 13);
			fill(0);
			text(this.ltFive, this.ltFive + numberShift, 570);
			fill(255);
		} else {
			rect(60, 558, this.ltFive, 13);
		}
		// Greater than five
		if(this.gtFive > 500) {
			rect(60, 576, 501, 13);
			fill(0);
			text(this.gtFive, 501 + numberShift, 588);
			fill(255);
		} else if (this.gtFive > numberThreshold) {
			rect(60, 576, this.gtFive, 13);
			fill(0);
			text(this.gtFive, this.gtFive + numberShift, 588);
			fill(255);
		} else {
			rect(60, 576, this.gtFive, 13);
		}
	}

	/** 
	 * Draw Block Info is the main function for the Traveler mode, and displays information
	 * from the current Block object. Try/catches attempt to shorten info to a simpler substring, but
	 * otherwise display the full string.
	 */
	public void drawBlockInfo() {
		stroke(255);
		fill(20, 180);
		rect(170, 155, 460, 260);
		noStroke();
		fill(255);
		textAlign(CENTER);
		textSize(30);
		if(this.currentBlock.isFirstBlock()) {
			text("LATEST BLOCK", this.SCREENWIDTH/2, 105);
		}
		double aveTX = this.currentBlock.getTotalBTC()/this.currentBlock.getNumTxs();
		// Try to create visually efficient substrings, if not draw full string
		textSize(26);
		text("Block Number: " + this.currentBlock.getBlockNumber(), this.SCREENWIDTH/2, 200);
		textSize(20);
		text("Transactions: " + this.currentBlock.getNumTxs(), this.SCREENWIDTH/2, 265);
		textSize(14);
		try {
			text("Reward: " + Double.toString(this.currentBlock.getReward()).substring(0,6) + " BTC", 340, 225);
		} catch (Exception ex) {
			text("Reward: " + Double.toString(this.currentBlock.getReward()) + " BTC", 340, 225);
		}
		try {
			text("Size: " + Double.toString(this.currentBlock.getStorageSizeInMB()).substring(0,5) + " MB", 470, 225);
		} catch (Exception ex) {
			text("Size: " + Double.toString(this.currentBlock.getStorageSizeInMB()) + " MB", 470, 225);
		}
		textSize(20);
		try {
			text("Average Transaction: " + Double.toString(aveTX).substring(0,6) + " BTC", this.SCREENWIDTH/2, 290);
		} catch (Exception ex) {
			text("Average Transaction: " + Double.toString(aveTX) + " BTC", this.SCREENWIDTH/2, 290);
		}
		try {
			text("Total Volume: " + Double.toString(this.currentBlock.getTotalBTC()).substring(0,8) + " BTC", this.SCREENWIDTH/2, 315);
		} catch (Exception ex) {
			text("Total Volume: " + Double.toString(this.currentBlock.getTotalBTC()) + " BTC", this.SCREENWIDTH/2, 315);
		}
		textSize(16);
		try {
			text("Max Transaction: " + Double.toString(this.currentBlock.getBiggestTx()).substring(0,7) + " BTC", this.SCREENWIDTH/2, 355);
		} catch (Exception ex) {
			text("Max Transaction: " + Double.toString(this.currentBlock.getBiggestTx()) + " BTC", this.SCREENWIDTH/2, 355);
		}
		text("By Address: " + this.currentBlock.getMaxAddress(), this.SCREENWIDTH/2, 380);
	}

	/** 
	 * Draw Menu Bar always displays the menu bar at the top of the screen with the logo
	 * and a transparent black bar.
	 */
	public void drawMenuBar() {
		fill(15, 150);
		rect(0, 0, this.SCREENWIDTH, 30);
		fill(255);
		textAlign(LEFT);
		textFont(bitFont);
		text("BIT", 40, 23);
		textFont(typeFont);
		text("explorer", 80, 21);
		textFont(sansFont);
		this.drawButtons();
		fill(255);
		textAlign(CENTER);
		textSize(16);
		text(this.modeString, this.SCREENWIDTH/2, 20);
		textSize(13);
	}

	/** 
	 * Draw Buttons iterates over a list of buttons depending on what mode the
	 * application is in. First it checks to see if the button is being hovered over,
	 * then it draws it due to specifications in the Button object.
	 */
	public void drawButtons() {
		stroke(255);
		if (this.modeString.equals(this.streamString)) {
			for (Button button : streamButtons) {
				button.overButton(mouseX, mouseY);
				fill(button.getButtonColor());
				rect(button.getxPos(), button.getyPos(), button.getWidth(), button.getHeight());
				textAlign(CENTER);
				fill(button.getTextColor());
				text(button.getButtonText(), button.getxPos() + button.getWidth()/2, button.getyPos() + 15);
			}
		} else if (this.modeString.equals(this.explorerString)){
			for (Button button : explorerButtons) {
				if (!(button.getButtonText().equals(this.childButton.getButtonText()) && this.currentBlock.isFirstBlock())) {
					button.overButton(mouseX, mouseY);
					fill(button.getButtonColor());
					rect(button.getxPos(), button.getyPos(), button.getWidth(), button.getHeight());
					textAlign(CENTER);
					fill(button.getTextColor());
					text(button.getButtonText(), button.getxPos() + button.getWidth()/2, button.getyPos() + 15);
				}
			}
		} else {
			for (Button button : startButtons) {
				button.overButton(mouseX, mouseY);
				fill(button.getButtonColor());
				rect(button.getxPos(), button.getyPos(), button.getWidth(), button.getHeight());
				textAlign(CENTER);
				fill(button.getTextColor());
				text(button.getButtonText(), button.getxPos() + button.getWidth()/2, button.getyPos() + 15);
			}
		}
		noStroke();
	}

	/** 
	 * Draw Balls is continuously called to move each ball according to its instance variables.
	 * Then it draws the ball in its current position, and displays the value if its greater than 1 BTC.
	 */
	public void drawBalls() {
		for (Ball ball :  existingBalls) {
			if(!this.isPaused) {
				ball.move(this.SCREENWIDTH, this.SCREENHEIGHT);
			} else {
				ball.mouseIsOver(mouseX, mouseY);
				if(ball.getIsSelected()) {
					stroke(255);	
				}
			}
			fill(ball.getRed(),ball.getGreen(),ball.getBlue(), 220);
			ellipse(ball.getXPosition(),ball.getYPosition(),ball.getDiameter(),ball.getDiameter());
			if (ball.getValueBTC() >= 1) {
				fill(255);
				textAlign(CENTER);
				String tempString = Double.toString(ball.getValueBTC());
				String valString = tempString.substring(0,tempString.indexOf(".") + 2) + " BTC";
				text(valString, ball.getXPosition(), ball.getYPosition());
			}
			if (ball.getIsSelected()) {
				fill(20, 180);
				rect(ball.getXPosition()-160, ball.getYPosition() + 5, 320, 45);
				noStroke();
				fill(255);
				textAlign(CENTER);
				text("From: " + ball.getFromAddress(), ball.getXPosition(), ball.getYPosition() + 23);	
				text("To: " + ball.getToAddress(), ball.getXPosition(), ball.getYPosition() + 42);	
			}
		}
	}

	/** 
	 * Draw Start Screen draws an opening screen to welcome the user and allow them to choose a boot mode.
	 */
	public void drawStartScreen() {
		stroke(255);
		fill(15, 190);
		rect(200, 175, 400, 200);
		noStroke();
		textAlign(CENTER);
		fill(255);
		textFont(sansFont);
		textSize(24);
		text("Welcome To", this.SCREENWIDTH/2, 205);
		textFont(bitFont);
		textSize(30);
		text("BIT", 320, 260);
		textFont(typeFont);
		textSize(32);
		text("explorer", 430, 255);
		textFont(sansFont);
		text("Select a mode to begin:", this.SCREENWIDTH/2, 290);
	}

	/** 
	 * Draw Color Key displays a color key to display the value of each ball.
	 * It is only displayed when showKey == true.
	 */
	public void drawColorKey() {
		if (this.showKey) {
			stroke(255);
			fill(15, 190);
			rect(615, 40, 150, 200);
			// < .05
			fill(255);
			rect(630, 60, 15, 15);
			// < .2
			fill(245, 171, 53);
			rect(630, 85, 15, 15);
			// < 1
			fill(3, 201, 169);
			rect(630, 110, 15, 15);
			// < 2
			fill(192, 57, 43);
			rect(630, 135, 15, 15);
			// < 5
			fill(155, 89, 182);
			rect(630, 160, 15, 15);
			// < 20
			fill(75, 119, 190);
			rect(630, 185, 15, 15);
			// > 20
			fill(210, 82, 127);
			rect(630, 210, 15, 15);
			noStroke();
			fill(255);
			textAlign(LEFT);
			text("0 < VAL < .05", 660, 72);
			text(".05 < VAL < .2", 660, 97);
			text(".2 < VAL < 1", 660, 122);
			text("1 < VAL < 2", 660, 147);
			text("2 < VAL < 5", 660, 172);
			text("5 < VAL < 20", 660, 197);
			text("VAL > 20", 660, 222);
		}
	}
	
	// Controller Method
	/** 
	 * Mouse Pressed is the main controlled method, and is a part of the Processing core.
	 * Since BitExplorer interacts through buttons, the function checks if any button is hovered over
	 * each time the mouse is pressed, and proceeds with an action if it is.
	 */
	public void mousePressed() {
		if (pauseButton.mouseIsOver() && this.modeString.equals(this.streamString)) {
			this.pauseVisualizer();
		} else if (clearButton.mouseIsOver() && this.modeString.equals(this.streamString)) {
			this.clearBalls();
		} else if (modeButton.mouseIsOver()) {
			if (this.modeString.equals(this.streamString)) {
				if (this.isConnected) {
					this.manageWSConnection();
				}
				this.modeString = this.explorerString;
			} else {
				if (!this.isPaused) {
					this.manageWSConnection();
				}
				this.modeString = this.streamString;
			}
		} else if (parentButton.mouseIsOver()) {
			this.fetchParentBlock();
		} else if (childButton.mouseIsOver()) {
			if(!(this.currentBlock.isFirstBlock())) {
				this.currentBlock = this.currentBlock.getChildBlock();
			}
		} else if (!this.hasStarted && streamButton.mouseIsOver()) {
			this.manageWSConnection();
			this.modeString = this.streamString;
			this.hasStarted = true;
		} else if (!this.hasStarted && explorerButton.mouseIsOver()) {
			this.modeString = this.explorerString;
			this.hasStarted = true;
		} else if (refreshButton.mouseIsOver()) {
			this.currentBlock = null;
		} else if (keyButton.mouseIsOver()) {
			if(this.showKey) {
				this.showKey = false;
				this.keyButton.setButtonText("KEY");
			} else {
				this.showKey = true;
				this.keyButton.setButtonText("HIDE");
			}
		}
	}

	// IO Methods
	/** 
	 * Manage WS Connection deals with the websocket connection for the TransactionStream.
	 * A new Stream must be created each time the socket is closed.
	 */

	public void manageWSConnection() {
		if (this.isConnected) {
			this.tStream.close();
			this.isConnected = false;
		} else  {
			try {
				this.tStream  = new TransactionStream(new URI("ws://ws.blockchain.info/inv"), this);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			this.tStream.connect();
			this.isConnected = true;
		}
	}
	
	/** 
	 * Pause Visualizer pauses all ball movement, closes the TransactionStream, and changes button
	 * text to UNPAUSE.
	 */
	public void pauseVisualizer() {
		if(this.isPaused) {
			this.pauseButton.setButtonText("PAUSE");
			this.isPaused = false;
		}else {
			this.pauseButton.setButtonText("UNPAUSE");
			this.isPaused = true;	
		}
		this.manageWSConnection();
	}

	/** 
	 * Fetch Current Block acquires the latest block hash from the API, and creates
	 * a block object with that hash and a null child.
	 */
	public void fetchCurrentBlock() {
		try {
			URL addr = new URL("https://blockchain.info/q/latesthash");
			URLConnection connect = addr.openConnection();
			BufferedReader input = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String hash = input.readLine();
			input.close();
			this.currentBlock = new Block(hash, null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** 
	 * Fetch Parent Block gets the parent block from the current block. If it has already been loaded,
	 * the view switches to the parent block immediately, otherwise the new block info will be pulled
	 * and parsed before display.
	 */
	public void fetchParentBlock() {
		if (this.currentBlock.getParentBlock() == null) {
			this.currentBlock = new Block(this.currentBlock.getParentBlockHash(), this.currentBlock);
			this.currentBlock.getChildBlock().setParentBlock(this.currentBlock);
		} else {
			this.currentBlock = this.currentBlock.getParentBlock();
		}
	}

	// Model Methods
	/** 
	 * Flush Queue is called continuously with draw. The queue is filled with all new transactions while
	 * draw is called, and then emptied into the existing balls. This is to avoid concurrent list modification.
	 */
	public void flushQueue() {
		while (newBallQueue.size() > 0 && !this.isPaused) {
			String[] dequeuedBall = newBallQueue.remove();
			Ball newBall = new Ball(Double.parseDouble(dequeuedBall[0]), dequeuedBall[1], dequeuedBall[2]);
			this.existingBalls.add(newBall);
		}
	}

	/** 
	 * Clear Balls is called when the clear button is clicked, and sets all current
	 * transaction data to zero.
	 */
	public void clearBalls() {
		this.existingBalls.clear();
		this.ltPointOne = 0;
		this.ltOne = 0;
		this.ltFive = 0;
		this.gtFive = 0;
		this.sessionTotalBTC = 0;
		this.numTX = 0;
	}

	/** 
	 * Add Ball is called by the TransactionStream every time a new message is received.
	 * It updates the graph, adds to total transaction counts, and adds the data to
	 * the newBallQueue.
	 */
	public void addBall(String from, String to, double value) {
		this.numTX++;
		if (value <= .1) {
			this.ltPointOne++;
		} else if (value <= 1) {
			this.ltOne++;
		} else if(value <= 5){
			this.ltFive++;
		} else {
			this.gtFive++;
		}
		this.sessionTotalBTC += value;
		String[] newBallData = { Double.toString(value), from, to};
		newBallQueue.add(newBallData);	
	}
}
