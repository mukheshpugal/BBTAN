void bg() {
  background(0);
  stroke(255); 
  //rect(0, height / 2 - width / 2, width, width);
  line(0, (height / 2) - (9 * width / 14.0), width, (height / 2) - (9 * width / 14.0));
  line(0, (height / 2) + (9 * width / 14.0), width, (height / 2) + (9 * width / 14.0));

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
    
    timerDisplay+= secrem;
    text(timerDisplay, width - 10, height - (width / 14.0) - 7);
  }
  { //score
    textAlign(CENTER, CENTER);
    text(level, width / 2, (width / 14.0));
  }
}
