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

  void show() {
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
