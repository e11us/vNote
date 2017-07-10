package app;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import machine.p;
import machine.Helper;
import machine._SysConfig;
import pin.Pin;
import pin.PinCopyable;
import pin.note.*;



public class DeskTopApp extends Application {
	// 
	public final static Dimension		screenSize		= Toolkit.getDefaultToolkit().getScreenSize();
	//
	protected ArrayList <PinCopyable>	ClipBoard		= new ArrayList <>();
	protected ArrayList <Point2D>		ClipBoardOS		= new ArrayList <Point2D>();
	protected boolean					ClipBoardCut	= false;
	//
	private ArrayList <DeskTopNote>		boardStack		= new ArrayList <>();
	private ArrayList <String>			boardNameStack	= new ArrayList <>();
	private DeskTopNote					currentBoard	= null;
	private Stage						pstage			= null;

	/*-----------------------------------------------------------------------------------------
	 * a public function to call launch.
	 */
	public void run() {
		this.launch( null );
	}

	/*-----------------------------------------------------------------------------------------
	 * init the first desktop node board.
	 */
	@Override
	public void init() {
		currentBoard= new DeskTopNote( _SysConfig.getDeskTopFile(), this );
		if( currentBoard == null ){
			System.exit( 201 );
		}
		boardStack.add( currentBoard );
	}

	/*-----------------------------------------------------------------------------------------
	 * start the desktop.
	 */
	@Override
	public void start( Stage stage ) throws Exception {
		pstage= stage;
		if( _SysConfig.maxWindowOnStart() ){
			stage.setMaximized( true );
		}else{
			stage.setWidth( currentBoard.getDim().getX() );
			stage.setHeight( currentBoard.getDim().getY() );
		}
		//
		stage.setScene( currentBoard.refreshRoot() );
		stage.show();
		currentBoard.setWholeShift();
		//
		stage.getIcons().add( new Image( "file:" + _SysConfig.getSysFolderName() +
				File.separatorChar + "icon.png" ) );
		stage.setOnCloseRequest( new EventHandler <WindowEvent>() {
			public void handle( WindowEvent we ) {
				p.p( "System closing" );
				closeApp();
				System.exit( 0 );
			}
		} );
		//
		stage.widthProperty().addListener( ( obs, oldVal, newVal ) -> {
			currentBoard.setWholeShift();
		} );
		stage.heightProperty().addListener( ( obs, oldVal, newVal ) -> {
			currentBoard.setWholeShift();
		} );
	}

	public Stage getPrimStage() {
		return pstage;
	}

	public int getStageWidth() {
		if( pstage != null )
			return (int)pstage.getWidth();
		return 0;
	}

	public int getStageHeight() {
		if( pstage != null )
			return (int)pstage.getHeight();
		return 0;
	}

	public void setTitle( String inp ) {
		if( pstage != null ){
			// if inp is not null, this is called by board.
			if( inp != null ){
				if( boardNameStack.size() == boardStack.size() )
					boardNameStack.remove( boardNameStack.size() - 1 );
				boardNameStack.add( inp );
			}
			// otherwise, just refresh the title.
			String til= new String();
			for( String tmp : boardNameStack ){
				if( til.length() == 0 )
					til= tmp;
				else til+= "  |  " + tmp;
			}
			pstage.setTitle( til );
		}
	}

	public String createNewBoard() {
		String newf= _SysConfig.getNewRandPath();
		new DeskTopNote( new File( newf ), this );
		return newf;
	}

	public void switch2Board( String inp ) {
		if( pstage == null )
			return;
		//
		currentBoard.storeXMLfil();
		zipBackUp( currentBoard.getBoardFile() );
		//
		currentBoard= new DeskTopNote( new File( inp ), this );
		boardStack.add( currentBoard );
		if( currentBoard == null ){
			System.exit( 201 );
		}
		pstage.setScene( currentBoard.refreshRoot() );
		currentBoard.setWholeShift();
		if( currentBoard.maxWindowOnStart() ){
			pstage.setMaximized( true );
		}else{
			pstage.setWidth( currentBoard.getDim().getX() );
			pstage.setHeight( currentBoard.getDim().getY() );
		}
	}

	public void popBoardStack() {
		if( boardStack.size() == 1 ){
			pstage.setIconified( true );
		}else if( boardStack.size() > 1 ){
			//
			currentBoard.storeXMLfil();
			zipBackUp( currentBoard.getBoardFile() );
			//
			boardStack.remove( currentBoard );
			currentBoard= boardStack.get( boardStack.size() - 1 );
			currentBoard.removeFocusOfMe();
			if( currentBoard.maxWindowOnStart() ){
				pstage.setMaximized( true );
			}else{
				pstage.setWidth( currentBoard.getDim().getX() );
				pstage.setHeight( currentBoard.getDim().getY() );
			}
			pstage.setScene( currentBoard.refreshRoot() );
			currentBoard.setWholeShift();
			//
			while( boardNameStack.size() > boardStack.size() ){
				boardNameStack.remove( boardNameStack.size() - 1 );
			}
			this.setTitle( null );
		}
	}

	public File chooseFile() {
		if( pstage != null ){
			FileChooser fileChooser= new FileChooser();
			fileChooser.setTitle( "Choose a File" );
			return fileChooser.showOpenDialog( pstage );
		}
		return null;
	}

	public File chooseFile( String dir ) {
		if( pstage != null ){
			FileChooser fileChooser= new FileChooser();
			File init= new File( dir );
			if( !init.exists() )
				init.mkdirs();
			fileChooser.setInitialDirectory( init );
			fileChooser.setTitle( "Choose a File" );
			return fileChooser.showOpenDialog( pstage );
		}
		return null;
	}

	public File chooseDir() {
		if( pstage != null ){
			DirectoryChooser chooser= new DirectoryChooser();
			chooser.setTitle( "Choose a Directory" );
			return chooser.showDialog( pstage );
		}
		return null;
	}

	public void zipBackUp( File inp ) {
		byte[] buffer= new byte[1024];
		try{
			File bkf= _SysConfig.getESMBoardBkFolder();
			bkf.mkdirs();
			//
			FileOutputStream fos= new FileOutputStream( new File(
					bkf.getAbsolutePath() + File.separatorChar +
							Helper.getCurrentTimeStampMS() + " " + inp.getName() +
							" BK.zip" ) );
			//
			ZipOutputStream zos= new ZipOutputStream( fos );
			ZipEntry ze= new ZipEntry( inp.getName() );
			zos.putNextEntry( ze );
			FileInputStream in= new FileInputStream( inp );
			//
			int len;
			while( ( len= in.read( buffer ) ) > 0 ){
				zos.write( buffer, 0, len );
			}
			in.close();
			zos.closeEntry();
			zos.close();
		}catch ( IOException ex ){
			ex.printStackTrace();
		}
	}

	/*-----------------------------------------------------------------------------------------
	 * closing the app. ( do all the exit procedure. )
	 */
	private void closeApp() {
		currentBoard.storeXMLfil();
		zipBackUp( currentBoard.getBoardFile() );
		logConcoleOutput();
	}

	private void logConcoleOutput() {
		Helper.writeFile( _SysConfig.getESMBoardBkFolder().getAbsolutePath()
				+ File.separatorChar + "ConsoleLog " +
				Helper.getCurrentTimeStampMS() + ".txt", true, p.CMB );
	}
}
