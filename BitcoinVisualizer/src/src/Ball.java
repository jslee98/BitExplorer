package src;

import java.util.Random;

public class Ball {

	// Fields
	private boolean isSelected;
	private double value;
	private final float MINSPEED = (float) 2.5;
	private final float MAXSPEED = 10;
	private float diameter, xPos, yPos, ySpd, xSpd;
	private int xDirection = 1;
	private int yDirection = 1;
	private int r, g, b;
	private static Random randGen = new Random();
	private String fAddress, tAddress;

	// Constructor
	public Ball(double val, String from, String to) {
		this.fAddress = from;
		this.tAddress = to;
		this.value = val;
		this.xPos = 350;
		this.yPos = 275;
		this.setDirection();
		this.setVariablesFromValue();
		this.setSpeed();
	}

	/** 
	 * Move first adds the current position to the product of (speed * direction) of both the x and y coordinates to
	 * "move" the ball. Then, it checks to see if the ball has hit a wall. If it has, direction
	 * is reversed.
	 */
	public void move(int screenWidth, int screenHeight) {
		this.xPos = this.xPos + ( this.xSpd * this.xDirection );
		this.yPos = this.yPos + ( this.ySpd * this.yDirection );

		if (this.xPos > screenWidth-this.diameter/2.0 || this.xPos < this.diameter/2.0) {
			this.xDirection *= -1;
		}
		if (this.yPos > screenHeight-this.diameter/2.0 || this.yPos < this.diameter/2.0) {
			this.yDirection *= -1;
		}
	}

	/** 
	 * Mouse Is Over checks to see if the ball is in the current location of the mouse.
	 */
	public void mouseIsOver(int mX, int mY) {
		if (mX >= this.xPos - this.diameter/2 && mX <= this.xPos + this.diameter/2 
				&& mY >= this.yPos - this.diameter/2 && mY <= this.yPos + this.diameter/2) {
			this.isSelected = true;
		} else {
			this.isSelected = false;
		}
	}

	/** 
	 * Set Speed gives the ball a random x and y speed within the constraints.
	 */
	public void setSpeed() {
		this.ySpd = this.MINSPEED + randGen.nextFloat() * (this.MAXSPEED - this.MINSPEED);
		this.xSpd = this.MINSPEED + randGen.nextFloat() * (this.MAXSPEED - this.MINSPEED);
	}

	/** 
	 * Set Variable From Value sets the instance variables diameter and color 
	 * based on the value of the transaction.
	 */
	public void setVariablesFromValue() {
		if (this.value < .05) {
			this.diameter = 10;
			// White
			this.r = 255;
			this.g= 255;
			this.b = 255;
		}  else if (this.value < .2) {
			this.diameter = 20;
			// Gold
			this.r = 245;
			this.g = 171;
			this.b = 53;
		} else if (this.value < 1) {
			this.diameter = 40;
			// Turquoise
			this.r = 3;
			this.g= 201;
			this.b = 169;
		} else if (this.value < 2) {
			this.diameter = 80;
			// Burgundy
			this.r = 192;
			this.g= 57;
			this.b = 43;
		}else if (this.value < 5) {
			this.diameter = 100;
			// Purple
			this.r = 155;
			this.g= 89;
			this.b = 182;
		}else if (this.value < 20) {
			this.diameter = 150;
			// Steel Blue
			this.r = 75;
			this.g= 119;
			this.b = 190;
		}else {
			this.diameter = 200;
			// Pink
			this.r = 210;
			this.g= 82;
			this.b = 127;
		}
	}
	
	/** 
	 * Set Direction takes no arguments and randomly sets the X and Y directions of a new ball.
	 */
	public void setDirection() {
		double randomDirection = randGen.nextDouble();
		if (randomDirection < .25) {
			this.xDirection = 1;
			this.yDirection = -1;
		} else if (randomDirection < .5) {
			this.xDirection = -1;
			this.yDirection = -1;
		} else if (randomDirection < .75) {
			this.xDirection = -1;
			this.yDirection = 1;
		} else {
			this.xDirection = 1;
			this.yDirection = 1;
		}
	}

	// Getters
	public float getDiameter() {
		return this.diameter;
	}

	public int getRed() {
		return this.r;
	}
	
	public int getGreen() {
		return this.g;
	}
	
	public int getBlue() {
		return this.b;
	}
	
	public float getXPosition() {
		return this.xPos;
	}
	
	public float getYPosition() {
		return this.yPos;
	}

	public double getValueBTC() {
		return this.value;
	}

	public String getFromAddress() {
		return this.fAddress;
	}

	public String getToAddress() {
		return this.tAddress;
	}

	public boolean getIsSelected() {
		return this.isSelected;
	}
}
