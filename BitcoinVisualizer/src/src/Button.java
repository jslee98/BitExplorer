package src;

public class Button {
	
	// Fields
	private int xPos, yPos, height, width, normalColor, highlightedColor;
	private boolean mouseIsOver = false;
	private String name;

	// Constructor
	public Button(int xCoordinate, int yCoordinate, int width, int height, String text) {
		this.xPos = xCoordinate;
		this.yPos = yCoordinate;
		this.height = height;
		this.width = width;
		this.normalColor = 120;
		this.highlightedColor = 240;
		this.name = text;
	}

	/** 
	 * Over Button checks to see if the mouse is hovering over the button, 
	 * and sets mouseIsOver to true if so.
	 */
	public void overButton(int mouseX, int mouseY)  {
		if (mouseX >= this.xPos && mouseX <= this.xPos + this.width && mouseY >= this.yPos && mouseY <= this.yPos + this.height) {
			this.mouseIsOver = true;
		} else {
			this.mouseIsOver = false;
		}
	}

	// Getters and Setters
	public int getButtonColor() {
		if (this.mouseIsOver) {
			return this.highlightedColor;
		} else {
			return this.normalColor;
		}
	} 
	
	public int getTextColor() {
		if (!this.mouseIsOver) {
			return this.highlightedColor;
		} else {
			return this.normalColor;
		}
	}
	
	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setRectColor(int rectColor) {
		this.normalColor = rectColor;
	}

	public int getHighlightedColor() {
		return highlightedColor;
	}

	public boolean mouseIsOver() {
		return mouseIsOver;
	}

	public void setMouseIsOver(boolean mouseIsOver) {
		this.mouseIsOver = mouseIsOver;
	}

	public String getButtonText() {
		return this.name;
	}
	
	public void setButtonText(String newName) {
		this.name = newName;
	}
}
