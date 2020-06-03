import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 
import shiffman.box2d.*; 
import org.jbox2d.common.*; 
import org.jbox2d.dynamics.joints.*; 
import org.jbox2d.collision.*; 
import org.jbox2d.collision.shapes.*; 
import org.jbox2d.collision.shapes.Shape; 
import org.jbox2d.dynamics.*; 
import org.jbox2d.dynamics.contacts.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class BBTAN extends PApplet {

//---------------------------------------------------------------------------------------------------------------
// Never touch these










SoundFile blop;
SoundFile zap;
ArrayList<Ball> balls;
ArrayList<Block> blocks;
ArrayList<Boundary> boundaries;
ArrayList<Special> specials;
ArrayList<ParticleSystem> fireworks;
PVector mouseInit, mouseFinal;
boolean aimed;

Box2DProcessing world;

//-----------------------------------------------------------------------------------------------------------------
// Game params
float nBlocks;
int shootInterval;
float radiusUniv;
float ballVelocity;

//-----------------------------------------------------------------------------------------------------------------
// Ingame vars
int startTimer;
int level;
int shootTimer;
int nOnFloor;
int addBall;
float xRest;
float animOffset;
boolean processing;
boolean shootActive;
boolean triggerAnim;
boolean playBlop;
boolean playZap;
PVector shootDir;

Ball c;

public void setup() {
  
  background(0);
  textFont(createFont("wagner.ttf", 32));
  blop = new SoundFile(this, "blop.wav");
  zap = new SoundFile(this, "zap.wav");
  balls = new ArrayList<Ball>();
  blocks = new ArrayList<Block>();
  boundaries = new ArrayList<Boundary>();
  specials = new ArrayList<Special>();
  fireworks = new ArrayList<ParticleSystem>();
  world = new Box2DProcessing(this);
  world.createWorld();
  world.setGravity(0, 0);
  world.listenForCollisions();
  boundaries.add(new Boundary(-50, -50, 50, height + 100));
  boundaries.add(new Boundary(0, (height / 2) - (9 * width / 14.0f) - 50, width, 50));
  boundaries.add(new Boundary(width, -50, 50, height + 100));

  //
  nBlocks = 7;
  radiusUniv = 10;
  ballVelocity = 80;
  shootInterval = 5;

  //To be reset
  startTimer = millis();
  level = 1;
  xRest = width / 2;
  animOffset = 0;
  triggerAnim = false;

  addLayer(0);

  for (int i = 0; i < 1; i++) {
    balls.add(new Ball(xRest, (height / 2) + (9 * width / 14.0f) - radiusUniv));
  }

  processing = false;
  xRest = width / 2;
  //c.shoot(new PVector(0, -1));
}

public void draw() {
  game();
}

public void game() {

  world.step();
  bg();
  for (int i = blocks.size()-1; i >= 0; i--) {
    Block b = blocks.get(i);
    if (b.value > 0)
      b.show(animOffset);
    if (b.delete) {
      world.destroyBody(b.body);
      blocks.remove(i);
    }
  }
  for (Ball c : balls) {
    c.checkRest();
    c.show();
  }
  for (int i = specials.size()-1; i >= 0; i--) {
    Special s = specials.get(i);
    s.show(animOffset);
    if (s.delete) {
      specials.remove(i);
    }
  }
  for (int i = fireworks.size()-1; i >= 0; i--) {
    ParticleSystem f = fireworks.get(i);
    f.show();
    if (f.delete) {
      fireworks.remove(i);
    }
  }
  if (playBlop && (frameCount % 10 == 0)) {
    blop.play();
    playBlop = false;
  }
  if (playZap && (frameCount % 10 == 0)) {
    zap.play();
    playZap = false;
  }

  //Level up phase
  if (nOnFloor == balls.size() && !triggerAnim) {
    level ++;
    addLayer(-1);
    triggerAnim = true;
  }

  if (triggerAnim) {
    animOffset += 0.05f;
    if (animOffset >= 1)
    {
      for (Block b : blocks)
        b.nextLevel();
      for (Special s : specials)
        s.nextLevel();
      animOffset = 0;
      triggerAnim = false;
      processing = false;
      nOnFloor = 0;

      for (Special s : specials)
        if (s.locY == 7)
          s.delete = true;     

      for (Block b : blocks)
        if (b.locY == 7)
          exit();

      //if game != over
      {
        while (addBall > 0) {
          balls.add(new Ball(xRest, (height / 2) + (9 * width / 14.0f) - radiusUniv));
          addBall--;
        }
      }
    }
  }

  //Shooting phase
  PVector tempDir = shoot();
  if (tempDir != null) {
    shootDir = tempDir;
    shootActive = true;
    shootTimer = 0;
  }

  if (shootActive) {
    if (shootTimer % shootInterval == 0) {
      balls.get(shootTimer / shootInterval).shoot(shootDir, false);
      if (shootTimer / shootInterval == balls.size() - 1)
        shootActive = false;
    }
    shootTimer++;
    fill(255);
    textAlign(CENTER, BOTTOM);
    textSize(18);
    text("x" + (balls.size() - shootTimer / shootInterval), balls.get(balls.size() - 1).c.x, (height / 2) + (9 * width / 14.0f) - 2 * radiusUniv - 10);
  }
}

public PVector shoot() {
  if (!processing) {
    fill(255);
    textAlign(CENTER, BOTTOM);
    textSize(18);
    text("x" + balls.size(), balls.get(balls.size() - 1).c.x, (height / 2) + (9 * width / 14.0f) - 2 * radiusUniv - 10);

    if (!mousePressed) {
      if (aimed) {
        processing = true;
        aimed = false;
        return mouseInit.sub(mouseFinal);
      }
      mouseInit = new PVector(mouseX, mouseY);
    }
    if (mousePressed) {
      mouseFinal = new PVector(mouseX, mouseY);
      stroke(255);
      PVector displace = mouseInit.copy();
      displace.sub(mouseFinal);
      noStroke();
      if (-PI + 0.25f < displace.heading() && -0.25f > displace.heading()) {
        fill(200);
        for (int i = 1; i < 10; i++)
          ellipse(xRest + i * displace.x / 10.0f, (height / 2) + (9 * width / 14.0f) - radiusUniv + i * displace.y / 10.0f, radiusUniv, radiusUniv);
      }
      aimed = true;
      if (mouseInit.x == mouseFinal.x && mouseInit.y == mouseFinal.y)
        aimed = false;
      if (!(-PI + 0.25f < displace.heading() && -0.25f > displace.heading()))
        aimed = false;
    }
  }
  return null;
}

public void addLayer(int begin) {
  int addLoc = floor(random(7));
  specials.add(new Special(addLoc, begin, 1));
  for (int i = 0; i < 7; i++) 
    if (i != addLoc) {
      float tempAdder = random(6);
      if (tempAdder < 4)
        blocks.add(new Block(i, begin, level, 0));
      else if (tempAdder < 4.2f)
        specials.add(new Special(i, begin, 2));
      else if (tempAdder < 4.25f)
        specials.add(new Special(i, begin, 3));
      else if (tempAdder < 4.3f)
        specials.add(new Special(i, begin, 4));
    }
}

public void beginContact(Contact cp) {
  Object a = cp.getFixtureA().getBody().getUserData();
  Object b = cp.getFixtureB().getBody().getUserData();
  if (a.getClass() == Block.class) {
    ((Block) a).deduct(true);
    ((Ball) b).noDestroy = 0;
  }
  if (b.getClass() == Block.class) {
    ((Block) b).deduct(true);
    ((Ball) b).noDestroy = 0;
  }
}

public void endContact(Contact cp) {
}
public void bg() {
  background(0);
  stroke(255); 
  //rect(0, height / 2 - width / 2, width, width);
  line(0, (height / 2) - (9 * width / 14.0f), width, (height / 2) - (9 * width / 14.0f));
  line(0, (height / 2) + (9 * width / 14.0f), width, (height / 2) + (9 * width / 14.0f));

  fill(255);
  textSize(32);
  {  //timer
    int timer = millis() - startTimer;
    int sec = timer / 1000;
    int min = sec / 60;
    int secrem = (59 - (sec % 60));
    int minrem = (29 - min);
    textAlign(RIGHT, CENTER);
    
    String timerDisplay = minrem + " : ";
    
    if (minrem < 10)
      timerDisplay = "0" + timerDisplay;
    if (secrem < 10)
      timerDisplay = timerDisplay + "0";
      
    if ((secrem == 0) && (minrem == 0))
      exit();
    
    timerDisplay+= secrem;
    text(timerDisplay, width - 10, height - (width / 14.0f) - 7);
  }
  { //score
    textAlign(CENTER, CENTER);
    text(level, width / 2, (width / 14.0f));
  }
}
class Ball {
  Vec2 c;
  boolean atRest;
  boolean onDaFloor;
  Body body;
  int noDestroy;
  int specialized;

  Ball(float xin, float yin) {
    atRest = true;
    onDaFloor = true;

    BodyDef bd = new BodyDef();

    bd.position = world.coordPixelsToWorld(xin, yin);
    bd.type = BodyType.DYNAMIC;
    body = world.world.createBody(bd);

    CircleShape cs = new CircleShape();
    cs.m_radius = world.scalarPixelsToWorld(radiusUniv);

    FixtureDef fd = new FixtureDef();
    fd.shape = cs;

    fd.density = 1;
    fd.friction = 0;
    fd.restitution = 1;
    fd.filter.groupIndex = -1;

    body.createFixture(fd);

    body.setUserData(this);

    c = world.getBodyPixelCoord(body);
  }

  public void shoot(PVector dirin, boolean correct) {
    dirin.normalize();
    dirin.mult(ballVelocity);
    body.setLinearVelocity(new Vec2(dirin.x, -dirin.y));
    atRest = false;
    onDaFloor = correct;
  }
  
  public PVector getVelocity() {
    return new PVector(body.getLinearVelocity().x, body.getLinearVelocity().y);
  }

  public void show() {
    fill(255);
    noStroke();
    c = world.getBodyPixelCoord(body);
    pushMatrix();
    translate(c.x, c.y);
    ellipse(0, 0, radiusUniv * 2, radiusUniv * 2);
    popMatrix();
  }

  public void checkRest() {
    if (!onDaFloor) {
      noDestroy++;
      if (noDestroy >= 1000) {
        noDestroy = 0;
        shoot(new PVector(0, 1), false);
      }
      for (Special s : specials)
        s.detectCollision(this);
    }
    if ((c.y + radiusUniv > (height / 2) + (9 * width / 14.0f)) && !onDaFloor) {
      noDestroy = 0;
      if (nOnFloor == 0)
        xRest = c.x;
      onDaFloor = (c.x == xRest);
      if (c.x < xRest)
        shoot(new PVector(1, 0), true);
      if (c.x > xRest)
        shoot(new PVector(-1, 0), true);
      body.setTransform(world.coordPixelsToWorld(c.x, (height / 2) + (9 * width / 14.0f) - radiusUniv + 2), 0);
    }
    if (onDaFloor && !atRest) {
      float tolerance = 10;
      if ((c.x > xRest - tolerance) && (c.x < xRest + tolerance)) {
        body.setLinearVelocity(new Vec2(0, 0));
        body.setTransform(world.coordPixelsToWorld(xRest, (height / 2) + (9 * width / 14.0f) - radiusUniv), 0);
        atRest = true;
        nOnFloor++;
      }
    }
  }
}
class Block {
  int locX, locY;
  int value;
  int type;
  int blockColor;
  boolean isDouble;
  boolean delete;
  Body body;

  //   Block pos :
  //   LT     (locX * width / nBlocks)                       ((locY * width / nBlocks) + (height / 2) - (width / 2))
  //   RT     (locX * width / nBlocks + width / nBlocks)     ((locY * width / nBlocks) + (height / 2) - (width / 2))
  //   RB     (locX * width / nBlocks + width / nBlocks)     ((locY * width / nBlocks) + (height / 2) - (width / 2) + width / nBlocks)
  //   LB     (locX * width / nBlocks)                       ((locY * width / nBlocks) + (height / 2) - (width / 2) + width / nBlocks)

  Block(int inX, int inY, int inV, int inT) {
    locX = inX;
    locY = inY;
    value = inV;
    type = inT;
    if (type == 0) {
      float randomBlock = random(8);
      if (randomBlock < 4) type = -1;
      else if (randomBlock < 5) type = 1;
      else if (randomBlock < 6) type = 2;
      else if (randomBlock < 7) type = 3;
      else type = 4;
    }
    isDouble = false;
    if (value % 10 == 0 && random(1) < 0.5f)
      isDouble = true;
    if (isDouble)
      value *= 2;
    makeBody();
  }

  public void makeBody() {
    PolygonShape sd = new PolygonShape();
    if (type == -1) {
      Vec2[] vertices = new Vec2[4];
      vertices[0] = world.vectorPixelsToWorld(new Vec2(-width / (2 * nBlocks), -width / (2 * nBlocks)));
      vertices[1] = world.vectorPixelsToWorld(new Vec2(width / (2 * nBlocks), -width / (2 * nBlocks)));
      vertices[2] = world.vectorPixelsToWorld(new Vec2(width / (2 * nBlocks), width / (2 * nBlocks)));
      vertices[3] = world.vectorPixelsToWorld(new Vec2(-width / (2 * nBlocks), width / (2 * nBlocks)));

      sd.set(vertices, vertices.length);
    }
    if (type == 1) {
      Vec2[] vertices = new Vec2[3];
      vertices[0] = world.vectorPixelsToWorld(new Vec2(width / (2 * nBlocks), -width / (2 * nBlocks)));
      vertices[1] = world.vectorPixelsToWorld(new Vec2(width / (2 * nBlocks), width / (2 * nBlocks)));
      vertices[2] = world.vectorPixelsToWorld(new Vec2(-width / (2 * nBlocks), width / (2 * nBlocks)));

      sd.set(vertices, vertices.length);
    }
    if (type == 2) {
      Vec2[] vertices = new Vec2[3];
      vertices[0] = world.vectorPixelsToWorld(new Vec2(-width / (2 * nBlocks), -width / (2 * nBlocks)));
      vertices[1] = world.vectorPixelsToWorld(new Vec2(width / (2 * nBlocks), width / (2 * nBlocks)));
      vertices[2] = world.vectorPixelsToWorld(new Vec2(-width / (2 * nBlocks), width / (2 * nBlocks)));

      sd.set(vertices, vertices.length);
    }
    if (type == 3) {
      Vec2[] vertices = new Vec2[3];
      vertices[0] = world.vectorPixelsToWorld(new Vec2(-width / (2 * nBlocks), -width / (2 * nBlocks)));
      vertices[1] = world.vectorPixelsToWorld(new Vec2(width / (2 * nBlocks), -width / (2 * nBlocks)));
      vertices[2] = world.vectorPixelsToWorld(new Vec2(-width / (2 * nBlocks), width / (2 * nBlocks)));

      sd.set(vertices, vertices.length);
    }
    if (type == 4) {
      Vec2[] vertices = new Vec2[3];
      vertices[0] = world.vectorPixelsToWorld(new Vec2(-width / (2 * nBlocks), -width / (2 * nBlocks)));
      vertices[1] = world.vectorPixelsToWorld(new Vec2(width / (2 * nBlocks), -width / (2 * nBlocks)));
      vertices[2] = world.vectorPixelsToWorld(new Vec2(width / (2 * nBlocks), width / (2 * nBlocks)));

      sd.set(vertices, vertices.length);
    }

    BodyDef bd = new BodyDef();
    bd.type = BodyType.STATIC;
    bd.position.set(world.coordPixelsToWorld((locX * width / nBlocks) + width / (2 * nBlocks), ((locY * width / nBlocks) + (height / 2) - (width / 2)) + width / (2 * nBlocks)));
    body = world.createBody(bd);

    body.createFixture(sd, 1);

    body.setUserData(this);
  }

  public void updateColor() {
    if (isDouble) {
      float r = map(value, 0, 2 * level, 128, 120);
      float g = map(value, 0, 2 * level, 249, 0);
      float b = map(value, 0, 2 * level, 255, 255);
      blockColor = color(r, g, b);
    } else {
      float r = map(value, 0, level, 255, 255);
      float g = map(value, 0, level, 247, 0);
      float b = map(value, 0, level, 147, 97);
      blockColor = color(r, g, b);
    }
  }

  public void deduct(boolean doSound) {
    value--;
    if (doSound)
      playBlop = true;
    if (value == 0) {
      fireworks.add(new ParticleSystem(((locX + 0.5f) * width / nBlocks), (((locY + 0.5f) * width / nBlocks) + (height / 2) - (width / 2)), 50, 80));
      delete = true;
    }
  }

  public void nextLevel() {
    world.destroyBody(body);
    locY++;
    makeBody();
  }

  public void show(float offset) {
    offset *= width / nBlocks;
    updateColor();
    stroke(blockColor);
    strokeWeight(4);
    textSize(18);
    fill(blockColor);

    {
      PVector LT = new PVector(locX * width / nBlocks + 5, (locY * width / nBlocks) + (height / 2) - (width / 2) + 5 + offset);
      PVector RT = new PVector(locX * width / nBlocks + width / nBlocks - 5, (locY * width / nBlocks) + (height / 2) - (width / 2) + 5 + offset);
      PVector LB = new PVector(locX * width / nBlocks + 5, (locY * width / nBlocks) + (height / 2) - (width / 2) + width / nBlocks - 5 + offset);
      PVector RB = new PVector(locX * width / nBlocks + width / nBlocks - 5, (locY * width / nBlocks) + (height / 2) - (width / 2) + width / nBlocks - 5 + offset);

      textAlign(CENTER, CENTER);
      if (type == -1) {
        text(value, LT.x + width / 14.0f - 5, LT.y + width / 14.0f - 5);
        noFill(); 
        rect(LT.x, LT.y, width / nBlocks - 10, width / nBlocks - 10);
      }
      if (type == 1) {
        text(value, RB.x - width / (4 * nBlocks), RB.y - width / (4 * nBlocks));
        noFill();
        triangle(RB.x, RB.y, RT.x, RT.y, LB.x, LB.y);
      }
      if (type == 2) {
        text(value, LB.x + width / (4 * nBlocks), LB.y - width / (4 * nBlocks));
        noFill();
        triangle(LT.x, LT.y, RB.x, RB.y, LB.x, LB.y);
      }
      if (type == 3) {
        text(value, LT.x + width / (4 * nBlocks), LT.y + width / (4 * nBlocks));
        noFill();
        triangle(LT.x, LT.y, RT.x, RT.y, LB.x, LB.y);
      }
      if (type == 4) {
        text(value, RT.x - width / (4 * nBlocks), RT.y + width / (4 * nBlocks));
        noFill();
        triangle(LT.x, LT.y, RT.x, RT.y, RB.x, RB.y);
      }
    }
  }
}
class Boundary {
  float x, y, w, h;
  Body body;
  
  Boundary(float xin, float yin, float win, float hin) {
    x = xin;
    y = yin;
    w = win;
    h = hin;
    
    PolygonShape sd = new PolygonShape();
    sd.setAsBox(world.scalarPixelsToWorld(w / 2), world.scalarPixelsToWorld(h / 2));

    BodyDef bd = new BodyDef();
    bd.type = BodyType.STATIC;
    bd.position.set(world.coordPixelsToWorld(x + w / 2, y + h / 2));
    body = world.createBody(bd);
    
    body.createFixture(sd, 1);
    
    body.setUserData(this);
  }
}
class Particle {
  PVector adder;
  float size;
  float velocity;
  float r, g, b, a;
  float lifespan;
  
  Particle(float lin) {
    adder = PVector.fromAngle(random(-PI, PI));
    size = random(5, 15);
    r = random(255);
    g = random(255);
    b = random(255);
    a = random(150, 200);
    velocity = random(0.4f, 1.4f);
    lifespan = lin;
  }
  
  public void show(float adr) {
    fill(r, g, b, a * ((lifespan - adr) / lifespan));
    noStroke();
    PVector velt = adder.copy();
    velt.mult(adr * velocity);
    rect(velt.x - size / 2, velt.y - size / 2, size, size);
  }
}
class ParticleSystem {
  float locX, locY;
  int lifespan;
  int lifeFrame;
  boolean delete;
  Particle [] particles;
  

  ParticleSystem(float xin, float yin, int nin, int lin) {
    locX = xin;
    locY = yin;
    lifespan = lin;
    particles = new Particle [nin];
    for (int i = 0; i < particles.length; i++) {
      particles[i] = new Particle(lifespan);
    }
    lifeFrame = 0;
  }

  public void show() {
    pushMatrix();
    translate(locX, locY);
    for (Particle p : particles) {
      p.show(lifeFrame);
    }
    popMatrix();
    lifeFrame++;
    if (lifeFrame == lifespan)
      delete = true;
  }
}
class Special {
  int locX, locY;
  int endLoc;
  int type;
  float radius;
  boolean delete;

  //1 - Add
  //2 - Bounce
  //3 - hor lazer
  //4 - ver lazer

  Special(int inX, int inY, int inT) {
    locX = inX;
    locY = inY;
    type = inT;
    radius = width / (5 * nBlocks);
    delete = false;
    endLoc = floor(random(2, 5));
  }

  public void nextLevel() {
    locY++;
    if (locY == endLoc && type != 1)
      delete = true;
  }

  public void detectCollision(Ball in) {
    if (dist(in.c.x, in.c.y, ((locX + 0.5f) * width / nBlocks), (((locY + 0.5f) * width / nBlocks) + (height / 2) - (width / 2))) < 3 * radius / 2 + radiusUniv)
      in.specialized++;
    else
      in.specialized = 0;

    if (in.specialized == 1)
      magic(in);
  }

  public void magic(Ball in) {
    if (type == 1) {
      addBall++;
      delete = true;
    }
    if (type == 2) {
      PVector direction = in.getVelocity();
      direction.rotate(random(3 * PI / 4, 5 * PI / 4));
      in.shoot(direction, false);
    }
    if (type == 3) {
      for (Block b : blocks) 
        if (locY == b.locY)
          b.deduct(false);
      noStroke();
      fill(250, 250, 0, 200);
      rect(0, (locY + 0.5f) * width / nBlocks + (height / 2) - (width / 2) - radius, width, 2 * radius);
      playZap = true;
    }
    if (type == 4) {
      for (Block b : blocks) 
        if (locX == b.locX)
          b.deduct(false);
      noStroke();
      fill(250, 250, 0, 200);
      rect((locX + 0.5f) * width / nBlocks - radius, 0, 2 * radius, height);
      playZap = true;
    }
  }

  public void show(float offset) {
    offset *= width / nBlocks;
    pushMatrix();
    translate(((locX + 0.5f) * width / nBlocks), (((locY + 0.5f) * width / nBlocks) + (height / 2) - (width / 2)) + offset);
    if (type == 1) {
      stroke(255, 249, 123);
      line(-radius / 3, 0, radius / 3, 0);
      line(0, -radius / 3, 0, radius / 3);
    }
    if (type == 2) {
      noStroke();
      fill(193, 0, 255);
      ellipse(0, 0, radius / 2, radius / 2);
      stroke(193, 0, 255);
    }
    if (type == 3) {
      stroke(70, 251, 232);
      line(-radius / 3, 0, radius / 3, 0);
    }
    if (type == 4) {
      stroke(70, 251, 188);
      line(0, -radius / 3, 0, radius / 3);
    }
    noFill();
    ellipse(0, 0, 2 * radius, 2 * radius);
    popMatrix();
  }
}
  public void settings() {  size(400, 640); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "BBTAN" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
