package app;

import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import machine.p;



public class popUp {
	private Stage	newStage= null;
	private String	name	= null;

	public popUp( String name ) {
		this.name= name;
	}

	public void set( Scene cont, boolean closeOnloseF ) {
		newStage= new Stage();
		newStage.setTitle( name );
		newStage.setScene( cont );
		newStage.focusedProperty().addListener( new ChangeListener <Boolean>() {
			@Override
			public void changed( ObservableValue <? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue ) {
				if( newPropertyValue ){
					// on focus.
				}else{
					// lost focus.			
					if( closeOnloseF )
						newStage.close();
				}
			}
		} );
		newStage.show();
	}

	public void close() {
		if( newStage != null ){
			newStage.close();
		}
	}
}