package pin.boarder;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.Node;



public interface PinBorder {
	public void set( int w, int h, int thic, String Cbd, String Ctl );

	public Group getNodes();

	public int getTopOS();

	public int getButtomOS();

	public int getLeftOS();

	public int getRightOS();
}
