package MagnetsSystem;

import ParticlesSystem.Particle;
import Utils.Plane;

/**
 * A plane magnet is a magnet materialized by an infinite plane.
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 */
public class PlaneMagnet extends Magnet
{
    protected Plane plane;
	
	/**
	 * Construct a line magnet
	 * @param line Support of the magnet
	 * @param force Attractive force 
	 */
	public PlaneMagnet(Plane plane, double force)
	{
		super(force);
		this.plane = plane;
	}
    
	/**
	 * Compute the attractive force to apply
	 * @param particle Particle on which force will be apply
	 */
	@Override	
	public void applyMagnetForce(Particle particle) {
		new PointMagnet(plane.getOrthographicProjection(particle.getPosition()), force * WEIGHT).applyMagnetForce(particle);
	}

	public Plane getPlane() {
		return plane;
	}

	@Override
	public String toString() {
		return "PlaneMagnet " + plane.toString() + " " + force;
	}
}
