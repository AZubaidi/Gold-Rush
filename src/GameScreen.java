import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
// box2d imports
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.ContactImpulse;

// tilemap imports
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class GameScreen extends BaseScreen
{
	private Player player;
	private World world;
	private int coins = 0;

	private ParticleActor baseSparkle;
	
	private ShapeRenderer shapeRenderer;
	
	SpriteBatch batch;
    Animation animation;

	private double angle;
	private double angleInDegrees;
	private float deltaX;
	private float deltaY;

	private int fireballSpeed;
	private Fireball fireball;
	
	private Image winImage;
	private TextButton replayButton;
	private Label timeLabel;
	private Label highScoreLabel;
	private Label finalTime;
	
	private float timeElapsed;
	private float highScore;
	private float currentScore;
	private boolean winCondition = false;

	private float mouseX;
	private float mouseY;

	private int direction = 1;

	private int energyCount=3;
	private BaseActor[] energy;
	private int energyTimer;

	private int jumpAllowed=1;



	private ArrayList<Box2DActor> removeList;

	TiledMap tiledMap;
	OrthographicCamera tiledCamera;
	TiledMapRenderer tiledMapRenderer;
	int[] backgroundLayer = {0};
	int[] tileLayer       = {1};

	// game world dimensions
	final int mapWidth = 1920; // bigger than before!
	final int mapHeight = 1280;

	public GameScreen(BaseGame g)
	{  super(g);  }

	public void energyCheck(int numberOfCheck) {
		for (int i=0; i<numberOfCheck;i++) {
			energy[i].setX(player.getX()-100+20*i);
			energy[i].setY(player.getY()+50);
		}
	}
	public void energyUpdate(BaseActor newCoin) {
		newCoin.setPosition(10f, 10f);
	}

	public void addSolid(RectangleMapObject rmo)
	{
		Rectangle r = rmo.getRectangle();            
		Box2DActor solid = new Box2DActor();
		solid.setPosition(r.x, r.y);
		solid.setSize(r.width, r.height);
		solid.setStatic();
		solid.setShapeRectangle();
		solid.initializePhysics(world);
	}

	public void create() 
	{        
		world = new World(new Vector2(0, -9.8f), true);
		removeList = new ArrayList<Box2DActor>();
		shapeRenderer=new ShapeRenderer();
		// background image provided by tilemap

		energy=new BaseActor[3];

		// player
		player = new Player();
		
		Animation walkAnim = GameUtils.parseImageFiles( 
				"assets/walk ", ".png", 3, 0.15f, Animation.PlayMode.LOOP_PINGPONG);
		player.storeAnimation( "walk", walkAnim );
		
		Animation standAnim = GameUtils.parseImageFiles( 
				"assets/idle ", ".png", 12, 0.15f, Animation.PlayMode.LOOP_PINGPONG);
		player.storeAnimation( "stand", standAnim );


		Texture jumpTex = new Texture(Gdx.files.internal("assets/jump.png"));
		jumpTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		player.storeAnimation( "jump", jumpTex );

		player.setSize(60,90);
		mainStage.addActor(player);

		// coin
		Coin baseCoin = new Coin();
		Texture coinTex = new Texture(Gdx.files.internal("assets/coin.png"));
		coinTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		baseCoin.storeAnimation( "default", coinTex );

		baseSparkle = new ParticleActor();
		baseSparkle.load("assets/sparkler.pfx", "assets/");

		//fireball
		fireballSpeed=600;

		fireball = new Fireball();
		Texture fireballTex = new Texture(Gdx.files.internal("assets/fireball_resized.png"));
		fireballTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		 Animation fireballAnimation = GameUtils.parseImageFiles( 
                "assets/fireball_000", ".png", 6, 0.15f, Animation.PlayMode.LOOP_PINGPONG);
        fireball.storeAnimation("default", fireballAnimation);
		fireball.setSize(75, 75);
		fireball.setStatic();
        fireball.setPosition(10000, 10000);
        fireball.initializePhysics(world);
        mainStage.addActor(fireball);
        
        
        //win screen assets 
        Texture winTex = new Texture(Gdx.files.internal("assets/Win.png"), true);
        winTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        winImage = new Image( winTex );
        
        //replay button
        replayButton = new TextButton("replay", game.skin, "uiTextButtonStyle");
        replayButton.addListener(
            new InputListener()
            {
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
                {  return true;  } 

                public void touchUp (InputEvent event, float x, float y, int pointer, int button) 
                {  
                	togglePaused();
                	game.setScreen( new GameScreen(game) );
                }
            });
        
        //time label 
        timeLabel = new Label( "Time: ---", game.skin, "uiLabelStyle" );
        
        uiTable.setPosition(280, 250);
        uiTable.add(timeLabel);
        
        //final time label
        finalTime = new Label( "Time: ---", game.skin, "uiLabelStyle" );
        
        //highscore label
        highScore = 0;
        highScoreLabel = new Label( "High Score: ---", game.skin, "uiLabelStyle" );


		// load tilemap
		tiledMap = new TmxMapLoader().load("assets/platform-map.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
		tiledCamera = new OrthographicCamera();
		tiledCamera.setToOrtho(false,viewWidth,viewHeight);
		tiledCamera.update();

		MapObjects objects = tiledMap.getLayers().get("ObjectData").getObjects();
		for (MapObject object : objects) 
		{
			String name = object.getName();
			// all object data assumed to be stored as rectangles

			RectangleMapObject rectangleObject = (RectangleMapObject)object;
			Rectangle r = rectangleObject.getRectangle();

			if ( name.equals("player") )
			{
				player.setPosition( r.x, r.y );
			}
			else if ( name.equals("coin") )
			{
				Coin coin = baseCoin.clone();
				coin.setPosition(r.x, r.y);
				mainStage.addActor(coin);
				coin.initializePhysics(world);
			}
			else if ( name.equals("fireball") )
			{				
				fireball.setPosition(r.x, r.y);				
			}
			else
				System.err.println("Unknown tilemap object: " + name);
		}

		player.setDynamic();
		player.setShapeRectangle();
		player.setPhysicsProperties(1, 0.5f, 0.1f);
		player.setMaxSpeedX(2);
		player.setFixedRotation();
		player.initializePhysics(world);

		objects = tiledMap.getLayers().get("PhysicsData").getObjects();
		for (MapObject object : objects) 
		{
			if (object instanceof RectangleMapObject)
				addSolid( (RectangleMapObject)object );
			else
				System.err.println("Unknown PhysicsData object.");
		}

		for (int i=0;i<3;i++) {
			energy [i]= new BaseActor();
			energy[i].setTexture( new Texture(Gdx.files.internal("assets/coin.png")) );
			energy[i].setPosition( player.getX()-100+20*i, player.getY()+100);
			mainStage.addActor( energy[i] );
		}

		world.setContactListener(
				new ContactListener() 
				{
					public void beginContact(Contact contact) 
					{   
						Object objC = GameUtils.getContactObject(contact, Coin.class);
						if (objC != null)
						{
							Object objP = GameUtils.getContactObject(contact, Player.class, "main");
							if (objP != null)
							{
								Coin c = (Coin)objC;
								removeList.add( c );
								ParticleActor sparkle = baseSparkle.clone();
								sparkle.setPosition( 
										c.getX() + c.getOriginX(), c.getY() + c.getOriginY() );
								sparkle.start();
								mainStage.addActor(sparkle);
								player.coinCollected();
							}
							return; // avoid possible jumps
						}

						Object objF = GameUtils.getContactObject(contact, Fireball.class);
						if (objF != null)
						{
							Fireball f = (Fireball)objF;
							f.setPosition(1000f, 1000f);
							ParticleActor sparkle = baseSparkle.clone();
							sparkle.setPosition( 
									f.getX() + f.getOriginX(), f.getY() + f.getOriginY() );
							sparkle.start();
							mainStage.addActor(sparkle);
						}

						Object objP = GameUtils.getContactObject(contact, Player.class, "bottom");
						if ( objP != null )
						{
							Player p = (Player)objP;
							p.adjustGroundCount(1);
							p.setActiveAnimation("stand");
						}
					}

					public void endContact(Contact contact) 
					{
						Object objC = GameUtils.getContactObject(contact, Coin.class);
						if (objC != null)
							return;

						Object objF = GameUtils.getContactObject(contact, Coin.class);
						if (objF != null)
							return;


						Object objP = GameUtils.getContactObject(contact, Player.class, "bottom");
						if ( objP != null )
						{
							Player p = (Player)objP;
							p.adjustGroundCount(-1);
						}

					}

					public void preSolve(Contact contact, Manifold oldManifold) { }

					public void postSolve(Contact contact, ContactImpulse impulse) { }
				});

	}

	public void update(float dt) 
	{   

		removeList.clear();
		world.step(1/60f, 6, 2);


		Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
		tiledCamera.unproject(mousePos);
		mouseX = mousePos.x;
		mouseY = mousePos.y;

		for (Box2DActor ba : removeList)
		{
			ba.destroy();
			world.destroyBody( ba.getBody() );
		}
		
		if (player.checkCoin() == 5) 
		{winCondition=true;}
		
        if (!winCondition)
        {
            timeElapsed += dt;
            timeLabel.setText( "Time: " + (int)timeElapsed );


        }
		
		if (winCondition)
		{
			togglePaused();
			currentScore = timeElapsed;
			if (currentScore < highScore || highScore == 0)
			{
				highScore = currentScore;
	            highScoreLabel.setText("High Score: " + (int)timeElapsed );
			}
			uiTable.clear();
	        uiTable.setPosition(0, 0);
			uiTable.add(winImage);
	        uiTable.row();
	        uiTable.add(timeLabel);
	        uiTable.row();
	        uiTable.add(highScoreLabel);
	        uiTable.row();
	        uiTable.add(replayButton);

		}

		if( Gdx.input.isKeyPressed(Keys.A) )
		{        	
			direction=-1;
			player.applyForce( new Vector2(-3.0f, 0) );
		}

		if( Gdx.input.isKeyPressed(Keys.D) )
		{
			direction=1;
			player.applyForce( new Vector2(3.0f, 0) );
		}
		if( Gdx.input.isKeyPressed(Keys.S) )
		{        	
			winCondition = true;
			System.out.println("DEBUG: remove update() isKeyPressed(s)");
		}
		player.setScale(direction,1);
		if ( player.getSpeed() > 0.1 && player.getAnimationName().equals("stand") )
			player.setActiveAnimation("walk");
		if ( player.getSpeed() < 0.1 && player.getAnimationName().equals("walk") )
			player.setActiveAnimation("stand");





		fireball.setRotation((float)angleInDegrees); 

		fireball.setX((float) (fireball.getX() + fireballSpeed * dt * Math.cos(angle)));
		fireball.setY((float) (fireball.getY() + fireballSpeed * dt * Math.sin(angle)));



		if (energyTimer<200&&energyCount!=3) {
			energyTimer++;
		}
		else if (energyCount!=3) {
			energyCount++;
		}
		else {        	
			energyTimer=0;
		}

		energyCheck(energyCount);

		if (player.isOnGround()) {
			jumpAllowed=1;
		}      
		
		

	}

	// this is the gameloop. update, then render.
	public void render(float dt) 
	{
		uiStage.act(dt);

		// only pause gameplay events, not UI events
		if ( !isPaused() )
		{
			mainStage.act(dt);
			update(dt);
		}


		// render


		Gdx.gl.glClearColor(0,0,0,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Camera mainCamera = mainStage.getCamera();
		mainCamera.position.x =  player.getX() + player.getOriginX();
		// bound main camera to layout
		mainCamera.position.x = MathUtils.clamp(
				mainCamera.position.x, viewWidth/2,  mapWidth - viewWidth/2);
		mainCamera.update();

		mainCamera.position.y =  player.getY() + player.getOriginY();
		// bound main camera to layout
		mainCamera.position.y = MathUtils.clamp(
				mainCamera.position.y, viewWidth/2,  mapWidth - viewWidth/2);
		mainCamera.update();

		// scroll background more slowly to create parallax effect
		tiledCamera.position.x = mainCamera.position.x/4 + mapWidth/4;
		tiledCamera.position.y = mainCamera.position.y/4+mapWidth/4;
		tiledCamera.update();
		tiledMapRenderer.setView(tiledCamera);
		tiledMapRenderer.render(backgroundLayer);



		tiledCamera.position.x = mainCamera.position.x;
		tiledCamera.position.y = mainCamera.position.y;
		tiledCamera.update();
		tiledMapRenderer.setView(tiledCamera);
		tiledMapRenderer.render(tileLayer);

		mainStage.draw();
		uiStage.draw();
		
		
		

	}

	public boolean keyDown(int keycode)
	{
		if (keycode == Keys.P)    
			togglePaused();

		if (keycode == Keys.R)    
			game.setScreen( new GameScreen(game) );

		if (keycode == Keys.W &&jumpAllowed>0 ) 
		{
			jumpAllowed-=1;
			Vector2 jumpVec = new Vector2(0,2);
			player.applyImpulse(jumpVec);
			player.setActiveAnimation("jump");
		}
		if (keycode==Keys.SPACE && energyCount!=0) {
			energyUpdate(energy[energyCount-1]);
			energyCount-=1;

			fireball.setX(player.getX());
			fireball.setY(player.getY());            

			deltaX = mouseX - player.getX();
			deltaY =  mouseY - player.getY();

			angle =  Math.atan2(deltaY, deltaX);
			angleInDegrees = Math.toDegrees(angle);
		}
		if (Gdx.input.isKeyPressed(Keys.E)&&energyCount!=0) 
		{
			energyUpdate(energy[energyCount-1]);
			energyCount-=1;
			ParticleActor sparkle = baseSparkle.clone();
			sparkle.setPosition( 
					player.getX() + player.getOriginX(), player.getY() + player.getOriginY() );
			sparkle.start();
			mainStage.addActor(sparkle);

			if (direction==1) {
				Vector2 jumpVec = new Vector2(-3,2);
				player.applyImpulse(jumpVec);
				player.setActiveAnimation("jump");
			}
			if (direction==-1) {
				Vector2 jumpVec = new Vector2(3,2);
				player.applyImpulse(jumpVec);
				player.setActiveAnimation("jump");
			}
		}
		if (keycode == Keys.S) 
		{
			player.setX(100);
		}

		return false;
	}

}