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

  void nextLevel() {
    locY++;
    if (locY == endLoc && type != 1)
      delete = true;
  }

  void detectCollision(Ball in) {
    if (dist(in.c.x, in.c.y, ((locX + 0.5) * width / nBlocks), (((locY + 0.5) * width / nBlocks) + (height / 2) - (width / 2))) < 3 * radius / 2 + radiusUniv)
      in.specialized++;
    else
      in.specialized = 0;

    if (in.specialized == 1)
      magic(in);
  }

  void magic(Ball in) {
    if (type == 1) {
      addBall++;
      delete = true;
    }
    if (type == 2) {
      in.shoot(PVector.random2D(), false);
    }
    if (type == 3) {
      for (Block b : blocks) 
        if (locY == b.locY)
          b.deduct(false);
      noStroke();
      fill(250, 250, 0, 200);
      rect(0, (locY + 0.5) * width / nBlocks + (height / 2) - (width / 2) - radius, width, 2 * radius);
      playZap = true;
    }
    if (type == 4) {
      for (Block b : blocks) 
        if (locX == b.locX)
          b.deduct(false);
      noStroke();
      fill(250, 250, 0, 200);
      rect((locX + 0.5) * width / nBlocks - radius, 0, 2 * radius, height);
      playZap = true;
    }
  }

  void show(float offset) {
    offset *= width / nBlocks;
    pushMatrix();
    translate(((locX + 0.5) * width / nBlocks), (((locY + 0.5) * width / nBlocks) + (height / 2) - (width / 2)) + offset);
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
