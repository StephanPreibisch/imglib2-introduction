package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * Perform a threshold on an image (all RealTypes),
 * display the result in a new ImgLib2 Img.
 */
public class ImgLib2_Threshold4 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_Threshold4().run( null );
	}

	public void run(String arg0)
	{
		// get the current ImageJ ImagePlus
		ImagePlus imp = WindowManager.getCurrentImage();
		
		// test if an image is open, otherwise load blobs
		if ( imp == null )
		{
			imp = new ImagePlus( getClass().getResource( "/blobs.tif" ).getFile() );
			imp.show();
		}

		// wrap it into an ImgLib2 Img (no copying)
		Img<FloatType> img = ImageJFunctions.wrapFloat( imp );

		if ( img == null )
		{
			IJ.log( "Cannot wrap image" );
			return;
		}
		
		// process wrapped image with ImgLib2
		process( img );
	}

	public <T extends RealType<T>> void process( Img<T> img )
	{
		// define threshold
		T threshold = img.firstElement().copy();
		threshold.setReal( 100 );

		// apply threshold to image
		Img< BitType > thresholdImg = threshold( img, threshold );
		
		// show the new Img that contains the threshold
		ImageJFunctions.show( thresholdImg );
	}

	public <T extends Comparable< T > & Type<T> > Img< BitType > threshold( Img< T > image, T threshold )
	{
		// create a new ImgLib2 image of same dimensions
		// but using BitType, which only requires 1 bit per pixel
		ImgFactory< BitType > imgFactory = new PlanarImgFactory< BitType >();
		Img< BitType > thresholdImg = imgFactory.create( image, new BitType() );

		// create a cursor on the Img and the destination, it will iterate all pixels
		Cursor<T> cursor = image.cursor();
		Cursor<BitType> cursorThresholdImg = thresholdImg.cursor();
		
		// iterate over all pixels
		while ( cursor.hasNext() )
		{
			// get the value of the next pixel in the input
			T pixelValue = cursor.next();

			// get the value of the next pixel in the output
			BitType thresholdImgValue = cursorThresholdImg.next();

			// set the 0 or 1 depending on the value
			if ( pixelValue.compareTo( threshold ) > 0 )
				thresholdImgValue.setReal( 1 );
			else
				thresholdImgValue.setReal( 0 );			
		}

		return thresholdImg;
	}
}