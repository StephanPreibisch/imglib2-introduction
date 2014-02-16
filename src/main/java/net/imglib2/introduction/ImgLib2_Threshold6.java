package net.imglib2.introduction;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Perform a threshold on part of an image (all RealTypes), using an IterableInterval only
 * display the result in a new ImgLib2 Img.
 */
public class ImgLib2_Threshold6 implements PlugIn
{
	public static void main( String[] args )
	{
		new ImageJ();
		new ImgLib2_Threshold6().run( null );
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
		
		process( Views.iterable( Views.interval( img, min, max ) ) );
	}

	public <T extends RealType<T>> void process( IterableInterval<T> img )
	{
		// define threshold
		T threshold = img.firstElement().copy();
		threshold.setReal( 100 );

		// apply threshold to image
		Img< BitType > thresholdImg = threshold( img, threshold );
		
		// show the new Img that contains the threshold
		ImageJFunctions.show( thresholdImg );
	}

	public <T extends Comparable< T > & Type<T> > Img< BitType > threshold( IterableInterval< T > image, T threshold )
	{
		// create a new ImgLib2 image of same dimensions
		// but using BitType, which only requires 1 bit per pixel
		ImgFactory< BitType > imgFactory = new CellImgFactory< BitType >( 16 );
		Img< BitType > thresholdImg = imgFactory.create( image, new BitType() );

		// test if they have the same iteration order (here we know they don't)
		boolean sameIterationOrder = image.iterationOrder().equals( thresholdImg.iterationOrder() );
		System.out.println( "same iteration order = " + sameIterationOrder );
		
		if ( sameIterationOrder )
		{
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
		}
		else
		{
			// create a localizing cursor on the Img, it will iterate all pixels
			Cursor<T> cursor = image.localizingCursor();

			// create a randomaccess on the binary image and translate it by the min of the input
			final long[] min = new long[ image.numDimensions() ];
			image.min( min );
			RandomAccess<BitType> randomAccess = Views.translate( thresholdImg, min ).randomAccess();
			
			// iterate over all pixels
			while ( cursor.hasNext() )
			{
				// get the value of the next pixel in the input
				T pixelValue = cursor.next();
				
				// set the random access to the location of the cursor
				randomAccess.setPosition( cursor );
				
				// get the value of the next pixel in the output
				BitType thresholdImgValue = randomAccess.get();
	
				// set the 0 or 1 depending on the value
				if ( pixelValue.compareTo( threshold ) > 0 )
					thresholdImgValue.setReal( 1 );
				else
					thresholdImgValue.setReal( 0 );			
			}			
		}
		
		return thresholdImg;
	}
}