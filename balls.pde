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

  void shoot(PVector dirin, boolean correct) {
    dirin.normalize();
    dirin.mult(ballVelocity);
    body.setLinearVelocity(new Vec2(dirin.x, -dirin.y));
    atRest = false;
    onDaFloor = correct;
  }
  
  PVector getVelocity() {
    return new PVector(body.getLinearVelocity().x, body.getLinearVelocity().y);
  }

  void show() {
    fill(255);
    noStroke();
    c = world.getBodyPixelCoord(body);
    pushMatrix();
    translate(c.x, c.y);
    ellipse(0, 0, radiusUniv * 2, radiusUniv * 2);
    popMatrix();
  }

  void checkRest() {
    if (!onDaFloor) {
      noDestroy++;
      if (noDestroy >= 1000) {
        noDestroy = 0;
        shoot(new PVector(0, 1), false);
      }
      for (Special s : specials)
        s.detectCollision(this);
    }
    if ((c.y + radiusUniv > (height / 2) + (9 * width / 14.0)) && !onDaFloor) {
      noDestroy = 0;
      if (nOnFloor == 0)
        xRest = c.x;
      onDaFloor = (c.x == xRest);
      if (c.x < xRest)
        shoot(new PVector(1, 0), true);
      if (c.x > xRest)
        shoot(new PVector(-1, 0), true);
      body.setTransform(world.coordPixelsToWorld(c.x, (height / 2) + (9 * width / 14.0) - radiusUniv + 2), 0);
    }
    if (onDaFloor && !atRest) {
      float tolerance = 10;
      if ((c.x > xRest - tolerance) && (c.x < xRest + tolerance)) {
        body.setLinearVelocity(new Vec2(0, 0));
        body.setTransform(world.coordPixelsToWorld(xRest, (height / 2) + (9 * width / 14.0) - radiusUniv), 0);
        atRest = true;
        nOnFloor++;
      }
    }
  }
}
