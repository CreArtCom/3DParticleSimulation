package ParticlesSystem;

import Simulation3D.Max;
import Utils.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A particle is a 3D point in a mechanical system.
 *
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 */
public class Particle
{	
	/** System responsible for this particle */
	protected ParticlesSystem system;
	
	/** Random generator used to apply moment */
    protected Random generator;
	
	/** Current net force */
	protected Vector3 force;
	
	/** Initial position of this particle, used to apply stiffness */
	protected Vector3 initPos;
	
	/** "History" of the particle : the system.getMemory()'th old positions of this particle */
	protected List<Vector3> oldPos;
	
	/** Current position of the particle scaled by Max.ENGINE_... */
    protected Vector3 position;
    
	/**
	 * Abstract constructor of a particle
	 * @param position Particle's position
	 * @param system Particle's System
	 */
    public Particle(Vector3 position, ParticlesSystem system)
    {
		this.system = system;
		this.generator = new Random();
		this.position = new Vector3(position);
		this.initPos = new Vector3(position);
		this.force = new Vector3();
		this.oldPos = new ArrayList<Vector3>(system.getMemory());
		clearHistory();
    }
	
	/**	Apply some random mouvement to the particle weighted by system.getMomentum() */
	protected void applyMoment()
	{
		Vector3 moment = new Vector3(generator.nextFloat() - 0.5f, generator.nextFloat() - 0.5f, generator.nextFloat() - 0.5f);
		moment.Scalar(system.getMomentum());
		force.Add(moment);
	}
	
	/** Apply some stiffness to the particle weighted by system.getStiffness() */
	protected void applyStiffness()
	{
		Vector3 delta = new Vector3(position);
		delta.Subtract(initPos);
		delta.Scalar(system.getStiffness());
		position.Subtract(delta);
	}
	
	/** Apply somme friction to the particle weighted by system.getFriction() */
	protected void applyFriction()
	{
		Vector3 temp = new Vector3(force);
		temp.Scalar(system.getFriction());
		force.Subtract(temp);
	}
	
	/** Compute the new particle's position */
	public void update() 
	{
		// Apply forces
		applyMoment();
		applyStiffness();
		
		// Update position
		computeRealPosition();
		applyFriction();
		
		// On dépile le trop plein et/ou le plus vieux
		while(oldPos.size() >= system.getMemory()) {
			oldPos.remove(0);
		}

		// On empile le manquement et/ou le nouveau
		while(oldPos.size() < system.getMemory()) {
			oldPos.add(new Vector3(position));
		}
	}
	
	/** 
	 * Check if the new virtually computed position is really reachable.
	 * If it is reachable, do nothing, otherwise, computes the farest postion reachable.
	 * A position could be unreachable by the fault of system.threshold or window borders.
	 */
	protected void computeRealPosition()
	{
		Vector3 netForce = new Vector3(force);
		double delta = netForce.getNorm();

		// Maximum threshold is overpassed
		if(delta > system.getSeuilMax()) {
			netForce.Scalar(system.getSeuilMax() / delta);
		}
		
		// Minimum threshold is overpassed
		if(delta > system.getSeuilMin())
		{
			// New position
			position.Add(netForce);

			// Left edge reached
			if(position.x < Max.ENGINE_MIN.x) {
				position.x = system.getEdgePosition(ParticlesSystem.LEFT_EDGE, position.x);
				force.x *= system.getEdgeVelocity(ParticlesSystem.LEFT_EDGE);
			}
			// Right edge reached
			else if(position.x > Max.ENGINE_MAX.x) {
				position.x = system.getEdgePosition(ParticlesSystem.RIGHT_EDGE, position.x);
				force.x *= system.getEdgeVelocity(ParticlesSystem.RIGHT_EDGE);
			}

			// Back edge reached
			if(position.y < Max.ENGINE_MIN.y) {
				position.y = system.getEdgePosition(ParticlesSystem.BACK_EDGE, position.y);
				force.y *= system.getEdgeVelocity(ParticlesSystem.BACK_EDGE);
			}
			// Front edge reached
			else if(position.y > Max.ENGINE_MAX.y) {
				position.y = system.getEdgePosition(ParticlesSystem.FRONT_EDGE, position.y);
				force.y *= system.getEdgeVelocity(ParticlesSystem.FRONT_EDGE);
			}

			// Bottom edge reached
			if(position.z < Max.ENGINE_MIN.z) {
				position.z = system.getEdgePosition(ParticlesSystem.BOTTOM_EDGE, position.z);
				force.z *= system.getEdgeVelocity(ParticlesSystem.BOTTOM_EDGE);
			}
			// Top edge reached
			else if(position.z > Max.ENGINE_MAX.z) {
				position.z = system.getEdgePosition(ParticlesSystem.TOP_EDGE, position.z);
				force.z *= system.getEdgeVelocity(ParticlesSystem.TOP_EDGE);
			}
		}
	}
	
	/** Clear old positions' history of this particle */
	private void clearHistory()
	{
		oldPos.clear();
		
		for(int k = 0; k < system.getMemory(); k++) {
			oldPos.add(new Vector3(position));
		}
	}
    

	/**
	 * Apply a force for the next update (erase others)
	 * @param delta Force value to apply
	 */
	public void applyForce(Vector3 delta) {
		force = new Vector3(delta);
	}

	/**
	 * Add a force for the next update (added to others)
	 * @param delta Force value to add
	 */
	public void addForce(Vector3 delta) {
		force.Add(delta);
	}
	
	public List<Vector3> getHistory() {
		return oldPos;
	}
	
	public Vector3 getPosition() {
		return position;
	}
}
