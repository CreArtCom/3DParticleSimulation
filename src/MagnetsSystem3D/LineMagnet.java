package MagnetsSystem3D;

import ParticlesSystem3D.Particle;
import Utils3D.Line3;

/**
 * A line magnet is a magnet materialized by an infinite line.
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 */
public class LineMagnet extends Magnet
{
    protected Line3 line;
	
	/**
	 * Construct a line magnet
	 * @param line Support of the magnet
	 * @param force Attractive force 
	 */
	public LineMagnet(Line3 line, double force)
	{
		super(force);
		this.line = line;
	}
    
	/**
	 * Compute the attractive force to apply
	 * @param particle Particle on which force will be apply
	 */
	@Override	
	public void applyMagnetForce(Particle particle) {
		new PointMagnet(line.getOrthographicProjection(particle.getPosition()), force * WEIGHT).applyMagnetForce(particle);
	}

	@Override
	public String toString() {
		return "LineMagnet " + line.toString() + " " + force;
	}
}
