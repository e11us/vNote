package machine;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;



public class Helper {
	static final String		alphaNum= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static final Desktop	dk		= Desktop.getDesktop();

	//
	/* =============================================================================================
	 *-////////////////////////////////////////////////////////////////////////////////////////////
	 * =============================================================================================
	 */
	public static javafx.scene.paint.Color str2color( String colorStr ) {
		try{
			if( colorStr.length() == 6 )
				return javafx.scene.paint.Color.web( colorStr );
			if( colorStr.length() == 8 ){
				return javafx.scene.paint.Color.web( colorStr,
						Integer.decode( "0x" + colorStr.substring( 6, 8 ).toLowerCase() ) / 255.0 );
			}
		}catch ( Exception ee ){
			return null;
		}
		return null;
	}

	public static javafx.scene.paint.Color rand2color() {
		return new javafx.scene.paint.Color( Math.random(),
				Math.random(), Math.random(), 1 );
	}

	public static String rand2colorHighStr() {
		String ret= (char) ( ( (int) ( Math.random() * 6 ) ) + 'a' ) + "" +
				(char) ( ( (int) ( Math.random() * 6 ) ) + 'a' ) + "" +
				(char) ( ( (int) ( Math.random() * 6 ) ) + 'a' ) + "" +
				(char) ( ( (int) ( Math.random() * 6 ) ) + 'a' ) + "" +
				(char) ( ( (int) ( Math.random() * 6 ) ) + 'a' ) + "" +
				(char) ( ( (int) ( Math.random() * 6 ) ) + 'a' ) + "";
		return ret;
	}

	//
	/* =============================================================================================
	 *-////////////////////////////////////////////////////////////////////////////////////////////
	 * =============================================================================================
	 */
	/* --------------------------------------------------------------------------
	 * --- return 32 to 126. printable char.
	 * --------------------------------------------------------------------------*/
	public static String randvalidchar() {
		return ( (char) ( Math.round( Math.random() * 96 + 32 ) ) ) + "";
	}

	/* --------------------------------------------------------------------------
	 * if it is all aph num
	 * --------------------------------------------------------------------------*/
	public static boolean isAlphNum( String inp ) {
		if( inp == null || inp.length() == 0 )
			return true;
		char[] arr= inp.toCharArray();
		int val;
		for( char t : arr ){
			val= t;
			if( val < 48 || ( val > 57 && val < 65 ) ||
					( val > 90 && val < 97 ) || val > 122 )
				return false;
		}
		return true;
	}

	/* --------------------------------------------------------------------------
	 * if it all number.
	 * --------------------------------------------------------------------------*/
	public static boolean isNum( String inp ) {
		if( inp == null || inp.length() == 0 )
			return true;
		char[] arr= inp.toCharArray();
		int val;
		for( char t : arr ){
			val= t;
			if( val < 48 || val > 57 )
				return false;
		}
		return true;
	}

	public static boolean containsAlphNum( String inp ) {
		if( inp == null || inp.length() == 0 )
			return false;
		char[] arr= inp.toCharArray();
		for( char t : arr ){
			int val= t;
			if( val < 48 || ( val > 57 && val < 65 ) ||
					( val > 90 && val < 97 ) || val > 122 )
				;
			else return true;
		}
		return false;
	}

	public static String clearNonAlpha( String inp ) {
		if( inp == null || inp.length() == 0 )
			return "";
		char[] arr= inp.toCharArray();
		for( int i= 0; i < arr.length; i++ ){
			int val= arr[i];
			if( val < 65 || ( val > 90 && val < 97 ) || val > 122 )
				arr[i]= ' ';
		}
		return new String( arr );
	}

	/* --------------------------------------------------------------------------
	 * --- return a random alphanumerical string of length 32. */
	public static String rand32AN() {
		StringBuilder ret= new StringBuilder( "" );
		for( int i= 0; i < 32; i++ ){
			ret.append( randAlphNum() );
		}
		return ret.toString();
	}

	/* --------------------------------------------------------------------------
	 * --- return a random alphanumerical string of length 40. */
	public static String rand40AN() {
		StringBuilder ret= new StringBuilder( "" );
		for( int i= 0; i < 40; i++ ){
			ret.append( randAlphNum() );
		}
		return ret.toString();
	}

	/* --------------------------------------------------------------------------
	 * --- return a random alphanumerical string of length 40.*/
	public static String randAN( int itor ) {
		if( itor <= 0 )
			return "";
		StringBuilder ret= new StringBuilder( "" );
		for( int i= 0; i < itor; i++ ){
			ret.append( randAlphNum() );
		}
		return ret.toString();
	}

	public static char randAlphNum() {
		return alphaNum.charAt( (int) ( Math.random() * 62 ) );
	}

	//
	/* =============================================================================================
	 *-////////////////////////////////////////////////////////////////////////////////////////////
	 * =============================================================================================
	 */
	/*||----------------------------------------------------------------------------------------------
	 ||| return a uniq file name by adding # at end if input filename exits. input is file path.
	||||--------------------------------------------------------------------------------------------*/
	public static String existingFileName( String inp ) {
		File ext= null;
		String name= inp;
		boolean sig= true;
		int ind= 1;
		while( sig ){
			ext= new File( name );
			if( ext.exists() ){
				name= Helper.getFileName( inp ) + "-" + ind++ + "."
						+ Helper.getFileExt( inp );
			}else{
				sig= false;
			}
		}
		return name;
	}

	/*||----------------------------------------------------------------------------------------------
	 ||| get filename without ext.
	||||--------------------------------------------------------------------------------------------*/
	public static String getFileName( String inp ) {
		if( inp == null )
			return null;
		for( int i= inp.length() - 1; i >= 0; i-- ){
			if( inp.charAt( i ) == '.' )
				return inp.substring( 0, i );
		}
		return inp;
	}

	/*||----------------------------------------------------------------------------------------------
	 ||| get the file ext. ( res without the '.' ) ( return txt instead of .txt )
	||||--------------------------------------------------------------------------------------------*/
	public static String getFileExt( String inp ) {
		if( inp == null )
			return null;
		for( int i= inp.length() - 1; i >= 0; i-- ){
			if( inp.charAt( i ) == '.' )
				return inp.substring( i + 1, inp.length() );
		}
		return "";
	}

	//
	/* =============================================================================================
	 *-////////////////////////////////////////////////////////////////////////////////////////////
	 * =============================================================================================
	 */
	public static void openFileWithWindows( String tmp2 ) {
		if( tmp2 == null )
			return;
		try{
			dk.getDesktop().open( new File( tmp2 ) );
		}catch ( IOException e ){
			e.printStackTrace();
			return;
		}
	}

	public static void openWebWithWindows( String tmp2 ) {
		if( tmp2 == null )
			return;
		try{
			dk.getDesktop().browse( new URI( tmp2 ) );
		}catch ( Exception e ){
			e.printStackTrace();
			return;
		}
	}

	//
	/* =============================================================================================
	 *-////////////////////////////////////////////////////////////////////////////////////////////
	 * =============================================================================================
	 */
	/*||----------------------------------------------------------------------------------------------
	 ||| return current date.
	||||--------------------------------------------------------------------------------------------*/
	public static String getCurrentDateStamp() {
		SimpleDateFormat sdfDate= new SimpleDateFormat( "yyyy_MM_dd" );// dd/MM/yyyy
		Date now= new Date();
		String strDate= sdfDate.format( now );
		return strDate;
	}

	/*||----------------------------------------------------------------------------------------------
	 ||| return current date and time and ms as int.
	||||--------------------------------------------------------------------------------------------*/
	public static String getCurrentDate() {
		LocalDate date= LocalDate.now();
		DateTimeFormatter fmt= DateTimeFormat.forPattern( "yyyyMMdd" );
		return date.toString( fmt );
	}

	public static String getCurrentTime() {
		SimpleDateFormat sdfDate= new SimpleDateFormat( "HHmmss" );
		Date now= new Date();
		String strDate= sdfDate.format( now );
		return strDate;
	}

	public static String getCurrentTimeMS() {
		DateTime dt= new DateTime();
		DateTimeFormatter fmt2= DateTimeFormat.forPattern( "SSSS" );
		return dt.toString( fmt2 );
	}

	/*||----------------------------------------------------------------------------------------------
	 ||| return current time and date.
	||||--------------------------------------------------------------------------------------------*/
	public static String getCurrentTimeStamp() {
		SimpleDateFormat sdfDate= new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss" );// dd/MM/yyyy
		Date now= new Date();
		String strDate= sdfDate.format( now );
		return strDate;
	}

	/*||----------------------------------------------------------------------------------------------
	 ||| return current time and date. also milli second.
	||||--------------------------------------------------------------------------------------------*/
	public static String getCurrentTimeStampMS() {
		SimpleDateFormat sdfDate= new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss_ms" );// dd/MM/yyyy
		Date now= new Date();
		String strDate= sdfDate.format( now );
		return strDate;
	}

	public static long getTimeLong() {
		return new Date().getTime();
	}

	//
	/* =============================================================================================
	 *-////////////////////////////////////////////////////////////////////////////////////////////
	 * =============================================================================================
	 */
	public static ArrayList <String> str2ALstr( String inp ) {
		ArrayList <String> ret= new ArrayList <>();
		if( inp == null || inp.length() == 0 )
			return ret;
		while( inp.indexOf( '\n' ) != -1 ){
			if( inp.indexOf( '\n' ) == inp.length() - 1 ){
				inp= inp.substring( 0, inp.indexOf( '\n' ) );
				break;
			}
			ret.add( inp.substring( 0, inp.indexOf( '\n' ) ) );
			inp= inp.substring( inp.indexOf( '\n' ) + 1, inp.length() );
		}
		ret.add( inp );
		return ret;
	}

	public static String ALstr2str( ArrayList <String> inp ) {
		StringBuilder ret= null;
		for( String tmp : inp ){
			if( ret == null )
				ret= new StringBuilder( tmp );
			else ret.append( "\n" + tmp );
		}
		return ret.toString();
	}

	//
	/* =============================================================================================
	 *-////////////////////////////////////////////////////////////////////////////////////////////
	 * =============================================================================================
	 */
	public static String getDateOfWeekAsString( int i ) {
		switch( i ){
			case 0 :
				return "Sunday";
			case 1 :
				return "Monday";
			case 2 :
				return "Tuesday";
			case 3 :
				return "WednesDay";
			case 4 :
				return "Thursday";
			case 5 :
				return "Friday";
			case 6 :
				return "Saturday";
			default :
				return null;
		}
	}

	public static String getMonthName( int num ) {
		String month= null;
		DateFormatSymbols dfs= new DateFormatSymbols();
		String[] months= dfs.getMonths();
		if( num >= 0 && num <= 11 ){
			month= months[num];
		}
		return month;
	}

	public static String getWeekDayName( int num ) {
		switch( num ){
			case 1 :
				return "Monday";
			case 2 :
				return "Tuesday";
			case 3 :
				return "Wednesday";
			case 4 :
				return "Thusday";
			case 5 :
				return "Friday";
			case 6 :
				return "Saturday";
			case 7 :
				return "Sunday";
			default :
				return null;
		}
	}

	public static int getMonthNum( String inp ) {
		if( inp == null || inp.length() < 3 )
			return 0;
		switch( inp.substring( 0, 3 ) ){
			case "Jan" :
				return 1;
			case "Feb" :
				return 2;
			case "Mar" :
				return 3;
			case "Apr" :
				return 4;
			case "May" :
				return 5;
			case "Jun" :
				return 6;
			case "Jul" :
				return 7;
			case "Aug" :
				return 8;
			case "Sep" :
				return 9;
			case "Oct" :
				return 10;
			case "Nov" :
				return 11;
			case "Dec" :
				return 12;
			default :
				return 0;
		}
	}

	//
	/* =============================================================================================
	 *-////////////////////////////////////////////////////////////////////////////////////////////
	 * =============================================================================================
	 */
	public static void append2File( String path, String inp ) {
		File spath= new File( path );
		if( !spath.exists() )
			try{
				spath.getParentFile().mkdirs();
				spath.createNewFile();
			}catch ( IOException e1 ){
				e1.printStackTrace();
				return;
			}
		//
		FileWriter fw;
		try{
			fw= new FileWriter( path, true );
			BufferedWriter bw= new BufferedWriter( fw );
			bw.write( inp + "\n" );
			bw.close();
			fw.close();
		}catch ( IOException e ){
			e.printStackTrace();
		}
	}

	public static void append2File( String path, ArrayList <String> inp ) {
		if( inp == null || inp.size() == 0 )
			return;
		File spath= new File( path );
		if( !spath.exists() )
			try{
				spath.getParentFile().mkdirs();
				spath.createNewFile();
			}catch ( IOException e1 ){
				e1.printStackTrace();
				return;
			}
		//
		FileWriter fw;
		try{
			fw= new FileWriter( path, true );
			BufferedWriter bw= new BufferedWriter( fw );
			for( String str : inp )
				bw.write( str + "\n" );
			bw.close();
			fw.close();
		}catch ( IOException e ){
			e.printStackTrace();
		}
	}

	public static void writeFile( String path, boolean OverRideExist, ArrayList <String> cont ) {
		if( path == null || cont == null || cont.size() == 0 )
			return;
		File spath= new File( path );
		if( !spath.exists() || OverRideExist ){
			spath.getParentFile().mkdirs();
			Writer out= null;
			try{
				out= new BufferedWriter( new OutputStreamWriter(
						new FileOutputStream( path ), "UTF-8" ) );
				for( String tt : cont ){
					out.write( tt + "\n" );
				}
				out.close();
			}catch ( FileNotFoundException e ){
				e.printStackTrace();
			}catch ( UnsupportedEncodingException e ){
				e.printStackTrace();
			}catch ( IOException e ){
				e.printStackTrace();
			}finally{
				try{
					out.close();
				}catch ( IOException e ){}
			}
		}
	}

	public static void writeFile( String path, boolean OverRideExist, String cont ) {
		if( path == null || cont == null || cont.length() == 0 )
			return;
		File spath= new File( path );
		if( !spath.exists() || OverRideExist ){
			spath.getParentFile().mkdirs();
			Writer out= null;
			try{
				out= new BufferedWriter( new OutputStreamWriter(
						new FileOutputStream( path ), "UTF-8" ) );
				out.write( cont );
				out.close();
			}catch ( FileNotFoundException e ){
				e.printStackTrace();
			}catch ( UnsupportedEncodingException e ){
				e.printStackTrace();
			}catch ( IOException e ){
				e.printStackTrace();
			}finally{
				try{
					out.close();
				}catch ( IOException e ){}
			}
		}
	}
}
