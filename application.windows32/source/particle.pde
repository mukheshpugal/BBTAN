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
    velocity = random(0.4, 1.4);
    lifespan = lin;
  }
  
  void show(float adr) {
    fill(r, g, b, a * ((lifespan - adr) / lifespan));
    noStroke();
    PVector velt = adder.copy();
    velt.mult(adr * velocity);
    rect(velt.x - size / 2, velt.y - size / 2, size, size);
  }
}
