import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Fireball extends newObject
{
	private int speed;
    public Fireball()
    {  
    super();
    speed = 2;
    }
    
    public void initializePhysics(World world)
    {       
    	setStatic();
        setShapeCircle();
        fixtureDef.isSensor = true;
        super.initializePhysics(world);
    } 
    
    
    public Fireball clone()
    {
        Fireball newbie = new Fireball();
        newbie.copy( this );
        return newbie;
    }
    
    public int speed() { return speed; }
    
     public void updateFireball(Fireball fireball, double angle) {
    	double angleInDegrees = Math.toDegrees(angle);
    	fireball.setRotation((float)angleInDegrees); 

    	Vector2 speedVec = new Vector2((float)(fireball.speed() * Math.cos(angle)),(float) (fireball.speed() * Math.sin(angle)));
        fireball.applyImpulse(speedVec);
    }
}