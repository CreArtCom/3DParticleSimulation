package ParticlesSystem3D;

import BlobsSystem3D.Blob;
import BlobsSystem3D.BlobsSystem;
import BlobsSystem3D.Brush;
import MagnetsSystem3D.MagnetsSystem;
import MagnetsSystem3D.PointMagnet;
import Simulation3D.Max;
import Simulation3D.Particles;
import Utils3D.Vector2;
import Utils3D.Vector3;
import com.cycling74.jitter.JitterMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * A particles' System is a mechanical 3D system which contains particles.
 * 
 * @author	CreArtCom's Studio
 * @author	Léo LEFEBVRE
 * @version	1.0
 */
public class ParticlesSystem 
{	
	public static final int		LEFT_EDGE	= 0;
	public static final int		BOTTOM_EDGE	= 1;
	public static final int		BACK_EDGE	= 2;
	public static final int		TOP_EDGE	= 3;
	public static final int		RIGHT_EDGE	= 4;
	public static final int		FRONT_EDGE	= 5;
	
	public static final int		EDGE_STOP	= 0;
	public static final int		EDGE_BOUND	= 1;
	public static final int		EDGE_BOOM	= 2;
	
    // Default parameters
    private static final Vector2	SEUIL			= new Vector2(0.001f, 0.1f);
	private static final double		MOMENTUM		= 0.05;
	private static final double		STIFFNESS		= 0.5;
	private static final double		FRICTION		= 0.1;
	private static final int		MEMORY			= 2;
	private static final int		MAXPARTICLES	= 1000;
	
	/** Global stiffness force applied to every particles */
	protected double stiffness;
	
	/** Global momentum force applied to every particles */
	protected double momentum;
	
	/** Global friction force applied to every particles */
	protected double friction;
	
	/** 
	 * Global threshold applied to every particles' mouvements.
	 * Movements under the min threshold are considered null, and movements over
	 * the max threshold are casted.
	 */
	protected Vector2 threshold;
	
	/** 
	 * Global memory applied to every particles.
	 * Memory set how many old particles' position to memorize (history)
	 */
	protected int memory;
	
	/** Maximal number of particles allowed in this system */
	protected int maxParticles;
	
	/** Not yet in use */
	protected boolean recycleParticles;
	
	/** 
	 * Temporary list of particles that have to be delete at next update.
	 * Using this list is important to avoid concurrent modification and 
	 * re-synchronize erase events to each update tick.
	 */
	protected List<Particle> particlesToDelete;
	
	/**	Current list of all particles in this system */
    protected List<Particle> particles;
	
	/** Magnets' system */
	protected MagnetsSystem magnetsSystem;
	
	/** Blobs' system */
	protected BlobsSystem blobsSystem;

	/** Object responsible for the simulation */
    protected Particles simulation;

	/** Define on each window edge which comportement to apply on particles */
	protected int[] edgeComportements;
	
	/** Prevent concurrent modification is particles' lists */
	protected Semaphore semaphore;

	/**
	 * Construct a particles' System
	 * @param simulation Object responsible for conducting the simulation
	 */
	public ParticlesSystem(Simulation3D.Particles simulation, MagnetsSystem magnetsSystem, BlobsSystem blobsSystem) 
	{
		this.simulation			= simulation;
		this.magnetsSystem		= magnetsSystem;
		this.blobsSystem		= blobsSystem;
		this.stiffness			= STIFFNESS;
		this.momentum			= MOMENTUM;
		this.friction			= FRICTION;
		this.threshold			= SEUIL;
		this.memory				= MEMORY;
		this.maxParticles		= MAXPARTICLES;
		this.particles			= new ArrayList<Particle>();
		this.particlesToDelete	= new ArrayList<Particle>();
		this.edgeComportements	= new int[]{EDGE_STOP, EDGE_STOP, EDGE_STOP, EDGE_STOP, EDGE_STOP, EDGE_STOP};
		this.semaphore			= new Semaphore(1, false);
	}

	/** 
	 * Update the whole particle's System (each particle position).
	 * If the semaphore is not free, do nothing.
	 * Else, delete every particles in particlesToDelete list and then update each particle position.
	 */
    public void update()
	{	
		if(semaphore.tryAcquire())
		{
			// On récupère la liste des blobs dont les coordonnées ont changées
			Map<Blob, List<Vector3[]>> blobsMouvements = blobsSystem.getBlobsMouvements();

			// Si la gomme est active on met à jour les dim de la matrice et la liste de particules
			if(!particlesToDelete.isEmpty())
			{
				for(Particle particle:particlesToDelete)
					particles.remove(particle);
				particlesToDelete.clear();

				simulation.setOutMatrixDim(memory, particles.size());
			}

			// Mise à jour du système de particules
			for(int i = 0; i < particles.size(); i++)
				updateParticlePosition(i, particles.get(i), blobsMouvements);
			
			semaphore.release();
		}	
    }

	/** Update a particle position (select which forces are applied) */
	private void updateParticlePosition(int index, Particle particle, Map<Blob, List<Vector3[]>> blobsMouvements)
	{
		// On récupère la liste des blobs intéressants
		for(Map.Entry<Blob, List<Vector3[]>> entry : blobsMouvements.entrySet())
		{
			Blob blob = (Blob)entry.getKey();
			
			if(blob.applyForce() || blob.applyEraser() || blob.applyAttractivity())
			{
				for(Vector3[] mouvement : entry.getValue())
				{
					// On ajoute la force des blobs aux particles
					if(blob.applyForce())
						addForce(particle, blob.getBrush(), mouvement);

					// On détruit les particles dans la brosse
					if(blob.applyEraser())
						delete(particle, blob.getBrush(), mouvement);
					
					// On attire les particles dans la brosse
					if(blob.applyAttractivity())
						addAttractivity(particle, blob.getBrush(), mouvement, blob.getAttractiveForce());
				}
			}
		}

		// On ajoute la force des attracteurs
		magnetsSystem.apply(particle);

		particle.update();
		setParticlePosition(index, particle);
	}
	
	/**
	 * Set up in the outMatrix the particle position scaled to Max.GL_...
	 * @param index Line's index in outFreeMatrix
	 * @param particle Particle to set up position
	 */
	protected void setParticlePosition(int index, Particle particle)
	{
		for(int i = 0; i < memory; i++) {
			simulation.setOutMatrix(i, index, particle.getHistory().get(i));
		}
	}
	
	/**
	 * Set up in the initMatrix the initial particle position scaled to Max.GL_...
	 * @param i	Column's index of the particle
	 * @param j Line's index of the particle
	 * @param initPos Initial position of the particle scaled by Max.ENGINE_...
	 */
	protected void setParticleInitPosition(int i, int j, Vector3 initPos) {
		simulation.setInitMatrix(i, j, initPos);
	}
	
	/**
	 * Add some force to the particle if its position intersect the brush
	 * @param particle Particle on which apply force
	 * @param brush Brush used by blob
	 * @param mouvement Describes the blob's mouvement : {position, delta} 
	 */
	protected void addForce(Particle particle, Brush brush, Vector3[] mouvement)
	{
		if(brush.intersect(particle.getPosition(), mouvement[0]))
			particle.addForce(mouvement[1]);
	}
	
	/**
	 * Delete the particle is its position intersect the brush.
	 * The particle is not directly cleared but added to particlesToDelete list.
	 * @param particle Particle to delete
	 * @param brush Brush used by blob
	 * @param mouvement Describes the blob's mouvement : {position, delta} 
	 */
	protected void delete(Particle particle, Brush brush, Vector3[] mouvement)
	{
		if(brush.intersect(particle.getPosition(), mouvement[0]))
			particlesToDelete.add(particle);
	}

	/**
	 * Add attractive force to the particle.
	 * A Magnet point is created on the stored blob position.
	 * @param particle Particle to delete
	 * @param brush Brush used by blob
	 * @param mouvement Describes the blob's mouvement : {position, delta} 
	 * @param force Attractive force to apply
	 */
	private void addAttractivity(Particle particle, Brush brush, Vector3[] mouvement, double force)
	{
		if(brush.intersect(particle.getPosition(), mouvement[0]))
			new PointMagnet(mouvement[0], force).applyMagnetForce(particle);
	}
	
	/**
	 * Add <parameter>nb</parameter> particles in the system at the given position
	 * @param position Position to add particles scaled by Max.ENGINE_...
	 * @param nbToAdd Number of particles to add
	 */
	public void addParticles(Vector3 position, int nbToAdd)
	{
		// Si la matrice est pas pleine on ajoute une ligne
		int diffToMax = maxParticles - particles.size();
		if(diffToMax > 0)
		{
			int realNbToAdd = diffToMax > nbToAdd ? nbToAdd : diffToMax;
			simulation.setOutMatrixDim(memory, particles.size() + realNbToAdd);
		}
		
		semaphore.acquireUninterruptibly();

		for(int i = 0; i < nbToAdd; i++)
		{
			if(particles.size() >= maxParticles)
				particles.remove(0);
			particles.add(new Particle(position, this));
		}

		semaphore.release();
	}
	
	/**
	 * Determine if the system has at least one particle
	 * @return <code>true</code> if at least one particle exists, <code>false</code> otherwise
	 */
	public boolean hasParticles() {
		return !particles.isEmpty();
	}
	
	/**
	 * Reset the particle system.
	 * Particles will be cleared.
	 */
	public void reset()
	{
		semaphore.acquireUninterruptibly();
		particles.clear();
		semaphore.release();
		simulation.setOutMatrixDim(0, 0);
	}

	/** Properly destroy the system */
	public void destroy()
	{
		semaphore.acquireUninterruptibly();
		particles.clear();
		semaphore.release();
		simulation.setOutMatrixDim(0, 0);
	}

	/**
	 * Set the threshold
	 * @param minThreshold New threshold min
	 * @param maxThreshold New threshold max
	 * @return The current particle's system
	 */
	public ParticlesSystem setThreshold(double minThreshold, double maxThreshold) {
		this.threshold = new Vector2(minThreshold, maxThreshold);
		return this;
	}
	
	/**
	 * Set the stiffness
	 * @param stiffness New stiffness
	 * @return The current particle's system
	 */
	public ParticlesSystem setStiffness(double stiffness) {
		this.stiffness = stiffness;
		return this;
	}
	
	/**
	 * Set the friction
	 * @param friction New friction
	 * @return The current particle's system
	 */
	public ParticlesSystem setFriction(double friction) {
		if(Math.abs(friction) > 1.f)
		{
			this.friction = Math.signum(friction) * 1.f;
			simulation.printOut("You cannot set friction up to 100% !");
		}
		else
			this.friction = friction;
		
		return this;
	}
	
	/**
	 * Set the momentum
	 * @param momentum New momentum
	 * @return The current particle's system
	 */
	public ParticlesSystem setMomentum(double momentum) {
		this.momentum = momentum;
		return this;
	}

	/**
	 * Set the maximum of particles
	 * @param maxParticles New maximum
	 */
	public void setMaxParticles(int maxParticles)
	{
		if(maxParticles >= 0)
		{
			// On réduit le nombre max de particules
			if(this.maxParticles > maxParticles)
			{
				// On conserve les maxParticles premières particules
				if(particles.size() > maxParticles)
				{
					semaphore.acquireUninterruptibly();
					particles = particles.subList(0, maxParticles);
					semaphore.release();
				}
				
				// On met à jour les dimensions de la matrice
				simulation.setOutMatrixDim(memory, particles.size());
			}

			this.maxParticles = maxParticles;
		}
	}
	
	/**
	 * Set particles' memory
	 * @param memory New memory
	 */
	public void setMemory(int memory)
	{
		if(memory > 0)
		{
			this.memory = memory;
			simulation.setOutMatrixDim(memory, particles.size());
		}
	}
	
	/**
	 * Getter : stiffness
	 * @return	La constante de raideur de ressort appliquée aux particules du
	 *			système.
     * @since	1.0
     */
	public double getStiffness() {
		return stiffness;
	}

	/**
	 * Getter : momentum
	 * @return	La pondération du moment appliquée aux particules du système.
	 *			Le moment est un le déplacement aléatoire d'une particule.
     * @since	1.0
     */
	public double getMomentum() {
		return momentum;
	}
	
	/**
	 * Getter : friction
	 * @return	Pourcentage de frottement appliqué aux déplacements des 
	 *			particules du système. 0% = libre, 100% = immobile
     * @since	1.0
     */
	public double getFriction() {
		return friction;
	}
	
	/**
	 * Getter : seuilMin
	 * @return	Seuil en deça duquel les particules du système seront 
	 *			considérées comme immobiles.
     * @since	1.0
     */
	public double getSeuilMin() {
		return threshold.x;
	}
	
	/**
	 * Getter : seuilMax
	 * @return	Seuil au dessus duquel les particules du système seront limités.
     * @since	1.0
     */
	public double getSeuilMax() {
		return threshold.y;
	}

	/**
	 * Getter : memory
	 * @return	Nombre de positions à mémoriser pour le tracé du déplacement des
	 *			particules libres du système.
     * @since	1.0
     */
	public int getMemory() {
		return memory;
	}

	/**
	 * Load initial's particles positions from a matrix.
	 * @param jm Matrix of initials positions
	 */
	public void loadParticles(JitterMatrix jm)
	{
		int[] dim = jm.getDim();
		int inf = Math.min(dim[0], maxParticles);
		
		if(inf == maxParticles)
			simulation.printOut("You are trying to add " + dim[0] + " particles but I can't contain more than " + maxParticles + " particles. See the \"" + Simulation3D.Particles.MSG_MAXPART + "\" message.");
		
		semaphore.acquireUninterruptibly();
		particles.clear();

		for(int i = 0; i < inf; i++)
		{
			float[] cell = jm.getcell2dFloat(i, 0);
			particles.add(new Particle(new Vector3(cell[0], cell[1], cell[2]), this));
		}
		semaphore.release();
		
		simulation.setOutMatrixDim(memory, particles.size());
	}
	
	/**
	 * Allow to particles to computes theirs new position when they meet an edge
	 * @param edge Which edge is met (see static members)
	 * @param position Current 1D position (because only one dimension goes through an edge!)
	 * @return New position after the edge meeting
	 */
	public double getEdgePosition(int edge, double position)
	{
		switch(edgeComportements[edge])
		{
			case EDGE_STOP:
				switch(edge)
				{
					case LEFT_EDGE:
						return Max.ENGINE_MIN.x;
					case BACK_EDGE:
						return Max.ENGINE_MIN.y;
					case BOTTOM_EDGE:
						return Max.ENGINE_MIN.z;
					case RIGHT_EDGE:
						return Max.ENGINE_MAX.x;
					case FRONT_EDGE:
						return Max.ENGINE_MAX.y;
					case TOP_EDGE:
						return Max.ENGINE_MAX.z;
				}

			case EDGE_BOUND:
				switch(edge)
				{
					case LEFT_EDGE:
						return (2 * Max.ENGINE_MIN.x) - position;
					case BACK_EDGE:
						return (2 * Max.ENGINE_MIN.y) - position;
					case BOTTOM_EDGE:
						return (2 * Max.ENGINE_MIN.z) - position;
					case RIGHT_EDGE:
						return (2 * Max.ENGINE_MAX.x) - position;
					case FRONT_EDGE:
						return (2 * Max.ENGINE_MAX.y) - position;
					case TOP_EDGE:
						return (2 * Max.ENGINE_MAX.z) - position;
				}
					
			case EDGE_BOOM:
				switch(edge)
				{
					case LEFT_EDGE:
						return Max.ENGINE_MAX.x - (Max.ENGINE_MIN.x - position);
					case BACK_EDGE:
						return Max.ENGINE_MAX.y - (Max.ENGINE_MIN.y - position);
					case BOTTOM_EDGE:
						return Max.ENGINE_MAX.z - (Max.ENGINE_MIN.z - position);
					case RIGHT_EDGE:
						return Max.ENGINE_MIN.x + (position - Max.ENGINE_MAX.x);
					case FRONT_EDGE:
						return Max.ENGINE_MIN.y + (position - Max.ENGINE_MAX.y);
					case TOP_EDGE:
						return Max.ENGINE_MIN.z + (position - Max.ENGINE_MAX.z);
				}
		}
		return position;
	}
	
	/**
	 * Get the current velocity for given edge
	 * @param edge Concerned edge (see static members)
	 * @return Velocity to apply on whatever goes through the edge
	 */
	public double getEdgeVelocity(int edge)
	{
		switch(edgeComportements[edge])
		{
			case EDGE_STOP:
				return 0.f;

			case EDGE_BOUND:
				return -0.5f;

			case EDGE_BOOM:
				return 1.f;
				
			default:
				return 1.f;
		}
	}

	/**
	 * Set the edge comportement for each window's edge (use static members)
	 * @param leftComportement Comportement on window's left edge
	 * @param bottomComportement Comportement on window's bottom edge
	 * @param rightComportement Comportement on window's right edge
	 * @param topComportement Comportement on window's top edge
	 */
	public void setEdges(int leftComportement, int bottomComportement, int rightComportement, int topComportement, int frontComportement, int backComportement)
	{
		edgeComportements[LEFT_EDGE]	= leftComportement;
		edgeComportements[BOTTOM_EDGE]	= bottomComportement;
		edgeComportements[RIGHT_EDGE]	= rightComportement;
		edgeComportements[TOP_EDGE]		= topComportement;	
		edgeComportements[FRONT_EDGE]	= frontComportement;
		edgeComportements[BACK_EDGE]	= backComportement;	
	}
}
