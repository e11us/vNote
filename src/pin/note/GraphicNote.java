package pin.note;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import app.DeskTopNote;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import machine.Helper;
import machine.p;
import net.coobird.thumbnailator.Thumbnails;
import pin.Pin;
import pin.PinCopyable;
import pin.boarder.PinBorderInterface;
import pin.boarder._PinBorderFactory;
import robo.ClipBoard;
import java.nio.file.*;



public class GraphicNote extends Pin implements PinNoteInterface {
	private static final String	name		= "GraphicNote";
	//
	private GraphicNote			handler		= this;
	private DeskTopNote			board		= null;
	private _PinNoteFactory		PNF			= null;
	private PinBorderInterface	bd			= _PinBorderFactory.getBoarder( "default" );
	//
	private Element				dat			= null;
	private ImageView			imgV		= new ImageView();;
	private Image				img			= null;
	private boolean				dragWasOn	= false;
	private boolean				resizeSig	= false;
	private boolean				focused		= false;
	// loc tmp copy.
	private int					gx, gy, width, height;

	/*-----------------------------------------------------------------------------------------
	 * constructor with data.
	 */
	public GraphicNote( org.w3c.dom.Node node, DeskTopNote bd, _PinNoteFactory fc ) {
		board= bd;
		PNF= fc;
		dat= (Element)node;
		gx= Integer.parseInt( dat.getAttribute( "GridLocationX" ) );
		gy= Integer.parseInt( dat.getAttribute( "GridLocationY" ) );
		//
		init();
	}

	/*-----------------------------------------------------------------------------------------
	 * constructor without data.( need to use createXMLdataElm & setImg after )
	 */
	public GraphicNote( int x, int y, DeskTopNote bd, _PinNoteFactory fnc ) {
		board= bd;
		PNF= fnc;
		gx= x;
		gy= y;
	}

	/*-----------------------------------------------------------------------------------------
	 * create a new blank GraphicNote.
	 */
	@Override
	public void createXMLdataElm( Document doc ) {
		dat= doc.createElement( name );
		dat.setAttribute( "GridLocationX", gx + "" );
		dat.setAttribute( "GridLocationY", gy + "" );
		dat.setAttribute( "GridSizeX", "1" );
		dat.setAttribute( "GridSizeY", "1" );
		dat.setAttribute( "ID", PNF.getNewID() );
	}

	/*-----------------------------------------------------------------------------------------
	 * set and test the image. ( copy the orginal and test for the resize. )
	 */
	public boolean setImg( File fd, File img, int[] gc ) {
		String name= board.getUniqNameInBoardFolder( ".jpg" );
		//
		try{
			Thumbnails.of( img ).size( gc[0], gc[1] )
					.toFile( fd.getPath().toString() + File.separatorChar + name + " T.jpg" );
		}catch ( Exception e ){
			board.popMsg( e.getLocalizedMessage() +
					"\n\nGraphic Note is not created. Please try again." );
			return false;
		}
		//
		try{
			Path src= Paths.get( img.toString() );
			Path dst= Paths.get( fd.getPath() + File.separatorChar + name + ".jpg" );
			Files.copy( src, dst );
		}catch ( IOException e ){
			board.popMsg( e.getLocalizedMessage() +
					"\n\nGraphic Note is not created. Please try again." );
			return false;
		}
		//
		dat.setAttribute( "ImgName", name );
		init();
		if( imgV == null )
			return false;
		return true;
	}

	/*-----------------------------------------------------------------------------------------
	 *  init the graphic note.
	 */
	public void init() {
		this.getChildren().add( imgV );
		this.setOnMouseClicked( e -> {
			if( board.isWallpaperMode() )
				return;
			// if this is end of drag.
			if( dragWasOn ){
				dragWasOn= false;
				board.removeFocusOfMe();
				return;
			}
			// if this is click.
			board.removeFocusOfMe();
			switch( e.getButton() ){
				case PRIMARY :
					if( e.getClickCount() == 1 ){
						handler.requestFocus();
						focused= true;
						setNoteGraphic( board.getGridSizeConfig() );
						return;
					}
				case SECONDARY :
					//
				default :
					break;
			}
		} );
		imgV.focusedProperty().addListener( new ChangeListener <Boolean>() {
			@Override
			public void changed( ObservableValue <? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue ) {
				if( newPropertyValue ){
					// on focus.
				}else{
					// lost focus.			
					focused= false;
					setNoteGraphic( board.getGridSizeConfig() );
				}
			}
		} );
		handler.focusedProperty().addListener( new ChangeListener <Boolean>() {
			@Override
			public void changed( ObservableValue <? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue ) {
				if( newPropertyValue ){
					// on focus.
				}else{
					// lost focus.			
					focused= false;
					setNoteGraphic( board.getGridSizeConfig() );
				}
			}
		} );
		this.setOnKeyTyped( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				switch( (int)event.getCharacter().charAt( 0 ) ){
					case 27 :
						// esc
						board.removeFocusOfMe();
						return;
					case 8 :
						// back space.
						deleteThis();
						return;
				}
				switch( event.getCharacter() ){
					case "w" :
						int h= Integer.parseInt( dat.getAttribute( "GridSizeY" ) );
						if( h > 1 ){
							dat.setAttribute( "GridSizeY", --h + "" );
							resizeSig= true;
						}
						break;
					case "s" :
						dat.setAttribute( "GridSizeY",
								( Integer.parseInt( dat.getAttribute( "GridSizeY" ) ) + 1 ) + "" );
						resizeSig= true;
						break;
					case "a" :
						int w= Integer.parseInt( dat.getAttribute( "GridSizeX" ) );
						if( w > 1 ){
							dat.setAttribute( "GridSizeX", --w + "" );
							resizeSig= true;
						}
						break;
					case "d" :
						dat.setAttribute( "GridSizeX",
								( Integer.parseInt( dat.getAttribute( "GridSizeX" ) ) + 1 ) + "" );
						resizeSig= true;
						break;
					/*
					case "x" :
					if( dat.getAttribute( "Link" ).equals( "null" ) ){
						File fl= board.chooseDir();
						if( fl != null ){
							dat.setAttribute( "Link", fl.getAbsolutePath().replace( '\\', '/' ) );
						}
					}
					break;
					case "c" :
					if( dat.getAttribute( "Link" ).equals( "null" ) ){
						File f2= board.chooseFile();
						if( f2 != null ){
							dat.setAttribute( "Link", f2.getAbsolutePath().replace( '\\', '/' ) );
						}
					}
					break;
					case "b" :
					// only create new board if the link is null.
					if( dat.getAttribute( "Link" ).equals( "null" ) )
						dat.setAttribute( "Link", "board " + board.createNewBoard() );
					break;
					case "v" :
					String msg= ClipBoard.getClipBoard();
					if( msg.startsWith( "http://" ) || msg.startsWith( "https://" ) ){
						if( dat.getAttribute( "Link" ).equals( "null" ) )
							dat.setAttribute( "Link", msg );
					}else{
						dat.setAttribute( "Note", msg );
						lb.setText( msg );
						ta.setText( msg );
					}
					break;
					case "z" :
					dat.setAttribute( "Link", "null" );
					break;
					*/
				}
				board.storeXMLfil();
				setNoteGraphic( board.getGridSizeConfig() );
			}
		} );
		this.setOnMouseDragged( e -> {
			if( board.isWallpaperMode() )
				return;
			// reset focus of all.
			board.removeFocusOfMe();
			//
			gx= PNF.location2GridX( (int) ( e.getSceneX() + board.getCamShift().getX() ) );
			gy= PNF.location2GridY( (int) ( e.getSceneY() + board.getCamShift().getY() ) );
			if( gx != Integer.parseInt( dat.getAttribute( "GridLocationX" ) ) ||
					gy != Integer.parseInt( dat.getAttribute( "GridLocationY" ) ) ){
				dat.setAttribute( "GridLocationX", gx + "" );
				dat.setAttribute( "GridLocationY", gy + "" );
				board.storeXMLfil();
				setNoteGraphic( board.getGridSizeConfig() );
				dragWasOn= true;
			}
		} );
	}
	/*-----------------------------------------------------------------------------------------
	 * some of the getter and others.
	 */

	// check if image is set.
	public boolean okGo() {
		if( img == null )
			return false;
		else return true;
	}

	@Override
	public String getID() {
		return dat.getAttribute( "ID" );
	}

	@Override
	public Node getXMLdataElm() {
		return dat;
	}

	@Override
	public String getTypeName() {
		return GraphicNote.name;
	}

	@Override
	public void deleteThis() {
		p.p( this.getClass().toString(), "Delete-" + GraphicNote.name + " ImgName: " +
				dat.getAttribute( "ImgName" ) );
		try{
			File ff= new File( board.createBoardFolder().getPath() + File.separatorChar
					+ dat.getAttribute( "ImgName" ) + " T.jpg" );
			if( ff.exists() )
				ff.deleteOnExit();
			ff= new File( board.createBoardFolder().getPath() + File.separatorChar
					+ dat.getAttribute( "ImgName" ) + ".jpg" );
			if( ff.exists() )
				ff.deleteOnExit();
			//
			PNF.remove( handler );
			board.remove( handler );
		}catch ( Exception ee ){
			ee.printStackTrace();
		}
	}

	@Override
	public void setStyle( Element sty ) {}
	/*
	@Override
	public String getFactyName() {
		return _PinNoteFactory.pinTypeName;
	}
	
	@Override
	public Element getXMLDatForDup() {
		return dat;
	}
	
	@Override
	public Point2D getGridloc() {
		return new Point2D( Integer.parseInt( dat.getAttribute( "GridSizeX" ) ),
				Integer.parseInt( dat.getAttribute( "GridSizeY" ) ) );
	}
	
	@Override
	public void deleteAfterCut() {
		deleteThis();
	}
	@Override
	public boolean setXMLDatForDup( Element inp ) {
		return false;
	}
	
	@Override
	public void selectHL() {}
	
	@Override
	public void selectDeHL() {}
	*/

	/*-----------------------------------------------------------------------------------------
	 * config all the graphical part of this node.
	 */
	protected void setNoteGraphic( int[] gc ) {
		// set the size for the note.
		int sx= Integer.parseInt( dat.getAttribute( "GridSizeX" ) );
		if( sx == 1 )
			width= sx * gc[0];
		else width= sx * gc[0] + ( sx - 1 ) * gc[2];
		int sy= Integer.parseInt( dat.getAttribute( "GridSizeY" ) );
		if( sy == 1 )
			height= sy * gc[1];
		else height= sy * gc[1] + ( sy - 1 ) * gc[3];;
		this.setMinHeight( height );
		this.setMaxHeight( height );
		this.setMinWidth( width );
		this.setMaxWidth( width );
		//
		if( resizeSig ){
			resizeSig= false;
			redoImg( gc );
		}
		//
		resetImg();
		if( img == null )
			return;
		//
		this.getChildren().remove( bd.getNodes() );
		if( focused ){
			this.bd.set( width, height, 2, "ffffff", "ffffff" );
			this.getChildren().add( bd.getNodes() );
		}
		// sheift the note. if the location is not managed.
		if( !super.locationManaged ){
			this.setTranslateX( ( gx - 1 ) * ( gc[0] + gc[2] ) +
					gc[2] / 2 );
			this.setTranslateY( ( gy - 1 ) * ( gc[1] + gc[3] ) +
					gc[3] / 2 );
		}
	}

	private void redoImg( int[] gc ) {
		String name= dat.getAttribute( "ImgName" );
		File fb= board.createBoardFolder();
		File ff= new File( fb.getPath() + File.separatorChar
				+ dat.getAttribute( "ImgName" ) + " T.jpg" );
		if( ff.exists() )
			ff.delete();
		try{
			Thumbnails.of( new File( fb.getPath().toString()
					+ File.separatorChar + name + ".jpg" ) ).size( width, height )
					.toFile( fb.getPath().toString() + File.separatorChar +
							name + " T.jpg" );
		}catch ( IOException e ){
			// silent fail.
		}
	}

	private void resetImg() {
		try{
			FileInputStream input;
			input= new FileInputStream(
					board.createBoardFolder().getPath() + File.separatorChar
							+ dat.getAttribute( "ImgName" ) + " T.jpg" );
			img= new Image( input );
			imgV.setImage( img );
		}catch ( FileNotFoundException e ){
			// silent fail.
			img= null;
		}
	}
}
