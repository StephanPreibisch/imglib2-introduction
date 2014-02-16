package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Perform a threshold on part of an image (all RealTypes),
 * display the result in a new ImgLib2 Img.
 */
public class ImgLib2_Threshold5 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_Threshold5().run( null );
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
		
		// process part of the image with ImgLib2
		long[] min = new long[ img.numDimensions() ];
		long[] max = new long[ img.numDimensions() ];
		
		for ( int d = 0; d < img.numDimensions(); ++d )
		{
			min[ d ] = 20;
			max[ d ] = img.dimension( d ) - 20;
		}
		
		process( Views.interval( img, min, max ) );
	}

	public <T extends RealType<T>> void process( RandomAccessibleInterval<T> img )
	{
		// define threshold
		T threshold = Views.iterable( img ).firstElement().copy();
		threshold.setReal( 100 );

		// apply threshold to image
		Img< BitType > thresholdImg = threshold( img, threshold );
		
		// show the new Img that contains the threshold
		ImageJFunctions.show( thresholdImg );
	}

	public <T extends Comparable< T > & Type<T> > Img< BitType > threshold( RandomAccessibleInterval< T > image, T threshold )
	{
		// create a new ImgLib2 image of same dimensions
		// but using BitType, which only requires 1 bit per pixel
		ImgFactory< BitType > imgFactory = new PlanarImgFactory< BitType >();
		Img< BitType > thresholdImg = imgFactory.create( image, new BitType() );

		// create a cursor on the Img and the destination, it will iterate all pixels
		Cursor<T> cursor = Views.iterable( image ).cursor();
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