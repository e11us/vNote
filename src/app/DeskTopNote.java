package app;

import java.io.File;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import machine.Helper;
import machine.p;
import pin.Pin;
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
	private File			file				= null;
	private Document		doc					= null;
	private Element			elm					= null;
	//
	private DeskTopApp		app					= null;
	private Scene			scn					= null;
	private Group			rootSys				= null;
	private Group			root				= null;
	private Region			focusHolder			= null;
	private Node			rightMenu			= null;
	private BorderPane		topMenuP			= null;
	private Point2D			camShift			= new Point2D( 0.0, 0.0 );
	private int				mouseLastX			= 0;
	private int				mouseLastY			= 0;
	//
	private _PinNoteFactory	PNF					= null;
	// 
	private int				ShiftKeyboardSpeedX	= 1;
	private int				ShiftKeyboardSpeedY	= 1;
	private int				ShiftKeyboardSpeedWX= 1;
	private int				ShiftKeyboardSpeedWY= 1;

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
		return ret;
	}

	public void openBoard( String inp ) {
		store();
		app.switch2Board( inp );
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

	public void configBoard() {}

	public void copyNote( PinNoteInterface pin ) {
		app.ClipBoard.removeAll( app.ClipBoard );
		app.ClipBoard.add( pin );
		app.ClipBoardType= "PinNoteInterface";
	}

	public void cutNote( PinNoteInterface pin ) {
		app.ClipBoard.removeAll( app.ClipBoard );
		app.ClipBoardCut= true;
		app.ClipBoard.add( pin );
		app.ClipBoardType= "PinNoteInterface";
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
		if( elm.getAttribute( "GridSizeY" ).equals( "" ) )
			elm.setAttribute( "GridSizeY", "100" );
		if( elm.getAttribute( "GridSpaceX" ).equals( "" ) )
			elm.setAttribute( "GridSpaceX", "15" );
		if( elm.getAttribute( "GridSpaceY" ).equals( "" ) )
			elm.setAttribute( "GridSpaceY", "10" );
		if( elm.getAttribute( "WindowSizeX" ).equals( "" ) )
			elm.setAttribute( "WindowSizeX", "1920" );
		if( elm.getAttribute( "WindowSizeY" ).equals( "" ) )
			elm.setAttribute( "WindowSizeY", "1080" );
		if( elm.getAttribute( "ColorBackGround" ).equals( "" ) )
			elm.setAttribute( "ColorBackGround", "333333" );
		if( elm.getAttribute( "WindowMaxOnStart" ).equals( "" ) )
			elm.setAttribute( "WindowMaxOnStart", "true" );
		if( elm.getAttribute( "Name" ).equals( "" ) )
			elm.setAttribute( "Name", Helper.randAN( 10 ) );
		if( elm.getAttribute( "DateCreate" ).equals( "" ) )
			elm.setAttribute( "DateCreate", Helper.getCurrentDate() );
		if( elm.getAttribute( "ScrollPolicy" ).equals( "" ) )
			elm.setAttribute( "ScrollPolicy", "full" );
	}

	/*-----------------------------------------------------------------------------------------
	 * setup the scene and the root.
	 */
	private void sceneSetup() {
		root= new Group();
		focusHolder= new Region();
		focusHolder.setMinHeight( 10000 );
		focusHolder.setMinWidth( 10000 );
		focusHolder.setTranslateZ( 3 );
		focusHolder.setStyle( "-fx-background-color: #" +
				elm.getAttribute( "ColorBackGround" ) + ";" );
		rootSys= new Group();
		rootSys.getChildren().add( focusHolder );
		rootSys.getChildren().add( root );
		if( Boolean.parseBoolean( elm.getAttribute( "WindowMaxOnStart" ) ) ){
			scn= new Scene( rootSys, DeskTopApp.screenSize.getWidth(),
					DeskTopApp.screenSize.getHeight(), true, SceneAntialiasing.BALANCED );
		}else{
			scn= new Scene( rootSys, getDim().getX(), getDim().getY(),
					true, SceneAntialiasing.BALANCED );
		}
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
				root.setTranslateY( ShiftKeyboardSpeedY + root.getTranslateY() );
				camShift= camShift.add( new Point2D( 0.0, (double) -ShiftKeyboardSpeedY ) );
			}else{
				root.setTranslateY( -ShiftKeyboardSpeedY + root.getTranslateY() );
				camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedY ) );
			}
		} );
		scn.setOnMouseMoved( e -> {
			mouseLastX= (int) ( e.getSceneX() + camShift.getX() );
			mouseLastY= (int) ( e.getSceneY() + camShift.getY() );
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
						// back space.
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
						break;
					case "s" :
						if( !elm.getAttribute( "ScrollPolicy" ).equals( "full" ) ){
							root.setTranslateY( -ShiftKeyboardSpeedY + root.getTranslateY() );
							camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedY ) );
						}else{
							root.setTranslateY( -ShiftKeyboardSpeedWY + root.getTranslateY() );
							camShift= camShift.add( new Point2D( 0.0, (double)ShiftKeyboardSpeedWY ) );
						}
						break;
					case "a" :
						if( !elm.getAttribute( "ScrollPolicy" ).equals( "full" ) ){
							root.setTranslateX( ShiftKeyboardSpeedX + root.getTranslateX() );
							camShift= camShift.add( new Point2D( (double) -ShiftKeyboardSpeedX, 0.0 ) );
						}else{
							root.setTranslateX( ShiftKeyboardSpeedWX + root.getTranslateX() );
							camShift= camShift.add( new Point2D( (double) -ShiftKeyboardSpeedWX, 0.0 ) );
						}
						break;
					case "d" :
						if( !elm.getAttribute( "ScrollPolicy" ).equals( "full" ) ){
							root.setTranslateX( -ShiftKeyboardSpeedX + root.getTranslateX() );
							camShift= camShift.add( new Point2D( (double)ShiftKeyboardSpeedX, 0.0 ) );
						}else{
							root.setTranslateX( -ShiftKeyboardSpeedWX + root.getTranslateX() );
							camShift= camShift.add( new Point2D( (double)ShiftKeyboardSpeedWX, 0.0 ) );
						}
						break;
					case " " :
						store();
						app.zipBackUp( file );
						break;
					case "r" :
						configBoard();
						break;
					case "V" :
						if( app.ClipBoard.size() != 0 ){
							switch( app.ClipBoardType ){
								case "PinNoteInterface" :
									root.getChildren().addAll( PNF.createNewNote(
											app.ClipBoard, mouseLastX, mouseLastY ) );
									break;
							}
							if( app.ClipBoardCut ){
								app.ClipBoard.removeAll( app.ClipBoard );
								app.ClipBoardCut= false;
							}
						}
						break;
					case "e":
					case "E":
						if( topMenuP == null ){
							createTopConfigMenu();
						}else{
							rootSys.getChildren().remove( topMenuP );
							topMenuP= null;
						}
						break;
				}
			}
		} );
	}

	/*-----------------------------------------------------------------------------------------
	 * mouse left click on empty space.
	 */
	private void LeftClick( MouseEvent e ) {
		removeFocusOfMe();
		removeRightMenu();
	}

	/*-----------------------------------------------------------------------------------------
	 * mouse right click on empty space.
	 */
	private void RightClick( MouseEvent e ) {
		removeFocusOfMe();
		//createBoardRightMenu( e.getSceneX() + camShift.getX(), e.getSceneY() + camShift.getY() );
		createBoardRightMenu( e.getSceneX(), e.getSceneY(), camShift.getX(), camShift.getY() );
	}

	/*-----------------------------------------------------------------------------------------
	 * create a right click menu.
	 */
	private void createBoardRightMenu( double x, double y, double xx, double yy ) {
		removeRightMenu();
		ContextMenu rightMenu= new ContextMenu();
		for( String tmp : _PinNoteFactory.NoteTypes ){
			MenuItem tmi= new MenuItem( "New " + tmp );
			tmi.setOnAction( e -> {
				root.getChildren().add( PNF.createNewNote(
						tmi.getText().substring( 4, tmi.getText().length() ),
						(int) ( xx + x ), (int) ( yy + y ) ) );
				store();
				removeRightMenu();
				removeFocusOfMe();
			} );
			rightMenu.getItems().add( tmi );
		}
		if( topMenuP == null ){
			MenuItem confi= new MenuItem( "Show Board Config" );
			confi.setOnAction( e -> {
				createTopConfigMenu();
			} );
			rightMenu.getItems().add( confi );
		}else{
			MenuItem confi= new MenuItem( "Close Board Config" );
			confi.setOnAction( e -> {
				rootSys.getChildren().remove( topMenuP );
				topMenuP= null;
			} );
			rightMenu.getItems().add( confi );
		}
		//
		rightMenu.show( root, x, y );
	}

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
			for( int i= 30; i < 500; i+= 5 ){
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
			for( int i= 30; i < 500; i+= 5 ){
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
			RadioMenuItem partSP= new RadioMenuItem( "partial scroll" );
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
			//
			ppt.getItems().add( new SeparatorMenuItem() );
			//
			/*
			
			// File menu - new, save, exit
			Menu fileMenu = new Menu("System");
			
			MenuItem saveMenuItem = new MenuItem("Save Board Now");
			MenuItem exitMenuItem = new MenuItem("Exit System");
			
			fileMenu.getItems().addAll(newMenuItem, saveMenuItem,
			    new SeparatorMenuItem(), exitMenuItem);
			
			Menu webMenu = new Menu("Web");
			CheckMenuItem htmlMenuItem = new CheckMenuItem("HTML");
			htmlMenuItem.setSelected(true);
			webMenu.getItems().add(htmlMenuItem);
			
			CheckMenuItem cssMenuItem = new CheckMenuItem("CSS");
			cssMenuItem.setSelected(true);
			webMenu.getItems().add(cssMenuItem);
			
			
			
			Menu tutorialManeu = new Menu("Tutorial");
			tutorialManeu.getItems().addAll(
			    new CheckMenuItem("Java"),
			    new CheckMenuItem("JavaFX"),
			    new CheckMenuItem("Swing"));
			
			sqlMenu.getItems().add(tutorialManeu);
			
			
			
			menuBar.getMenus().addAll(exitConfig,fileMenu, webMenu, sqlMenu);
			
			*/
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * 
	 */
	private void removeRightMenu() {
		if( rightMenu != null ){
			root.getChildren().remove( rightMenu );
		}
	}
}
