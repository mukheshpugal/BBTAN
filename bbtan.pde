//---------------------------------------------------------------------------------------------------------------
// Never touch these
import processing.sound.*;
import shiffman.box2d.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.collision.*;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.*;

SoundFile blop;
ArrayList<Ball> balls = new ArrayList<Ball>();
ArrayList<Block> blocks = new ArrayList<Block>();
ArrayList<Boundary> boundaries = new ArrayList<Boundary>();
ArrayList<Special> specials = new ArrayList<Special>();
ArrayList<ParticleSystem> fireworks = new ArrayList<ParticleSystem>();
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
PVector shootDir;

Ball c;

void setup() {
  size(400, 640);
  background(0);
  textFont(createFont("wagner.ttf", 32));
  blop = new SoundFile(this, "blop.wav");
  world = new Box2DProcessing(this);
  world.createWorld();
  world.setGravity(0, 0);
  world.listenForCollisions();
  boundaries.add(new Boundary(-50, -50, 50, height + 100));
  boundaries.add(new Boundary(0, (height / 2) - (9 * width / 14.0) - 50, width, 50));
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
    balls.add(new Ball(xRest, (height / 2) + (9 * width / 14.0) - radiusUniv));
  }

  processing = false;
  xRest = width / 2;
  //c.shoot(new PVector(0, -1));
}

void draw() {
  game();
}

void game() {

  //Kinda simulaion phase
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
  if (playBlop && (frameCount % 5 == 0)) {
    blop.play();
    playBlop = false;
  }

  //Level up phase
  if (nOnFloor == balls.size() && !triggerAnim) {
    level ++;
    addLayer(-1);
    triggerAnim = true;
  }

  if (triggerAnim) {
    animOffset += 0.05;
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
          balls.add(new Ball(xRest, (height / 2) + (9 * width / 14.0) - radiusUniv));
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
    text("x" + (balls.size() - shootTimer / shootInterval), balls.get(balls.size() - 1).c.x, (height / 2) + (9 * width / 14.0) - 2 * radiusUniv - 10);
  }
}

PVector shoot() {
  if (!processing) {
    fill(255);
    textAlign(CENTER, BOTTOM);
    textSize(18);
    text("x" + balls.size(), balls.get(balls.size() - 1).c.x, (height / 2) + (9 * width / 14.0) - 2 * radiusUniv - 10);

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
      if (-PI + 0.25 < displace.heading() && -0.25 > displace.heading()) {
        fill(200);
        for (int i = 1; i < 10; i++)
          ellipse(xRest + i * displace.x / 10.0, (height / 2) + (9 * width / 14.0) - radiusUniv + i * displace.y / 10.0, radiusUniv, radiusUniv);
      }
      aimed = true;
      if (mouseInit.x == mouseFinal.x && mouseInit.y == mouseFinal.y)
        aimed = false;
      if (!(-PI + 0.25 < displace.heading() && -0.25 > displace.heading()))
        aimed = false;
    }
  }
  return null;
}

void addLayer(int begin) {
  int addLoc = floor(random(7));
  specials.add(new Special(addLoc, begin, 1));
  for (int i = 0; i < 7; i++) 
    if (i != addLoc) {
      float tempAdder = random(6);
      if (tempAdder < 4)
        blocks.add(new Block(i, begin, level, 0));
      else if (tempAdder < 4.2)
        specials.add(new Special(i, begin, 2));
      else if (tempAdder < 4.25)
        specials.add(new Special(i, begin, 3));
      else if (tempAdder < 4.3)
        specials.add(new Special(i, begin, 4));
    }
}

void beginContact(Contact cp) {
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

void endContact(Contact cp) {
}
