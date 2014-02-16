package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;

/**
 * Perform a threshold on an image (32-bit only),
 * display the result in a new ImgLib2 Img.
 */
public class ImgLib2_Threshold2 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_Threshold2().run( null );
	}

	public void run(String arg0)
	{
		// get the current ImageJ ImagePlus
		ImagePlus imp = WindowManager.getCurrentImage();
		
		Img<FloatType> img;
		
		// test if an image is open, otherwise load blobs
		if ( imp == null )
		{
			// create the ImgOpener
	        ImgOpener imgOpener = new ImgOpener();

	        // load the image as FloatType using the ArrayImg
	        try 
	        {
				img = imgOpener.openImg( getClass().getResource( "/blobs.tif" ).getFile(), new ArrayImgFactory<FloatType>(), new FloatType() );
			} 
	        catch (ImgIOException e) 
	        {
				e.printStackTrace();
				return;
			}
	        
	        // display the image
	        ImageJFunctions.show( img );
		}
		else
		{
			// wrap it into an ImgLib2 Img (no copying)
			img = ImageJFunctions.wrapFloat( imp );	
		}

		// test if it could be wrapped
		if ( img == null )
		{
			IJ.log( "Cannot wrap image" );
			return;
		}

		// process wrapped image with ImgLib2
		process( img );
	}

	public void process( Img<FloatType> img )
	{
		// define threshold
		float threshold = 100;

		// compute the threshold on the image in-place
		// (overwrite each pixel with the threshold value)
		Img<FloatType> thresholdImg = threshold( img, threshold );

		// show the new Img that contains the threshold
		ImageJFunctions.show( thresholdImg );
	}

    public Img<FloatType> threshold( Img< FloatType > img, float threshold )
	{
		// create a new ImgLib2 image of same type & dimensions
		ImgFactory<FloatType> imgFactory = img.factory();
		Img<FloatType> thresholdImg = imgFactory.create( img, img.firstElement() );

		// create a cursor on the Img and the destination, it will iterate all pixels
		Cursor<FloatType> cursor = img.cursor();
		Cursor<FloatType> cursorThresholdImg = thresholdImg.cursor();
		
		// iterate over all pixels
		while ( cursor.hasNext() )
		{
			// get the value of the next pixel in the input
			FloatType pixelValue = cursor.next();
			
			// get the value of the next pixel in the output
			FloatType thresholdImgValue = cursorThresholdImg.next();

			// set the 0 or 255 depending on the value
			if ( pixelValue.get() > threshold )
				thresholdImgValue.set( 1 );
			else
				thresholdImgValue.set( 0 );	
		}

		return thresholdImg;
	}
}