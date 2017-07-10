package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import machine.Helper;
import machine._SysConfig;
import machine.p;
import pin.Pin;
import pin.PinCopyable;
import pin.PinInterface;
import pin.note.PinNoteInterface;
import pin.note.StickyNote;
import pin.note._PinNoteFactory;



/* -------------------------------------------------------------------------------------------
 * 
 * -------------------------------------------------------------------------------------------
 */
public class DeskTopNote {
	//
	private File					file				= null;
	private Document				doc					= null;
	private Element					elm					= null;
	//
	// focusholder at z=3, bgImg at z=4. liveBG at z= 2.
	private DeskTopApp				app					= null;
	private Scene					scn					= null;
	private Group					rootSys				= null;
	private Group					root				= null;
	private Group					bgNode				= null;
	private Region					focusHolder			= null;
	private Node					rightMenu			= null;
	private BorderPane				topMenuP			= null;
	//
	private Point2D					camShift			= new Point2D( 0.0, 0.0 );
	private int						mouseLastX			= 0;
	private int						mouseLastY			= 0;
	private boolean					cltDown				= false;
	private Point2D					mouseDragInit		= null;
	private Rectangle				dragRec				= null;
	private ArrayList <PinCopyable>	copylist			= null;
	//
	private _PinNoteFactory			PNF					= null;
	// 
	private int						ShiftKeyboardSpeedX	= 1;
	private int						ShiftKeyboardSpeedY	= 1;
	private int						ShiftKeyboardSpeedWX= 1;
	private int						ShiftKeyboardSpeedWY= 1;

	/*-----------------------------------------------------------------------------------------
	 * getters and setter and other public func.
	 */
	public int[] getGridSizeConfig() {
		int[] ret= { Integer.parseInt( elm.getAttribute( "GridSizeX" ) ),
				Integer.parseInt( elm.getAttribute( "GridSizeY" ) ),
				Integer.parseInt( elm.getAttribute( "GridSpaceX" ) ),
				Integer.parseInt( elm.getAttribute( "GridSpaceY" ) ) };
		return ret;
	}

	public Point2D getDim() {
		return new Point2D( Integer.parseInt( elm.getAttribute( "WindowSizeX" ) ),
				Integer.parseInt( elm.getAttribute( "WindowSizeY" ) ) );
	}

	public Point2D getCamShift() {
		return camShift;
	}

	public void scrollBoard( int val ) {
		if( val > 0.0 ){
			root.setTranslateY( ShiftKeyboardSpeedY + root.getTranslateY() );
			camShift= camShift.add( new Point2D( 0.0, (double) -ShiftKeyboardSpeedY ) );
		}else{
			root.setTranslateY( -ShiftKeyboardSpeedY + root.getTranslateY() );
			camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedY ) );
		}
	}

	public void storeXMLfil() {
		store();
	}

	public void remove( Pin inp ) {
		root.getChildren().remove( inp );
		store();
	}

	public void removeFocusOfMe() {
		focusHolder.requestFocus();
	}

	public String createNewBoard() {
		String ret= app.createNewBoard();
		p.p( this.getClass().toString(), "New Board: " + ret + " is created." );
		return ret;
	}

	public void openBoard( String inp ) {
		store();
		p.p( this.getClass().toString(), "Switch to Board: " + inp );
		app.switch2Board( inp );
	}

	public File createBoardFolder() {
		File fold= new File( Helper.getFileName( file.toString() ) );
		fold.mkdirs();
		return fold;
	}

	public String getUniqNameInBoardFolder( String ext ) {
		String uniq= Helper.randAN( 12 );
		File ret= new File( Helper.getFileName( file.toString() ) + uniq + ext );
		while( ret.exists() ){
			uniq= Helper.randAN( 12 );
			ret= new File( Helper.getFileName( file.toString() ) + uniq + ext );
		}
		return uniq;
	}

	public File chooseDir() {
		return app.chooseDir();
	}

	public File chooseFile() {
		return app.chooseFile();
	}

	public boolean maxWindowOnStart() {
		return Boolean.parseBoolean( elm.getAttribute( "WindowMaxOnStart" ) );
	}

	public String getName() {
		return elm.getAttribute( "Name" );
	}

	public void copyNote( PinCopyable pin ) {
		ArrayList <PinCopyable> cp= new ArrayList <PinCopyable>();
		cp.add( pin );
		copyNode( cp );
	}

	public void cutNote( PinCopyable pin ) {
		ArrayList <PinCopyable> cp= new ArrayList <PinCopyable>();
		cp.add( pin );
		copyNode( cp );
		app.ClipBoardCut= true;
	}

	public void popMsg( String inp ) {
		popUp pu= new popUp( "Attention." );
		VBox comp= new VBox();
		Label lb= new Label( inp );
		lb.setWrapText( true );
		lb.setMaxWidth( 300 );
		lb.setMaxHeight( 100 );
		lb.setMinWidth( 300 );
		lb.setMinHeight( 100 );
		comp.getChildren().add( lb );
		Scene stageScene= new Scene( comp, 300, 100 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * constructor:
	 * create a board data with a given root elm. 
	 * then sort root child node to spec pin factory.
	 */
	protected DeskTopNote( File inp, DeskTopApp app ) {
		p.p( this.getClass().toString(), "Constructor running for: " +
				inp.getAbsolutePath() + " " );
		// load file. setup elm.
		this.file= inp;
		this.app= app;
		load();
		// load all Pin Factory.
		refreshBoard();
		// set up scene.
		sceneSetup();
		removeFocusOfMe();
		p.p( this.getClass().toString(), "Constructor finished." );
	}

	/*-----------------------------------------------------------------------------------------
	 * reload all the Pin for the board( the node for javaFX) in the root group.
	 */
	protected Scene refreshRoot() {
		//
		setTitle();
		//
		// empty root all first.
		root.getChildren().removeAll( root.getChildren() );
		// for pinNote.
		root.getChildren().addAll( PNF.getAllNodes() );
		// for ...
		// for ...
		// return at last.
		return scn;
	}

	protected File getBoardFile() {
		return file;
	}

	protected void setWholeShift() {
		// for x.
		int xs= app.getStageWidth() / ( ShiftKeyboardSpeedX );
		if( app.getStageWidth() % ( ShiftKeyboardSpeedX ) > Integer.parseInt( elm.getAttribute( "GridSizeX" ) ) )
			xs++ ;
		ShiftKeyboardSpeedWX= xs * ( ShiftKeyboardSpeedX );
		// for y.
		int ys= app.getStageHeight() / ( ShiftKeyboardSpeedY );
		if( app.getStageHeight() % ( ShiftKeyboardSpeedY ) > Integer.parseInt( elm.getAttribute( "GridSizeY" ) ) )
			ys++ ;
		ShiftKeyboardSpeedWY= ys * ( ShiftKeyboardSpeedY );
	}

	/*-----------------------------------------------------------------------------------------
	 * reload all the facotry content and other setting config from the XML file. 
	 */
	private void refreshBoard() {
		// for pinNote. setup factory.
		NodeList nl= elm.getElementsByTagName( _PinNoteFactory.pinTypeName );
		if( nl.getLength() == 0 ){
			elm.appendChild( _PinNoteFactory.createElm( doc ) );
			nl= elm.getElementsByTagName( _PinNoteFactory.pinTypeName );
			store();
		}
		PNF= new _PinNoteFactory( doc, nl.item( 0 ), this );
		// for the grid size.
		ShiftKeyboardSpeedX= Integer.parseInt( elm.getAttribute( "GridSizeX" ) ) +
				Integer.parseInt( elm.getAttribute( "GridSpaceX" ) );
		ShiftKeyboardSpeedY= Integer.parseInt( elm.getAttribute( "GridSizeY" ) ) +
				Integer.parseInt( elm.getAttribute( "GridSpaceY" ) );
		// for the grid whole shift.
		setWholeShift();
	}

	/*-----------------------------------------------------------------------------------------
	 * load the doc from the XML file.
	 */
	private boolean load() {
		if( !file.exists() ){
			if( !createDefaultFile( file ) )
				return false;
		}
		//
		if( !file.canRead() )
			return false;
		//
		this.doc= parseDoc( file );
		this.elm= doc.getDocumentElement();
		// make sure all attri is there.
		completeAllAttr();
		store();
		if( doc == null )
			return false;
		return true;
	}

	/*-----------------------------------------------------------------------------------------
	 * retore the root elm to the XML file.
	 */
	private boolean store() {
		try{
			file.delete();
			Source source= new DOMSource( doc );
			Result result= new StreamResult( file );
			Transformer xformer= TransformerFactory.newInstance().newTransformer();
			xformer.transform( source, result );
			return true;
		}catch ( Exception e ){
			e.printStackTrace();
		}
		return false;
	}

	/*-----------------------------------------------------------------------------------------
	 * parse the XML file into doc.
	 */
	private Document parseDoc( File inp ) {
		try{
			DocumentBuilderFactory dbFactory= DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder= dbFactory.newDocumentBuilder();
			Document doc= dBuilder.parse( inp );
			doc.getDocumentElement().normalize();
			return doc;
		}catch ( ParserConfigurationException e ){
			e.printStackTrace();
		}catch ( SAXException e ){
			e.printStackTrace();
		}catch ( IOException e ){
			e.printStackTrace();
		}
		return null;
	}

	/*-----------------------------------------------------------------------------------------
	 * create a default empty XML file for this board.
	 */
	private boolean createDefaultFile( File inp ) {
		XMLOutputFactory xof= XMLOutputFactory.newInstance();
		XMLStreamWriter xtw= null;
		try{
			xtw= xof.createXMLStreamWriter( new FileWriter( inp ) );
			xtw.writeStartDocument();
			xtw.writeStartElement( "board" );
			xtw.writeEndElement();
			xtw.writeEndDocument();
			xtw.flush();
			xtw.close();
			return true;
		}catch ( XMLStreamException | IOException e ){
			e.printStackTrace();
			return false;
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * make sure that all attribute are there.
	 */
	private void completeAllAttr() {
		if( elm.getAttribute( "GridSizeX" ).equals( "" ) )
			elm.setAttribute( "GridSizeX", "100" );
		if( elm.getAttribute( "GridSizeXmin" ).equals( "" ) )
			elm.setAttribute( "GridSizeXmin", "50" );
		if( elm.getAttribute( "GridSizeXmax" ).equals( "" ) )
			elm.setAttribute( "GridSizeXmax", "600" );
		if( elm.getAttribute( "GridSizeXstep" ).equals( "" ) )
			elm.setAttribute( "GridSizeXstep", "5" );
		if( elm.getAttribute( "GridSizeY" ).equals( "" ) )
			elm.setAttribute( "GridSizeY", "100" );
		if( elm.getAttribute( "GridSizeYmin" ).equals( "" ) )
			elm.setAttribute( "GridSizeYmin", "10" );
		if( elm.getAttribute( "GridSizeYmax" ).equals( "" ) )
			elm.setAttribute( "GridSizeYmax", "300" );
		if( elm.getAttribute( "GridSizeYstep" ).equals( "" ) )
			elm.setAttribute( "GridSizeYstep", "3" );
		if( elm.getAttribute( "GridSpaceX" ).equals( "" ) )
			elm.setAttribute( "GridSpaceX", "15" );
		if( elm.getAttribute( "GridSpaceY" ).equals( "" ) )
			elm.setAttribute( "GridSpaceY", "10" );
		if( elm.getAttribute( "WindowSizeX" ).equals( "" ) )
			elm.setAttribute( "WindowSizeX", "1920" );
		if( elm.getAttribute( "WindowSizeY" ).equals( "" ) )
			elm.setAttribute( "WindowSizeY", "1080" );
		if( elm.getAttribute( "ColorBackGround1" ).equals( "" ) )
			elm.setAttribute( "ColorBackGround1", "333333" );
		if( elm.getAttribute( "ColorBackGround2" ).equals( "" ) )
			elm.setAttribute( "ColorBackGround2", "777777" );
		if( elm.getAttribute( "BackGroundImage" ).equals( "" ) )
			elm.setAttribute( "BackGroundImage", "null" );
		if( elm.getAttribute( "BgSelection" ).equals( "" ) )
			elm.setAttribute( "BgSelection", "color" );
		if( elm.getAttribute( "WindowMaxOnStart" ).equals( "" ) )
			elm.setAttribute( "WindowMaxOnStart", "true" );
		if( elm.getAttribute( "Name" ).equals( "" ) )
			elm.setAttribute( "Name", Helper.randAN( 10 ) );
		if( elm.getAttribute( "DateCreate" ).equals( "" ) )
			elm.setAttribute( "DateCreate", Helper.getCurrentDate() );
		if( elm.getAttribute( "ScrollPolicy" ).equals( "" ) )
			elm.setAttribute( "ScrollPolicy", "full" );
		if( elm.getAttribute( "ScrollGridSize" ).equals( "" ) )
			elm.setAttribute( "ScrollGridSize", "1" );
	}

	/*-----------------------------------------------------------------------------------------
	 * setup the scene and the root.
	 */
	private void sceneSetup() {
		root= new Group();
		focusHolder= new Region();
		focusHolder.setMinHeight( _SysConfig.getScreenSizeY() );
		focusHolder.setMinWidth( _SysConfig.getScreenSizeX() );
		focusHolder.setTranslateZ( 3 );
		focusHolder.setStyle( "-fx-background-color: rgba(1.0, 1.0, 1.0, 0.03);" );
		//
		bgNode= new Group();
		SetBg();
		//
		rootSys= new Group();
		rootSys.getChildren().add( bgNode );
		rootSys.getChildren().add( focusHolder );
		rootSys.getChildren().add( root );
		if( Boolean.parseBoolean( elm.getAttribute( "WindowMaxOnStart" ) ) ){
			scn= new Scene( rootSys, DeskTopApp.screenSize.getWidth(),
					DeskTopApp.screenSize.getHeight(), true, SceneAntialiasing.BALANCED );
		}else{
			scn= new Scene( rootSys, getDim().getX(), getDim().getY(),
					true, SceneAntialiasing.BALANCED );
		}
		//
		scn.setOnMouseMoved( e -> {
			mouseLastX= (int) ( e.getSceneX() + camShift.getX() );
			mouseLastY= (int) ( e.getSceneY() + camShift.getY() );
		} );
		// focus holder. switch mouse the key.
		focusHolder.setOnMouseClicked( e -> {
			switch( e.getButton() ){
				case PRIMARY :
					LeftClick( e );
					break;
				case SECONDARY :
					RightClick( e );
					break;
			}
		} );
		focusHolder.setOnScroll( e -> {
			if( e.getDeltaY() > 0.0 ){
				root.setTranslateY( ShiftKeyboardSpeedY *
						Integer.parseInt( elm.getAttribute( "ScrollGridSize" ) )
						+ root.getTranslateY() );
				camShift= camShift.add( new Point2D( 0.0, (double) -ShiftKeyboardSpeedY *
						Integer.parseInt( elm.getAttribute( "ScrollGridSize" ) ) ) );
			}else{
				root.setTranslateY( -ShiftKeyboardSpeedY *
						Integer.parseInt( elm.getAttribute( "ScrollGridSize" ) )
						+ root.getTranslateY() );
				camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedY *
						Integer.parseInt( elm.getAttribute( "ScrollGridSize" ) ) ) );
			}
		} );
		focusHolder.setOnMouseDragged( e -> {
			switch( e.getButton() ){
				case PRIMARY :
					mouseleftDrag( (int)e.getSceneX(), (int)e.getSceneY() );
					break;
				default :
					break;
			}
		} );
		focusHolder.setOnMouseReleased( e -> {
			mouseleftDragEnd();
			// upon realase drag rec, if there is rec, check and make list of node inside rec.
			if( dragRec != null ){
				Rectangle tmpRec= new Rectangle();
				tmpRec.setX( dragRec.getX() + camShift.getX() );
				tmpRec.setY( dragRec.getY() + camShift.getY() );
				tmpRec.setWidth( dragRec.getWidth() );
				tmpRec.setHeight( dragRec.getHeight() );
				//
				copylist= new ArrayList <PinCopyable>();
				app.ClipBoardOS.remove( app.ClipBoardOS );
				for( Node tmp : root.getChildren() ){
					if( tmp instanceof PinCopyable &&
							tmp.getBoundsInParent().intersects( tmpRec.getBoundsInParent() ) ){
						copylist.add( (PinCopyable)tmp );
						( (PinCopyable)tmp ).selectHL();
					}
				}
			}
		} );
		focusHolder.setOnMousePressed( e -> {
			switch( e.getButton() ){
				case PRIMARY :
					//
					removeRightMenu();
					//
					if( mouseDragInit == null && dragRec != null ){
						mouseleftDragRecEnd();
					}
					break;
				default :
					break;
			}
		} );
		focusHolder.setOnKeyPressed( e -> {
			if( e.isControlDown() ){
				cltDown= true;
			}
		} );
		focusHolder.setOnKeyReleased( e -> {
			if( !e.isControlDown() ){
				cltDown= false;
			}
		} );
		focusHolder.setOnKeyTyped( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				switch( (int)event.getCharacter().charAt( 0 ) ){
					case 27 :
						// esc
						store();
						app.popBoardStack();
						return;
					case 8 :
						// backspace.
						mouseleftDragRecEnd();
						if( copylist != null && copylist.size() > 0 ){
							for( PinCopyable tmp : copylist ){
								tmp.deleteAfterCut();
							}
						}
						return;
				}
				switch( event.getCharacter() ){
					case "w" :
						if( !elm.getAttribute( "ScrollPolicy" ).equals( "full" ) ){
							root.setTranslateY( ShiftKeyboardSpeedY + root.getTranslateY() );
							camShift= camShift.add( new Point2D( 0.0, (double) -ShiftKeyboardSpeedY ) );
						}else{
							root.setTranslateY( ShiftKeyboardSpeedWY + root.getTranslateY() );
							camShift= camShift.add( new Point2D( 0.0, (double) -ShiftKeyboardSpeedWY ) );
						}
						setTitle();
						break;
					case "s" :
						if( !elm.getAttribute( "ScrollPolicy" ).equals( "full" ) ){
							root.setTranslateY( -ShiftKeyboardSpeedY + root.getTranslateY() );
							camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedY ) );
						}else{
							root.setTranslateY( -ShiftKeyboardSpeedWY + root.getTranslateY() );
							camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedWY ) );
						}
						setTitle();
						break;
					case "a" :
						if( !elm.getAttribute( "ScrollPolicy" ).equals( "full" ) ){
							root.setTranslateX( ShiftKeyboardSpeedX + root.getTranslateX() );
							camShift= camShift.add( new Point2D( (double) -ShiftKeyboardSpeedX, 0.0 ) );
						}else{
							root.setTranslateX( ShiftKeyboardSpeedWX + root.getTranslateX() );
							camShift= camShift.add( new Point2D( (double) -ShiftKeyboardSpeedWX, 0.0 ) );
						}
						setTitle();
						break;
					case "d" :
						if( !elm.getAttribute( "ScrollPolicy" ).equals( "full" ) ){
							root.setTranslateX( -ShiftKeyboardSpeedX + root.getTranslateX() );
							camShift= camShift.add( new Point2D( (double)ShiftKeyboardSpeedX, 0.0 ) );
						}else{
							root.setTranslateX( -ShiftKeyboardSpeedWX + root.getTranslateX() );
							camShift= camShift.add( new Point2D( (double)ShiftKeyboardSpeedWX, 0.0 ) );
						}
						setTitle();
						break;
					case " " :
						store();
						app.zipBackUp( file );
						break;
					case "e" :
					case "E" :
						if( topMenuP == null ){
							createTopConfigMenu();
						}else{
							rootSys.getChildren().remove( topMenuP );
							topMenuP= null;
						}
						break;
					case "x" :
						root.setTranslateX( 0 );
						root.setTranslateY( 0 );
						camShift= new Point2D( 0.0, 0.0 );
						setTitle();
						break;
					case "n" :
						setBoardNamePopUp();
						break;
					case "m" :
						popUpConsoleLog();
						break;
					case "X" :
						app.ClipBoardCut= true;
					case "C" :
						if( copylist.size() > 0 ){
							copyNode( copylist );
						}else app.ClipBoardCut= false;
						// remove.
						mouseleftDragRecEnd();
						break;
					case "V" :
						if( app.ClipBoard.size() != 0 ){
							Pin tcp;
							for( PinCopyable tmp : app.ClipBoard ){
								switch( tmp.getFactyName() ){
									case _PinNoteFactory.pinTypeName :
										tcp= PNF.createNewNote(
												tmp, mouseLastX, mouseLastY );
										if( tcp != null ){
											root.getChildren().add( tcp );
											if( app.ClipBoardCut ){
												// if good copy, delete cut case.
												tmp.deleteAfterCut();
											}
										}
										break;
									// case : ( for other factory ).
								}
							}
							// clear up after each paste.
							app.ClipBoardCut= false;
							app.ClipBoard.removeAll( app.ClipBoard );
						}
						break;
					case "r" :
						popMsg( "Total Notes: " + root.getChildren().size() + " \n" +
								"Board Name: " + elm.getAttribute( "Name" ) + " \n" +
								"Creation Date: " + elm.getAttribute( "DateCreate" ) );
				}
			}
		} );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void copyNode( ArrayList <PinCopyable> nodes ) {
		app.ClipBoard.removeAll( app.ClipBoard );
		app.ClipBoard.addAll( nodes );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void setTitle() {
		app.setTitle( elm.getAttribute( "Name" ) + " [" +
				(int) ( camShift.getX() / ShiftKeyboardSpeedX ) + " , " +
				(int) ( camShift.getY() / ShiftKeyboardSpeedY ) + "]" );
	}

	/*-----------------------------------------------------------------------------------------
	 * mouse left click on empty space.
	 */
	private void LeftClick( MouseEvent e ) {
		removeFocusOfMe();
	}

	/*-----------------------------------------------------------------------------------------
	 * mouse right click on empty space.
	 */
	private void RightClick( MouseEvent e ) {
		removeFocusOfMe();
		mouseleftDragEnd();
		mouseleftDragRecEnd();
		createBoardRightMenu( e.getScreenX(), e.getScreenY(), e.getSceneX(), e.getSceneY(),
				camShift.getX(), camShift.getY() );
	}

	/*-----------------------------------------------------------------------------------------
	 * create a right click menu.
	 */
	private void createBoardRightMenu( double xs, double ys, double x, double y, double xx, double yy ) {
		removeRightMenu();
		ContextMenu rightMenu= new ContextMenu();
		for( String tmp : _PinNoteFactory.NoteTypes ){
			MenuItem tmi= new MenuItem( "New " + tmp );
			tmi.setOnAction( e -> {
				Pin ret= PNF.createNewNote(
						tmi.getText().substring( 4, tmi.getText().length() ),
						(int) ( xx + x ), (int) ( yy + y ) );
				if( ret != null ){
					root.getChildren().add( ret );
					store();
				}
				removeRightMenu();
				removeFocusOfMe();
			} );
			rightMenu.getItems().add( tmi );
		}
		//
		rightMenu.show( root, xs, ys );
	}

	/*-----------------------------------------------------------------------------------------
	 * top config Menu
	 */
	private void createTopConfigMenu() {
		if( topMenuP == null ){
			MenuBar topMenu= new MenuBar();
			topMenu.prefWidthProperty().bind( app.getPrimStage().widthProperty() );
			topMenuP= new BorderPane();
			topMenuP.setTop( topMenu );
			rootSys.getChildren().add( topMenuP );
			//
			Menu gw= new Menu( "Grid Width" );
			ToggleGroup gwG= new ToggleGroup();
			int xs= Integer.parseInt( elm.getAttribute( "GridSizeXmin" ) );
			int xe= Integer.parseInt( elm.getAttribute( "GridSizeXmax" ) );
			int xt= Integer.parseInt( elm.getAttribute( "GridSizeXstep" ) );
			for( int i= xs; i < xe; i+= xt ){
				RadioMenuItem mi= new RadioMenuItem( i + "" );
				mi.setToggleGroup( gwG );
				mi.setOnAction( e -> {
					elm.setAttribute( "GridSizeX", Integer.parseInt( mi.getText() ) + "" );
					refreshBoard();
					refreshRoot();
				} );
				if( i == Integer.parseInt( elm.getAttribute( "GridSizeX" ) ) )
					mi.setSelected( true );
				gw.getItems().add( mi );
			}
			topMenu.getMenus().add( gw );
			//
			Menu gh= new Menu( "Grid Height" );
			ToggleGroup ghG= new ToggleGroup();
			int ys= Integer.parseInt( elm.getAttribute( "GridSizeYmin" ) );
			int ye= Integer.parseInt( elm.getAttribute( "GridSizeYmax" ) );
			int yt= Integer.parseInt( elm.getAttribute( "GridSizeYstep" ) );
			for( int i= ys; i < ye; i+= yt ){
				RadioMenuItem mi= new RadioMenuItem( i + "" );
				mi.setToggleGroup( ghG );
				mi.setOnAction( e -> {
					elm.setAttribute( "GridSizeY", Integer.parseInt( mi.getText() ) + "" );
					refreshBoard();
					refreshRoot();
				} );
				if( i == Integer.parseInt( elm.getAttribute( "GridSizeY" ) ) )
					mi.setSelected( true );
				gh.getItems().add( mi );
			}
			topMenu.getMenus().add( gh );
			//
			Menu gws= new Menu( "Grid S Width" );
			ToggleGroup gwsG= new ToggleGroup();
			for( int i= 1; i < 50; i+= 1 ){
				RadioMenuItem mi= new RadioMenuItem( i + "" );
				mi.setToggleGroup( gwsG );
				mi.setOnAction( e -> {
					elm.setAttribute( "GridSpaceX", Integer.parseInt( mi.getText() ) + "" );
					refreshBoard();
					refreshRoot();
				} );
				if( i == Integer.parseInt( elm.getAttribute( "GridSpaceX" ) ) )
					mi.setSelected( true );
				gws.getItems().add( mi );
			}
			topMenu.getMenus().add( gws );
			//
			Menu ghs= new Menu( "Grid S Height" );
			ToggleGroup ghsG= new ToggleGroup();
			for( int i= 1; i < 50; i+= 1 ){
				RadioMenuItem mi= new RadioMenuItem( i + "" );
				mi.setToggleGroup( ghsG );
				mi.setOnAction( e -> {
					elm.setAttribute( "GridSpaceY", Integer.parseInt( mi.getText() ) + "" );
					refreshBoard();
					refreshRoot();
				} );
				if( i == Integer.parseInt( elm.getAttribute( "GridSpaceY" ) ) )
					mi.setSelected( true );
				ghs.getItems().add( mi );
			}
			topMenu.getMenus().add( ghs );
			//
			Menu ppt= new Menu( "Property" );
			topMenu.getMenus().add( ppt );
			ToggleGroup pptSP= new ToggleGroup();
			RadioMenuItem partSP= new RadioMenuItem( "grid scroll" );
			partSP.setToggleGroup( pptSP );
			RadioMenuItem wholSP= new RadioMenuItem( "window scroll" );
			wholSP.setToggleGroup( pptSP );
			ppt.getItems().add( partSP );
			ppt.getItems().add( wholSP );
			if( elm.getAttribute( "ScrollPolicy" ).equals( "full" ) )
				wholSP.setSelected( true );
			else partSP.setSelected( true );
			wholSP.setOnAction( e -> {
				elm.setAttribute( "ScrollPolicy", "full" );
			} );
			partSP.setOnAction( e -> {
				elm.setAttribute( "ScrollPolicy", "part" );
			} );
			ppt.getItems().add( new SeparatorMenuItem() );
			//
			MenuItem name= new MenuItem( "change name" );
			name.setOnAction( e -> {
				setBoardNamePopUp();
			} );
			MenuItem scrolGD= new MenuItem( "scroll grid size" );
			scrolGD.setOnAction( e -> {
				setScrollGridSizePopUp();
			} );
			ppt.getItems().add( name );
			ppt.getItems().add( scrolGD );
			ppt.getItems().add( new SeparatorMenuItem() );
			//
			MenuItem chooseBG= new MenuItem( "choose background picture" );
			chooseBG.setOnAction( e -> {
				File bg= app.chooseFile( _SysConfig.sysFolderName + File.separatorChar
						+ _SysConfig.backgroundFolderName );
				if( bg != null ){
					elm.setAttribute( "BackGroundImage", bg.toString().replace( '\\', '/' ) );
					elm.setAttribute( "BgSelection", "img" );
					SetBg();
				}
			} );
			ppt.getItems().add( chooseBG );
			//
			ToggleGroup bgo= new ToggleGroup();
			RadioMenuItem bgimg= new RadioMenuItem( "bg use image" );
			bgimg.setToggleGroup( bgo );
			RadioMenuItem bgcol= new RadioMenuItem( "bg use color" );
			bgcol.setToggleGroup( bgo );
			ppt.getItems().add( bgimg );
			ppt.getItems().add( bgcol );
			if( elm.getAttribute( "BgSelection" ).equals( "img" ) )
				bgimg.setSelected( true );
			else bgcol.setSelected( true );
			bgimg.setOnAction( e -> {
				elm.setAttribute( "BgSelection", "img" );
				SetBg();
			} );
			bgcol.setOnAction( e -> {
				elm.setAttribute( "BgSelection", "color" );
				SetBg();
			} );
			ppt.getItems().add( new SeparatorMenuItem() );
			//
			//	CheckMenuItem htmlMenuItem = new CheckMenuItem("HTML");
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * left mouse drag a rec and relase.
	 */
	public void mouseleftDrag( int x, int y ) {
		if( mouseDragInit == null ){
			mouseDragInit= new Point2D( x, y );
			if( dragRec != null ){
				mouseleftDragRecEnd();
			}
			dragRec= new Rectangle();
			dragRec.setX( x );
			dragRec.setY( y );
			dragRec.setFill( new Color( 1.0, 1.0, 1.0, 0.1 ) );
			rootSys.getChildren().add( dragRec );
		}else{
			if( x >= mouseDragInit.getX() )
				dragRec.setWidth( x - mouseDragInit.getX() );
			else{
				dragRec.setWidth( mouseDragInit.getX() - x );
				dragRec.setX( x );
			}
			if( y >= mouseDragInit.getY() )
				dragRec.setHeight( y - mouseDragInit.getY() );
			else{
				dragRec.setHeight( mouseDragInit.getY() - y );
				dragRec.setY( y );
			}
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * end mouse drag.
	 */
	private void mouseleftDragEnd() {
		mouseDragInit= null;
	}

	private void mouseleftDragRecEnd() {
		// remove rec.
		rootSys.getChildren().remove( dragRec );
		// de HL copylist content if there is any.
		if( copylist != null && copylist.size() > 0 ){
			for( PinCopyable tmp : copylist ){
				tmp.selectDeHL();
			}
		}
		dragRec= null;
	}

	/*-----------------------------------------------------------------------------------------
	 * create a pop up menu for inter the new board name.
	 */
	private void setBoardNamePopUp() {
		popUp pu= new popUp( "Set Board Name." );
		VBox comp= new VBox();
		Label sig= new Label( "Press Enter key to set and exit." );
		TextField nameField= new TextField( elm.getAttribute( "Name" ) );
		nameField.setOnKeyPressed( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				if( event.getCode() == KeyCode.ENTER ){
					pu.close();
					elm.setAttribute( "Name", nameField.getText() );
					setTitle();
				}
			}
		} );
		comp.getChildren().addAll( sig, nameField );
		Scene stageScene= new Scene( comp, 300, 100 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * create a pop up menu for inter the new board name.
	 */
	private void setScrollGridSizePopUp() {
		popUp pu= new popUp( "Set Scroll Grid Size." );
		VBox comp= new VBox();
		Label sig= new Label( "Press Enter key to set and exit." );
		TextField nameField= new TextField( elm.getAttribute( "ScrollGridSize" ) );
		nameField.setOnKeyPressed( new EventHandler <KeyEvent>() {
			@Override
			public void handle( KeyEvent event ) {
				if( event.getCode() == KeyCode.ENTER ){
					pu.close();
					try{
						Integer.parseInt( nameField.getText() );
						elm.setAttribute( "ScrollGridSize", nameField.getText() );
					}catch ( Exception ee ){}
				}
			}
		} );
		comp.getChildren().addAll( sig, nameField );
		Scene stageScene= new Scene( comp, 300, 100 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void removeRightMenu() {
		if( rightMenu != null ){
			root.getChildren().remove( rightMenu );
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void popUpConsoleLog() {
		popUp pu= new popUp( "Console Output." );
		VBox comp= new VBox();
		TextArea ta= new TextArea( Helper.ALstr2str( p.CMB ) );
		ta.setEditable( false );
		ta.setWrapText( true );
		int w= _SysConfig.getScreenSizeX();
		int h= _SysConfig.getScreenSizeY();
		ta.setMaxWidth( w / 2 );
		ta.setMaxHeight( h / 2 );
		ta.setMinWidth( w / 2 );
		ta.setMinHeight( h / 2 );
		comp.getChildren().add( ta );
		Scene stageScene= new Scene( comp, w / 2, h / 2 );
		pu.set( stageScene, true );
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void SetBg() {
		bgNode.getChildren().removeAll( bgNode.getChildren() );
		//
		if( elm.getAttribute( "BgSelection" ).equals( "img" )
				&& !elm.getAttribute( "BackGroundImage" ).equals( "null" ) ){
			try{
				FileInputStream input;
				input= new FileInputStream( elm.getAttribute( "BackGroundImage" ) );
				Image img= new Image( input );
				ImageView imgV= new ImageView();
				imgV.setImage( img );
				imgV.setTranslateZ( 4 );
				//
				focusHolder.setStyle( "-fx-background-color: rgba(1.0, 1.0, 1.0, 0.03);" );
				bgNode.getChildren().add( imgV );
				return;
			}catch ( FileNotFoundException e ){
				// silent fail.
			}
		}
		focusHolder.setStyle( "-fx-background-color: " +
				"linear-gradient(#" + elm.getAttribute( "ColorBackGround1" ) +
				", #" + elm.getAttribute( "ColorBackGround2" ) + " );" );
	}
}
