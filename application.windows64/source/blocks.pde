class Block {
  int locX, locY;
  int value;
  int type;
  color blockColor;
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
    if (value % 10 == 0 && random(1) < 0.5)
      isDouble = true;
    if (isDouble)
      value *= 2;
    makeBody();
  }

  void makeBody() {
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

  void updateColor() {
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

  void deduct(boolean doSound) {
    value--;
    if (doSound)
      playBlop = true;
    if (value == 0) {
      fireworks.add(new ParticleSystem(((locX + 0.5) * width / nBlocks), (((locY + 0.5) * width / nBlocks) + (height / 2) - (width / 2)), 50, 80));
      delete = true;
    }
  }

  void nextLevel() {
    world.destroyBody(body);
    locY++;
    makeBody();
  }

  void show(float offset) {
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
        text(value, LT.x + width / 14.0 - 5, LT.y + width / 14.0 - 5);
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
