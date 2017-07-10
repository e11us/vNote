package pin.boarder;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.Node;



public interface PinBorderInterface {
	// 0 for no link, 1 = web, 2 = local, 3 = board.
	public void setLinkType( int inp );

	public void set( int w, int h, int thic, String Cbd, String Ctl );

	public Group getNodes();

	public int getTopOS();

	public int getButtomOS();

	public int getLeftOS();

	public int getRightOS();
}
