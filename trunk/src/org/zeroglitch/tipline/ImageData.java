package org.zeroglitch.tipline;

public class ImageData {
	

		   private static ImageData instance = null;
		   protected ImageData() {
		      // Exists only to defeat instantiation.
		   }
		   
		   public static  ImageData getInstance() {
		      if(instance == null) {
		         instance = new ImageData();
		      }
		      return instance;
		   }

}
