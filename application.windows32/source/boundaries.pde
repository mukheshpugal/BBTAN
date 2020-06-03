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
