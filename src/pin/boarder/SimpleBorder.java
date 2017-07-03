package pin.boarder;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Line;



public class SimpleBorder implements PinBorder {
	private Group				nodes;
	private int					thic;
	private static final int	space	= 5;

	protected SimpleBorder() {}

	public void set( int w, int h, int thic, String Cbd, String Ctl ) {
		nodes= new Group();
		this.thic= thic;
		//
		Line t= new Line( 0, 0, w, 0 );
		Line b= new Line( 0, h, w, h );
		Line l= new Line( 0, 0, 0, h );
		Line r= new Line( w, 0, w, h );
		t.setStyle( "-fx-stroke: #" + Cbd + ";" );
		b.setStyle( "-fx-stroke: #" + Cbd + ";" );
		l.setStyle( "-fx-stroke: #" + Cbd + ";" );
		r.setStyle( "-fx-stroke: #" + Cbd + ";" );
		t.setStrokeWidth( thic );
		b.setStrokeWidth( thic );
		l.setStrokeWidth( thic );
		r.setStrokeWidth( thic );
		nodes.getChildren().add( t );
		nodes.getChildren().add( b );
		nodes.getChildren().add( l );
		nodes.getChildren().add( r );
	}

	@Override
	public int getTopOS() {
		return thic + space;
	}

	@Override
	public int getButtomOS() {
		return thic + space;
	}

	@Override
	public int getLeftOS() {
		return thic + space;
	}

	@Override
	public int getRightOS() {
		return thic + space;
	}

	@Override
	public Group getNodes() {
		return nodes;
	}
}
