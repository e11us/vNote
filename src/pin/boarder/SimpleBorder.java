package pin.boarder;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;



public class SimpleBorder implements PinBorderInterface {
	private Group				nodes;
	private int					thic;
	private static final int	spaceX	= 3;
	private static final int	spaceY	= 2;
	// 0 for no link, 1 = web, 2 = local, 3 = board.
	private int					linkType= 0;

	protected SimpleBorder() {}

	public void setLinkType( int inp ) {
		linkType= inp;
	}

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
		if( linkType == 1 ){
			Circle cl= new Circle();
			cl.setTranslateX( w - thic - 3 );
			cl.setTranslateY( thic + 3 );
			cl.setRadius( 2 );
			cl.setStyle( "-fx-fill: #" + Cbd + ";" );
			nodes.getChildren().add( cl );
		}else if( linkType == 2 ){
			Rectangle rt= new Rectangle();
			rt.setX( w - thic - 3 );
			rt.setY( thic - 1 );
			rt.setWidth( 4 );
			rt.setHeight( 4 );
			rt.setStyle( "-fx-fill: #" + Cbd + ";" );
			nodes.getChildren().add( rt );
		}else if( linkType == 3 ){
			Rectangle rt= new Rectangle();
			rt.setX( thic * 2 );
			rt.setY( thic - 1 );
			rt.setWidth( thic );
			rt.setHeight( 4 );
			rt.setStyle( "-fx-fill: #" + Cbd + ";" );
			nodes.getChildren().add( rt );
			rt= new Rectangle();
			rt.setX( thic * 4 );
			rt.setY( thic - 1 );
			rt.setWidth( thic );
			rt.setHeight( 4 );
			rt.setStyle( "-fx-fill: #" + Cbd + ";" );
			nodes.getChildren().add( rt );
		}
	}

	@Override
	public int getTopOS() {
		if( linkType == 0 )
			return thic + spaceY;
		else return thic + spaceY + 4;
	}

	@Override
	public int getButtomOS() {
		return thic + spaceY;
	}

	@Override
	public int getLeftOS() {
		return thic + spaceX;
	}

	@Override
	public int getRightOS() {
		return thic + spaceX;
	}

	@Override
	public Group getNodes() {
		return nodes;
	}
}
