package BlobsSystem3D;

import Utils3D.Vector3;

/**
 * A box brush is a brush materialized by a 3D parallelepiped.
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 * @since	2.0
 */
public class BoxBrush extends Brush
{	
	/** Width of the rectangle (x) */
	protected double width;
	
	/** Height of the rectangle (y) */
	protected double height;
	
	/** Depth of the rectangle (z) */
	protected double depth;
	
	/** Half-Height of the rectangle. Helps to reduce redundants computes. */
	protected double hHeight;
	
	/** Half-Width of the rectangle. Helps to reduce redundants computes. */
	protected double hWidth;
	
	/** Half-Depth of the rectangle. Helps to reduce redundants computes. */
	protected double hDepth;

	/** Default constructor of a box brush (0.1 area blob-centered) */
	public BoxBrush() {
		this(0.1, 0.1, 0.1);
	}
	
	/**
	 * Construct a box brush
	 * @param width Width of the box
	 * @param height Height of the box
	 * @param depth	Depth of the box
	 */
	public BoxBrush(double width, double height, double depth)
	{
		this.width		= width;
		this.hWidth		= width / 2.;
		this.height		= height;
		this.hHeight	= height / 2.;
		this.depth		= depth;
		this.hDepth		= depth / 2.;
	}
	
	/**
	 * Determine whether the given position is in the current blob's position of this rectangle
	 * @param position Position of the given point
	 * @param blobPos Current blob position
	 * @return <code>true</code> if the given position is in the current rectangle's position, <code>false</code> otherwise
	 */
	@Override
	public boolean intersect(Vector3 position, Vector3 blobPos) {
		return (blobPos.x > (position.x - hWidth) && 
				blobPos.x < (position.x + hWidth) && 
				blobPos.y > (position.y - hHeight) && 
				blobPos.y < (position.y + hHeight) &&
				blobPos.z > (position.z - hDepth) && 
				blobPos.z < (position.z + hDepth));
	}

	/**
	 * Set the box height
	 * @param height New height
	 */
	public void setHeight(double height) {
		this.height = height;
		this.hHeight = height / 2.;
	}

	/**
	 * Set the box width
	 * @param width New width
	 */
	public void setWidth(double width) {
		this.width = width;
		this.hWidth = width / 2.;
	}
	
	/**
	 * Set the box depth
	 * @param depth New depth
	 */
	public void setDepth(double depth) {
		this.depth = depth;
		this.hDepth = depth / 2.;
	}
}
