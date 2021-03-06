package BlobsSystem3D;

import Utils3D.Vector3;

/**
 * A brush is a geometric form attached to a blob and which can interfere with 
 * others objects.
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 */
public abstract class Brush
{
	/**
	 * Determine whether the given position is in the current blob's position of this brush
	 * @param position Position of the given point
	 * @param blobPos Current blob position
	 * @return <code>true</code> if the given point is in the Brush, <code>false</code> otherwise
	 */
	public abstract boolean intersect(Vector3 position, Vector3 blobPos);
}
