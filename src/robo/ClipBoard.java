package robo;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.TreeSet;
import machine.p;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.*;



public class ClipBoard implements _SystemClockLis {
	//
	public static void addListener( ClipBoardLis lis ) {
		listeners.add( lis );
	}

	public static void removeListener( ClipBoardLis lis ) {
		if( listeners.contains( lis ) )
			listeners.remove( lis );
	}

	public static ClipBoard _init() {
		if( tk == null ){
			tk= Toolkit.getDefaultToolkit().getSystemClipboard();
			return new ClipBoard();
		}else return null;
	}

	public static String getClipBoard() {
		return last;
	}

	public static void setClipBoard( String inp ) {
		StringSelection selection= new StringSelection( inp );
		tk.setContents( selection, selection );
	}

	@Override
	public void doTask() {
		try{
			last= (String)tk.getData( DataFlavor.stringFlavor );
			for( ClipBoardLis tmp : listeners ){
				tmp.reciveContent( last );
			}
		}catch ( UnsupportedFlavorException | IOException e ){
			// silent fail.
			//e.printStackTrace();
		}
	}

	//
	//----------------------------------------------------------------------------------------------------
	private static HashSet <ClipBoardLis>	listeners	= new HashSet();
	private static Clipboard				tk			= null;
	private static String					last		= null;

	private ClipBoard() {
		// private constructor.
	}
}
