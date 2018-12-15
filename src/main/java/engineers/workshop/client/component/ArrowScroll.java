package engineers.workshop.client.component;

import engineers.workshop.client.GuiBase;

public abstract class ArrowScroll {

	private static final int ARROW_SRC_X = 36;
	private static final int ARROW_SRC_Y = 112;
	private static final int ARROW_W = 6;
	private static final int ARROW_H = 10;
	private static final int TEXT_Y = 3;
	private int x;
	private int y;
	private int width;
	private boolean clicked;

	public ArrowScroll(int x, int y, int width) {
		this.x = x;
		this.y = y;
		this.width = width;
	}

	public void draw(GuiBase gui, int mX, int mY) {
		if (isVisible()) {
			gui.prepare();
			boolean left = drawArrow(gui, mX, mY, true);
			boolean right = drawArrow(gui, mX, mY, false);
			if (!left && !right) {
				clicked = false;
			}

			gui.drawCenteredString(getText(), x + ARROW_W, y + TEXT_Y, width - ARROW_W, 0.7F, 0x1E1E1E);
		}
	}

	private boolean drawArrow(GuiBase gui, int mX, int mY, boolean left) {
		int srcX = ARROW_SRC_X + (left ? 0 : ARROW_W);
		boolean hover = inArrowBounds(gui, mX, mY, left);
		int srcY = ARROW_SRC_Y + (hover ? clicked ? 1 : 2 : 0) * ARROW_H;

		gui.drawRect(left ? x : x + width, y, srcX, srcY, ARROW_W, ARROW_H);

		return hover;
	}

	private boolean inArrowBounds(GuiBase gui, double mX, double mY, boolean left) {
		return gui.inBounds(left ? x : x + width, y, ARROW_W, ARROW_H, mX, mY);
	}

	public boolean isVisible() {
		return true;
	}

	public void onUpdate() {}

	public void onClick(GuiBase gui, double mX, double mY) {
		if (inArrowBounds(gui, mX, mY, true)) {
			int id = getId();
			id--;
			if (id < 0) {
				id = getLength() - 1;
			}
			clicked = true;
			setId(id);
			onUpdate();

		} else if (inArrowBounds(gui, mX, mY, false)) {
			int id = getId();
			id++;
			if (id >= getLength()) {
				id = 0;
			}
			clicked = true;
			setId(id);
			onUpdate();
		}
	}

	public void onRelease() {
		clicked = false;
	}

	public abstract String getText();

	public abstract int getId();

	public abstract void setId(int id);

	protected abstract int getLength();
}
