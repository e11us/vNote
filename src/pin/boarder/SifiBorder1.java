package pin.boarder;

import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;



public class SifiBorder1 implements PinBorderInterface {
	private static final double	topTitleH	= 15.0;
	private Group				nodes;
	private int					thic;
	// 0 for no link, 1 = web, 2 = local, 3 = board.
	private int					linkType	= 0;

	@Override
	public void set( int w, int h, int tx, String Cbd, String Ctl ) {
		nodes= new Group();
		this.thic= 3;
		//
		Polyline polyline= new Polyline();
		polyline.getPoints().addAll( new Double[] {
				(double)w, (double)0,
				0.0, 0.0,
				0.0, topTitleH,
				(double)w, topTitleH,
				(double)w, (double)h,
				0.0, (double)h,
				0.0, (double) ( 28 ) } );
		polyline.setStyle( "-fx-stroke: #" + Cbd + ";" );
		polyline.setStrokeWidth( thic );
		nodes.getChildren().add( polyline );
		//
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
	public Group getNodes() {
		return nodes;
	}

	@Override
	public int getTopOS() {
		return 25;
	}

	@Override
	public int getButtomOS() {
		return thic + 3;
	}

	@Override
	public int getLeftOS() {
		return thic + 5;
	}

	@Override
	public int getRightOS() {
		return thic + 3;
	}

	@Override
	public void setLinkType( int inp ) {
		linkType= inp;
	}
}
