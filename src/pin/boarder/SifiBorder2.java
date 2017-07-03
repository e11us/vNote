package pin.boarder;

import javafx.scene.Group;



public class SifiBorder2 implements PinBorder {
	private Group	nodes;
	private int		thic;

	@Override
	public void set( int w, int h, int thic, String Cbd, String Ctl ) {
		// TODO Auto-generated method stub
	}

	@Override
	public Group getNodes() {
		return nodes;
	}

	@Override
	public int getTopOS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getButtomOS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLeftOS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRightOS() {
		// TODO Auto-generated method stub
		return 0;
	}
}
