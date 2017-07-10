package pin.boarder;

import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;



public class SifiBorder1 implements PinBorderInterface {
	private Group	nodes;
	private int		thic;

	@Override
	public void set( int w, int h, int tx, String Cbd, String Ctl ) {
		nodes= new Group();
		this.thic= 3;
		//
		Polyline polyline= new Polyline();
		polyline.getPoints().addAll( new Double[] {
				(double)w, (double)0,
				0.0, 0.0,
				0.0, 16.0,
				(double)w, 16.0,
				(double)w, (double)h,
				0.0, (double)h,
				0.0, (double) ( 28 ) } );
		polyline.setStyle( "-fx-stroke: #" + Cbd + ";" );
		polyline.setStrokeWidth( thic );
		Line ll= new Line( 10, 28, 10, h - 20 );
		ll.setStyle( "-fx-stroke: #" + Cbd + ";" );
		ll.setStrokeWidth( thic );
		nodes.getChildren().add( polyline );
		nodes.getChildren().add( ll );
	}

	@Override
	public Group getNodes() {
		return nodes;
	}

	@Override
	public int getTopOS() {
		return 28;
	}

	@Override
	public int getButtomOS() {
		return 7;
	}

	@Override
	public int getLeftOS() {
		return 16;
	}

	@Override
	public int getRightOS() {
		return 8;
	}
}
