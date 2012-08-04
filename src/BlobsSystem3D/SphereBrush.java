package BlobsSystem3D;

import Utils3D.Vector3;

/**
 * A sphere brush is a brush materialized by a 3D sphere.
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 */
public class SphereBrush extends Brush
{
	/** Radius of the circle */
	protected double radius;

	/** Default constructor of a Brush (radius = 0.1) */
	public SphereBrush() {
		this.radius	= 0.1f;
	}
	
	/**
	 * Construct a circle brush
	 * @param radius Radius of the circle
	 */
	public SphereBrush(double radius) {
		this.radius	= radius;
	}
	
	/**
	 * Determine whether the given position is in the current blob's position of this circle
	 * @param position Position of the given point
	 * @param blobPos Current blob position
	 * @return <code>true</code> if the given position is in the current circle's position, <code>false</code> otherwise
	 */
	@Override
	public boolean intersect(Vector3 position, Vector3 blobPos)
	{
		Vector3 temp = new Vector3(position);
		temp.Subtract(blobPos);
		return (temp.getNorm() <= radius);
	}
	
	/**
	 * Set the radius value
	 * @param radius New radius
	 */
	public void setRadius(double radius) {
		this.radius	= radius;
	}
}
